package org.mth.protractorfx

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Dimension2D
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.ImageCursor
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.RadioMenuItem
import javafx.scene.control.ToggleGroup
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import org.mth.protractorfx.tool.MeasureUnit
import org.mth.protractorfx.tool.MeasureUnit.*
import java.io.File
import java.util.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.reflect.KProperty

operator fun <T> ObservableValue<T>.getValue(thisRef: Any, property: KProperty<*>): T = value

operator fun <T> Property<T>.setValue(thisRef: Any, property: KProperty<*>, value: T?) = setValue(value)

operator fun IntegerProperty.getValue(thisRef: Any, property: KProperty<*>): Int = value

operator fun IntegerProperty.setValue(thisRef: Any, property: KProperty<*>, value: Int) = setValue(value)

operator fun DoubleProperty.getValue(thisRef: Any, property: KProperty<*>): Double = value

operator fun DoubleProperty.setValue(thisRef: Any, property: KProperty<*>, value: Double) = setValue(value)


@JvmField
var scene = Scene(Group())

@JvmField
var stage = Stage()

val pane: Pane by lazy { scene.lookup("#container") as Pane }

//@JvmField
lateinit var chain: DotChain

val chains: Deque<DotChain> = LinkedList()

@JvmField
val SHORTCUT_RECT_SELECTION = KeyCodeCombination(KeyCode.S)


@JvmField
val CURSOR_ANGLE = ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("angle_x32.png")))

@JvmField
val CURSOR_INSERT_DOT = ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("insert_cursor_x32.png")))

@JvmField
val CURSOR_REMOVE_DOT = ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("delete_cursor_x32.png")))

@JvmField
val CURSOR_RECT_SELECTION =
    ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("selection_cursor_x32.png")))


@JvmField
val SNAPSHOT_DIR = File("C:\\Users\\matti\\OneDrive\\Documenti\\Java\\ProtractorFX\\src\\main\\resources\\snapshot")


@JvmField
var ANGLE_LABEL_PRECISION = 1

@JvmField
val measureUnitProperty = SimpleObjectProperty(DECIMAL_DEGREE)

val MEASURE_UNIT: MeasureUnit
    get() = measureUnitProperty.value

/**
 * Find the node in the collection with the minim distance from the given [point].
 * @param excludeLeaves If `true` the nodes with only one incoming connections (leaves) are ignored from the search
 */
fun getNearestDot(point: Point2D, points: Collection<Dot>, excludeLeaves: Boolean = false): Dot {
    var nearestDot: Dot? = null
    var minDistance: Double = Double.MAX_VALUE
    var currentDistance: Double

    points.forEach { dot ->
        if (!(excludeLeaves && dot.isLeaf())) {
            currentDistance = point.distance(dot.getCenter())

            if (currentDistance < minDistance) {
                minDistance = currentDistance
                nearestDot = dot
            }
        }
    }

    return nearestDot!!
}


fun angleBetween(p1: Point2D, p2: Point2D, unit: MeasureUnit = DECIMAL_DEGREE, positive: Boolean = true): Double {
    val dot = p1.x * p2.x + p1.y * p2.y
    val det = p1.x * p2.y - p2.x * p1.y
    var angle = atan2(det, dot) // radians

    when (unit) {
        DECIMAL_DEGREE -> {
            angle = Math.toDegrees(angle)

            // put angle measure in range [0,360]
            if (positive && angle < 0)
                angle += 360
        }
        RADIANS -> {
            if (positive && angle < 0)
                angle += 2 * PI
        }
        CENTESIMAL_DEGREE, SEXAGESIMAL_DEGREES -> throw IllegalArgumentException()
    }

    return angle
}

fun defaultColors() = listOf(
    Color.BLACK,
    Color.SLATEBLUE,
    Color.ORANGERED,
    Color.MAGENTA,
    Color.PLUM,
    Color.OLIVEDRAB,
    Color.TAN,
    Color.PEACHPUFF
)

/**
 * Populate the [menu] with a [RadioMenuItem] for each color. To each menu item is attached a property (named *color*)
 * containing the reference to the related [Color] object.
 * @param colorAction The action that has to be executed on the selection of the [RadioMenuItem]
 */
fun initColorMenu(
    menu: Menu,
    colorAction: (Color) -> Unit,
    colorRectangleSize: Dimension2D = Dimension2D(14.0, 14.0),
) {
    val toggleGroup = ToggleGroup()

    defaultColors().forEach { color ->
        val menuItem = RadioMenuItem()
        menuItem.graphic = Rectangle(colorRectangleSize.width, colorRectangleSize.height, color)
        menuItem.properties["color"] = color
        menuItem.onAction = EventHandler { colorAction.invoke(color) }

        menu.items.add(menuItem)
        toggleGroup.toggles.add(menuItem)
    }
}

fun Arc.getCenter() = Point2D(centerX, centerY)
fun Circle.getCenter() = Point2D(centerX, centerY)

fun Point2D.rotate(alpha: Double) = Point2D(
    this.dotProduct(cos(alpha), -sin(alpha)),
    this.dotProduct(sin(alpha), cos(alpha)),
)

