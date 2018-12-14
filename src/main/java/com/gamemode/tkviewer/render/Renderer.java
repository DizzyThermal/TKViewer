package com.gamemode.tkviewer.render;

import java.awt.*;

public interface Renderer {
    int getCount();
    Image[] getFrames(int index);
    String getInfo(int index);
    int getFrameIndex(int index, int offset);
}
