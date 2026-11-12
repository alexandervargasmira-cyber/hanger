package com.edtech.algebrahanger.models

/**
 * Representa el lado del colgador algebraico.
 */
enum class HangerSide {
    LEFT, RIGHT
}

/**
 * Tipo de elemento visual en la balanza:
 * - VARIABLE_X: Representa una incógnita 'x' (peso positivo o globo negativo).
 * - CONSTANT_1: Representa una unidad numérica '1' (peso positivo o globo negativo).
 */
enum class ItemType {
    VARIABLE_X, CONSTANT_1
}

/**
 * Modelo para un elemento físico individual en la balanza (pesa o globo).
 * Permite interacciones táctiles individuales, seguimiento de arrastre y aniquilación de pares cero.
 */
data class HangerItem(
    val id: String,
    val side: HangerSide,
    val type: ItemType,
    val isNegative: Boolean, // false = Pesa (hacia abajo), true = Globo (hacia arriba)
    val index: Int = 0,
    val isDisappearing: Boolean = false // Para animación de desvanecimiento en par cero
) {
    val weightValue: Float
        get() = if (isNegative) -1f else 1f
}

/**
 * Representación algebraica neta de la ecuación:
 * leftX * x + leftConst = rightX * x + rightConst
 */
data class Equation(
    val leftX: Int = 0,
    val leftConst: Int = 0,
    val rightX: Int = 0,
    val rightConst: Int = 0
) {
    val isBalanced: Boolean
        get() = (leftX == rightX) && (leftConst == rightConst)

    /**
     * Calcula la solución matemática teórica de la ecuación (si existe y es única).
     */
    fun calculateSolution(): Float? {
        val diffX = leftX - rightX
        val diffConst = rightConst - leftConst
        if (diffX == 0) return null
        return diffConst.toFloat() / diffX.toFloat()
    }

    /**
     * Formatea la ecuación a string legible: ej. "3x + 2 = x + 8"
     */
    fun toDisplayString(): String {
        val leftStr = formatSide(leftX, leftConst)
        val rightStr = formatSide(rightX, rightConst)
        return "$leftStr = $rightStr"
    }

    private fun formatSide(xCount: Int, constCount: Int): String {
        val parts = mutableListOf<String>()
        if (xCount != 0) {
            when (xCount) {
                1 -> parts.add("x")
                -1 -> parts.add("-x")
                else -> parts.add("${xCount}x")
            }
        }
        if (constCount != 0) {
            if (parts.isEmpty()) {
                parts.add("$constCount")
            } else {
                if (constCount > 0) parts.add("+ $constCount")
                else parts.add("- ${-constCount}")
            }
        }
        return if (parts.isEmpty()) "0" else parts.joinToString(" ")
    }
}

/**
 * Estado de una acción de eliminación pendiente para enseñar el equilibrio de operaciones:
 * Si el usuario retira un elemento de un lado, la balanza se desequilibra hasta que retire
 * el elemento equivalente del lado opuesto.
 */
data class PendingImbalance(
    val removedItemType: ItemType,
    val isNegative: Boolean,
    val originSide: HangerSide,
    val count: Int = 1
)

/**
 * Estado general de la UI y de las físicas del colgador.
 */
data class HangerState(
    val equation: Equation,
    val leftItems: List<HangerItem> = emptyList(),
    val rightItems: List<HangerItem> = emptyList(),
    val pendingImbalance: PendingImbalance? = null,
    val history: List<String> = emptyList(),
    val isSolved: Boolean = false,
    val currentLevel: Int = 1,
    val currentEquationIndex: Int = 0,
    val statusFeedback: String? = null,
    val zeroPairCountCelebrated: Int = 0
)
