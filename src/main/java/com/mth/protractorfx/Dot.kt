package com.mth.protractorfx

import javafx.animation.FillTransition
import javafx.geometry.Point2D
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Circle
import javafx.util.Duration
import java.lang.Math.toDegrees
import kotlin.math.max

class Dot(x: Double, y: Double, val chain: DotChain) : Circle() {

    data class AngleMeasure(val neighbor1: Dot, val neighbor2: Dot, val arc: Arc) {
        override fun equals(other: Any?): Boolean {
            return if (other == null || other !is AngleMeasure)
                false
            else {
                other.neighbor1 == neighbor1 && other.neighbor2 == neighbor2
            }
        }

        override fun hashCode(): Int {
            var result = neighbor1.hashCode()
            result = 31 * result + neighbor2.hashCode()
            result = 31 * result + arc.hashCode()
            return result
        }

        fun getMeasure(): Double {
            val p1 = Point2D(neighbor1.centerX, neighbor1.centerY)
            val p2 = Point2D(neighbor2.centerX, neighbor2.centerY)
            val arcCenter = Point2D(arc.centerX, arc.centerY)

            return angleBetween(p2.subtract(arcCenter), p1.subtract(arcCenter), degree = true)
        }

        fun getInitialAngle(): Double {
            val p1 = Point2D(neighbor1.centerX, neighbor1.centerY)
            val p2 = Point2D(neighbor2.centerX, neighbor2.centerY)
            val arcCenter = Point2D(arc.centerX, arc.centerY)

            return angleBetween(p1.subtract(arcCenter), Point2D(1.0, 0.0), degree = true)
        }
    }

    var chainColor: Color = Color.BLACK
    var selected: Boolean = false
        set(value) {
            field = value
            fill = if (value)
                Color.GRAY
            else
                chainColor
        }

    private val angleArcs = mutableListOf<AngleMeasure>()


    init {
        radius = DOT_RADIUS
        centerX = x
        centerY = y
//        fill = chainColor
        DragSupport(this)

        addEventHandler(MouseEvent.MOUSE_CLICKED) {
            selected = true
            requestFocus()

            if (it.isShiftDown) {
                chain.selection.add(this)
            } else {
                chain.selection.forEach { dot ->
                    if (dot != this)
                        dot.selected = false
                }
                chain.selection.clear()
                chain.selection.add(this)
            }
            it.consume()
        }
    }

    fun isLeaf() = neighbors().size < 2

    fun neighbors() = chain.neighbors(this)

    fun highLight() {
        FillTransition(Duration.seconds(0.2), chainColor, Color.TOMATO).apply {
            isAutoReverse = true
            shape = this@Dot
            play()
        }
    }

    fun updateNeighboringAngles() {
        angleArcs.forEach {
            it.arc.apply {
                startAngle = it.getInitialAngle()
                length = it.getMeasure()
            }
        }
    }

    fun addAngleMeasure(dot1: Dot, dot2: Dot) {
        // create the arc
        val arc = Arc().apply {
            centerXProperty().bind(this@Dot.centerXProperty())
            centerYProperty().bind(this@Dot.centerYProperty())
            radiusX = 20.0
            radiusY = 20.0
            type = ArcType.ROUND
            fill = Color.TRANSPARENT
            stroke = Color.DARKGREEN
            strokeWidth = 1.5
        }


        val angleMeasure = AngleMeasure(dot1, dot2, arc)

        if (!angleArcs.contains(angleMeasure)) {
            angleArcs.add(angleMeasure)
            (parent as Pane).children.add(arc)

            arc.apply {
                length = angleMeasure.getMeasure()
                startAngle = angleMeasure.getInitialAngle()
                toBack()
            }
        } else {
            println("Angle already measured")
        }
    }

    companion object {
        const val DOT_RADIUS = 6.0
    }

    internal class DragSupport(dot: Dot) {
        private val anchorMap: MutableMap<Dot, Point2D> = mutableMapOf()
        private var anchorPoint = Point2D(.0, .0)
        private var dr = Point2D(.0, .0)
        private var dragAnchorPoint = Point2D(0.0, 0.0)

        init {
            dot.setOnDragDetected {
                dot.radius = 3.0
                dot.toBack()
            }
            dot.setOnMouseReleased {
                dot.radius = DOT_RADIUS
                dot.toFront()
            }
            dot.setOnMousePressed { event ->
                anchorMap.clear()
                dot.chain.selection.forEach {
                    anchorMap[it] = Point2D(it.centerX, it.centerY)
                }

                anchorPoint = Point2D(event.screenX, event.screenY)
                dragAnchorPoint = Point2D(dot.centerX, dot.centerY)
                event.consume()
            }
            dot.setOnMouseDragged {
                val dragPoint = Point2D(it.screenX, it.screenY)
                val updateList: HashSet<Dot> = HashSet()

                anchorMap.forEach { (dot, oldCenter) ->
                    dr = dragPoint.subtract(anchorPoint)
                    val newCenter = oldCenter.add(dr)

                    dot.apply {
                        centerX = max(newCenter.x, DOT_RADIUS)
                        centerY = max(newCenter.y, DOT_RADIUS)
                    }

                    updateList.add(dot)
                    updateList.addAll(dot.neighbors())
                }

                updateList.forEach { dot ->
                    dot.updateNeighboringAngles()
                }
            }
        }

    }

}