package com.gamemode.tkviewer.render;

import java.awt.*;

public interface Renderer {
    int getCount();
    int getCount(boolean useEpfCount);
    Image[] getFrames(int index, int paletteIndex);
    String getInfo(int index);
    int getFrameIndex(int index, int offset);
    void dispose();
    long getPaletteCount();
}
