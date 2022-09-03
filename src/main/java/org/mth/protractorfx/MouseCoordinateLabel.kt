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

    private const val COORDINATE_TEMPLATE = "[%.0f, %.0f]"

    /**
     * Shortcut to show the mouse coordinates
     */
    val combination = KeyCodeCombination(KeyCode.E)

    /**
     * Track the mouse motion events for displaying the coordinates
     */
    private val mouseHandler = EventHandler<MouseEvent> {
        isVisible = true
        update(it.x, it.y)
    }

    init {
        isVisible = false // initially invisible
        styleClass.add("coordinate-label")
        text = "[100, 100]"
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
        text = COORDINATE_TEMPLATE.format(x, y - pane.boundsInParent.minY)

        layoutX = max(0.0, x - width / 2)
        layoutY = max(0.0, y - pane.boundsInParent.minY - height - 2)
    }
}