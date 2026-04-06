package com.basejava.webapp.storage;

import com.basejava.webapp.util.Config;

public class SqlStorageTest extends AbstractStorageTest {
    public SqlStorageTest() {
        super(new SqlStorage(
                Config.get().getDbUrl(),
                Config.get().getDbUser(),
                Config.get().getDbPassword()));
    }
}