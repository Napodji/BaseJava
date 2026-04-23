package com.basejava.webapp.storage;

import com.basejava.webapp.exception.NotExistStorageException;
import com.basejava.webapp.model.AbstractSection;
import com.basejava.webapp.model.ContactType;
import com.basejava.webapp.model.ListSection;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.model.SectionType;
import com.basejava.webapp.model.TextSection;
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
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL Driver not found!", e);
        }
        sqlHelper = new SqlHelper(
                () -> DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        );
    }

    @Override
    public void clear() {
        sqlHelper.execute(
                "DELETE FROM resume",
                PreparedStatement::execute
        );
    }

    @Override
    public Resume get(String uuid) {
        return sqlHelper.executeQuery(
                """
                        SELECT uuid, full_name
                        FROM resume
                        WHERE uuid = ?
                        """,
                ps -> {
                    ps.setObject(1, UUID.fromString(uuid));
                    Resume r;
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new NotExistStorageException(uuid);
                        }
                        r = new Resume(
                                rs.getString("uuid"),
                                rs.getString("full_name")
                        );
                    }

                    // контакты
                    sqlHelper.execute(
                            """
                                    SELECT type, value
                                    FROM contact
                                    WHERE resume_uuid = ?
                                    """,
                            psContacts -> {
                                psContacts.setObject(1, UUID.fromString(uuid));
                                try (ResultSet rs = psContacts.executeQuery()) {
                                    while (rs.next()) {
                                        addContact(rs, r);
                                    }
                                }
                            }
                    );

                    // секции (без OrganizationSection)
                    sqlHelper.execute(
                            """
                                    SELECT type, content
                                    FROM section
                                    WHERE resume_uuid = ?
                                    """,
                            psSections -> {
                                psSections.setObject(1, UUID.fromString(uuid));
                                try (ResultSet rs = psSections.executeQuery()) {
                                    while (rs.next()) {
                                        addSection(rs, r);
                                    }
                                }
                            }
                    );

                    return r;
                }
        );
    }

    @Override
    public void update(Resume r) {
        sqlHelper.transactionalExecute(conn -> {
            // обновляем резюме
            try (PreparedStatement ps = conn.prepareStatement(
                    """
                            UPDATE resume
                            SET full_name = ?
                            WHERE uuid = ?
                            """
            )) {
                ps.setString(1, r.getFullName());
                ps.setObject(2, UUID.fromString(r.getUuid()));
                if (ps.executeUpdate() == 0) {
                    throw new NotExistStorageException(r.getUuid());
                }
            }

            // чистим контакты
            try (PreparedStatement ps = conn.prepareStatement(
                    """
                            DELETE FROM contact
                            WHERE resume_uuid = ?
                            """
            )) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.executeUpdate();
            }

            // чистим секции
            try (PreparedStatement ps = conn.prepareStatement(
                    """
                            DELETE FROM section
                            WHERE resume_uuid = ?
                            """
            )) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.executeUpdate();
            }

            // записываем заново
            insertContacts(conn, r);
            insertSections(conn, r);

            return null;
        });
    }

    @Override
    public void save(Resume r) {
        sqlHelper.transactionalExecute(conn -> {
            // резюме
            try (PreparedStatement ps = conn.prepareStatement(
                    """
                            INSERT INTO resume (uuid, full_name)
                            VALUES (?, ?)
                            """
            )) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.setString(2, r.getFullName());
                ps.execute();
            }

            // контакты и секции
            insertContacts(conn, r);
            insertSections(conn, r);

            return null;
        });
    }

    @Override
    public void delete(String uuid) {
        sqlHelper.execute(
                """
                        DELETE FROM resume
                        WHERE uuid = ?
                        """,
                ps -> {
                    ps.setObject(1, UUID.fromString(uuid));
                    if (ps.executeUpdate() == 0) {
                        throw new NotExistStorageException(uuid);
                    }
                }
        );
    }

    @Override
    public List<Resume> getAllSorted() {
        return sqlHelper.executeQuery(
                """
                        SELECT uuid, full_name
                        FROM resume
                        ORDER BY full_name, uuid
                        """,
                ps -> {
                    Map<String, Resume> map = new LinkedHashMap<>();

                    // 1) тянем все резюме
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String uuid = rs.getString("uuid");
                            String fullName = rs.getString("full_name");
                            map.put(uuid, new Resume(uuid, fullName));
                        }
                    }

                    if (map.isEmpty()) {
                        return new ArrayList<>();
                    }

                    // 2) тянем все контакты
                    sqlHelper.execute(
                            """
                                    SELECT resume_uuid, type, value
                                    FROM contact
                                    """,
                            psContacts -> {
                                try (ResultSet rs = psContacts.executeQuery()) {
                                    while (rs.next()) {
                                        Resume r = map.get(rs.getString("resume_uuid"));
                                        if (r != null) {
                                            addContact(rs, r);
                                        }
                                    }
                                }
                            }
                    );

                    // 3) тянем все секции
                    sqlHelper.execute(
                            """
                                    SELECT resume_uuid, type, content
                                    FROM section
                                    """,
                            psSections -> {
                                try (ResultSet rs = psSections.executeQuery()) {
                                    while (rs.next()) {
                                        Resume r = map.get(rs.getString("resume_uuid"));
                                        if (r != null) {
                                            addSection(rs, r);
                                        }
                                    }
                                }
                            }
                    );

                    return new ArrayList<>(map.values());
                }
        );
    }

    @Override
    public int size() {
        return sqlHelper.executeQuery(
                "SELECT count(*) FROM resume",
                ps -> {
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? rs.getInt(1) : 0;
                    }
                }
        );
    }

    private void addContact(ResultSet rs, Resume r) throws SQLException {
        String type = rs.getString("type");
        String value = rs.getString("value");
        if (type != null && value != null) {
            r.addContact(ContactType.valueOf(type), value);
        }
    }

    private void addSection(ResultSet rs, Resume r) throws SQLException {
        SectionType type = SectionType.valueOf(rs.getString("type"));
        String content = rs.getString("content");

        switch (type) {
            case OBJECTIVE, PERSONAL -> r.setSection(type, new TextSection(content));
            case ACHIEVEMENT, QUALIFICATIONS -> {
                String[] items = content.split("\n");
                r.setSection(type, new ListSection(items));
            }
            default -> {
                // EXPERIENCE, EDUCATION (OrganizationSection) пока не храним в БД
            }
        }
    }

    private void insertContacts(Connection conn, Resume r) throws SQLException {
        UUID resumeUuid = UUID.fromString(r.getUuid());

        try (PreparedStatement ps = conn.prepareStatement(
                """
                        INSERT INTO contact (resume_uuid, type, value)
                        VALUES (?, ?, ?)
                        """
        )) {
            for (Map.Entry<ContactType, String> e : r.getContacts().entrySet()) {
                ps.setObject(1, resumeUuid);
                ps.setString(2, e.getKey().name());
                ps.setString(3, e.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertSections(Connection conn, Resume r) throws SQLException {
        UUID resumeUuid = UUID.fromString(r.getUuid());

        try (PreparedStatement ps = conn.prepareStatement(
                """
                        INSERT INTO section (resume_uuid, type, content)
                        VALUES (?, ?, ?)
                        """
        )) {
            for (Map.Entry<SectionType, AbstractSection> e : r.getSections().entrySet()) {
                SectionType type = e.getKey();
                AbstractSection section = e.getValue();

                switch (type) {
                    case OBJECTIVE, PERSONAL -> {
                        ps.setObject(1, resumeUuid);
                        ps.setString(2, type.name());
                        ps.setString(3, ((TextSection) section).getContent());
                        ps.addBatch();
                    }
                    case ACHIEVEMENT, QUALIFICATIONS -> {
                        List<String> items = ((ListSection) section).getItems();
                        ps.setObject(1, resumeUuid);
                        ps.setString(2, type.name());
                        ps.setString(3, String.join("\n", items));
                        ps.addBatch();
                    }
                    default -> {
                        // EXPERIENCE, EDUCATION (OrganizationSection) не сохраняем
                    }
                }
            }
            ps.executeBatch();
        }
    }
}