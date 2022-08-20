package org.mth.protractorfx

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import org.mth.protractorfx.log.LogFactory
import java.util.logging.Logger

/**
 * The popup menu shown on each measure label
 */
object MeasureLabelPopup : ContextMenu() {

    @FXML
    lateinit var backgroundColorMenu: Menu

    private val log: Logger = LogFactory.configureLog(MeasureLabelPopup::class.java)

    /**
     *
     * The graph node the label is associated to. This has to be set before the popup is shown.
     */
    private var dot: Dot? = null

    val colorMenu = Menu("Font color")
    val fontSizeMenu = Menu("Font size")
    val fontStyleMenu = Menu("Font style")

    var backgroundVisibilityMenuItem = CheckMenuItem("visible")

    init {
        val toggleGroup = ToggleGroup()
        val colorToggleGroup = ToggleGroup()
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
        listOf(
            Color.BLACK,
            Color.SLATEBLUE,
            Color.ORANGERED,
            Color.MAGENTA,
            Color.PLUM,
            Color.OLIVEDRAB,
            Color.TAN,
            Color.PEACHPUFF
        ).forEach { color ->
            val menuItem = RadioMenuItem()
            menuItem.graphic = Rectangle(14.0, 14.0, color)
            menuItem.properties["font-color"] = color
            menuItem.onAction = EventHandler { changeFontColor(color) }

            colorMenu.items.add(menuItem)
            colorToggleGroup.toggles.add(menuItem)
        }

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


        val backgroundMenu = Menu("Background").apply {
            backgroundVisibilityMenuItem.onAction =
                EventHandler { showLabelBackground(backgroundVisibilityMenuItem.isSelected) }
            items.add(backgroundVisibilityMenuItem)
        }

        items.addAll(fontSizeMenu, fontStyleMenu, colorMenu, backgroundMenu)
    }

    private fun resetLabelPosition() {

    }

    private fun showLabelBackground(visible: Boolean) {
        if (checkDot())
            dot!!.chain.measureLabelBackgroundVisibility = visible
    }

    private fun changeFontWeight(fontWeight: FontWeight) {
        if (checkDot()) {
            dot!!.chain.measureLabelFontWeight = fontWeight

            log.info("Font weight updated: $fontWeight")
        }
    }

    /**
     * Update the font color for all measure labels in the chain
     * @param color The new font color
     */
    private fun changeFontColor(color: Color) {
        checkDot()

        dot!!.chain.measureLabelFontColor = color

        log.info("Color updated: $color")
    }

    /**
     * Update the font size for all labels in the chain
     * @param fontSize The new font size
     */
    private fun changeFontSize(fontSize: Double) {
        checkDot()

        dot!!.chain.measureLabelFontSize = fontSize

        log.info("Font size updated: $fontSize")
    }

    private fun checkDot(): Boolean {
        if (dot == null) {
            log.warning("Associated dot cannot be null")
            return false
        }

        return true
    }

    /**
     * Show the context menu over the [measureLabel].
     * @param dot The graph-node related to the label
     */
    fun show(measureLabel: Node, dot: Dot, x: Double, y: Double) {
        this.dot = dot

        // select the MenuItem related to the current font color
        colorMenu.items.forEach {
            val menuItem = it as RadioMenuItem
            val color = it.properties["font-color"] as Color
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

        backgroundVisibilityMenuItem.isSelected = dot.chain.measureLabelBackgroundVisibility

        this.show(measureLabel, x, y)
    }
}