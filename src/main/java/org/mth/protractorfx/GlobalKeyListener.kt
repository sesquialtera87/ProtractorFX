package org.mth.protractorfx

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import org.mth.protractorfx.command.CommandManager
import org.mth.protractorfx.command.CommandPopup
import org.mth.protractorfx.log.LogFactory

object GlobalKeyListener {
    val log = LogFactory.configureLog(GlobalKeyListener::class.java)

    val SHORTCUT_UNDO = KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN)
    val SHORTCUT_UNDO_POPUP =
        KeyCodeCombination(KeyCode.P, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN)

    fun install() {

        scene.setOnKeyPressed {
//            log.finest("Key pressed on scene: $it")

            if (MouseCoordinateLabel.combination.match(it)) {
                showMouseCoordinates.value = true
            }
        }

        scene.setOnKeyReleased {
//            log.finest("Key released on scene: $it")

            if (MouseCoordinateLabel.combination.match(it)) {
                showMouseCoordinates.value = false
            }
//            else if (SHORTCUT_UNDO.match(it)) {
//                log.finest("UNDO")
//
//                CommandManager.undo()
//            }
            else if (SHORTCUT_UNDO_POPUP.match(it)) {
                CommandPopup.show(scene.window)
            }
        }

        scene.setOnKeyTyped {
//            log.finest("Key typed on scene: $it")
        }
    }
}