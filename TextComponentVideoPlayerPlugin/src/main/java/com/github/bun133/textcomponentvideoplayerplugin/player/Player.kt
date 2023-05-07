package com.github.bun133.textcomponentvideoplayerplugin.player

import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class Player {
    var playingVideoRef: Progressive<BufferedImage>? = null
    var playingVideoAttribute: VideoAttribute? = null

    var playStartTick: Int? = null

    var currentFrameIndex: Int = 0
    val loader: VideoLoader = VideoLoader()

    fun flush(currentTick: Int): BufferedImage? {
        if (playingVideoRef == null || playingVideoAttribute == null) return null

        val deltaTick = if (playStartTick == null) 0 else currentTick - playStartTick!!
        // Since one tick is equals to 1/20 sec,
        val deltaSec = deltaTick / 20.0
        // using deltaSec and playingVideoAttribute.frameRate to calculate currentFrameIndex
        // and frame is int so,
        val deltaFrame = (deltaSec * playingVideoAttribute!!.frameRate).roundToInt()
            .coerceIn(0, (playingVideoAttribute!!.frameCount - 1).coerceAtLeast(0))

        println("deltaFrame: $deltaFrame")

        return playingVideoRef!!.getAt(deltaFrame)
    }
}