package com.gamemode.tkviewer;

import com.gamemode.tkviewer.gui.GUI;

import javax.swing.JFrame;

public class TKViewer {

    public static final String TKVIEWER_VERSION = "3.1";

    public static void main(String[] args) {
        GUI gui = new GUI("TKViewer - " + TKVIEWER_VERSION);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(640, 480);
        gui.setResizable(false);
        gui.setVisible(true);
    }
}