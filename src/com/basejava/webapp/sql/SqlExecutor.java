package com.basejava.webapp.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecutor {
    void accept(PreparedStatement ps) throws SQLException;
}