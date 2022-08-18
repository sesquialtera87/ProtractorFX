package org.mth.protractorfx

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

@JvmField
var ANGLE_LABEL_FONT_SIZE = 12.0

@JvmField
var ANGLE_LABEL_TEXT_COLOR: Color = Color.BLACK

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