package com.basejava.webapp.sql;

import com.basejava.webapp.exception.ExistStorageException;
import com.basejava.webapp.exception.StorageException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;

public class SqlHelper {
    private final ConnectionFactory connectionFactory;

    public SqlHelper(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void execute(String sql, SqlExecutor executor) {
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            executor.accept(ps);

        } catch (SQLException e) {
            throw convertException(e);
        }
    }

    public <T> T executeQuery(String sql, SqlQuery<T> executor) {
        try (Connection conn = connectionFactory.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            return executor.execute(ps);

        } catch (SQLException e) {
            throw convertException(e);
        }
    }

    public <T> T transactionalExecute(SqlTransaction<T> executor) {
        try (Connection conn = connectionFactory.getConnection()) {
            try {
                conn.setAutoCommit(false);
                T res = executor.execute(conn);
                conn.commit();
                return res;
            } catch (SQLException e) {
                conn.rollback();
                throw convertException(e);
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    private StorageException convertException(SQLException e) {
        if (e instanceof PSQLException psqlEx &&
                "23505".equals(psqlEx.getSQLState())) { // unique_violation
            return new ExistStorageException(null);
        }
        return new StorageException(e);
    }
}