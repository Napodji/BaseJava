package com.basejava.webapp.storage;

import com.basejava.webapp.exception.ExistStorageException;
import com.basejava.webapp.exception.NotExistStorageException;
import com.basejava.webapp.model.Resume;

public abstract class AbstractStorage implements Storage {
    @Override
    public void update(Resume r) {
        Object searchKey = getExistedSearchKey(r.getUuid());
        updateResume(r, searchKey);
    }

    @Override
    public void save(Resume r) {
        Object searchKey = getNotExistedSearchKey(r.getUuid());
        saveResume(r, searchKey);
    }

    @Override
    public Resume get(String uuid) {
        Object searchKey = getExistedSearchKey(uuid);
        return getResume(searchKey);
    }

    @Override
    public void delete(String uuid) {
        Object searchKey = getExistedSearchKey(uuid);
        deleteResume(searchKey);
    }

    private Object getExistedSearchKey(String uuid) {
        Object searchKey = getSearchKey(uuid);
        if (!isExist(searchKey)) {
            throw new NotExistStorageException(uuid);
        }
        return searchKey;
    }

    private Object getNotExistedSearchKey(String uuid) {
        Object searchKey = getSearchKey(uuid);
        if (isExist(searchKey)) {
            throw new ExistStorageException(uuid);
        }
        return searchKey;
    }

    protected abstract Object getSearchKey(String uuid);

    protected abstract boolean isExist(Object searchKey);

    protected abstract void updateResume(Resume r, Object searchKey);

    protected abstract void saveResume(Resume r, Object searchKey);

    protected abstract Resume getResume(Object searchKey);

    protected abstract void deleteResume(Object searchKey);
}
