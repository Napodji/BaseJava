package com.basejava.webapp.storage;

import com.basejava.webapp.model.Resume;

/**
 * Array based storage for Resumes
 */
public class ArrayStorage {
    private static final int STORAGE_LIMIT = 10000;

    private Resume[] storage = new Resume[STORAGE_LIMIT];
    private int size = 0;

    public void save(Resume r) {
        if (size >= STORAGE_LIMIT) {
            System.out.println("Ошибка: хранилище переполнено");
            return;
        }
        if (findIndex(r.getUuid()) != -1) {
            System.out.println("Ошибка: резюме с uuid " + r.getUuid() + " уже существует");
            return;
        }
        storage[size] = r;
        size++;
    }

    public Resume get(String uuid) {
        int index = getExistingIndex(uuid);
        if (index == -1) {
            return null;
        }
        return storage[index];
    }

    public Resume[] getAll() {
        Resume[] result = new Resume[size];
        System.arraycopy(storage, 0, result, 0, size);
        return result;
    }

    public void update(Resume r) {
        int index = getExistingIndex(r.getUuid());
        if (index == -1) {
            return;
        }
        storage[index] = r;
    }

    public void delete(String uuid) {
        int index = getExistingIndex(uuid);
        if (index == -1) {
            return;
        }
        storage[index] = storage[size - 1];
        storage[size - 1] = null;
        size--;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            storage[i] = null;
        }
        size = 0;
    }

    public int size() {
        return size;
    }

    private int getExistingIndex(String uuid) {
        int index = findIndex(uuid);
        if (index == -1) {
            System.out.println("Ошибка: резюме с uuid " + uuid + " не найдено");
        }
        return index;
    }

    private int findIndex(String uuid) {
        for (int i = 0; i < size; i++) {
            if (storage[i].getUuid().equals(uuid)) {
                return i;
            }
        }
        return -1;
    }
}
