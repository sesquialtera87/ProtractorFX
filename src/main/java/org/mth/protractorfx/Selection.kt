package org.mth.protractorfx

object Selection {

    fun select(dot: Dot) {
        clear()
        addToSelection(dot)
    }

    fun addToSelection(dot: Dot) {
        dot.selected = true
    }

    fun addToSelection(dotChain: DotChain) {
        dotChain.forEach { it.selected = true }
    }

    fun clear() {
        chains.flatten().forEach { it.selected = false }
    }

    fun selectedDot() = selectedDots().first()

    fun selectedDots() = chains.flatten().filter { it.selected }

}