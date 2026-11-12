package com.example

import com.edtech.algebrahanger.models.Equation
import com.edtech.algebrahanger.utils.EquationParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun parseSimpleEquation() {
        val eq = EquationParser.parse("3x - 4 = 2x + 5")
        assertEquals(3, eq.leftX)
        assertEquals(-4, eq.leftConst)
        assertEquals(2, eq.rightX)
        assertEquals(5, eq.rightConst)
    }

    @Test
    fun parseEquationWithDefaultCoefficients() {
        val eq = EquationParser.parse("x + 2 = 7")
        assertEquals(1, eq.leftX)
        assertEquals(2, eq.leftConst)
        assertEquals(0, eq.rightX)
        assertEquals(7, eq.rightConst)
    }

    @Test
    fun parseInfiniteSolutionsThrows() {
        try {
            EquationParser.parse("2x + 4 = 2x + 4")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("infinite_solutions", e.message)
        }
    }

    @Test
    fun parseNoSolutionThrows() {
        try {
            EquationParser.parse("2x + 4 = 2x + 9")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("no_solution", e.message)
        }
    }

    @Test
    fun solutionCalculation() {
        val eq = Equation(leftX = 3, leftConst = 2, rightX = 1, rightConst = 8)
        // 3x + 2 = x + 8  =>  2x = 6  =>  x = 3
        val sol = eq.calculateSolution()
        assertNotNull(sol)
        assertEquals(3.0f, sol!!, 0.001f)
    }
}
