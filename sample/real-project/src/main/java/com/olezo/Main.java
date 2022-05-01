package com.olezo;

import com.olezo.config.PlatformClientHolder;

public class Main {
    private static PlatformClientHolder platformClientHolder;

    public static void main(String[] args) {
        new Main().testIfCompiling();
    }

    public void testIfCompiling() {
        platformClientHolder.getFavoriteClient(true);
    }
}
