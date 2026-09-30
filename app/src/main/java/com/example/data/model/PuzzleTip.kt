package com.example.data.model

data class PuzzleTip(
    val id: Int,
    val titleKey: String,
    val descKey: String,
    val tag: String,
    val fallbackTitle: String,
    val fallbackDesc: String
)

object PuzzleTipsCatalog {
    val tips = listOf(
        PuzzleTip(
            id = 1,
            titleKey = "tip_1_title",
            descKey = "tip_1_desc",
            tag = "Strategy",
            fallbackTitle = "Perimeter First",
            fallbackDesc = "Route paths along the outer edges of the board first. Keeping lines near the walls keeps the center clear for intersecting paths!"
        ),
        PuzzleTip(
            id = 2,
            titleKey = "tip_2_title",
            descKey = "tip_2_desc",
            tag = "Corner Trap",
            fallbackTitle = "Corner Priority",
            fallbackDesc = "Dots positioned in corners only have two possible exits. Plan their paths early so you never get boxed into a dead end."
        ),
        PuzzleTip(
            id = 3,
            titleKey = "tip_3_title",
            descKey = "tip_3_desc",
            tag = "3 Stars",
            fallbackTitle = "100% Coverage",
            fallbackDesc = "Connecting all colors is only half the goal! To achieve a 3-star rating, make sure every single grid cell is filled by a line."
        ),
        PuzzleTip(
            id = 4,
            titleKey = "tip_4_title",
            descKey = "tip_4_desc",
            tag = "Controls",
            fallbackTitle = "Smooth Backtracking",
            fallbackDesc = "Made a wrong turn? Simply drag your finger backwards along your line to smoothly retract it without restarting the puzzle."
        ),
        PuzzleTip(
            id = 5,
            titleKey = "tip_5_title",
            descKey = "tip_5_desc",
            tag = "Mastery",
            fallbackTitle = "Two-Way Tracing",
            fallbackDesc = "On big 7x7 and 8x8 boards, trace paths halfway from both matching dots towards each other to find the easiest route."
        ),
        PuzzleTip(
            id = 6,
            titleKey = "tip_6_title",
            descKey = "tip_6_desc",
            tag = "Board Safety",
            fallbackTitle = "Avoid 1x1 Holes",
            fallbackDesc = "Watch out for lonely empty cells! A line must pass through every cell, so never create an enclosed gap no other line can reach."
        ),
        PuzzleTip(
            id = 7,
            titleKey = "tip_7_title",
            descKey = "tip_7_desc",
            tag = "QoL",
            fallbackTitle = "Undo Is Free",
            fallbackDesc = "Don't hesitate to test bold ideas. Tap the Undo button anytime to take back your last move without losing progress."
        ),
        PuzzleTip(
            id = 8,
            titleKey = "tip_8_title",
            descKey = "tip_8_desc",
            tag = "Spacing",
            fallbackTitle = "Parallel Highways",
            fallbackDesc = "Routing neighbor lines parallel to each other along edges guarantees clean board fill and prevents criss-cross blocks."
        )
    )

    fun getRandomTip(): PuzzleTip = tips.random()

    fun getTip(index: Int): PuzzleTip = tips[index % tips.size]
}
