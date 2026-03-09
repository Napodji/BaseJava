package com.basejava.webapp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.basejava.webapp.ResumeTestData;
import com.basejava.webapp.exception.ExistStorageException;
import com.basejava.webapp.exception.NotExistStorageException;
import com.basejava.webapp.model.Resume;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractStorageTest {
    protected static final File STORAGE_DIR = new File("storage");
    protected Storage storage;

    private static final String UUID_1 = "uuid1";
    private static final String UUID_2 = "uuid2";
    private static final String UUID_3 = "uuid3";
    private static final String UUID_4 = "uuid4";

    private static final Resume RESUME_1 = ResumeTestData.createResume(UUID_1, "Андрей Тихонов");
    private static final Resume RESUME_2 = ResumeTestData.createResume(UUID_2, "Егор Титов");
    private static final Resume RESUME_3 = ResumeTestData.createResume(UUID_3, "Илья Цымбаларь");
    private static final Resume RESUME_4 = ResumeTestData.createResume(UUID_4, "Александр Филимонов");

    protected AbstractStorageTest(Storage storage) {
        this.storage = storage;
    }

    @BeforeEach
    public void setUp() {
        storage.clear();
        storage.save(RESUME_1);
        storage.save(RESUME_2);
        storage.save(RESUME_3);
    }

    @Test
    public void sizeTest() {
        assertEquals(3, storage.size());
    }

    @Test
    public void clearTest() {
        storage.clear();
        assertEquals(0, storage.size());
        assertEquals(List.of(), storage.getAllSorted());
    }

    @Test
    public void saveTest() {
        storage.save(RESUME_4);
        assertEquals(4, storage.size());
        assertEquals(RESUME_4, storage.get(UUID_4));
    }

    @Test
    public void saveExistTest() {
        assertThrows(ExistStorageException.class, () -> storage.save(RESUME_1));
    }

    @Test
    public void updateTest() {
        Resume updatedResume = ResumeTestData.createResume(UUID_2, "New name");
        storage.update(updatedResume);
        assertEquals(updatedResume, storage.get(UUID_2));
    }

    @Test
    public void updateNotExistTest() {
        assertThrows(NotExistStorageException.class, () -> storage.update(RESUME_4));
    }

    @Test
    public void getTest() {
        assertEquals(RESUME_1, storage.get(UUID_1));
        assertEquals(RESUME_2, storage.get(UUID_2));
        assertEquals(RESUME_3, storage.get(UUID_3));
    }

    @Test
    public void getNotExistTest() {
        assertThrows(NotExistStorageException.class, () -> storage.get(UUID_4));
    }

    @Test
    public void deleteTest() {
        storage.delete(UUID_2);
        assertEquals(2, storage.size());
        assertThrows(NotExistStorageException.class, () -> storage.get(UUID_2));
    }

    @Test
    public void deleteNotExistTest() {
        assertThrows(NotExistStorageException.class, () -> storage.delete(UUID_4));
    }

    @Test
    public void getAllSortedTest() {
        List<Resume> expected = List.of(RESUME_1, RESUME_2, RESUME_3);
        assertEquals(expected, storage.getAllSorted());
    }
}
