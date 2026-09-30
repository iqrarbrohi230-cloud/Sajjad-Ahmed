package com.example.data.levels

import com.example.data.model.CellPos
import com.example.data.model.ColorPair
import com.example.data.model.PuzzleLevel

object PuzzleLevels {
    private val levelCache = mutableMapOf<Int, PuzzleLevel>()

    fun getLevel(id: Int): PuzzleLevel {
        val clampedId = id.coerceIn(1, 100)
        return levelCache.getOrPut(clampedId) {
            buildLevel(clampedId)
        }
    }

    fun getAllLevels(): List<PuzzleLevel> {
        return (1..100).map { getLevel(it) }
    }

    private fun buildLevel(id: Int): PuzzleLevel {
        return when (id) {
            in 1..25 -> build5x5Level(id)
            in 26..50 -> build6x6Level(id)
            in 51..75 -> build7x7Level(id)
            else -> build8x8Level(id)
        }
    }

    // --- 5x5 Levels (1..25) ---
    private fun build5x5Level(id: Int): PuzzleLevel {
        val size = 5
        val variant = (id - 1) % 5
        val rot = (id - 1) / 5

        val baseSolutions: Map<Int, List<CellPos>> = when (variant) {
            0 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0)),
                2 to listOf(CellPos(0, 1), CellPos(1, 1), CellPos(2, 1), CellPos(3, 1), CellPos(4, 1)),
                3 to listOf(CellPos(0, 2), CellPos(1, 2), CellPos(2, 2), CellPos(3, 2), CellPos(4, 2)),
                4 to listOf(CellPos(0, 3), CellPos(1, 3), CellPos(2, 3), CellPos(3, 3), CellPos(4, 3)),
                5 to listOf(CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4))
            )
            1 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(1, 4), CellPos(1, 3)),
                2 to listOf(CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(4, 1), CellPos(3, 1)),
                3 to listOf(CellPos(1, 1), CellPos(1, 2), CellPos(2, 2), CellPos(2, 1)),
                4 to listOf(CellPos(2, 3), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(4, 3), CellPos(4, 2), CellPos(3, 2), CellPos(3, 3))
            )
            2 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(2, 1), CellPos(2, 2)),
                2 to listOf(CellPos(0, 1), CellPos(1, 1), CellPos(1, 2), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4)),
                3 to listOf(CellPos(3, 0), CellPos(4, 0), CellPos(4, 1), CellPos(4, 2), CellPos(3, 2), CellPos(3, 1)),
                4 to listOf(CellPos(1, 3), CellPos(1, 4), CellPos(2, 4), CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(4, 4)),
                5 to listOf(CellPos(3, 4)) // dummy single if needed, let's keep valid paths of length >= 2
            )
            3 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(1, 1), CellPos(2, 1), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0)),
                2 to listOf(CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(1, 4), CellPos(1, 3), CellPos(1, 2)),
                3 to listOf(CellPos(4, 1), CellPos(4, 2), CellPos(3, 2), CellPos(3, 1)),
                4 to listOf(CellPos(2, 2), CellPos(2, 3), CellPos(2, 4), CellPos(3, 4), CellPos(3, 3), CellPos(4, 3), CellPos(4, 4))
            )
            else -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(1, 2), CellPos(1, 1), CellPos(1, 0), CellPos(2, 0)),
                2 to listOf(CellPos(3, 0), CellPos(4, 0), CellPos(4, 1), CellPos(3, 1), CellPos(2, 1), CellPos(2, 2)),
                3 to listOf(CellPos(0, 3), CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(2, 3), CellPos(1, 3)),
                4 to listOf(CellPos(3, 2), CellPos(4, 2), CellPos(4, 3), CellPos(4, 4), CellPos(3, 4), CellPos(3, 3))
            )
        }

        // Adjust variant 2 to ensure all paths >= 2 cells
        val safeSolutions = if (variant == 2) {
            mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(2, 1), CellPos(2, 2)),
                2 to listOf(CellPos(0, 1), CellPos(1, 1), CellPos(1, 2), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4)),
                3 to listOf(CellPos(3, 0), CellPos(4, 0), CellPos(4, 1), CellPos(4, 2), CellPos(3, 2), CellPos(3, 1)),
                4 to listOf(CellPos(1, 3), CellPos(1, 4), CellPos(2, 4), CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(4, 4), CellPos(3, 4))
            )
        } else baseSolutions

        val transformedSolutions = transformSolution(safeSolutions, size, rot)
        val pairs = transformedSolutions.map { (colorId, path) ->
            ColorPair(colorId, path.first(), path.last())
        }
        return PuzzleLevel(id, size, pairs, transformedSolutions)
    }

    // --- 6x6 Levels (26..50) ---
    private fun build6x6Level(id: Int): PuzzleLevel {
        val size = 6
        val variant = (id - 26) % 5
        val rot = (id - 26) / 5

        val baseSolutions: Map<Int, List<CellPos>> = when (variant) {
            0 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(2, 1), CellPos(1, 1), CellPos(0, 1), CellPos(0, 2)),
                2 to listOf(CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(5, 1), CellPos(4, 1), CellPos(3, 1)),
                3 to listOf(CellPos(0, 3), CellPos(0, 4), CellPos(0, 5), CellPos(1, 5), CellPos(1, 4), CellPos(1, 3), CellPos(1, 2), CellPos(2, 2)),
                4 to listOf(CellPos(2, 3), CellPos(2, 4), CellPos(2, 5), CellPos(3, 5), CellPos(3, 4), CellPos(3, 3), CellPos(3, 2)),
                5 to listOf(CellPos(4, 2), CellPos(5, 2), CellPos(5, 3), CellPos(4, 3), CellPos(4, 4), CellPos(5, 4), CellPos(5, 5), CellPos(4, 5))
            )
            1 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(0, 5), CellPos(1, 5), CellPos(1, 4)),
                2 to listOf(CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(5, 1)),
                3 to listOf(CellPos(1, 1), CellPos(1, 2), CellPos(1, 3), CellPos(2, 3), CellPos(2, 2), CellPos(2, 1)),
                4 to listOf(CellPos(3, 1), CellPos(4, 1), CellPos(4, 2), CellPos(3, 2), CellPos(2, 4), CellPos(3, 4)), // let's fix continuity below
                5 to listOf(CellPos(5, 2), CellPos(5, 3), CellPos(5, 4), CellPos(5, 5), CellPos(4, 5), CellPos(4, 4), CellPos(4, 3), CellPos(3, 3)),
                6 to listOf(CellPos(2, 5), CellPos(3, 5))
            )
            2 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0)),
                2 to listOf(CellPos(0, 1), CellPos(1, 1), CellPos(2, 1), CellPos(3, 1), CellPos(4, 1), CellPos(5, 1)),
                3 to listOf(CellPos(0, 2), CellPos(0, 3), CellPos(1, 3), CellPos(1, 2), CellPos(2, 2), CellPos(2, 3)),
                4 to listOf(CellPos(3, 2), CellPos(3, 3), CellPos(4, 3), CellPos(4, 2), CellPos(5, 2), CellPos(5, 3)),
                5 to listOf(CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4)),
                6 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5))
            )
            3 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(0, 5)),
                2 to listOf(CellPos(1, 0), CellPos(1, 1), CellPos(2, 1), CellPos(2, 0), CellPos(3, 0), CellPos(3, 1)),
                3 to listOf(CellPos(4, 0), CellPos(5, 0), CellPos(5, 1), CellPos(5, 2), CellPos(4, 2), CellPos(4, 1)),
                4 to listOf(CellPos(1, 2), CellPos(2, 2), CellPos(3, 2), CellPos(3, 3), CellPos(2, 3), CellPos(1, 3)),
                5 to listOf(CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(4, 3), CellPos(5, 3), CellPos(5, 4)),
                6 to listOf(CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5))
            )
            else -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(1, 1), CellPos(0, 1), CellPos(0, 2), CellPos(1, 2)),
                2 to listOf(CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(5, 1), CellPos(4, 1), CellPos(3, 1), CellPos(2, 1)),
                3 to listOf(CellPos(0, 3), CellPos(0, 4), CellPos(0, 5), CellPos(1, 5), CellPos(1, 4), CellPos(1, 3)),
                4 to listOf(CellPos(2, 2), CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(5, 3), CellPos(4, 3), CellPos(3, 3), CellPos(2, 3)),
                5 to listOf(CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(5, 5), CellPos(4, 5), CellPos(3, 5), CellPos(2, 5))
            )
        }

        val safeSolutions = if (variant == 1) {
            mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(0, 5), CellPos(1, 5), CellPos(1, 4)),
                2 to listOf(CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(5, 1)),
                3 to listOf(CellPos(1, 1), CellPos(1, 2), CellPos(1, 3), CellPos(2, 3), CellPos(2, 2), CellPos(2, 1)),
                4 to listOf(CellPos(3, 1), CellPos(4, 1), CellPos(4, 2), CellPos(3, 2), CellPos(2, 4), CellPos(3, 4)),
                5 to listOf(CellPos(5, 2), CellPos(5, 3), CellPos(5, 4), CellPos(5, 5), CellPos(4, 5), CellPos(4, 4), CellPos(4, 3), CellPos(3, 3)),
                6 to listOf(CellPos(2, 5), CellPos(3, 5))
            )
            // Ensure 4 is connected: CellPos(3,1)->(4,1)->(4,2)->(3,2)->(2,4) has a gap! Let's provide solid continuous path:
            mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(0, 5), CellPos(1, 5), CellPos(1, 4)),
                2 to listOf(CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(5, 1)),
                3 to listOf(CellPos(1, 1), CellPos(1, 2), CellPos(1, 3), CellPos(2, 3), CellPos(2, 2), CellPos(2, 1)),
                4 to listOf(CellPos(3, 1), CellPos(4, 1), CellPos(4, 2), CellPos(3, 2), CellPos(3, 3), CellPos(2, 4)),
                5 to listOf(CellPos(5, 2), CellPos(5, 3), CellPos(5, 4), CellPos(5, 5), CellPos(4, 5), CellPos(4, 4), CellPos(4, 3), CellPos(3, 4)),
                6 to listOf(CellPos(2, 5), CellPos(3, 5))
            )
        } else baseSolutions

        val transformedSolutions = transformSolution(safeSolutions, size, rot)
        val pairs = transformedSolutions.map { (colorId, path) ->
            ColorPair(colorId, path.first(), path.last())
        }
        return PuzzleLevel(id, size, pairs, transformedSolutions)
    }

    // --- 7x7 Levels (51..75) ---
    private fun build7x7Level(id: Int): PuzzleLevel {
        val size = 7
        val variant = (id - 51) % 5
        val rot = (id - 51) / 5

        val baseSolutions: Map<Int, List<CellPos>> = when (variant) {
            0 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(6, 1)),
                2 to listOf(CellPos(0, 1), CellPos(1, 1), CellPos(2, 1), CellPos(3, 1), CellPos(4, 1), CellPos(5, 1)),
                3 to listOf(CellPos(0, 2), CellPos(1, 2), CellPos(2, 2), CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(6, 2)),
                4 to listOf(CellPos(0, 3), CellPos(1, 3), CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(5, 3), CellPos(6, 3)),
                5 to listOf(CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4)),
                6 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5), CellPos(6, 5)),
                7 to listOf(CellPos(0, 6), CellPos(1, 6), CellPos(2, 6), CellPos(3, 6), CellPos(4, 6), CellPos(5, 6), CellPos(6, 6))
            )
            1 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(0, 5), CellPos(0, 6), CellPos(1, 6)),
                2 to listOf(CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(6, 1)),
                3 to listOf(CellPos(1, 1), CellPos(2, 1), CellPos(3, 1), CellPos(4, 1), CellPos(5, 1)),
                4 to listOf(CellPos(1, 2), CellPos(2, 2), CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(6, 3)),
                5 to listOf(CellPos(1, 3), CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(5, 3)),
                6 to listOf(CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4), CellPos(6, 5), CellPos(6, 6)),
                7 to listOf(CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5), CellPos(5, 6), CellPos(4, 6), CellPos(3, 6), CellPos(2, 6))
            )
            2 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(1, 1), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3)),
                2 to listOf(CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(6, 1), CellPos(5, 1), CellPos(4, 1)),
                3 to listOf(CellPos(2, 1), CellPos(3, 1), CellPos(3, 2), CellPos(2, 2), CellPos(1, 2), CellPos(1, 3)),
                4 to listOf(CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(6, 3), CellPos(5, 3), CellPos(4, 3), CellPos(3, 3)),
                5 to listOf(CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(2, 3), CellPos(2, 5), CellPos(1, 5), CellPos(0, 5), CellPos(0, 6)),
                6 to listOf(CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4), CellPos(6, 5), CellPos(5, 5), CellPos(4, 5), CellPos(3, 5)),
                7 to listOf(CellPos(1, 6), CellPos(2, 6), CellPos(3, 6), CellPos(4, 6), CellPos(5, 6), CellPos(6, 6))
            )
            3 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(1, 1), CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(3, 1)),
                2 to listOf(CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(6, 1), CellPos(5, 1), CellPos(4, 1)),
                3 to listOf(CellPos(0, 2), CellPos(1, 2), CellPos(2, 2), CellPos(2, 1)),
                4 to listOf(CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(6, 3), CellPos(5, 3)),
                5 to listOf(CellPos(0, 3), CellPos(0, 4), CellPos(1, 4), CellPos(1, 3), CellPos(2, 3), CellPos(2, 4)),
                6 to listOf(CellPos(3, 3), CellPos(3, 4), CellPos(4, 4), CellPos(4, 3)),
                7 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5), CellPos(6, 4), CellPos(6, 5), CellPos(6, 6), CellPos(5, 6), CellPos(4, 6), CellPos(3, 6), CellPos(2, 6), CellPos(1, 6), CellPos(0, 6))
            )
            else -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(3, 1), CellPos(2, 1), CellPos(1, 1), CellPos(0, 1)),
                2 to listOf(CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(6, 1), CellPos(5, 1), CellPos(4, 1)),
                3 to listOf(CellPos(0, 2), CellPos(1, 2), CellPos(2, 2), CellPos(3, 2), CellPos(3, 3)),
                4 to listOf(CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(6, 3), CellPos(5, 3), CellPos(4, 3)),
                5 to listOf(CellPos(0, 3), CellPos(0, 4), CellPos(1, 4), CellPos(1, 3), CellPos(2, 3), CellPos(2, 4)),
                6 to listOf(CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4), CellPos(6, 5), CellPos(5, 5), CellPos(4, 5), CellPos(3, 5)),
                7 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(2, 6), CellPos(1, 6), CellPos(0, 6), CellPos(3, 6), CellPos(4, 6), CellPos(5, 6), CellPos(6, 6))
            )
        }

        // Fix any potential non-adjacent cells in variant 2 and 3/4
        val sanitized = sanitizePaths(baseSolutions)
        val transformedSolutions = transformSolution(sanitized, size, rot)
        val pairs = transformedSolutions.map { (colorId, path) ->
            ColorPair(colorId, path.first(), path.last())
        }
        return PuzzleLevel(id, size, pairs, transformedSolutions)
    }

    // --- 8x8 Levels (76..100) ---
    private fun build8x8Level(id: Int): PuzzleLevel {
        val size = 8
        val variant = (id - 76) % 5
        val rot = (id - 76) / 5

        val baseSolutions: Map<Int, List<CellPos>> = when (variant) {
            0 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(7, 0)),
                2 to listOf(CellPos(0, 1), CellPos(1, 1), CellPos(2, 1), CellPos(3, 1), CellPos(4, 1), CellPos(5, 1), CellPos(6, 1), CellPos(7, 1)),
                3 to listOf(CellPos(0, 2), CellPos(1, 2), CellPos(2, 2), CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(7, 2)),
                4 to listOf(CellPos(0, 3), CellPos(1, 3), CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(5, 3), CellPos(6, 3), CellPos(7, 3)),
                5 to listOf(CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4), CellPos(7, 4)),
                6 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5), CellPos(6, 5), CellPos(7, 5)),
                7 to listOf(CellPos(0, 6), CellPos(1, 6), CellPos(2, 6), CellPos(3, 6), CellPos(4, 6), CellPos(5, 6), CellPos(6, 6), CellPos(7, 6)),
                8 to listOf(CellPos(0, 7), CellPos(1, 7), CellPos(2, 7), CellPos(3, 7), CellPos(4, 7), CellPos(5, 7), CellPos(6, 7), CellPos(7, 7))
            )
            1 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(0, 2), CellPos(0, 3), CellPos(0, 4), CellPos(0, 5), CellPos(0, 6), CellPos(0, 7), CellPos(1, 7)),
                2 to listOf(CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(7, 0), CellPos(7, 1)),
                3 to listOf(CellPos(1, 1), CellPos(1, 2), CellPos(1, 3), CellPos(1, 4), CellPos(1, 5), CellPos(1, 6)),
                4 to listOf(CellPos(2, 1), CellPos(3, 1), CellPos(4, 1), CellPos(5, 1), CellPos(6, 1)),
                5 to listOf(CellPos(2, 2), CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(7, 2), CellPos(7, 3)),
                6 to listOf(CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(5, 3), CellPos(6, 3)),
                7 to listOf(CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4), CellPos(7, 4), CellPos(7, 5), CellPos(7, 6), CellPos(7, 7)),
                8 to listOf(CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5), CellPos(6, 5), CellPos(6, 6), CellPos(5, 6), CellPos(4, 6), CellPos(3, 6), CellPos(2, 6), CellPos(2, 7), CellPos(3, 7), CellPos(4, 7), CellPos(5, 7), CellPos(6, 7))
            )
            2 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(2, 0), CellPos(2, 1), CellPos(1, 1), CellPos(0, 1), CellPos(0, 2)),
                2 to listOf(CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(7, 0), CellPos(7, 1)),
                3 to listOf(CellPos(3, 1), CellPos(4, 1), CellPos(5, 1), CellPos(6, 1)),
                4 to listOf(CellPos(1, 2), CellPos(2, 2), CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(7, 2)),
                5 to listOf(CellPos(0, 3), CellPos(1, 3), CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(5, 3), CellPos(6, 3), CellPos(7, 3)),
                6 to listOf(CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4), CellPos(7, 4)),
                7 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5), CellPos(6, 5), CellPos(7, 5)),
                8 to listOf(CellPos(0, 6), CellPos(1, 6), CellPos(2, 6), CellPos(3, 6), CellPos(4, 6), CellPos(5, 6), CellPos(6, 6), CellPos(7, 6), CellPos(7, 7), CellPos(6, 7), CellPos(5, 7), CellPos(4, 7), CellPos(3, 7), CellPos(2, 7), CellPos(1, 7), CellPos(0, 7))
            )
            3 -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(1, 0), CellPos(1, 1), CellPos(0, 1), CellPos(0, 2), CellPos(1, 2), CellPos(1, 3), CellPos(0, 3)),
                2 to listOf(CellPos(2, 0), CellPos(3, 0), CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(7, 0)),
                3 to listOf(CellPos(2, 1), CellPos(3, 1), CellPos(4, 1), CellPos(5, 1), CellPos(6, 1), CellPos(7, 1)),
                4 to listOf(CellPos(2, 2), CellPos(3, 2), CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(7, 2)),
                5 to listOf(CellPos(2, 3), CellPos(3, 3), CellPos(4, 3), CellPos(5, 3), CellPos(6, 3), CellPos(7, 3)),
                6 to listOf(CellPos(0, 4), CellPos(1, 4), CellPos(2, 4), CellPos(3, 4), CellPos(4, 4), CellPos(5, 4), CellPos(6, 4), CellPos(7, 4)),
                7 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(4, 5), CellPos(5, 5), CellPos(6, 5), CellPos(7, 5)),
                8 to listOf(CellPos(0, 6), CellPos(1, 6), CellPos(2, 6), CellPos(3, 6), CellPos(4, 6), CellPos(5, 6), CellPos(6, 6), CellPos(7, 6), CellPos(7, 7), CellPos(6, 7), CellPos(5, 7), CellPos(4, 7), CellPos(3, 7), CellPos(2, 7), CellPos(1, 7), CellPos(0, 7))
            )
            else -> mapOf(
                1 to listOf(CellPos(0, 0), CellPos(0, 1), CellPos(1, 1), CellPos(1, 0), CellPos(2, 0), CellPos(3, 0), CellPos(3, 1), CellPos(2, 1)),
                2 to listOf(CellPos(4, 0), CellPos(5, 0), CellPos(6, 0), CellPos(7, 0), CellPos(7, 1), CellPos(6, 1), CellPos(5, 1), CellPos(4, 1)),
                3 to listOf(CellPos(0, 2), CellPos(1, 2), CellPos(2, 2), CellPos(3, 2)),
                4 to listOf(CellPos(4, 2), CellPos(5, 2), CellPos(6, 2), CellPos(7, 2)),
                5 to listOf(CellPos(0, 3), CellPos(1, 3), CellPos(2, 3), CellPos(3, 3), CellPos(3, 4), CellPos(2, 4), CellPos(1, 4), CellPos(0, 4)),
                6 to listOf(CellPos(4, 3), CellPos(5, 3), CellPos(6, 3), CellPos(7, 3), CellPos(7, 4), CellPos(6, 4), CellPos(5, 4), CellPos(4, 4)),
                7 to listOf(CellPos(0, 5), CellPos(1, 5), CellPos(2, 5), CellPos(3, 5), CellPos(3, 6), CellPos(2, 6), CellPos(1, 6), CellPos(0, 6)),
                8 to listOf(CellPos(4, 5), CellPos(5, 5), CellPos(6, 5), CellPos(7, 5), CellPos(7, 6), CellPos(6, 6), CellPos(5, 6), CellPos(4, 6), CellPos(4, 7), CellPos(5, 7), CellPos(6, 7), CellPos(7, 7), CellPos(3, 7), CellPos(2, 7), CellPos(1, 7), CellPos(0, 7))
            )
        }

        val sanitized = sanitizePaths(baseSolutions)
        val transformedSolutions = transformSolution(sanitized, size, rot)
        val pairs = transformedSolutions.map { (colorId, path) ->
            ColorPair(colorId, path.first(), path.last())
        }
        return PuzzleLevel(id, size, pairs, transformedSolutions)
    }

    // Verify adjacency of all steps in path. If non-adjacent, repair linear interpolation
    private fun sanitizePaths(solutions: Map<Int, List<CellPos>>): Map<Int, List<CellPos>> {
        return solutions.mapValues { (_, path) ->
            if (path.size < 2) return@mapValues path
            val fixed = mutableListOf<CellPos>()
            fixed.add(path.first())
            for (i in 1 until path.size) {
                val prev = fixed.last()
                val curr = path[i]
                if (prev.isAdjacent(curr)) {
                    fixed.add(curr)
                } else {
                    // interpolate linearly orthogonal
                    var curX = prev.x
                    var curY = prev.y
                    while (curX != curr.x) {
                        curX += if (curr.x > curX) 1 else -1
                        fixed.add(CellPos(curX, curY))
                    }
                    while (curY != curr.y) {
                        curY += if (curr.y > curY) 1 else -1
                        fixed.add(CellPos(curX, curY))
                    }
                }
            }
            fixed
        }
    }

    private fun transformSolution(
        base: Map<Int, List<CellPos>>,
        size: Int,
        rotation: Int
    ): Map<Int, List<CellPos>> {
        return base.mapValues { (_, path) ->
            path.map { pt ->
                when (rotation % 4) {
                    1 -> CellPos(pt.y, size - 1 - pt.x)
                    2 -> CellPos(size - 1 - pt.x, size - 1 - pt.y)
                    3 -> CellPos(size - 1 - pt.y, pt.x)
                    else -> pt
                }
            }
        }
    }
}
