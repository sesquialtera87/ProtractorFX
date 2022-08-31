package org.mth.protractorfx.tool

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent.KEY_PRESSED
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent.*
import org.mth.protractorfx.log.LogFactory
import org.mth.protractorfx.scene
import java.util.logging.Logger

enum class Tool(val tool: AbstractTool) {

    SELECTION(Tools.selectionTool),
    MEASURE(Tools.measureTool),
    INSERTION(Tools.insertionTool),
    DELETION(Tools.deletionTool);


    companion object {

        private val log: Logger = LogFactory.configureLog(Tool::class.java)

        fun initialize() {
            log.finest("Initializing the tools' listeners")

            val tools = values().map { it.tool }

            scene.addEventHandler(KEY_PRESSED) { event ->
                log.finest("Key pressed: $event")

                when (event.code) {
                    KeyCode.ESCAPE -> tools.forEach { it.deactivate() }
                    KeyCode.DELETE -> {
                        Tools.deleteDot()
                        event.consume()
                    }
                    else -> {}
                }

                // find the tool that match the key combination
                values().filter { it.tool.shortcut.match(event) }
                    .forEach { tool ->
                        log.finest("Shortcut detected for tool $tool")

                        tool.tool.apply {
                            if (active) {
                                deactivate()

                                log.finest("Deactivating tool $tool")
                            } else {
                                values().filter { it != tool }.forEach {
                                    log.finest("Deactivating tool $it")
                                    it.tool.deactivate()
                                }
                                activate()

                                log.finest("Activating tool $tool")
                            }
                        }

                        event.consume()
                    }
            }

            scene.addEventHandler(MOUSE_PRESSED) { event ->
                if (event.isPrimaryButtonDown) {
                    tools.filter { it.active }
                        .forEach { it.onPress(event) }

                    event.consume()
                }
            }

            scene.addEventHandler(MOUSE_RELEASED) { event ->
                if (event.button == MouseButton.PRIMARY) {
                    tools.filter { it.active }
                        .forEach { it.onRelease(event) }

                    event.consume()
                }
            }

            scene.addEventHandler((MOUSE_DRAGGED)) { event ->
                if (event.button == MouseButton.PRIMARY) {
                    tools.filter { it.active }
                        .forEach { it.onDrag(event) }
                }
            }
        }
    }
}