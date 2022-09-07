package org.mth.protractorfx.command

interface Action {
    fun execute():Boolean
    fun undo()
    val name: String
}