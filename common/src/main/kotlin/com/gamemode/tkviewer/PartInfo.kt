package com.gamemode.tkviewer

import com.gamemode.tkviewer.render.PartRenderer

class PartInfo(var partIndex: Int, var animationIndex: Int, var iconFrameIndex: Int, var paletteIndex: Int,
               var shouldRender: Boolean, val partRenderer: PartRenderer)