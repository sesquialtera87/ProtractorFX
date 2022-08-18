package org.mth.protractorfx

import javafx.animation.FillTransition
import javafx.geometry.Point2D
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.transform.Transform
import javafx.util.Duration
import org.mth.protractorfx.log.LogFactory
import java.lang.Math.toRadians
import java.util.logging.Logger
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class Dot(x: Double, y: Double, val chain: DotChain) : Circle() {

    /**
     * @param neighbor1 The first vertex of the oriented angle
     * @param neighbor2 The second vertex of the oriented angle
     */
    data class AngleDescriptor(val neighbor1: Dot, val neighbor2: Dot) {

        /**
         * The label displaying the angle measure
         */
        val angleLabel = Text()
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
            }

            angleLabel.apply {
                xProperty().bind(dot.centerXProperty())
                yProperty().bind(dot.centerYProperty())
                text = "%.${ANGLE_LABEL_PRECISION}f".format(getMeasure())
                isVisible = true
                fill = dot.chain.measureLabelFontColor
                font = Font.font(font.name, dot.chain.measureLabelFontSize)
            }

            angleLabel.setOnMouseClicked {
                if (it.button == MouseButton.SECONDARY) {
                    // show the label context-menu
                    MeasureLabelPopup.show(angleLabel, dot, it.screenX, it.screenY)
                }
            }

            // listen to changes of the font color
            dot.chain.measureLabelFontColorProperty.addListener { _, _, color ->
                angleLabel.fill = color
            }

            // listen to changes of the font size
            dot.chain.measureLabelFontSizeProperty.addListener { _, _, size ->
                angleLabel.font = Font.font(
                    angleLabel.font.name,
                    dot.chain.measureLabelFontWeight,
                    size.toDouble()
                )
            }

            // listen to changes of the font weight
            dot.chain.measureLabelFontWeightProperty.addListener { _, _, weight ->
                angleLabel.font = Font.font(
                    angleLabel.font.name,
                    weight,
                    dot.chain.measureLabelFontSize
                )
            }

            updateAngleLabelPosition()

            // add the nodes to the Pane and move them to background
            pane.children.addAll(arc, angleLabel)

            arc.toBack()
            angleLabel.toBack()
        }

        private fun updateAngleLabelPosition() {
            val alpha = -toRadians(getMeasure()) / 2
            val widthTranslation = Point2D(-angleLabel.layoutBounds.width / 2 + 4, angleLabel.layoutBounds.height / 2)
            var translationVector = neighbor1.getCenter().subtract(arc.centerX, arc.centerY).normalize()
            translationVector = translationVector.multiply(40.0)
            translationVector = Point2D(
                translationVector.x * cos(alpha) - translationVector.y * sin(alpha),
                translationVector.y * cos(alpha) + translationVector.x * sin(alpha)
            )

            angleLabel.apply {
                transforms.clear()
                transforms.addAll(
                    Transform.translate(widthTranslation.x, widthTranslation.y),
                    Transform.translate(translationVector.x, translationVector.y)
                )
//                rotate = if (initialAngle <= 180) 180 - initialAngle else 360 - initialAngle
            }
        }

        fun update() {
            // the updated angle measure
            val angleMeasure = getMeasure()

            // update arc properties
            arc.apply {
                length = angleMeasure
                startAngle = getInitialAngle()
            }

            // update label position and text
            if (angleLabel.isVisible) {
                angleLabel.text = "%.${ANGLE_LABEL_PRECISION}f°".format(angleMeasure)

                updateAngleLabelPosition()
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

        private fun getMeasure(): Double {
            val p1 = neighbor1.getCenter()
            val p2 = neighbor2.getCenter()
            val arcCenter = Point2D(arc.centerX, arc.centerY)

            return angleBetween(p2.subtract(arcCenter), p1.subtract(arcCenter), degree = true)
        }

        private fun getInitialAngle(): Double {
            val p1 = neighbor1.getCenter()
            val arcCenter = Point2D(arc.centerX, arc.centerY)

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
                SELECTED_COLOR
            else
                chainColor
        }

    val angleArcs = mutableListOf<AngleDescriptor>()


    init {
        // listen for color changes
        chain.chainColor.addListener { _, _, _ -> fill = chainColor }

        radius = DOT_RADIUS
        centerX = x
        centerY = y
        fill = chainColor

        DragSupport(this)

        addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                log.fine("Click on dot with left mouse button")

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
    }

    fun getCenter() = Point2D(centerX, centerY)

    fun delete() {
        log.info("Deleting dot")
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
        val log: Logger = LogFactory.configureLog(Dot::class.java)

        const val DOT_RADIUS = 6.0
        const val DOT_RADIUS_SMALL = 2.5
        val SELECTED_COLOR: Color = Color.GRAY
    }

    internal class DragSupport(dot: Dot) {

        private val anchorMap: MutableMap<Dot, Point2D> = mutableMapOf()
        private var anchorPoint = Point2D(.0, .0)
        private var dr = Point2D(.0, .0)
        private var anchorPointForDrag = Point2D(.0, .0)

        init {
            dot.setOnDragDetected {
                log.fine("Drag detected")

                dot.radius = DOT_RADIUS_SMALL
                dot.toBack()
            }

            dot.setOnMouseReleased {
                dot.radius = DOT_RADIUS
                dot.toFront()
            }

            dot.setOnMousePressed { event ->
                anchorMap.clear()
                anchorMap[dot] = Point2D(dot.centerX, dot.centerY)

                dot.chain.selection.forEach {
                    anchorMap[it] = Point2D(it.centerX, it.centerY)
                }

                anchorPoint = Point2D(event.screenX, event.screenY)
                anchorPointForDrag = Point2D(dot.centerX, dot.centerY)
                event.consume()

                log.fine("Mouse pressed. \n\tAnchor point = $anchorPoint \n\tDrag anchor = $anchorPointForDrag")
            }

            dot.setOnMouseDragged {
                val currentDragPoint = Point2D(it.screenX, it.screenY)

                // set of nodes for which update the angle measures
                val updateList: HashSet<Dot> = HashSet()

                anchorMap.forEach { (dot, oldCenter) ->
                    // calculate the delta from the anchor point
                    dr = currentDragPoint.subtract(anchorPoint)

                    // get the new center coordinates of the node
                    val newCenter = oldCenter.add(dr)

                    dot.apply {
                        centerX = max(newCenter.x, DOT_RADIUS)
                        centerY = max(newCenter.y, DOT_RADIUS)
                    }

                    // add the node and its neighbors to the update-set
                    updateList.add(dot)
                    updateList.addAll(dot.neighbors())
                }

                updateList.forEach { dot ->
                    dot.updateNeighboringAngles()
                }
            }
        }

        companion object {
            private val log: Logger = LogFactory.configureLog(DragSupport::class.java)!!
        }

    }

}