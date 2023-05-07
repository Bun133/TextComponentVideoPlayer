package com.github.bun133.textcomponentvideoplayerplugin

import com.github.bun133.textcomponentvideoplayerplugin.player.PlayerController
import com.github.bun133.textcomponentvideoplayerplugin.player.VideoResource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.java.JavaPlugin

class TextComponentVideoPlayerPlugin : JavaPlugin() {
    val controller: PlayerController by lazy {
        PlayerController(this)
    }

    val screens = mutableListOf<Location>()

    var isPlaying = false

    override fun onEnable() {
        getCommand("player")!!.setExecutor(PlayerCommand(this))
        server.scheduler.runTaskTimer(this, Runnable { screenUpdate() }, 0, 1)
    }

    override fun onDisable() {
    }

    private fun screenUpdate() {
        if (!isPlaying) return

        val img = controller.flush() ?: return
        println("Received!")
        val converted = BufferedImageConverter.convert(img)
        screens.forEach {
            setScreen(converted, it)
        }
    }

    private fun setScreen(converted: List<TextComponent>, loc: Location) {
        converted.forEachIndexed { index, textComponent ->
            val placeLocation = loc.clone().add(0.0, index.toDouble() * 10.0, 0.0)
            // check if there is already text display there
            var entity = placeLocation.world.getNearbyEntitiesByType(
                EntityType.TEXT_DISPLAY.entityClass,
                placeLocation,
                1.0,
                1.0,
                1.0
            ).firstOrNull()
            if (entity == null) entity = placeLocation.world.spawnEntity(placeLocation, EntityType.TEXT_DISPLAY)

            entity as TextDisplay

            entity.text(textComponent)
        }
    }
}

class PlayerCommand(val plugin: TextComponentVideoPlayerPlugin) : TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.isEmpty()) return mutableListOf("place", "load", "play")
        return null
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        when (args.getOrNull(0)) {
            "place" -> {
                plugin.screens.add(sender.location)
                sender.sendMessage(Component.text("Placed!"))
                return true
            }

            "load" -> {
                val url = args.getOrNull(1) ?: return false
                plugin.controller.load(VideoResource.ResourceFile(url))
                sender.sendMessage(Component.text("Loaded!"))
                return true
            }

            "play" -> {
                plugin.controller.play()
                plugin.isPlaying = true
                sender.sendMessage(Component.text("Play!"))
                return true
            }

            else -> return false
        }
    }
}
