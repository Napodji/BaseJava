package com.basejava.webapp;

import java.io.File;

public class MainFile {
    public static void main(String[] args) {
        File rootDir = new File("src");
        printDirectoryTree(rootDir, "");
    }

    private static void printDirectoryTree(File dir, String indent) {
        File[] files = dir.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println(indent + "[DIR]  " + file.getName());
                printDirectoryTree(file, indent + "    ");
            } else {
                System.out.println(indent + "[FILE] " + file.getName());
            }
        }
    }
}
