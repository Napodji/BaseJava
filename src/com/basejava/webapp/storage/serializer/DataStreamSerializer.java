package com.basejava.webapp.storage.serializer;

import com.basejava.webapp.model.AbstractSection;
import com.basejava.webapp.model.ContactType;
import com.basejava.webapp.model.ListSection;
import com.basejava.webapp.model.Organization;
import com.basejava.webapp.model.OrganizationSection;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.model.SectionType;
import com.basejava.webapp.model.TextSection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataStreamSerializer implements StreamSerializer {
    @Override
    public void doWrite(Resume r, OutputStream os) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(os)) {
            dos.writeUTF(r.getUuid());
            dos.writeUTF(r.getFullName());

            // Contacts
            Map<ContactType, String> contacts = r.getContacts();
            dos.writeInt(contacts.size());
            for (Map.Entry<ContactType, String> entry : contacts.entrySet()) {
                dos.writeUTF(entry.getKey().name());
                dos.writeUTF(entry.getValue());
            }

            // Sections
            Map<SectionType, AbstractSection> sections = r.getSections();
            dos.writeInt(sections.size());
            for (Map.Entry<SectionType, AbstractSection> entry : sections.entrySet()) {
                SectionType type = entry.getKey();
                dos.writeUTF(type.name());
                switch (type) {
                    case PERSONAL, OBJECTIVE -> {
                        dos.writeUTF(((TextSection) entry.getValue()).getContent());
                    }
                    case ACHIEVEMENT, QUALIFICATIONS -> {
                        List<String> items = ((ListSection) entry.getValue()).getItems();
                        dos.writeInt(items.size());
                        for (String item : items) {
                            dos.writeUTF(item);
                        }
                    }
                    case EXPERIENCE, EDUCATION -> {
                        List<Organization> orgs = ((OrganizationSection) entry.getValue()).getOrganizations();
                        dos.writeInt(orgs.size());
                        for (Organization org : orgs) {
                            dos.writeUTF(org.getHomePage().getName());
                            dos.writeUTF(nullSafe(org.getHomePage().getUrl()));
                            List<Organization.Position> positions = org.getPositions();
                            dos.writeInt(positions.size());
                            for (Organization.Position pos : positions) {
                                writeLocalDate(dos, pos.getStartDate());
                                writeLocalDate(dos, pos.getEndDate());
                                dos.writeUTF(pos.getTitle());
                                dos.writeUTF(nullSafe(pos.getDescription()));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Resume doRead(InputStream is) throws IOException {
        try (DataInputStream dis = new DataInputStream(is)) {
            String uuid = dis.readUTF();
            String fullName = dis.readUTF();
            Resume resume = new Resume(uuid, fullName);

            // Contacts
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                resume.setContact(ContactType.valueOf(dis.readUTF()), dis.readUTF());
            }

            // Sections
            int sectionSize = dis.readInt();
            for (int i = 0; i < sectionSize; i++) {
                SectionType type = SectionType.valueOf(dis.readUTF());
                resume.setSection(type, readSection(dis, type));
            }

            return resume;
        }
    }

    private AbstractSection readSection(DataInputStream dis, SectionType type)
            throws IOException {
        return switch (type) {
            case PERSONAL, OBJECTIVE -> new TextSection(dis.readUTF());
            case ACHIEVEMENT, QUALIFICATIONS -> {
                int size = dis.readInt();
                List<String> items = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    items.add(dis.readUTF());
                }
                yield new ListSection(items);
            }
            case EXPERIENCE, EDUCATION -> {
                int orgSize = dis.readInt();
                List<Organization> orgs = new ArrayList<>();
                for (int i = 0; i < orgSize; i++) {
                    String name = dis.readUTF();
                    String url = emptyToNull(dis.readUTF());
                    int posSize = dis.readInt();
                    List<Organization.Position> positions = new ArrayList<>();
                    for (int j = 0; j < posSize; j++) {
                        positions.add(new Organization.Position(
                                readLocalDate(dis),
                                readLocalDate(dis),
                                dis.readUTF(),
                                emptyToNull(dis.readUTF())
                        ));
                    }
                    orgs.add(new Organization(name, url, positions));
                }
                yield new OrganizationSection(orgs);
            }
        };
    }

    private void writeLocalDate(DataOutputStream dos, LocalDate date) throws IOException {
        dos.writeInt(date.getYear());
        dos.writeInt(date.getMonthValue());
    }

    private LocalDate readLocalDate(DataInputStream dis) throws IOException {
        return LocalDate.of(dis.readInt(), dis.readInt(), 1);
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
