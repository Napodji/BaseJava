package com.basejava.webapp;

import com.basejava.webapp.model.AbstractSection;
import com.basejava.webapp.model.ContactType;
import com.basejava.webapp.model.ListSection;
import com.basejava.webapp.model.Organization;
import com.basejava.webapp.model.OrganizationSection;
import com.basejava.webapp.model.Position;
import com.basejava.webapp.model.Resume;
import com.basejava.webapp.model.SectionType;
import com.basejava.webapp.model.TextSection;

public class ResumeTestData {
    public static void main(String[] args) {
        Resume resume = createResume("777", "Григорий Кислин");
        printResume(resume);
    }

    public static Resume createResume(String uuid, String fullName) {
        Resume resume = new Resume(uuid, fullName);

        // CONTACTS
        resume.setContact(ContactType.PHONE, "+7(921) 855-0482");
        resume.setContact(ContactType.SKYPE, "grigory.kislin");
        resume.setContact(ContactType.MAIL, "gkislin@yandex.ru");
        resume.setContact(ContactType.LINKEDIN, "https://www.linkedin.com/in/gkislin");
        resume.setContact(ContactType.GITHUB, "https://github.com/gkislin");
        resume.setContact(ContactType.STACKOVERFLOW, "https://stackoverflow.com/users/548473");
        resume.setContact(ContactType.HOME_PAGE, "http://gkislin.ru/");

        // OBJECTIVE
        resume.setSection(SectionType.OBJECTIVE,
                new TextSection("Ведущий стажировок и корпоративного обучения "
                        + "по Java Web и Enterprise технологиям"));

        // PERSONAL
        resume.setSection(SectionType.PERSONAL,
                new TextSection("Аналитический склад ума, сильная логика, "
                        + "креативность, инициативность."));

        // ACHIEVEMENT
        resume.setSection(SectionType.ACHIEVEMENT, new ListSection(
                "Организация команды и target architecture для многомиллионного проекта.",
                "Реализация двух домов онлайн платформы JavaOps.",
                "Реализация c нуля Rich-client приложения на Swing."
        ));

        // QUALIFICATIONS
        resume.setSection(SectionType.QUALIFICATIONS, new ListSection(
                "JEE AS: GlassFish, JBoss, Tomcat, Jetty, WebLogic",
                "Version control: Subversion, Git, Mercury",
                "DB: PostgreSQL, Oracle, MySQL, SQLite, H2",
                "Languages: Java, Scala, Python, JavaScript, Groovy"
        ));

        // EXPERIENCE
        resume.setSection(SectionType.EXPERIENCE, new OrganizationSection(
                new Organization("Java Online Projects", "http://javaops.ru/",
                        new Position("10/2013", "Сейчас", "Автор проекта",
                                "Создание, организация и проведение Java онлайн проектов.")),
                new Organization("Wrike", "https://www.wrike.com/",
                        new Position("10/2014", "01/2016", "Старший разработчик",
                                "Проектирование и разработка онлайн платформы."))
        ));

        // EDUCATION
        resume.setSection(SectionType.EDUCATION, new OrganizationSection(
                new Organization("Coursera", "https://www.coursera.org/",
                        new Position("03/2013", "05/2013",
                                "Functional Programming in Scala", null)),
                new Organization("ИТМО", "http://www.ifmo.ru/",
                        new Position("09/1993", "07/1996", "Аспирантура", null),
                        new Position("09/1987", "07/1993", "Инженер", null))
        ));

        return resume;
    }

    public static void printResume(Resume resume) {
        System.out.println("=== " + resume.getFullName() + " ===\n");

        System.out.println("--- КОНТАКТЫ ---");
        for (ContactType type : ContactType.values()) {
            String value = resume.getContact(type);
            if (value != null) {
                System.out.println(type.getTitle() + ": " + value);
            }
        }

        System.out.println("\n--- СЕКЦИИ ---");
        for (SectionType type : SectionType.values()) {
            AbstractSection section = resume.getSection(type);
            if (section != null) {
                System.out.println("\n" + type.getTitle() + ":\n" + section);
            }
        }
    }
}
