package org.mth.protractorfx

import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseEvent.MOUSE_MOVED
import java.awt.MouseInfo
import kotlin.math.max

object MouseCoordinateLabel : Label() {

    val combination = KeyCodeCombination(KeyCode.E)

    private val mouseHandler = EventHandler<MouseEvent> {
        isVisible = true
        update(it.x, it.y)
    }

    init {
        styleClass.add("coordinate-label")
        text = ""
        showMouseCoordinates.addListener { _, _, value ->
            if (value) {
                scene.addEventHandler(MOUSE_MOVED, mouseHandler)
                isVisible = true
                toFront()

                MouseInfo.getPointerInfo().apply {
                    update(location.x.toDouble(), location.y.toDouble())
                }
            } else {
                scene.removeEventHandler(MOUSE_MOVED, mouseHandler)
                isVisible = false
                toBack()
            }

        }
    }

    fun update(x: Double, y: Double) {
        text = "[%.0f, %.0f]".format(x, y - pane.boundsInParent.minY)
        layoutX = max(0.0, x - pane.boundsInParent.minX - width / 2)
        layoutY = max(0.0, y - pane.boundsInParent.minY - height - 2)
    }
}