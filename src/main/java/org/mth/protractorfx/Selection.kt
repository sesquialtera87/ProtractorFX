package org.mth.protractorfx

import java.util.*

object Selection : Iterable<Dot> {

    /**
     * The number of dots actually selected
     */
    val size: Int get() = selectedDots().size

    fun isEmpty() = size == 0

    /**
     * Select the [dot], clearing all other previous selected nodes
     */
    fun select(dot: Dot) {
        clear()
        addToSelection(dot)
    }

    /**
     * Remove the [dot] from the selection
     */
    fun unselect(dot: Dot) {
        dot.selected = false
    }

    /**
     * Add the [dot] to the selection, maintaining the previously selected dots.
     */
    fun addToSelection(dot: Dot) {
        dot.selected = true
    }

    /**
     * Add to the selection all the dots belonging to the [dotChain], preserving the previously selected dots.
     */
    fun addToSelection(dotChain: DotChain) {
        dotChain.forEach { it.selected = true }
    }

    /**
     * Remove all dots in the selection
     */
    fun clear() {
        chains.flatten().forEach {
            it.selected = false
        }
    }

    /**
     * Return the selected [Dot], if any. In the case that more than one dot is selected, the method returns the dot
     * actually focused.
     */
    fun selectedDot(): Optional<Dot> {
        for (dot in this) {
            if (dot.isFocused)
                return Optional.of(dot)
        }

        return Optional.empty()
    }

    /**
     * Return a collection of the current selected dots
     */
    fun selectedDots() = chains.flatten().filter { it.selected }

    override fun iterator() = selectedDots().iterator()
}