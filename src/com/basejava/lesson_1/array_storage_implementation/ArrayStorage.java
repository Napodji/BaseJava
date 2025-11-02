package com.basejava.lesson_1.array_storage_implementation;

import java.util.Arrays;

/**
 * Array based storage for Resumes
 */
public class ArrayStorage {
    private static final int CAPACITY = 10_000;

    private Resume[] storage = new Resume[CAPACITY];
    private int size = 0;

    void clear() {
        Arrays.fill(storage, 0, size, null);
        size = 0;
    }

    void save(Resume resume) {
        if (size >= CAPACITY) {
            System.out.println("Ошибка: хранилище заполнено");
            return;
        }
        storage[size] = resume;
        size++;
    }

    Resume get(String uuid) {
        int index = findIndex(uuid);
        if (index == -1) {
            return null;
        }
        return storage[index];
    }

    void delete(String uuid) {
        int index = findIndex(uuid);
        if (index == -1) {
            System.out.println("Ошибка: резюме " + uuid + " не найдено");
            return;
        }

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(storage, index + 1, storage, index, numMoved);
        }
        storage[--size] = null;
    }

    /**
     * @return array, contains only Resumes in storage (without null)
     */
    Resume[] getAll() {
        return Arrays.copyOf(storage, size);
    }

    int size() {
        return size;
    }

    private int findIndex(String uuid) {
        for (int i = 0; i < size; i++) {
            if (storage[i].uuid.equals(uuid)) {
                return i;
            }
        }
        return -1;
    }
}
