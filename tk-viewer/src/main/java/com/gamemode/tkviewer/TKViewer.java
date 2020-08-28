package com.gamemode.tkviewer;

import com.gamemode.tkviewer.gui.TKViewerGUI;

import javax.swing.JFrame;

public class TKViewer {

    public static final String TKVIEWER_VERSION = "3.3";

    public static void main(String[] args) {
        TKViewerGUI TKViewerGui = new TKViewerGUI("TKViewer - " + TKVIEWER_VERSION);
        TKViewerGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TKViewerGui.setSize(640, 480);
        TKViewerGui.setResizable(false);
        TKViewerGui.setVisible(true);
    }
}