package ai.skymind.nativerl;

import ai.skymind.nativerl.util.Reflect;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

/**
 *
 * @author saudet
 */
public class ActionProcessor {
    public static final String METHOD_NAME = "actions";

    Class agentClass;
    Class actionClass;
    Field[] actionFields;
    Method actionMethod;
    Constructor actionConstructor;
    boolean usesAgentId;

    public ActionProcessor(String agentClassName) throws ReflectiveOperationException {
        this(Class.forName(agentClassName, false, ActionProcessor.class.getClassLoader()));
    }
    public ActionProcessor(Class agentClass) throws ReflectiveOperationException {
        this.agentClass = agentClass;
        this.actionClass = Reflect.findLocalClass(agentClass, METHOD_NAME);
        this.actionFields = Reflect.getFields(actionClass);
        this.actionMethod = Reflect.getVoidMethod(actionClass);
        try {
            this.actionConstructor = actionClass.getDeclaredConstructor(agentClass, long.class);
            this.usesAgentId = true;
        } catch (NoSuchMethodException e) {
            try {
                this.actionConstructor = actionClass.getDeclaredConstructor(agentClass, int.class);
                this.usesAgentId = true;
            } catch (NoSuchMethodException e2) {
                this.actionConstructor = actionClass.getDeclaredConstructor(agentClass);
                this.usesAgentId = false;
            }
        }
        this.actionConstructor.setAccessible(true);
    }

    public Class getActionClass() {
        return actionClass;
    }

    public String[] getActionNames() {
        return Reflect.getFieldNames(actionFields);
    }

    public AnnotationProcessor[] getActionSpaces() throws ReflectiveOperationException {
        return Reflect.getFieldAnnotations(actionFields);
    }

    public double[] getActions(Object agent, Random random) throws ReflectiveOperationException {
        return getActions(agent, random, 0);
    }
    public double[] getActions(Object agent, Random random, int agentId) throws ReflectiveOperationException {
        Object a = usesAgentId ? actionConstructor.newInstance(agent, agentId) : actionConstructor.newInstance(agent);
        Reflect.setFieldDoubles(actionFields, a, null, random);
        return Reflect.getFieldDoubles(actionFields, a);
    }

    public void doActions(Object agent, double[] actions) throws ReflectiveOperationException {
        doActions(agent, actions, 0);
    }
    public void doActions(Object agent, double[] actions, int agentId) throws ReflectiveOperationException {
        Object a = usesAgentId ? actionConstructor.newInstance(agent, agentId) : actionConstructor.newInstance(agent);
        Reflect.setFieldDoubles(actionFields, a, actions, null);
        actionMethod.invoke(a);
    }
}
