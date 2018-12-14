package com.gamemode.tkviewer;

import javax.swing.JFrame;

public class TKViewer {
    public static void main(String[] args) {
        GUI gui = new GUI("TKViewer");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(640, 480);
        gui.setResizable(false);
        gui.setVisible(true);
    }
}