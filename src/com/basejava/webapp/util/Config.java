package com.basejava.webapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String PROPS = "config/resumes.properties";
    private static final Config INSTANCE = new Config();

    private final File storageDir;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private Config() {
        String homeDir = getHomeDir();
        File propsFile = new File(homeDir + "/" + PROPS);
        try (InputStream is = new FileInputStream(propsFile)) {
            Properties props = new Properties();
            props.load(is);

            storageDir = new File(props.getProperty("storage.dir"));
            dbUrl      = props.getProperty("db.url");
            dbUser     = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");
        } catch (IOException e) {
            throw new IllegalStateException("Invalid config file " + propsFile.getAbsolutePath(), e);
        }
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    // проверка на наличие homeDir
    public static String getHomeDir() {
        String homeDir = System.getProperty("homeDir");
        if (homeDir == null) {
            throw new IllegalStateException("System property 'homeDir' is not set. ");
        }
        return homeDir;
    }

    public File getStorageDir() {
        return storageDir;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}