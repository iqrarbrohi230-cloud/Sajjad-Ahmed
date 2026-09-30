package com.example.data.model

import androidx.compose.ui.graphics.Color

data class CellPos(val x: Int, val y: Int) {
    fun isAdjacent(other: CellPos): Boolean {
        val dx = kotlin.math.abs(x - other.x)
        val dy = kotlin.math.abs(y - other.y)
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1)
    }
}

enum class GameColor(
    val id: Int,
    val displayName: String,
    val primaryColor: Color,
    val glowColor: Color,
    val lightColor: Color
) {
    RED(1, "Red", Color(0xFFFF334B), Color(0x66FF334B), Color(0xFFFF8093)),
    BLUE(2, "Blue", Color(0xFF0084FF), Color(0x660084FF), Color(0xFF64B5F6)),
    GREEN(3, "Green", Color(0xFF00E676), Color(0x6600E676), Color(0xFF69F0AE)),
    YELLOW(4, "Yellow", Color(0xFFFFD600), Color(0x66FFD600), Color(0xFFFFF176)),
    ORANGE(5, "Orange", Color(0xFFFF6D00), Color(0x66FF6D00), Color(0xFFFFB74D)),
    PURPLE(6, "Purple", Color(0xFFD500F9), Color(0x66D500F9), Color(0xFFEA80FC)),
    CYAN(7, "Cyan", Color(0xFF00E5FF), Color(0x6600E5FF), Color(0xFF84FFFF)),
    PINK(8, "Pink", Color(0xFFFF1744), Color(0x66FF1744), Color(0xFFFF80AB)),
    LIME(9, "Lime", Color(0xFFAEEA00), Color(0x66AEEA00), Color(0xFFCCFF90)),
    WHITE(10, "Silver", Color(0xFFCFD8DC), Color(0x66CFD8DC), Color(0xFFECEFF1));

    companion object {
        fun fromId(id: Int): GameColor = entries.firstOrNull { it.id == id } ?: RED
    }
}

data class ColorPair(
    val colorId: Int,
    val start: CellPos,
    val end: CellPos
)

data class PuzzleLevel(
    val id: Int,
    val size: Int,
    val pairs: List<ColorPair>,
    val solution: Map<Int, List<CellPos>> = emptyMap(),
    val parMoves: Int = pairs.size
) {
    val difficultyCategory: String
        get() = when (size) {
            5 -> "Beginner"
            6 -> "Easy"
            7 -> "Medium"
            else -> "Hard"
        }
}
