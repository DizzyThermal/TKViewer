package com.gamemode.tkviewer.resources

import java.util.ArrayList

class Palette(var animationColorCount: Int, animationColorOffsets: List<Int>, colors: List<Color>, val paletteMetadata: PaletteMetadata) {
    var animationColorOffsets: MutableList<Int>
    var colors: MutableList<Color>

    val redBytes: ByteArray
        get() {
            val redBytes = ByteArray(colors.size)
            for (i in colors.indices) {
                redBytes[i] = colors[i].red!!.toByte()
            }

            return redBytes
        }

    val greenBytes: ByteArray
        get() {
            val greenBytes = ByteArray(colors.size)
            for (i in colors.indices) {
                greenBytes[i] = colors[i].green!!.toByte()
            }

            return greenBytes
        }

    val blueBytes: ByteArray
        get() {
            val blueBytes = ByteArray(colors.size)
            for (i in colors.indices) {
                blueBytes[i] = colors[i].blue!!.toByte()
            }

            return blueBytes
        }

    init {

        this.animationColorOffsets = ArrayList()
        for (i in animationColorOffsets.indices) {
            this.animationColorOffsets.add(animationColorOffsets[i])
        }

        this.colors = ArrayList()
        for (i in colors.indices) {
            this.colors.add(colors[i])
        }
    }
}