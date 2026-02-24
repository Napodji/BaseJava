package com.basejava.webapp;

import java.io.File;
import java.util.Arrays;

public class MainFile {
    public static void main(String[] args) {
        File rootDir = new File("src");
        System.out.println(rootDir.getName());
        printDirectoryTree(rootDir, "");
    }

    private static void printDirectoryTree(File dir, String indent) {
        File[] files = dir.listFiles();

        if (files == null) return;

        // Сортировка
        Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });

        for (int i = 0; i < files.length; i++) {
            boolean isLast = (i == files.length - 1);
            String connector = isLast ? "└── " : "├── ";
            String nextIndent = isLast ? "    " : "│   ";

            System.out.println(indent + connector + files[i].getName());

            if (files[i].isDirectory()) {
                printDirectoryTree(files[i], indent + nextIndent);
            }
        }
    }
}
