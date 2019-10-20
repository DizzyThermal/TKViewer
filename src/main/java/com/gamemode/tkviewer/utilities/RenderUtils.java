package com.gamemode.tkviewer.utilities;

import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.PivotData;

import java.util.List;

public class RenderUtils {
    /**
     * Private constructor to prevent instantiation of static utility class
     */
    private RenderUtils() {}

    public static PivotData getPivotData(List<Frame> frames) {
        // Determine Canvas Size
        int left, top, right, bottom;
        left = top = 10000;
        right = bottom = -10000;
        for (Frame frame : frames) {
            if (frame == null) {
                continue;
            }

            if (frame.getLeft() < left) {
                left = frame.getLeft();
            }
            if (frame.getTop() < top) {
                top = frame.getTop();
            }
            if (frame.getRight() > right) {
                right = frame.getRight();
            }
            if (frame.getBottom() > bottom) {
                bottom = frame.getBottom();
            }
        }

        int width = right-left;
        int height = bottom-top;

        return new PivotData(Math.abs(left), Math.abs(top), width, height);
    }
}
