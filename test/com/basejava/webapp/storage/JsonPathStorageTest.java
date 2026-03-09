package com.basejava.webapp.storage;

import com.basejava.webapp.storage.serializer.JsonStreamSerializer;

class JsonPathStorageTest extends AbstractStorageTest {
    public JsonPathStorageTest() {
        super(new FileStorage(STORAGE_DIR, new JsonStreamSerializer()));
    }
}