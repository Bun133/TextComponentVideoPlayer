package com.github.bun133.textcomponentvideoplayerplugin.player

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.image.BufferedImage
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class VideoProcessor {
    fun process(video: VideoResource, context: JavaPlugin): Pair<VideoAttribute, Progressive<BufferedImage>> {
        return when (video) {
            is VideoResource.ResourceFile -> {
                val file = context.getResource(video.resource)
                    ?: throw IllegalArgumentException("Resource not found: ${video.resource}")
                inProcess(file)
            }

            is VideoResource.VideoDirectURL -> {
                val url = URL(video.url)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                inProcess(connection.inputStream)
            }
        }
    }

    private fun inProcess(file: InputStream): Pair<VideoAttribute, Progressive<BufferedImage>> {
        var isPaused = false
        var isDisposed = false
        val progressive = ProcessingVideo({
            isPaused = it
        }, {
            isDisposed = true
        })

        val grabber = FFmpegFrameGrabber(file)
        grabber.start()

        progressive.allSize = grabber.lengthInFrames

        val numThreads = 2
        val frameQueue = LinkedBlockingQueue<IndexedValue<Frame>>()

        val threadPool = ThreadPoolExecutor(
            numThreads, numThreads, 0L,
            TimeUnit.MILLISECONDS, LinkedBlockingQueue<Runnable>()
        )

        fun end() {
            progressive.isPaused = true
            threadPool.shutdown()
            grabber.close()
        }

        // Main Thread
        Thread {
            var index = 0
            while (!isDisposed) {
                while (isPaused) {
                    // Wait Processing
                }

                if (isDisposed) {   // Can be changed by another thread
                    break
                }


                val frame = grabber.grabImage() ?: break

                frameQueue.add(IndexedValue(index, frame))
                index++
            }

            // Release resources
            end()
            Bukkit.broadcast(Component.text("FrameSize:${index}"))
            Bukkit.broadcast(Component.text("Ready:${progressive.ready.size}"))
        }.start()

        for (i in 0 until numThreads) {
            threadPool.execute {
                val frameConverter = Java2DFrameConverter()
                while (true) {
                    try {
                        val frame = frameQueue.poll(1000L, TimeUnit.MILLISECONDS) ?: break
                        val image = frameConverter.getBufferedImage(frame.value)

                        progressive.ready.add(IndexedValue(frame.index, image))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        val attribute = VideoAttribute(
            grabber.videoFrameRate,
            null,
            progressive.allSize!!
        )

        return attribute to progressive
    }
}

private class ProcessingVideo(private val pause: (isPaused: Boolean) -> Unit, private val disposeF: () -> Unit) :
    Progressive<BufferedImage> {
    val ready = mutableListOf<IndexedValue<BufferedImage>>()
    var allSize: Int? = null
    override fun getAt(index: Int): BufferedImage? {
        return ready.find { it.index == index }?.value
    }

    override fun getAllReady(): List<BufferedImage> {
        return ready.map { it.value }.toList()
    }

    override fun readyUntil(): Int {
        if (ready.isEmpty()) {
            return -1
        }
        return ready.maxOf { it.index }
    }

    override fun allSize() = allSize

    override var isPaused = false
    private var isDisposed = false

    override fun dispose() {
        disposeF()
        // release  resources

        ready.clear()
        allSize = null

        isDisposed = true
    }

    override fun isDisposed(): Boolean = isDisposed
}

data class VideoAttribute(
    val frameRate: Double,
    val fileTitle: String?,
    val frameCount: Int
)