package com.edtech.algebrahanger.utils

import com.edtech.algebrahanger.models.Equation

object EquationParser {

    /**
     * Convierte un string de ecuación (ej. "3x - 4 = 2x + 5") a un objeto Equation.
     * Arroja excepciones claras si la sintaxis es inválida o carece de solución única.
     */
    fun parse(input: String): Equation {
        val cleanInput = input.replace(" ", "").lowercase()
        if (!cleanInput.contains("=") || !cleanInput.contains("x")) {
            throw IllegalArgumentException("invalid_equation")
        }

        val parts = cleanInput.split("=")
        if (parts.size != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw IllegalArgumentException("invalid_equation")
        }

        val (leftX, leftConst) = parseSide(parts[0])
        val (rightX, rightConst) = parseSide(parts[1])

        // Al menos un lado debe tener alguna variable o término constante
        if (leftX == 0 && rightX == 0) {
            throw IllegalArgumentException("invalid_equation")
        }

        // Validaciones Matemáticas
        if (leftX == rightX && leftConst == rightConst) {
            throw IllegalArgumentException("infinite_solutions")
        }
        if (leftX == rightX && leftConst != rightConst) {
            throw IllegalArgumentException("no_solution")
        }

        return Equation(leftX, leftConst, rightX, rightConst)
    }

    /**
     * Desglosa un lado de la ecuación (ej. "3x-4+2x" o "-x+5") sumando los coeficientes de x
     * y los términos constantes independientes.
     */
    private fun parseSide(side: String): Pair<Int, Int> {
        var xCount = 0
        var constCount = 0

        // Normalizamos los signos negativos para separar términos conservando el signo
        val normalized = side.replace("-", "+-")
        val terms = normalized.split("+").filter { it.isNotEmpty() }

        if (terms.isEmpty()) {
            return Pair(0, 0)
        }

        for (rawTerm in terms) {
            val term = rawTerm.trim()
            if (term.isEmpty()) continue

            if (term.contains("x")) {
                val coeffStr = term.replace("x", "")
                val coeff = when (coeffStr) {
                    "", "+" -> 1
                    "-" -> -1
                    else -> coeffStr.toIntOrNull() ?: throw IllegalArgumentException("invalid_equation")
                }
                xCount += coeff
            } else {
                val value = term.toIntOrNull() ?: throw IllegalArgumentException("invalid_equation")
                constCount += value
            }
        }
        return Pair(xCount, constCount)
    }
}
