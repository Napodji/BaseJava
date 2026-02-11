package com.basejava.webapp.storage;

import com.basejava.webapp.model.Resume;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUuidStorage extends AbstractStorage<String> {
    private final Map<String, Resume> storage = new HashMap<>();

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    protected List<Resume> getList() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    protected String getSearchKey(String uuid) {
        return storage.containsKey(uuid) ? uuid : null;
    }

    @Override
    protected boolean isExist(String searchKey) {
        return searchKey != null;
    }

    @Override
    protected void saveResume(Resume r, String searchKey) {
        storage.put(r.getUuid(), r);
    }

    @Override
    protected Resume getResume(String searchKey) {
        return storage.get( searchKey);
    }

    @Override
    protected void deleteResume(String searchKey) {
        storage.remove( searchKey);
    }

    @Override
    protected void updateResume(Resume r, String searchKey) {
        storage.put( searchKey, r);
    }
}
