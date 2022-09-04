package org.mth.protractorfx.command

interface Action {
    fun execute()
    fun undo()
    val name: String
}