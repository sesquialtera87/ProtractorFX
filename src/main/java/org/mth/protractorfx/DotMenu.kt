package org.mth.protractorfx

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.RadioMenuItem
import javafx.scene.paint.Color
import org.mth.protractorfx.command.CommandManager
import org.mth.protractorfx.log.LogFactory
import java.net.URL
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger

/**
 * The popup menu shown on each measure label
 */
class DotMenu : Initializable {

    private val log: Logger = LogFactory.configureLog(DotMenu::class.java)

    /**
     *
     * The graph node the label is associated to. This has to be set before the popup is shown.
     */
    private var dot: Dot? = null

    @FXML
    lateinit var chainColorMenu: Menu

    @FXML
    fun selectAllDotsInChain() {
        dot {
            Selection.clear()
            Selection.addToSelection(it.chain)
        }
    }

    @FXML
    fun removeChain() {
        dot {
            val pane = it.parent

            it.chain.dispose()
            chains.remove(it.chain)

            pane.requestFocus()
        }
    }

    @FXML
    fun newChain() {
        CommandManager.execute(ImageProtractor.NewChainAction())
    }

    private fun checkDot(): Optional<Dot> {
        if (dot == null) {
            log.warning("Associated dot cannot be null")
            return Optional.empty()
        }

        return Optional.ofNullable(dot)
    }

    fun configureBeforeShow(dot: Dot) {
        this.dot = dot

        // select the MenuItem related to the current chain color
        chainColorMenu.items.forEach {
            val menuItem = it as RadioMenuItem
            val color = it.properties["color"] as Color
            menuItem.isSelected = color == dot.chainColor
        }

    }

    fun dot(consumer: Consumer<Dot>) = checkDot().ifPresent { consumer.accept(it) }

    private fun changeChainColor(color: Color) {
        dot { it.chain.chainColor.set(color) }
    }

    override fun initialize(url: URL?, bundle: ResourceBundle?) {
        initColorMenu(chainColorMenu, { color -> changeChainColor(color) })
    }


    companion object {
        private lateinit var controller: DotMenu

        private val menu: ContextMenu by lazy {
            val fxmlLoader = FXMLLoader()
            val contextMenu: ContextMenu =
                fxmlLoader.load<Any>(HelloApplication::class.java.getResourceAsStream("DotMenu.fxml")) as ContextMenu
            controller = fxmlLoader.getController()
            contextMenu
        }

        fun configureBeforeShow(dot: Dot, x: Double, y: Double) {
            menu.show(dot, x, y)
            controller.configureBeforeShow(dot)
        }
    }
}