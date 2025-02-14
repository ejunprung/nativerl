import os
import random
import sys
import traceback
from typing import Optional

import fire
import numpy as np
import ray
from pathmind_training import (
    Stopper,
    get_loggers,
    get_scheduler,
    modify_anylogic_db_properties,
    write_completion_report,
)
from pathmind_training.callbacks import get_callback_function, get_callbacks
from pathmind_training.environments import get_environment, get_gym_environment
from pathmind_training.freezing import freeze_trained_policy
from pathmind_training.models import get_custom_model
from ray.tune import run, sample_from


def main(
    environment: str,
    is_gym: bool = False,
    is_pathmind_simulation: bool = False,
    obs_selection: str = None,
    algorithm: str = "PPO",
    scheduler: str = "PBT",
    output_dir: str = os.getcwd(),
    multi_agent: bool = False,
    max_memory_in_mb: int = 4096,
    cpu_count: int = 8,
    num_cpus: int = 1,
    num_gpus: int = 0,
    num_workers: int = 1,
    num_hidden_layers: int = 2,
    num_hidden_nodes: int = 256,
    max_iterations: int = 500,
    convergence_check_start_iteration: int = 250,
    max_time_in_sec: int = 43200,
    max_episodes: int = 200000,
    num_samples: int = 4,
    resume: bool = False,
    checkpoint_frequency: int = 5,
    keep_checkpoints_number: int = 5,
    max_failures: int = 5,
    debug_metrics: bool = False,
    user_log: bool = False,
    autoregressive: bool = False,
    episode_reward_range: float = 0.01,
    entropy_slope: float = 0.01,
    vf_loss_range: float = 0.1,
    value_pred: float = 0.01,
    action_masking: bool = False,
    freezing: bool = False,
    discrete: bool = True,
    random_seed: Optional[int] = None,
    custom_callback: Optional[str] = None,
    gamma: float = 0.99,
    train_batch_mode: str = "complete_episodes",
    train_batch_size: Optional[int] = None,
    rollout_fragment_length: int = 200,
    reward_balance_period: int = 1,
    num_reward_terms: int = None,
    alphas: str = None,
    use_auto_norm: bool = True,
):
    """

    :param environment: The name of a subclass of "Environment" to use as environment for training.
    :param is_gym: if True, "environment" must be a gym environment.
    :param is_pathmind_simulation: if True, use the "Simulation" interface from the pathmind package.
    :param obs_selection: If provided, read the names of the observations to be selected from this yaml file.
    :param algorithm: The algorithm to use with RLlib for training and the PythonPolicyHelper.
    :param scheduler: The tune scheduler used for picking trials, currently supports "PBT"
    :param output_dir: The directory where to output the logs of RLlib.
    :param multi_agent: Indicates that we need multi-agent support with the Environment class provided.
    :param max_memory_in_mb: The maximum amount of memory in MB to use for Java environments.
    :param cpu_count: The number of CPUs to let init Ray for the training.
    :param num_cpus: The number of CPU cores to let RLlib use during training.
    :param num_gpus: The number of GPUs to let RLlib use during training. (e.g. 1 = One GPU)
    :param num_workers: The number of parallel workers that RLlib should execute during training.
    :param num_hidden_layers: The number of hidden layers in the MLP to use for the learning model.
    :param num_hidden_nodes: The number of nodes per layer in the MLP to use for the learning model.
    :param max_iterations: The maximum number of training iterations as a stopping criterion.
    :param convergence_check_start_iteration: The training iteration in which the convergence check should begin.
    :param max_time_in_sec: Maximum amount of  time in seconds.
    :param max_episodes: Maximum number of episodes per trial.
    :param num_samples: Number of population-based training samples.
    :param resume: Resume training when AWS spot instance terminates.
    :param checkpoint_frequency: Periodic checkpointing to allow training to recover from AWS spot instance termination.
    :param keep_checkpoints_number: Maximum number of checkpoints to keep. Checkpoints beyond this threshold are deleted on a rolling basis.
    :param max_failures: Maximum number of failures before terminating training. Forgives rare AnyLogic errors so that training may continue.
    :param debug_metrics: Indicates that we save raw metrics data to metrics_raw column in progress.csv.
    :param user_log: Reduce size of output log file.
    :param autoregressive: Whether to use auto-regressive models.
    :param episode_reward_range: Episode reward range threshold
    :param entropy_slope: Entropy slope threshold
    :param vf_loss_range: VF loss range threshold
    :param value_pred: value pred threshold
    :param action_masking: Whether to use action masking or not.
    :param freezing: Whether to use policy freezing or not
    :param discrete: Discrete vs continuous actions, defaults to True (i.e. discrete)
    :param random_seed: Optional random seed for this experiment.
    :param custom_callback: Optional name of a custom Python function returning a callback implementation
        of Ray's "DefaultCallbacks", e.g. "tests.custom_callback.get_callback"
    :param gamma: gamma value
    :param train_batch_mode: Train Batch Mode [truncate_episodes, complete_episodes]
    :param train_batch_size: Optional train batch size
    :param rollout_fragment_length: Divide episodes into fragments of this many steps each during rollouts.
    :param reward_balance_period: How often (iterations) to recalculate betas and adjust reward function
    :param num_reward_terms: Number of conceptual chunks (possibly multiple lines) reward function is chopped into: each chunk gets an alpha and beta.
    :param alphas: User defined importance weights on conceptual chunks (reward terms)
    :param use_auto_norm: Whether or not to call updateBeta

    :return: runs training for the given environment, with nativerl
    """

    if random_seed:
        random.seed(random_seed)
        np.random.seed(random_seed)

    jar_dir = os.getcwd()
    os.chdir(jar_dir)
    output_dir = os.path.abspath(output_dir)
    modify_anylogic_db_properties()

    env_config = {
        "use_reward_terms": alphas is not None,
        "reward_balance_period": reward_balance_period,
        "num_reward_terms": num_reward_terms if alphas else 1,
        "alphas": np.asarray(alphas) if alphas else np.ones(num_reward_terms),
        "use_auto_norm": use_auto_norm
        and (alphas is not None)
        and (num_reward_terms != 1),
    }

    if env_config["use_reward_terms"]:
        assert (
            env_config["alphas"].size == env_config["num_reward_terms"]
        ), f"alphas array size ({env_config['alphas'].size}) must be == num_reward_terms ({env_config['num_reward_terms']})"

    if is_gym:
        env_name, env_creator = get_gym_environment(environment_name=environment)
    else:
        env_name = get_environment(
            jar_dir=jar_dir,
            is_multi_agent=multi_agent,
            environment_name=environment,
            max_memory_in_mb=max_memory_in_mb,
            is_pathmind_simulation=is_pathmind_simulation,
            obs_selection=obs_selection,
        )
        env_creator = env_name

    env_instance = env_creator(env_config=env_config)
    env_instance.max_steps = (
        env_instance._max_episode_steps
        if hasattr(env_instance, "_max_episode_steps")
        else 20000
    )

    ray.init(log_to_driver=user_log, dashboard_host="127.0.0.1", num_cpus=cpu_count, num_gpus=num_gpus)

    model = get_custom_model(
        num_hidden_nodes=num_hidden_nodes,
        num_hidden_layers=num_hidden_layers,
        autoregressive=autoregressive,
        action_masking=action_masking,
        discrete=discrete,
    )

    stopper = Stopper(
        output_dir=output_dir,
        algorithm=algorithm,
        max_iterations=max_iterations,
        max_time_in_sec=max_time_in_sec,
        max_episodes=max_episodes,
        convergence_check_start_iteration=convergence_check_start_iteration,
        episode_reward_range_th=episode_reward_range,
        entropy_slope_th=entropy_slope,
        vf_loss_range_th=vf_loss_range,
        value_pred_th=value_pred,
    )

    if custom_callback:
        # from tests.custom_callback import get_callback as foo
        callbacks = get_callback_function(custom_callback)()
    else:
        callbacks = get_callbacks(
            debug_metrics, env_config["use_reward_terms"], is_gym, checkpoint_frequency
        )

    assert scheduler in [
        "PBT",
        "PB2",
    ], f"Scheduler has to be either PBT or PB2, got {scheduler}"
    scheduler_instance = get_scheduler(
        scheduler_name=scheduler, train_batch_size=train_batch_size
    )
    loggers = get_loggers()

    config = {
        "env": env_name,
        "env_config": env_config,
        "callbacks": callbacks,
        "num_workers": num_workers,
        "num_cpus_per_worker": num_cpus,
        "num_gpus_per_worker": num_gpus / num_workers,
        "model": model,
        "use_gae": True,
        "vf_loss_coeff": 1.0,
        "vf_clip_param": np.inf,
        "lambda": 0.95,
        "clip_param": 0.2,
        "lr": 1e-4,
        "gamma": gamma,
        "entropy_coeff": 0.0,
        "num_sgd_iter": sample_from(lambda spec: random.choice([10, 20, 30])),
        "sgd_minibatch_size": sample_from(lambda spec: random.choice([128, 512, 2048])),
        "train_batch_size": train_batch_size
        if train_batch_size
        else sample_from(lambda spec: random.choice([4000, 8000, 12000])),
        "rollout_fragment_length": rollout_fragment_length,
        "batch_mode": train_batch_mode,  # Set rollout samples to episode length
        "horizon": env_instance.max_steps,  # Set max steps per episode
        "no_done_at_end": multi_agent,  # Disable "de-allocation" of agents for simplicity
    }

    trials = run(
        algorithm,
        scheduler=scheduler_instance,
        num_samples=num_samples,
        stop=stopper.stop,
        loggers=loggers,
        config=config,
        local_dir=output_dir if output_dir else None,
        resume=resume,
        checkpoint_freq=checkpoint_frequency,
        keep_checkpoints_num=keep_checkpoints_number,
        checkpoint_at_end=True,
        max_failures=max_failures,
        export_formats=["model"],
        queue_trials=True,
    )

    if freezing:
        best_freezing_log_dir = freeze_trained_policy(
            env=env_instance,
            env_name=env_name,
            callbacks=callbacks,
            trials=trials,
            loggers=loggers,
            algorithm=algorithm,
            output_dir=f"{output_dir}/{algorithm}/freezing",
            is_discrete=discrete,
            multi_agent=multi_agent,
        )
        write_completion_report(
            trials=trials,
            output_dir=output_dir,
            algorithm=algorithm,
            best_freezing_log_dir=best_freezing_log_dir,
        )
    else:
        write_completion_report(
            trials=trials, output_dir=output_dir, algorithm=algorithm
        )

    ray.shutdown()


