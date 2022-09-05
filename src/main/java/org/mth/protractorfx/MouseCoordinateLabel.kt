package org.mth.protractorfx

import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseEvent.MOUSE_MOVED
import javafx.scene.transform.Transform
import javafx.scene.transform.Translate
import java.awt.MouseInfo
import kotlin.math.max

object MouseCoordinateLabel : Label() {

    private const val COORDINATE_TEMPLATE = "[%.0f, %.0f]"

    /**
     * Shortcut to show the mouse coordinates
     */
    val combination = KeyCodeCombination(KeyCode.E)

    private val translation: Translate = Transform.translate(.0, .0)

    /**
     * Track the mouse motion events for displaying the coordinates
     */
    private val mouseHandler = EventHandler<MouseEvent> {
        isVisible = true
        update(it.screenX, it.screenY)
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

        transforms.add(translation)
    }

    /**
     * @param x The horizontal screen coordinate of the mouse pointer
     * @param y The vertical screen coordinate of the mouse pointer
     */
    fun update(x: Double, y: Double) {
        val bounds = pane.localToScreen(pane.boundsInLocal)
        val point = pane.screenToLocal(x, y)

        text = COORDINATE_TEMPLATE.format(x - bounds.minX, y - bounds.minY)

        layoutX = max(0.0, point.x)
        layoutY = max(0.0, point.y)

        translation.y = -height
    }
}