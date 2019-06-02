/*
 * This file is generated by jOOQ.
 */
package ai.skymind.skynet.data.db.jooq;


import ai.skymind.skynet.data.db.jooq.tables.Databasechangelog;
import ai.skymind.skynet.data.db.jooq.tables.Databasechangeloglock;
import ai.skymind.skynet.data.db.jooq.tables.Mdp;
import ai.skymind.skynet.data.db.jooq.tables.Model;
import ai.skymind.skynet.data.db.jooq.tables.Policy;
import ai.skymind.skynet.data.db.jooq.tables.Project;
import ai.skymind.skynet.data.db.jooq.tables.Run;
import ai.skymind.skynet.data.db.jooq.tables.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = -912355666;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.databasechangelog</code>.
     */
    public final Databasechangelog DATABASECHANGELOG = ai.skymind.skynet.data.db.jooq.tables.Databasechangelog.DATABASECHANGELOG;

    /**
     * The table <code>public.databasechangeloglock</code>.
     */
    public final Databasechangeloglock DATABASECHANGELOGLOCK = ai.skymind.skynet.data.db.jooq.tables.Databasechangeloglock.DATABASECHANGELOGLOCK;

    /**
     * The table <code>public.mdp</code>.
     */
    public final Mdp MDP = ai.skymind.skynet.data.db.jooq.tables.Mdp.MDP;

    /**
     * The table <code>public.model</code>.
     */
    public final Model MODEL = ai.skymind.skynet.data.db.jooq.tables.Model.MODEL;

    /**
     * The table <code>public.policy</code>.
     */
    public final Policy POLICY = ai.skymind.skynet.data.db.jooq.tables.Policy.POLICY;

    /**
     * The table <code>public.project</code>.
     */
    public final Project PROJECT = ai.skymind.skynet.data.db.jooq.tables.Project.PROJECT;

    /**
     * The table <code>public.run</code>.
     */
    public final Run RUN = ai.skymind.skynet.data.db.jooq.tables.Run.RUN;

    /**
     * The table <code>public.user</code>.
     */
    public final User USER = ai.skymind.skynet.data.db.jooq.tables.User.USER;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        List result = new ArrayList();
        result.addAll(getSequences0());
        return result;
    }

    private final List<Sequence<?>> getSequences0() {
        return Arrays.<Sequence<?>>asList(
            Sequences.MDP_ID_SEQ,
            Sequences.MODEL_ID_SEQ,
            Sequences.POLICY_ID_SEQ,
            Sequences.PROJECT_ID_SEQ,
            Sequences.RUN_ID_SEQ,
            Sequences.USER_ID_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Databasechangelog.DATABASECHANGELOG,
            Databasechangeloglock.DATABASECHANGELOGLOCK,
            Mdp.MDP,
            Model.MODEL,
            Policy.POLICY,
            Project.PROJECT,
            Run.RUN,
            User.USER);
    }
}
