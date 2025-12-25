package com.basejava.webapp.storage;

import com.basejava.webapp.exception.StorageException;
import com.basejava.webapp.model.Resume;
import java.util.Arrays;

/**
 * Array based storage for Resumes
 */
public abstract class AbstractArrayStorage extends AbstractStorage {
    protected static final int STORAGE_LIMIT = 10_000;
    protected final Resume[] storage = new Resume[STORAGE_LIMIT];
    protected int size = 0;

    @Override
    public final void clear() {
        Arrays.fill(storage, 0, size, null);
        size = 0;
    }

    @Override
    public final Resume[] getAll() {
        return Arrays.copyOf(storage, size);
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    protected Object getSearchKey(String uuid) {
        return findIndex(uuid);
    }

    @Override
    protected boolean isExist(Object searchKey) {
        return (int) searchKey >= 0;
    }

    @Override
    protected void saveResume(Resume r, Object searchKey) {
        if (size >= STORAGE_LIMIT) {
            throw new StorageException("Storage overflow", r.getUuid());
        }
        insertResume(r, (int) searchKey);
        size++;
    }

    @Override
    protected Resume getResume(Object searchKey) {
        return storage[(int) searchKey];
    }

    @Override
    protected void deleteResume(Object searchKey) {
        fillDeletedElement((int) searchKey);
        storage[size - 1] = null;
        size--;
    }

    @Override
    protected void updateResume(Resume r, Object searchKey) {
        storage[(int) searchKey] = r;
    }

    protected abstract int findIndex(String uuid);

    protected abstract void insertResume(Resume r, int index);

    protected abstract void fillDeletedElement(int index);
}
