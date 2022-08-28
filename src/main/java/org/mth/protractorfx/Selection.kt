package org.mth.protractorfx

import java.util.*

object Selection {

    val size: Int get() = selectedDots().size

    fun select(dot: Dot) {
        clear()
        addToSelection(dot)
    }

    fun unselect(dot: Dot) {
        dot.selected = false
    }

    fun addToSelection(dot: Dot) {
        dot.selected = true
    }

    fun addToSelection(dotChain: DotChain) {
        dotChain.forEach { it.selected = true }
    }

    fun clear() {
        chains.flatten().forEach {
            it.selected = false
            println(it)
        }
    }

    fun selectedDot() = if (size == 1) Optional.of(selectedDots().first())
    else Optional.empty()

    fun selectedDots() = chains.flatten().filter { it.selected }

}