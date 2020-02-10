package com.sinhtai;

import org.opencv.core.Core;

/**
 * Hello world!
 */
public class App {
    static {
        //Loading the core library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
