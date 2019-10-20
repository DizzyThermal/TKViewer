package com.gamemode.tkviewer.utilities;

// Static Utilities Class
public class Utils {
    public static String pad(int number, int length) {
        String val = Integer.toString(number);
        while (val.length() < length) {
            val = "0" + val;
        }

        return val;
    }

}
