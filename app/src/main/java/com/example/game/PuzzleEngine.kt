package com.example.game

import com.example.data.model.CellPos
import com.example.data.model.ColorPair
import com.example.data.model.PuzzleLevel

data class BoardState(
    val level: PuzzleLevel,
    val paths: Map<Int, List<CellPos>> = emptyMap(),
    val connectedColors: Set<Int> = emptySet(),
    val movesCount: Int = 0,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val coveragePercent: Int = 0
)

class PuzzleEngine(private var level: PuzzleLevel) {
    private var paths: MutableMap<Int, MutableList<CellPos>> = mutableMapOf()
    private var connectedColors: MutableSet<Int> = mutableSetOf()
    private val history = mutableListOf<Map<Int, List<CellPos>>>()
    private var movesCount = 0
    private var activeColorId: Int? = null

    init {
        reset(level)
    }

    fun reset(newLevel: PuzzleLevel = level) {
        level = newLevel
        paths.clear()
        connectedColors.clear()
        history.clear()
        movesCount = 0
        activeColorId = null
    }

    fun getBoardState(): BoardState {
        val totalCells = level.size * level.size
        val occupiedCells = paths.values.flatten().toSet().size
        val coverage = if (totalCells > 0) (occupiedCells * 100) / totalCells else 0

        val allConnected = level.pairs.all { connectedColors.contains(it.colorId) }
        val isFullBoard = coverage >= 100 || occupiedCells >= totalCells

        val isWin = allConnected
        val stars = if (isWin) {
            when {
                isFullBoard && movesCount <= level.pairs.size + 1 -> 3
                coverage >= 85 -> 2
                else -> 1
            }
        } else 0

        return BoardState(
            level = level,
            paths = paths.mapValues { it.value.toList() },
            connectedColors = connectedColors.toSet(),
            movesCount = movesCount,
            isCompleted = isWin,
            stars = stars,
            coveragePercent = coverage
        )
    }

    fun startDrag(pos: CellPos): Int? {
        if (pos.x !in 0 until level.size || pos.y !in 0 until level.size) return null

        // Check if touched an endpoint
        val pair = level.pairs.firstOrNull { it.start == pos || it.end == pos }
        if (pair != null) {
            pushHistory()
            movesCount++
            activeColorId = pair.colorId
            connectedColors.remove(pair.colorId)
            val path = mutableListOf<CellPos>()
            // Start from the touched endpoint
            val startPt = if (pos == pair.start) pair.start else pair.end
            path.add(startPt)
            paths[pair.colorId] = path
            return pair.colorId
        }

        // Check if touched an existing path
        for ((cId, path) in paths) {
            val idx = path.indexOf(pos)
            if (idx != -1) {
                pushHistory()
                movesCount++
                activeColorId = cId
                connectedColors.remove(cId)
                // Retract path to this point
                val truncated = path.subList(0, idx + 1).toMutableList()
                paths[cId] = truncated
                return cId
            }
        }

        return null
    }

    fun dragTo(targetPos: CellPos, onConnected: () -> Unit = {}, onStep: (Int) -> Unit = {}): Boolean {
        val cId = activeColorId ?: return false
        if (targetPos.x !in 0 until level.size || targetPos.y !in 0 until level.size) return false

        val currentPath = paths[cId] ?: return false
        val lastPos = currentPath.lastOrNull() ?: return false
        if (targetPos == lastPos) return false

        // Check if backtracking along active path
        val backIdx = currentPath.indexOf(targetPos)
        if (backIdx != -1) {
            val truncated = currentPath.subList(0, backIdx + 1).toMutableList()
            paths[cId] = truncated
            connectedColors.remove(cId)
            return true
        }

        // Interpolate if rapid drag jumped a cell orthogonally
        val steps = getOrthogonalPath(lastPos, targetPos)
        if (steps.isEmpty()) return false

        for (nextPos in steps) {
            if (!stepOneCell(cId, nextPos, onConnected, onStep)) {
                return false
            }
        }
        return true
    }

