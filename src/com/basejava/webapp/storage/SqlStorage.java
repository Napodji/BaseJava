package com.basejava.webapp.storage;

import com.basejava.webapp.exception.NotExistStorageException;
import com.basejava.webapp.model.ContactType;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.sql.SqlHelper;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
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
                            String type = rs.getString("type");
                            String value = rs.getString("value");
                            if (type != null && value != null) {
                                r.addContact(ContactType.valueOf(type), value);
                            }
                        } while (rs.next());
                        return r;
                    }
                });
    }

    @Override
    public void update(Resume r) {
        sqlHelper.execute(
                "UPDATE resume SET full_name = ? WHERE uuid = ?",
                ps -> {
                    ps.setString(1, r.getFullName());
                    ps.setObject(2, UUID.fromString(r.getUuid()));
                    if (ps.executeUpdate() == 0) {
                        throw new NotExistStorageException(r.getUuid());
                    }
                });
    }

    @Override
    public void save(Resume r) {
        sqlHelper.execute(
                "INSERT INTO resume (uuid, full_name) VALUES (?, ?)",
                ps -> {
                    ps.setObject(1, UUID.fromString(r.getUuid()));
                    ps.setString(2, r.getFullName());
                    ps.execute();
                });

        for (Map.Entry<ContactType, String> e : r.getContacts().entrySet()) {
            sqlHelper.execute(
                    "INSERT INTO contact (resume_uuid, type, value) VALUES (?, ?, ?)",
                    ps -> {
                        ps.setObject(1, UUID.fromString(r.getUuid()));
                        ps.setString(2, e.getKey().name());
                        ps.setString(3, e.getValue());
                        ps.execute();
                    });
        }
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
                "SELECT * FROM resume r ORDER BY full_name, uuid",
                ps -> {
                    try (ResultSet rs = ps.executeQuery()) {
                        List<Resume> resumes = new ArrayList<>();
                        while (rs.next()) {
                            resumes.add(new Resume(
                                    rs.getString("uuid"),
                                    rs.getString("full_name")
                            ));
                        }
                        return resumes;
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
}