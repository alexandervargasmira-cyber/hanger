// EquationParser.kt
package com.edtech.algebrahanger.utils

import com.edtech.algebrahanger.models.Equation

object EquationParser {
    
    /**
     * Convierte un string "3x - 4 = 2x + 5" a un objeto Equation.
     */
    fun parse(input: String): Equation {
        val cleanInput = input.replace(" ", "").lowercase()
        val parts = cleanInput.split("=")
        if (parts.size != 2) throw IllegalArgumentException("invalid_equation")

        val (leftX, leftConst) = parseSide(parts[0])
        val (rightX, rightConst) = parseSide(parts[1])

        // Validaciones Matemáticas
        if (leftX == rightX && leftConst == rightConst) throw IllegalArgumentException("infinite_solutions")
        if (leftX == rightX && leftConst != rightConst) throw IllegalArgumentException("no_solution")

        return Equation(leftX, leftConst, rightX, rightConst)
    }

    private fun parseSide(side: String): Pair<Int, Int> {
        var xCount = 0
        var constCount = 0
        
        // Normalizar los signos para facilitar el split
        val normalized = side.replace("-", "+-")
        val terms = normalized.split("+").filter { it.isNotEmpty() }

        for (term in terms) {
            if (term.contains("x")) {
                val coeffStr = term.replace("x", "")
                xCount += when (coeffStr) {
                    "", "+" -> 1
                    "-" -> -1
                    else -> coeffStr.toIntOrNull() ?: 0
                }
            } else {
                constCount += term.toIntOrNull() ?: 0
            }
        }
        return Pair(xCount, constCount)
    }
}