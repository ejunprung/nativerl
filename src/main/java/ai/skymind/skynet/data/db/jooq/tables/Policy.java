/*
 * This file is generated by jOOQ.
 */
package ai.skymind.skynet.data.db.jooq.tables;


import ai.skymind.skynet.data.db.jooq.Indexes;
import ai.skymind.skynet.data.db.jooq.Keys;
import ai.skymind.skynet.data.db.jooq.Public;
import ai.skymind.skynet.data.db.jooq.tables.records.PolicyRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


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
public class Policy extends TableImpl<PolicyRecord> {

    private static final long serialVersionUID = -431830027;

    /**
     * The reference instance of <code>public.policy</code>
     */
    public static final Policy POLICY = new Policy();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PolicyRecord> getRecordType() {
        return PolicyRecord.class;
    }

    /**
     * The column <code>public.policy.id</code>.
     */
    public final TableField<PolicyRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('policy_id_seq'::regclass)", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>public.policy.userId</code>.
     */
    public final TableField<PolicyRecord, Integer> USERID = createField("userId", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.policy.modelId</code>.
     */
    public final TableField<PolicyRecord, Integer> MODELID = createField("modelId", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.policy.mdpId</code>.
     */
    public final TableField<PolicyRecord, Integer> MDPID = createField("mdpId", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.policy.runId</code>.
     */
    public final TableField<PolicyRecord, Integer> RUNID = createField("runId", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.policy.createdAt</code>.
     */
    public final TableField<PolicyRecord, Timestamp> CREATEDAT = createField("createdAt", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>public.policy.fileId</code>. This is the file id as returned by rescale api
     */
    public final TableField<PolicyRecord, String> FILEID = createField("fileId", org.jooq.impl.SQLDataType.VARCHAR, this, "This is the file id as returned by rescale api");

    /**
     * Create a <code>public.policy</code> table reference
     */
    public Policy() {
        this(DSL.name("policy"), null);
    }

    /**
     * Create an aliased <code>public.policy</code> table reference
     */
    public Policy(String alias) {
        this(DSL.name(alias), POLICY);
    }

    /**
     * Create an aliased <code>public.policy</code> table reference
     */
    public Policy(Name alias) {
        this(alias, POLICY);
    }

    private Policy(Name alias, Table<PolicyRecord> aliased) {
        this(alias, aliased, null);
    }

    private Policy(Name alias, Table<PolicyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Policy(Table<O> child, ForeignKey<O, PolicyRecord> key) {
        super(child, key, POLICY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.POLICY_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<PolicyRecord, Integer> getIdentity() {
        return Keys.IDENTITY_POLICY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PolicyRecord> getPrimaryKey() {
        return Keys.POLICY_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PolicyRecord>> getKeys() {
        return Arrays.<UniqueKey<PolicyRecord>>asList(Keys.POLICY_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<PolicyRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<PolicyRecord, ?>>asList(Keys.POLICY__POLICYOWNER, Keys.POLICY__POLICY_MODEL, Keys.POLICY__POLICY_MDP, Keys.POLICY__POLICY_CREATED_BY_RUN);
    }

    public User user() {
        return new User(this, Keys.POLICY__POLICYOWNER);
    }

    public Model model() {
        return new Model(this, Keys.POLICY__POLICY_MODEL);
    }

    public Mdp mdp() {
        return new Mdp(this, Keys.POLICY__POLICY_MDP);
    }

    public Run run() {
        return new Run(this, Keys.POLICY__POLICY_CREATED_BY_RUN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Policy as(String alias) {
        return new Policy(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Policy as(Name alias) {
        return new Policy(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Policy rename(String name) {
        return new Policy(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Policy rename(Name name) {
        return new Policy(name, null);
    }
}
