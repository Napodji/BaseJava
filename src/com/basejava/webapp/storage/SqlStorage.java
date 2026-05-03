package com.basejava.webapp.storage;

import com.basejava.webapp.exception.NotExistStorageException;
import com.basejava.webapp.model.AbstractSection;
import com.basejava.webapp.model.ContactType;
import com.basejava.webapp.model.Link;
import com.basejava.webapp.model.ListSection;
import com.basejava.webapp.model.Organization;
import com.basejava.webapp.model.OrganizationSection;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.model.SectionType;
import com.basejava.webapp.model.TextSection;
import com.basejava.webapp.sql.SqlHelper;
import java.sql.Connection;
import java.sql.Date;
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
                    UUID resumeUuid = UUID.fromString(uuid);
                    ps.setObject(1, resumeUuid);

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

                    sqlHelper.execute(
                            """
                                    SELECT type, value
                                    FROM contact
                                    WHERE resume_uuid = ?
                                    """,
                            psContacts -> {
                                psContacts.setObject(1, resumeUuid);
                                try (ResultSet rs = psContacts.executeQuery()) {
                                    while (rs.next()) {
                                        addContact(rs, r);
                                    }
                                }
                            }
                    );

                    sqlHelper.execute(
                            """
                                    SELECT type, content
                                    FROM section
                                    WHERE resume_uuid = ?
                                    """,
                            psSections -> {
                                psSections.setObject(1, resumeUuid);
                                try (ResultSet rs = psSections.executeQuery()) {
                                    while (rs.next()) {
                                        addSection(rs, r);
                                    }
                                }
                            }
                    );

                    sqlHelper.execute(
                            """
                                    SELECT o.id, o.section_type, o.name, o.url,
                                           op.start_date, op.end_date, op.title, op.description
                                    FROM organization o
                                    LEFT JOIN organization_position op ON o.id = op.organization_id
                                    WHERE o.resume_uuid = ?
                                    ORDER BY o.id, op.start_date DESC
                                    """,
                            psOrgs -> {
                                psOrgs.setObject(1, resumeUuid);
                                try (ResultSet rs = psOrgs.executeQuery()) {
                                    addOrganizations(rs, r);
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

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM contact WHERE resume_uuid = ?"
            )) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM section WHERE resume_uuid = ?"
            )) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM organization WHERE resume_uuid = ?"
            )) {
                ps.setObject(1, UUID.fromString(r.getUuid()));
                ps.executeUpdate();
            }

            insertContacts(conn, r);
            insertSections(conn, r);
            insertOrganizations(conn, r);

            return null;
        });
    }

    @Override
    public void save(Resume r) {
        sqlHelper.transactionalExecute(conn -> {
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

            insertContacts(conn, r);
            insertSections(conn, r);
            insertOrganizations(conn, r);

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
                    Map<String, Resume> resumesByUuid = new LinkedHashMap<>();

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String uuid = rs.getString("uuid");
                            resumesByUuid.put(uuid, new Resume(uuid, rs.getString("full_name")));
                        }
                    }

                    if (resumesByUuid.isEmpty()) {
                        return new ArrayList<>();
                    }

                    sqlHelper.execute(
                            "SELECT resume_uuid, type, value FROM contact",
                            psContacts -> {
                                try (ResultSet rs = psContacts.executeQuery()) {
                                    while (rs.next()) {
                                        Resume r = resumesByUuid.get(rs.getString("resume_uuid"));
                                        if (r != null) addContact(rs, r);
                                    }
                                }
                            }
                    );

                    sqlHelper.execute(
                            "SELECT resume_uuid, type, content FROM section",
                            psSections -> {
                                try (ResultSet rs = psSections.executeQuery()) {
                                    while (rs.next()) {
                                        Resume r = resumesByUuid.get(rs.getString("resume_uuid"));
                                        if (r != null) addSection(rs, r);
                                    }
                                }
                            }
                    );

                    sqlHelper.execute(
                            """
                                       SELECT o.resume_uuid, o.id, o.section_type, o.name, o.url,
                                              op.start_date, op.end_date, op.title, op.description
                                         FROM organization o
                                    LEFT JOIN organization_position op ON o.id = op.organization_id
                                     ORDER BY o.resume_uuid, o.id, op.start_date DESC
                                    """,
                            psOrgs -> {
                                try (ResultSet rs = psOrgs.executeQuery()) {
                                    String currentUuid = null;
                                    Resume currentResume = null;
                                    List<ResultSet> rows = new ArrayList<>();
                                    // обрабатываем построчно через вспомогательный метод
                                    Map<Integer, List<Object[]>> orgRows = new LinkedHashMap<>();
                                    Map<Integer, String[]> orgMeta = new LinkedHashMap<>();
                                    Map<Integer, String> orgResumeUuid = new LinkedHashMap<>();
                                    while (rs.next()) {
                                        int orgId = rs.getInt("id");
                                        if (!orgMeta.containsKey(orgId)) {
                                            orgMeta.put(orgId, new String[]{
                                                    rs.getString("section_type"),
                                                    rs.getString("name"),
                                                    rs.getString("url")
                                            });
                                            orgResumeUuid.put(orgId, rs.getString("resume_uuid"));
                                            orgRows.put(orgId, new ArrayList<>());
                                        }
                                        Date startDate = rs.getDate("start_date");
                                        if (startDate != null) {
                                            orgRows.get(orgId).add(new Object[]{
                                                    startDate,
                                                    rs.getDate("end_date"),
                                                    rs.getString("title"),
                                                    rs.getString("description")
                                            });
                                        }
                                    }
                                    Map<String, Map<SectionType, List<Organization>>> byResume = new LinkedHashMap<>();
                                    for (Map.Entry<Integer, String[]> entry : orgMeta.entrySet()) {
                                        int orgId = entry.getKey();
                                        String[] meta = entry.getValue();
                                        String rUuid = orgResumeUuid.get(orgId);
                                        SectionType sType = SectionType.valueOf(meta[0]);
                                        List<Organization.Position> positions = new ArrayList<>();
                                        for (Object[] row : orgRows.get(orgId)) {
                                            positions.add(new Organization.Position(
                                                    ((Date) row[0]).toLocalDate(),
                                                    ((Date) row[1]).toLocalDate(),
                                                    (String) row[2],
                                                    (String) row[3]
                                            ));
                                        }
                                        Organization org = new Organization(new Link(meta[1], meta[2]), positions);
                                        byResume.computeIfAbsent(rUuid, k -> new LinkedHashMap<>())
                                                .computeIfAbsent(sType, k -> new ArrayList<>())
                                                .add(org);
                                    }
                                    for (Map.Entry<String, Map<SectionType, List<Organization>>> entry : byResume.entrySet()) {
                                        Resume r = resumesByUuid.get(entry.getKey());
                                        if (r != null) {
                                            for (Map.Entry<SectionType, List<Organization>> sec : entry.getValue().entrySet()) {
                                                r.setSection(sec.getKey(), new OrganizationSection(sec.getValue()));
                                            }
                                        }
                                    }
                                }
                            }
                    );

                    return new ArrayList<>(resumesByUuid.values());
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
            case ACHIEVEMENT, QUALIFICATIONS -> r.setSection(type, new ListSection(content.split("\n")));
        }
    }

    private void addOrganizations(ResultSet rs, Resume r) throws SQLException {
        Map<Integer, Organization> orgById = new LinkedHashMap<>();
        Map<Integer, SectionType> sectionTypeById = new LinkedHashMap<>();
        Map<Integer, List<Organization.Position>> positionsById = new LinkedHashMap<>();

        while (rs.next()) {
            int orgId = rs.getInt("id");
            if (!orgById.containsKey(orgId)) {
                SectionType sType = SectionType.valueOf(rs.getString("section_type"));
                sectionTypeById.put(orgId, sType);
                positionsById.put(orgId, new ArrayList<>());
                orgById.put(orgId, new Organization(
                        new Link(rs.getString("name"), rs.getString("url")),
                        positionsById.get(orgId)
                ));
            }
            Date startDate = rs.getDate("start_date");
            if (startDate != null) {
                positionsById.get(orgId).add(new Organization.Position(
                        startDate.toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getString("title"),
                        rs.getString("description")
                ));
            }
        }

        Map<SectionType, List<Organization>> bySection = new LinkedHashMap<>();
        for (Map.Entry<Integer, Organization> entry : orgById.entrySet()) {
            SectionType sType = sectionTypeById.get(entry.getKey());
            bySection.computeIfAbsent(sType, k -> new ArrayList<>()).add(entry.getValue());
        }
        for (Map.Entry<SectionType, List<Organization>> entry : bySection.entrySet()) {
            r.setSection(entry.getKey(), new OrganizationSection(entry.getValue()));
        }
    }

    private void insertContacts(Connection conn, Resume r) throws SQLException {
        UUID resumeUuid = UUID.fromString(r.getUuid());
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO contact (resume_uuid, type, value) VALUES (?, ?, ?)"
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
                "INSERT INTO section (resume_uuid, type, content) VALUES (?, ?, ?)"
        )) {
            for (Map.Entry<SectionType, AbstractSection> e : r.getSections().entrySet()) {
                SectionType type = e.getKey();
                switch (type) {
                    case OBJECTIVE, PERSONAL -> {
                        ps.setObject(1, resumeUuid);
                        ps.setString(2, type.name());
                        ps.setString(3, ((TextSection) e.getValue()).getContent());
                        ps.addBatch();
                    }
                    case ACHIEVEMENT, QUALIFICATIONS -> {
                        ps.setObject(1, resumeUuid);
                        ps.setString(2, type.name());
                        ps.setString(3, String.join("\n", ((ListSection) e.getValue()).getItems()));
                        ps.addBatch();
                    }
                }
            }
            ps.executeBatch();
        }
    }

    private void insertOrganizations(Connection conn, Resume r) throws SQLException {
        UUID resumeUuid = UUID.fromString(r.getUuid());
        for (Map.Entry<SectionType, AbstractSection> e : r.getSections().entrySet()) {
            SectionType type = e.getKey();
            if (type != SectionType.EXPERIENCE && type != SectionType.EDUCATION) continue;

            OrganizationSection section = (OrganizationSection) e.getValue();
            for (Organization org : section.getOrganizations()) {
                int orgId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO organization (resume_uuid, section_type, name, url) VALUES (?, ?, ?, ?) RETURNING id"
                )) {
                    ps.setObject(1, resumeUuid);
                    ps.setString(2, type.name());
                    ps.setString(3, org.getHomePage().getName());
                    ps.setString(4, org.getHomePage().getUrl());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        orgId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO organization_position (organization_id, start_date, end_date, title, description) VALUES (?, ?, ?, ?, ?)"
                )) {
                    for (Organization.Position pos : org.getPositions()) {
                        ps.setInt(1, orgId);
                        ps.setDate(2, Date.valueOf(pos.getStartDate()));
                        ps.setDate(3, Date.valueOf(pos.getEndDate()));
                        ps.setString(4, pos.getTitle());
                        ps.setString(5, pos.getDescription());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }
}