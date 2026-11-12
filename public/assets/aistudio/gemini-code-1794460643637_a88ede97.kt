// EquationModels.kt
package com.edtech.algebrahanger.models

data class Equation(
    val leftX: Int = 0,
    val leftConst: Int = 0,
    val rightX: Int = 0,
    val rightConst: Int = 0
) {
    val isBalanced: Boolean
        get() = (leftX == rightX) && (leftConst == rightConst)
}

data class HangerState(
    val equation: Equation,
    val history: List<String> = emptyList(),
    val isSolved: Boolean = false
)