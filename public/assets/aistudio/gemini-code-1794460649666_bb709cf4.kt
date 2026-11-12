// EquationRepository.kt
package com.edtech.algebrahanger.data

import com.edtech.algebrahanger.models.Equation

object EquationRepository {
    // 50 Ecuaciones distribuidas en 5 niveles (10 por nivel)
    val levels = mapOf(
        1 to listOf( // Nivel 1: One-Step (+) ej. x + 2 = 5
            Equation(1, 2, 0, 5), Equation(1, 4, 0, 10), Equation(1, 1, 0, 7),
            Equation(1, 5, 0, 9), Equation(1, 3, 0, 8), Equation(0, 6, 1, 2),
            Equation(1, 7, 0, 12), Equation(1, 8, 0, 15), Equation(0, 10, 1, 4), Equation(1, 0, 0, 3)
        ),
        2 to listOf( // Nivel 2: One-Step (* /) ej. 2x = 6
            Equation(2, 0, 0, 6), Equation(3, 0, 0, 12), Equation(4, 0, 0, 8),
            Equation(5, 0, 0, 20), Equation(2, 0, 0, 14), Equation(6, 0, 0, 18),
            Equation(3, 0, 0, 15), Equation(4, 0, 0, 16), Equation(7, 0, 0, 21), Equation(8, 0, 0, 24)
        ),
        3 to listOf( // Nivel 3: Two-Step ej. 2x + 1 = 5
            Equation(2, 1, 0, 5), Equation(3, -2, 0, 7), Equation(2, 4, 0, 10),
            Equation(4, -1, 0, 11), Equation(3, 2, 0, 14), Equation(5, -3, 0, 12),
            Equation(2, -5, 0, 9), Equation(4, 3, 0, 15), Equation(3, -4, 0, 11), Equation(6, 1, 0, 13)
        ),
        4 to listOf( // Nivel 4: Variables Both Sides (+) ej. 3x + 2 = x + 8
            Equation(3, 2, 1, 8), Equation(4, 1, 2, 7), Equation(5, 3, 2, 12),
            Equation(2, 5, 1, 9), Equation(4, 4, 3, 10), Equation(6, 2, 2, 14),
            Equation(3, 7, 1, 13), Equation(5, 1, 3, 11), Equation(4, 6, 2, 12), Equation(7, 2, 4, 17)
        ),
        5 to listOf( // Nivel 5: Multi-Step (+ y -) ej. 2x - 3 = -x + 6
            Equation(2, -3, -1, 6), Equation(3, -5, 1, -1), Equation(4, -2, -2, 10),
            Equation(-1, 8, 2, -1), Equation(-2, 10, -4, 6), Equation(3, -7, -2, 8),
            Equation(5, -4, 2, 5), Equation(-3, 12, 1, -4), Equation(4, -6, -3, 8), Equation(-2, 5, -5, -4)
        )
    )
}