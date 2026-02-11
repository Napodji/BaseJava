package com.basejava.webapp.storage;

import com.basejava.webapp.model.Resume;
import java.util.ArrayList;
import java.util.List;

public class ListStorage extends AbstractStorage<Integer> {
    private final List<Resume> storage = new ArrayList<>();

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    protected List<Resume> getList() {
        return new ArrayList<>(storage);
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    protected Integer getSearchKey(String uuid) {
        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getUuid().equals(uuid)) {
                return i;
            }
        }
        return null;
    }

    @Override
    protected boolean isExist(Integer searchKey) {
        return searchKey != null;
    }

    @Override
    protected void saveResume(Resume r, Integer searchKey) {
        storage.add(r);
    }

    @Override
    protected Resume getResume(Integer searchKey) {
        return storage.get(searchKey);
    }

    @Override
    protected void deleteResume(Integer searchKey) {
        storage.remove(searchKey.intValue());
    }

    @Override
    protected void updateResume(Resume r, Integer searchKey) {
        storage.set(searchKey, r);
    }
}
