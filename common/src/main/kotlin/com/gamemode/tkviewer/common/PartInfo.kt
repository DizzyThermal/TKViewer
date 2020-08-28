package com.gamemode.tkviewer.common

import com.gamemode.tkviewer.common.render.PartRenderer

class PartInfo(var partIndex: Int, var animationIndex: Int, var iconFrameIndex: Int, var paletteIndex: Int,
               var shouldRender: Boolean, val partRenderer: PartRenderer)