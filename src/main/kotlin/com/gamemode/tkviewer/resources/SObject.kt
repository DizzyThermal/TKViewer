package com.gamemode.tkviewer.resources

import java.nio.ByteBuffer
import java.util.ArrayList

class SObject(var movementDirection: Byte, var height: Byte, tileIndices: List<Int>, val unknownBytes: ByteBuffer) {
    var tileIndices: MutableList<Int>

    init {

        this.tileIndices = ArrayList()
        for (i in tileIndices.indices) {
            this.tileIndices.add(tileIndices[i])
        }
    }
}