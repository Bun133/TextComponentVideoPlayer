package com.github.bun133.textcomponentvideoplayerplugin.player

import com.github.bun133.textcomponentvideoplayerplugin.TextComponentVideoPlayerPlugin
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.awt.image.BufferedImage

class PlayerController(val plugin: TextComponentVideoPlayerPlugin) {
    private val player = Player()

    /**
     * Load Video File to Player
     */
    fun load(resource: VideoResource) {
        val data = player.loader.load(resource, plugin)

        // Set video data
        player.playingVideoAttribute = data.first
        player.playingVideoRef = data.second

        // seek to the start
        position = 0
    }

    /**
     * Start Playing Video
     */
    fun play() {
        if (player.playingVideoRef == null) {
            throw IllegalStateException("Video not loaded")
        }
        player.playStartTick = plugin.server.currentTick
        Bukkit.broadcast(Component.text("Real Ready:${player.playingVideoRef!!.readyUntil()}"))
    }

    /**
     * @return current position frame index.
     */
    var position
        get() = player.currentFrameIndex
        set(value) {
            player.currentFrameIndex = value
        }


    /**
     * @return BufferedImage of current frame
     */
    fun flush() = player.flush(plugin.server.currentTick)
}

sealed class VideoResource {
    class VideoDirectURL(val url: String) : VideoResource()
    class ResourceFile(val resource: String) : VideoResource()
}

class VideoLoader {

    private val processor = VideoProcessor()
    fun load(resource: VideoResource, plugin: JavaPlugin): Pair<VideoAttribute, Progressive<BufferedImage>> {
        return processor.process(resource, plugin)
    }
}

// Frame Dots Data
//
typealias RawFrame = ByteArray
typealias RawVideoData = Array<RawFrame>