package com.basejava.webapp.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.basejava.webapp.exception.ExistStorageException;
import com.basejava.webapp.exception.NotExistStorageException;
import com.basejava.webapp.model.Resume;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractStorageTest {
    protected Storage storage;

    private static final String UUID_1 = "uuid1";
    private static final String UUID_2 = "uuid2";
    private static final String UUID_3 = "uuid3";
    private static final String UUID_4 = "uuid4";

    private static final Resume RESUME_1 = new Resume(UUID_1, "Андрей Тихонов");
    private static final Resume RESUME_2 = new Resume(UUID_2, "Егор Титов");
    private static final Resume RESUME_3 = new Resume(UUID_3, "Илья Цымбаларь");
    private static final Resume RESUME_4 = new Resume(UUID_4, "Александр Филимонов");

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
        assertEquals(Collections.emptyList(), storage.getAllSorted());
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
        Resume newResume = new Resume(UUID_2, "New name");
        storage.update(newResume);
        assertSame(newResume, storage.get(UUID_2));
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
        List<Resume> actual = storage.getAllSorted();
        assertEquals(3, actual.size());
        assertEquals(RESUME_1, actual.get(0));
        assertEquals(RESUME_2, actual.get(1));
        assertEquals(RESUME_3, actual.get(2));
    }

}
