package com.github.bun133.textcomponentvideoplayerplugin.player

// This interface represents a process that is running and partly able to be read.
interface Progressive<C : Any> {
    /**
     * @return item at the specified index. returns null if the content is not ready.
     */
    fun getAt(index: Int): C?

    /**
     * Get all content ready in list
     */
    fun getAllReady(): List<C>

    /**
     * @return index of the last ready item.
     * in other words, 0 to this index are ready.
     */
    fun readyUntil(): Int

    /**
     * @return all size of the content. returns null if the content is not known in length.
     */
    fun allSize(): Int?

    /**
     * @return is the content processing paused.
     * if this set to true, the content processing will be immediately stopped.
     */
    var isPaused: Boolean

    /**
     * Dispose the content.
     * if this is called, the content processing will be immediately stopped.
     * and the content and its processor will be released.
     */
    fun dispose()

    /**
     * @return is the content disposed.
     */
    fun isDisposed(): Boolean
}

/**
 * gets the item at the specified index.
 * but if the item is not ready, it will return nearest item before it.
 */
fun <R : Any> Progressive<R>.getAtOrFallBack(index: Int): R {
    val data = getAt(index)
    if (data != null) {
        return data
    }

    val readyUntil = readyUntil()
    val fallBack = getAt(readyUntil)
    if (fallBack != null) {
        return fallBack
    }
    throw IllegalStateException("The content is not ready and fallback is not available.")
}