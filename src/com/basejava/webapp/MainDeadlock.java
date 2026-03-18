package com.basejava.webapp;

public class MainDeadlock {
    private static final Object LOCK1 = new Object();
    private static final Object LOCK2 = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (LOCK1) {
                System.out.println("Thread A took LOCK1");
                try {
                    // стабильное воспроизведение deadlock
                    Thread.sleep(100); } catch (InterruptedException e) {
                    e.printStackTrace(); }
                synchronized (LOCK2) {
                    System.out.println("Thread A took LOCK2");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (LOCK2) {
                System.out.println("Thread B took LOCK2");
                try {
                    Thread.sleep(100); } catch (InterruptedException e) {
                    e.printStackTrace(); }
                synchronized (LOCK1) {
                    System.out.println("Thread B took LOCK1");
                }
            }
        }).start();
    }
}