    private fun stepOneCell(
        cId: Int,
        nextPos: CellPos,
        onConnected: () -> Unit,
        onStep: (Int) -> Unit
    ): Boolean {
        val currentPath = paths[cId] ?: return false
        val pair = level.pairs.firstOrNull { it.colorId == cId } ?: return false

        // Cannot enter another color's endpoints
        val otherPair = level.pairs.firstOrNull {
            it.colorId != cId && (it.start == nextPos || it.end == nextPos)
        }
        if (otherPair != null) {
            return false
        }

        // If hits another color's path, cut or remove that other path
        for ((otherColor, otherPath) in paths) {
            if (otherColor != cId) {
                val hitIdx = otherPath.indexOf(nextPos)
                if (hitIdx != -1) {
                    connectedColors.remove(otherColor)
                    if (hitIdx == 0) {
                        paths[otherColor] = mutableListOf()
                    } else {
                        paths[otherColor] = otherPath.subList(0, hitIdx).toMutableList()
                    }
                }
            }
        }

        // Add to active path
        currentPath.add(nextPos)
        onStep(currentPath.size)

        // Check if reached destination endpoint
        val targetEndpoint = if (currentPath.first() == pair.start) pair.end else pair.start
        if (nextPos == targetEndpoint) {
            connectedColors.add(cId)
            activeColorId = null
            onConnected()
            return false // Finished drawing
        }

        return true
    }

    fun endDrag() {
        activeColorId = null
    }

    fun undo(): Boolean {
        if (history.isEmpty()) return false
        val last = history.removeAt(history.size - 1)
        paths.clear()
        paths.putAll(last.mapValues { it.value.toMutableList() })
        recalculateConnections()
        return true
    }

    fun applyHint(): Int? {
        val unsolvedPair = level.pairs.firstOrNull { !connectedColors.contains(it.colorId) }
            ?: level.pairs.firstOrNull() ?: return null

        val cId = unsolvedPair.colorId
        val solutionPath = level.solution[cId] ?: return null

        pushHistory()
        // Remove conflicts with hint path
        for (pos in solutionPath) {
            for ((otherId, otherPath) in paths) {
                if (otherId != cId && otherPath.contains(pos)) {
                    connectedColors.remove(otherId)
                    val cutIdx = otherPath.indexOf(pos)
                    if (cutIdx <= 1) {
                        paths[otherId] = mutableListOf()
                    } else {
                        paths[otherId] = otherPath.subList(0, cutIdx).toMutableList()
                    }
                }
            }
        }

        paths[cId] = solutionPath.toMutableList()
        connectedColors.add(cId)
        movesCount++
        return cId
    }

    private fun recalculateConnections() {
        connectedColors.clear()
        for (pair in level.pairs) {
            val path = paths[pair.colorId] ?: continue
            if (path.size >= 2) {
                val isConn = (path.first() == pair.start && path.last() == pair.end) ||
                        (path.first() == pair.end && path.last() == pair.start)
                if (isConn) {
                    connectedColors.add(pair.colorId)
                }
            }
        }
    }

    private fun pushHistory() {
        if (history.size >= 30) history.removeAt(0)
        history.add(paths.mapValues { it.value.toList() })
    }

    private fun getOrthogonalPath(from: CellPos, to: CellPos): List<CellPos> {
        val dx = to.x - from.x
        val dy = to.y - from.y
        if (kotlin.math.abs(dx) + kotlin.math.abs(dy) == 1) {
            return listOf(to)
        }
        if (kotlin.math.abs(dx) <= 1 && kotlin.math.abs(dy) <= 1) {
            // Diagonal step: pick orthogonal intermediate
            return listOf(CellPos(to.x, from.y), to)
        }
        // If too far, don't jump
        return emptyList()
    }
}
