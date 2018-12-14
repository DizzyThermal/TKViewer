package com.gamemode.tkviewer.resources

/**
 * Color represents the RGB (Red, Green, Blue) data of an unsigned integer (Long)
 */
class Color(var rgba: Long) {
    val red: Long? = rgba!! shr 0x18
    val green: Long? = rgba and 0x00FF0000 shr 16
    val blue: Long? = rgba and 0x0000FF00 shr 8

    init { this.rgba = java.lang.Long.rotateRight(rgba, 8) }
}