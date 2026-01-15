package com.basejava.webapp.storage;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.basejava.webapp.exception.StorageException;
import com.basejava.webapp.model.Resume;
import org.junit.jupiter.api.Test;

public abstract class AbstractArrayStorageTest extends AbstractStorageTest {
    protected AbstractArrayStorageTest(Storage storage) {
        super(storage);
    }

    @Test
    public void saveOverflowTest() {
        storage.clear();
        try {
            for (int i = 0; i < AbstractArrayStorage.STORAGE_LIMIT; i++) {
                storage.save(new Resume("uuid" + i, "Name" + i));
            }
        } catch (StorageException e) {
            fail("Storage overflow occurred before reaching limit");
        }
        assertThrows(StorageException.class, () -> storage.save(new Resume("overflow", "Overflow Name")));
    }
}
