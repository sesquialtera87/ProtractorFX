package org.mth.protractorfx

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Point2D
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import org.mth.protractorfx.log.LogFactory
import java.net.URL
import java.util.*
import java.util.logging.Logger
import kotlin.random.Random

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
    lateinit var backgroundColorMenu: Menu

    @FXML
    fun selectAllDotsInChain() {
        checkDot().ifPresent { dot ->
            Selection.clear()
            Selection.addToSelection(dot.chain)
        }
    }

    @FXML
    fun removeChain() {
        checkDot().ifPresent { dot ->
            val pane = dot.parent

            dot.chain.dispose()
            chains.remove(dot.chain)

            pane.requestFocus()
        }
    }

    @FXML
    fun newChain() {
        checkDot().ifPresent { dot ->
            with(Random(3)) {
                val newChain = DotChain(dot.parent as Pane, Point2D(nextDouble(10.0, 50.0), nextDouble(10.0, 50.0)))
                chains.add(newChain)
            }
        }
    }

    private fun changeFontWeight(fontWeight: FontWeight) {
        checkDot().ifPresent {
            it.chain.measureLabelFontWeight = fontWeight

            log.info("Font weight updated: $fontWeight")
        }
    }

    /**
     * Update the font color for all measure labels in the chain
     * @param color The new font color
     */
    private fun changeFontColor(color: Color) {
        checkDot().ifPresent {
            it.chain.measureLabelFontColor = color

            log.info("Color updated: $color")
        }
    }

    /**
     * Update the font size for all labels in the chain
     * @param fontSize The new font size
     */
    private fun changeFontSize(fontSize: Double) {
        checkDot().ifPresent {
            it.chain.measureLabelFontSize = fontSize

            log.info("Font size updated: $fontSize")
        }
    }

    private fun checkDot(): Optional<Dot> {
        if (dot == null) {
            log.warning("Associated dot cannot be null")
            return Optional.empty()
        }

        return Optional.ofNullable(dot)
    }

    /**
     * Show the context menu over the [measureLabel].
     * @param dot The graph-node related to the label
     */
    fun show(dot: Dot) { // todo change method name
        this.dot = dot

        // select the MenuItem related to the current font color
//        fontColorMenu.items.forEach {
//            val menuItem = it as RadioMenuItem
//            val color = it.properties["font-color"] as Color
//            menuItem.isSelected = color == dot.chain.measureLabelFontColor
//        }

    }

    override fun initialize(url: URL?, bundle: ResourceBundle?) {

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

        fun show(dot: Dot, x: Double, y: Double) {
            menu.show(dot, x, y)
            controller.show(dot)
        }
    }
}