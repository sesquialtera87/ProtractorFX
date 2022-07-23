package com.mth.protractorfx

import javafx.animation.FillTransition
import javafx.geometry.Point2D
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.scene.transform.Transform
import javafx.util.Duration
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class Dot(x: Double, y: Double, val chain: DotChain) : Circle() {

    /**
     * @param neighbor1 The first vertex of the oriented angle
     * @param neighbor2 The second vertex of the oriented angle
     */
    data class AngleDescriptor(val neighbor1: Dot, val neighbor2: Dot) {

        private val angleLabel = Text()
        private val arc: Arc = Arc()

        fun build(dot: Dot, pane: Pane) {
            // create the arc
            arc.apply {
                centerXProperty().bind(dot.centerXProperty())
                centerYProperty().bind(dot.centerYProperty())
                radiusX = 20.0
                radiusY = 20.0
                type = ArcType.ROUND
                fill = Color.TRANSPARENT
                stroke = Color.DARKGREEN
                strokeWidth = 1.5
                length = getMeasure()
                startAngle = getInitialAngle()
                toBack()
            }

            angleLabel.apply {
                xProperty().bind(dot.centerXProperty())
                yProperty().bind(dot.centerYProperty())
                text = "%.${ANGLE_LABEL_PRECISION}f".format(getMeasure())
                isVisible = true
                toBack()
            }

            val alpha = -toRadians(getMeasure()) / 2
            val widthTranslation = Point2D(-angleLabel.layoutBounds.width / 2 + 4, angleLabel.layoutBounds.height / 2)
            var translationVector = neighbor1.getCenter().subtract(arc.centerX, arc.centerY).normalize()
            translationVector = translationVector.multiply(40.0)
            translationVector = Point2D(
                translationVector.x * cos(alpha) - translationVector.y * sin(alpha),
                translationVector.y * cos(alpha) + translationVector.x * sin(alpha)
            )

            val initialAngle = getInitialAngle()

            angleLabel.apply {
                transforms.addAll(
                    Transform.translate(widthTranslation.x, widthTranslation.y),
                    Transform.translate(translationVector.x, translationVector.y)
                )
//                rotate = if (initialAngle <= 180) 180 - initialAngle else 360 - initialAngle
            }

            pane.children.addAll(arc, angleLabel)
        }

        fun update() {
            val angleMeasure = getMeasure()

            arc.apply {
                length = angleMeasure
                startAngle = getInitialAngle()
            }

            angleLabel.apply {
                text = "%.${ANGLE_LABEL_PRECISION}f".format(angleMeasure)
            }
        }

        override fun equals(other: Any?): Boolean {
            return if (other == null || other !is AngleDescriptor)
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
            val p1 = neighbor1.getCenter()
            val p2 = neighbor2.getCenter()
            val arcCenter = Point2D(arc.centerX, arc.centerY)

            return angleBetween(p2.subtract(arcCenter), p1.subtract(arcCenter), degree = true)
        }

        fun getInitialAngle(): Double {
            val p1 = neighbor1.getCenter()
            val arcCenter = Point2D(arc.centerX, arc.centerY)

            println("Initial angle = " + angleBetween(p1.subtract(arcCenter), Point2D(1.0, 0.0), degree = true))
            return angleBetween(p1.subtract(arcCenter), Point2D(1.0, 0.0), degree = true)
        }
    }

    /**
     * Get the color of the parent chain
     */
    private val chainColor: Color get() = chain.chainColor.get()

    var selected: Boolean = false
        set(value) {
            field = value
            fill = if (value)
                Color.GRAY
            else
                chainColor
        }

    private val angleArcs = mutableListOf<AngleDescriptor>()


    init {
        // listen for color changes
        chain.chainColor.addListener { _, _, _ -> fill = chainColor }

        radius = DOT_RADIUS
        centerX = x
        centerY = y
        fill = chainColor

        DragSupport(this)

        addEventHandler(MouseEvent.MOUSE_CLICKED) {
            selected = true
            requestFocus()

            if (dotDeletionEnabled) {
                delete()
            } else if (it.isShiftDown) {
                chain.selection.add(this)
                it.consume()
            } else {
                chain.selection.forEach { dot ->
                    if (dot != this)
                        dot.selected = false
                }
                chain.selection.clear()
                chain.selection.add(this)
                it.consume()
            }
        }
    }

    fun getCenter() = Point2D(centerX, centerY)

    fun delete() {
        println("Deletion")
        chain.removeDot(this)
        chain.clearSelection()
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
        angleArcs.forEach { it.update() }
    }

    fun addAngleMeasure(dot1: Dot, dot2: Dot) {
        val pane = parent as Pane
        val angleDescriptor = AngleDescriptor(dot1, dot2)

        if (!angleArcs.contains(angleDescriptor)) {
            angleArcs.add(angleDescriptor)
            angleDescriptor.build(this, pane)
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