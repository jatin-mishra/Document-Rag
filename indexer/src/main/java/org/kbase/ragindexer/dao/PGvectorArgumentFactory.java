package org.kbase.ragindexer.dao;

import com.pgvector.PGvector;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

/**
 * Teaches JDBI how to bind pgvector's {@link PGvector} type into a
 * {@code PreparedStatement}. {@code PGvector} extends {@code PGobject} and
 * carries its own type name ("vector"), so a plain {@code setObject} is enough —
 * the PostgreSQL JDBC driver sends it in a form the {@code vector} column accepts.
 */
public class PGvectorArgumentFactory extends AbstractArgumentFactory<PGvector> {

    public PGvectorArgumentFactory() {
        super(Types.OTHER);
    }

    @Override
    protected Argument build(PGvector value, ConfigRegistry config) {
        return (position, statement, ctx) -> statement.setObject(position, value);
    }
}
