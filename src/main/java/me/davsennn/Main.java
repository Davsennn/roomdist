package me.davsennn;

import javax.swing.*;

public final class Main {
    public static final String version = "0.3.0";
    public static final int threads = Runtime.getRuntime().availableProcessors();

    static void main() {
        Config.setDefaults();
        SwingUtilities.invokeLater(GUI::createWindow);
    }

}
