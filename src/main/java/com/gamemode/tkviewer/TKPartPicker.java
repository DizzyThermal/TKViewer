package com.gamemode.tkviewer;

import com.gamemode.tkviewer.gui.TKPartPickerGUI;
import com.gamemode.tkviewer.gui.TKViewerGUI;

import javax.swing.*;

public class TKPartPicker {

    public static final String TKPARTPICKER_VERSION = "1.0";

    public static void main(String[] args) {
        TKPartPickerGUI tkPartPickerGui = new TKPartPickerGUI("TKPartPicker - " + TKPARTPICKER_VERSION);
        tkPartPickerGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tkPartPickerGui.setSize(1024, 768);
        tkPartPickerGui.setResizable(false);
        tkPartPickerGui.setVisible(true);
    }
}