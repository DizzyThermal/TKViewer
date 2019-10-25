package com.gamemode.tkpartpicker.resources

import com.gamemode.tkviewer.render.PartRenderer
import com.gamemode.tkviewer.resources.EffectImage

class PartInfo(var partIndex: Int, var animationIndex: Int, var effectImages: List<EffectImage>?, val shouldRender: Boolean, val partRenderer: PartRenderer)