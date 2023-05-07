package com.github.bun133.textcomponentvideoplayerplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import java.awt.image.BufferedImage

class BufferedImageConverter {
    companion object {
        const val FILLING_CHAR = '■'

        /**
         * convert input to TextComponents.
         */
        fun convert(img: BufferedImage): List<TextComponent> {
            val components = mutableListOf<TextComponent>()

            // iterate all pixels in rows
            for (y in 0 until img.height) {
                // iterate all pixels in columns
                val rowText = Component.text("")
                for (x in 0 until img.width) {
                    // get pixel color
                    val color = img.getRGB(x, y)
                    // get text color
                    val textColor = TextColor.color(color)

                    rowText.append(Component.text(FILLING_CHAR).color(textColor))
                }

                components.add(rowText)
            }

            return components
        }
    }
}