def test(
    environment: str,
    module_path: str = None,
):
    """
    :check if valid environment or not for Model Analyzer

    :param environment: The name of a subclass of "Environment" to use as environment for training.
    :param module_path: The path where the model related codes(python or java) live.

    :return: model types in normal cases, otherwise return exit code -1
    """

    jar_dir = module_path if module_path else os.getcwd()
    os.chdir(jar_dir)

    if module_path:
        sys.path.append(module_path)

    try:
        # try Pathmind simulation
        env_config = {
            "use_reward_terms": False,
            "reward_balance_period": 0,
            "num_reward_terms": 0,
            "alphas": np.ones(0),
        }
        env_name = get_environment(
            jar_dir=jar_dir,
            is_multi_agent=True,
            environment_name=environment,
            is_pathmind_simulation=True,
        )

        env_creator = env_name
        env_instance = env_creator(env_config=env_config)
        env_instance.__init__(env_config=env_config)

        mode = "pm_single" if len(env_instance.reset()) == 1 else "pm_multi"
        print(f"model-analyzer-mode:{mode}")
    except Exception as e:
        print(
            "cannot initiate Pathmind simulation env. it will try to initiate GYM env."
        )
        traceback.print_tb(e.__traceback__)
        try:
            # try GYM simulation
            env_name, env_creator = get_gym_environment(environment_name=environment)
            env_instance = env_creator(env_config={})
            env_instance.__init__()

            print(f"model-analyzer-mode:py_single")
        except Exception as e1:
            print("cannot initiate GYM env")
            traceback.print_tb(e1.__traceback__)
            print(f"model-analyzer-error:{e1}")
            sys.exit(-1)

    sys.exit(0)


def from_config(config_file="./config.json"):
    """Run training from a config file
    :param config_file: JSON file with arguments as per "training" CLI.
    :return:
    """
    import json

    with open(config_file, "r") as f:
        config_string = f.read()
        config = json.loads(config_string)
    return main(**config)


if __name__ == "__main__":
    fire.Fire({"training": main, "from_config": from_config, "test": test})
