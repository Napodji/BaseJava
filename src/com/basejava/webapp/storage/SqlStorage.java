package com.basejava.webapp.storage;

import com.basejava.webapp.exception.NotExistStorageException;
import com.basejava.webapp.model.ContactType;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.sql.SqlHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SqlStorage implements Storage {
    private final SqlHelper sqlHelper;

    public SqlStorage(String dbUrl, String dbUser, String dbPassword) {
        sqlHelper = new SqlHelper(
                () -> DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        );
    }

    @Override
    public void clear() {
        sqlHelper.execute("DELETE FROM resume", ps -> ps.execute());
    }

    @Override
    public Resume get(String uuid) {
        return sqlHelper.executeQuery(
                  "SELECT r.uuid, r.full_name, c.type, c.value " +
                        "FROM resume r " +
                        "LEFT JOIN contact c ON r.uuid = c.resume_uuid " +
                        "WHERE r.uuid = ?",
                ps -> {
                    ps.setObject(1, UUID.fromString(uuid));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new NotExistStorageException(uuid);
                        }
                        Resume r = new Resume(
                                rs.getString("uuid"),
                                rs.getString("full_name")
                        );
                        do {
                            addContact(rs, r);
                        } while (rs.next());
                        return r;
                    }
                });
    }

    @Override
    public void update(Resume r) {
        sqlHelper.transactionalExecute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE resume SET full_name = ? WHERE uuid = ?")) {
                ps.setString(1, r.getFullName());
                ps.setObject(2, UUID.fromString(r.getUuid()));
                if (ps.executeUpdate() == 0) {
                    throw new NotExistStorageException(r.getUuid());
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM contact WHERE resume_uuid = ?")) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.executeUpdate();
            }

            insertContacts(conn, r);
            return null;
        });
    }

    @Override
    public void save(Resume r) {
        sqlHelper.transactionalExecute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO resume (uuid, full_name) VALUES (?,?)")) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.setString(2, r.getFullName());
                ps.execute();
            }

            insertContacts(conn, r);
            return null;
        });
    }

    @Override
    public void delete(String uuid) {
        sqlHelper.execute(
                "DELETE FROM resume WHERE uuid = ?",
                ps -> {
                    ps.setObject(1, UUID.fromString(uuid));
                    if (ps.executeUpdate() == 0) {
                        throw new NotExistStorageException(uuid);
                    }
                });
    }

    @Override
    public List<Resume> getAllSorted() {
        return sqlHelper.executeQuery(
                  "SELECT r.uuid, r.full_name, c.type, c.value " +
                        "FROM resume r " +
                        "LEFT JOIN contact c ON r.uuid = c.resume_uuid " +
                        "ORDER BY r.full_name, r.uuid",
                ps -> {
                    try (ResultSet rs = ps.executeQuery()) {
                        Map<String, Resume> map = new LinkedHashMap<>();
                        while (rs.next()) {
                            String uuid = rs.getString("uuid");
                            Resume r = map.get(uuid);
                            if (r == null) {
                                r = new Resume(uuid, rs.getString("full_name"));
                                map.put(uuid, r);
                            }
                            addContact(rs, r);
                        }
                        return new ArrayList<>(map.values());
                    }
                });
    }

    @Override
    public int size() {
        return sqlHelper.executeQuery(
                "SELECT count(*) FROM resume",
                ps -> {
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? rs.getInt(1) : 0;
                    }
                });
    }

    private void addContact(ResultSet rs, Resume r) throws SQLException {
        String type = rs.getString("type");
        String value = rs.getString("value");
        if (type != null && value != null) {
            r.addContact(ContactType.valueOf(type), value);
        }
    }

    private void insertContacts(Connection conn, Resume r) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO contact (resume_uuid, type, value) VALUES (?,?,?)")) {
            for (Map.Entry<ContactType, String> e : r.getContacts().entrySet()) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.setString(2, e.getKey().name());
                ps.setString(3, e.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}