package com.gamemode.tkviewer

class Part(val id: Long, val paletteId: Long, val frameIndex: Long, val frameCount: Long, val chunks: List<PartChunk>,
           val partMetadata: PartMetadata)