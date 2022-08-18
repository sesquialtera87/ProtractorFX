package org.mth.protractorfx

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.ImageCursor
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File
import kotlin.math.atan2
import kotlin.reflect.KProperty

@JvmField
var scene = Scene(Group())

@JvmField
var stage = Stage()

@JvmField
val SHORTCUT_DELETE = KeyCodeCombination(KeyCode.DELETE)

@JvmField
val SHORTCUT_DELETE_SWITCH = KeyCodeCombination(KeyCode.X)


@JvmField
val CURSOR_ANGLE = ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("angle_x32.png")))

@JvmField
val CURSOR_INSERT_DOT = ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("insert_cursor_x32.png")))

@JvmField
val CURSOR_REMOVE_DOT = ImageCursor(Image(ImageProtractor::class.java.getResourceAsStream("delete_cursor_x32.png")))


@JvmField
val SNAPSHOT_DIR = File("C:\\Users\\matti\\OneDrive\\Documenti\\Java\\ProtractorFX\\src\\main\\resources\\snapshot")

@JvmField
var dotInsertionEnabled = false

@JvmField
var dotDeletionEnabled = false

@JvmField
var angleMeasureEnabled = false

@JvmField
var ANGLE_LABEL_PRECISION = 1


fun angleBetween(p1: Point2D, p2: Point2D, degree: Boolean = false): Double {
    val dot = p1.x * p2.x + p1.y * p2.y
    val det = p1.x * p2.y - p2.x * p1.y
    var angle = atan2(det, dot)

    if (degree) {
        // convert to degree
        angle = Math.toDegrees(angle)

        // put angle measure in range [0,360]
        if (angle < 0)
            angle += 360
    }

    return angle
}

operator fun <T> ObservableValue<T>.getValue(thisRef: Any, property: KProperty<*>): T = value

operator fun <T> Property<T>.setValue(thisRef: Any, property: KProperty<*>, value: T?) = setValue(value)

operator fun IntegerProperty.getValue(thisRef: Any, property: KProperty<*>): Int = value

operator fun IntegerProperty.setValue(thisRef: Any, property: KProperty<*>, value: Int) = setValue(value)

operator fun DoubleProperty.getValue(thisRef: Any, property: KProperty<*>): Double = value

operator fun DoubleProperty.setValue(thisRef: Any, property: KProperty<*>, value: Double) = setValue(value)