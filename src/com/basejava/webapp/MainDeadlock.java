package com.basejava.webapp;

public class MainDeadlock {
    private static final Object LOCK1 = new Object();
    private static final Object LOCK2 = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (LOCK1) {
                System.out.println("Thread A locked LOCK1");
                sleep();
                synchronized (LOCK2) {
                    System.out.println("Thread A locked LOCK2");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (LOCK2) {
                System.out.println("Thread B locked LOCK2");
                sleep();
                synchronized (LOCK1) {
                    System.out.println("Thread B locked LOCK1");
                }
            }
        }).start();
    }

    private static void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
