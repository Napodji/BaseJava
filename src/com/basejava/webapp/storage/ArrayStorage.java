package com.basejava.webapp.storage;

import com.basejava.webapp.model.Resume;

import java.util.Arrays;

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
        storage[size++] = r;
    }

    public Resume get(String uuid) {
        int index = findIndex(uuid);
        if (index == -1) {
            System.out.println("Ошибка: резюме с uuid " + uuid + " не найдено");
            return null;
        }
        return storage[index];
    }

    public Resume[] getAll() {
        return Arrays.copyOf(storage, size);
    }

    public void update(Resume r) {
        int index = findIndex(r.getUuid());
        if (index == -1) {
            System.out.println("Ошибка: резюме с uuid " + r.getUuid() + " не найдено");
            return;
        }
        storage[index] = r;
    }

    public void delete(String uuid) {
        int index = findIndex(uuid);
        if (index == -1) {
            System.out.println("Ошибка: резюме с uuid " + uuid + " не найдено");
            return;
        }
        size--;
        storage[index] = storage[size];
        storage[size] = null;
    }

    public void clear() {
        Arrays.fill(storage, 0, size, null);
        size = 0;
    }

    public int size() {
        return size;
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
