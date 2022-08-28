package org.mth.protractorfx

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import org.mth.protractorfx.log.LogFactory
import java.net.URL
import java.util.*
import java.util.logging.Logger

/**
 * The popup menu shown on each measure label
 */
class MeasureLabelMenu : Initializable {

    private val log: Logger = LogFactory.configureLog(MeasureLabelMenu::class.java)

    /**
     *
     * The graph node the label is associated to. This has to be set before the popup is shown.
     */
    private var dot: Dot? = null

    @FXML
    lateinit var fontColorMenu: Menu

    @FXML
    lateinit var fontSizeMenu: Menu

    @FXML
    lateinit var fontStyleMenu: Menu

    @FXML
    lateinit var backgroundColorMenu: Menu

    @FXML
    lateinit var backgroundVisibilityMenuItem: CheckMenuItem

    @FXML
    fun showLabelBackground() {
        checkDot().ifPresent { dot ->
            dot.chain.measureLabelBackgroundVisibility = backgroundVisibilityMenuItem.isSelected
        }
    }

    @FXML
    fun resetLabelLocations() {
        checkDot().ifPresent { dot ->
            dot.angleDecorators.forEach { it.resetLabelPosition() }
        }
    }

    @FXML
    fun removeMeasure() {
        checkDot().ifPresent { dot ->
            val decorator = dot.angleDecorators.first { it.angleLabel == menu.ownerNode }
            decorator.dispose(dot.parent as Pane)
            dot.angleDecorators.remove(decorator)
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
        fontColorMenu.items.forEach {
            val menuItem = it as RadioMenuItem
            val color = it.properties["color"] as Color
            menuItem.isSelected = color == dot.chain.measureLabelFontColor
        }

        // select the MenuItem related to the current font size
        fontSizeMenu.items.forEach {
            val menuItem = it as RadioMenuItem
            val size = it.properties["font-size"] as Int
            menuItem.isSelected = size.toDouble() == dot.chain.measureLabelFontSize
        }

        // select the MenuItem related to the current font size
        fontStyleMenu.items.forEach {
            val menuItem = it as RadioMenuItem
            val weight = it.properties["font-weight"] as FontWeight
            menuItem.isSelected = weight == dot.chain.measureLabelFontWeight
        }

        // select the MenuItem related to the current background color
        backgroundColorMenu.items.forEach {
            val menuItem = it as RadioMenuItem
            val color = it.properties["color"] as Color
            menuItem.isSelected = color == dot.chain.measureLabelBackgroundColor
        }

        backgroundVisibilityMenuItem.isSelected = dot.chain.measureLabelBackgroundVisibility
    }

    override fun initialize(url: URL?, bundle: ResourceBundle?) {
        val toggleGroup = ToggleGroup()
        val styleToggleGroup = ToggleGroup()

        // populate the font-size menu
        listOf(8, 10, 12, 14, 16).forEach { size ->
            val menuItem = RadioMenuItem()
            menuItem.text = "$size"
            menuItem.properties["font-size"] = size
            menuItem.onAction = EventHandler { changeFontSize(size.toDouble()) }

            fontSizeMenu.items.add(menuItem)
            toggleGroup.toggles.add(menuItem)
        }

        // populate the font-color menu
        initColorMenu(fontColorMenu, { color -> changeFontColor(color) })

        // populate the font-weight menu
        var menuItem = RadioMenuItem("Plain")
        menuItem.isSelected = true
        menuItem.properties["font-weight"] = FontWeight.NORMAL
        menuItem.onAction = EventHandler { changeFontWeight(FontWeight.NORMAL) }
        fontStyleMenu.items.add(menuItem)
        styleToggleGroup.toggles.add(menuItem)

        menuItem = RadioMenuItem("Bold")
        menuItem.properties["font-weight"] = FontWeight.BOLD
        menuItem.onAction = EventHandler { changeFontWeight(FontWeight.BOLD) }
        fontStyleMenu.items.add(menuItem)
        styleToggleGroup.toggles.add(menuItem)

        // populate the background-color menu
        initColorMenu(backgroundColorMenu, { color -> changeBackgroundColor(color) })
    }

    private fun changeBackgroundColor(color: Color) {
        checkDot().ifPresent { dot ->
            dot.chain.measureLabelBackgroundColor = color
        }
    }

    companion object {
        private lateinit var controller: MeasureLabelMenu

        private val menu: ContextMenu by lazy {
            val fxmlLoader = FXMLLoader()
            val contextMenu: ContextMenu =
                fxmlLoader.load<Any>(HelloApplication::class.java.getResourceAsStream("MeasureLabelMenu.fxml")) as ContextMenu
            controller = fxmlLoader.getController()
            contextMenu
        }

        fun show(measureLabel: Node, dot: Dot, x: Double, y: Double) {
            menu.show(measureLabel, x, y)
            controller.show(dot)
        }
    }
}