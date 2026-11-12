// HangerViewModel.kt
package com.edtech.algebrahanger.viewmodel

import androidx.lifecycle.ViewModel
import com.edtech.algebrahanger.models.Equation
import com.edtech.algebrahanger.models.HangerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HangerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HangerState(Equation()))
    val uiState: StateFlow<HangerState> = _uiState.asStateFlow()

    // Flujo para calcular la inclinación en base a la diferencia de pesos
    // Asumimos que la "x" tiene un peso dinámico equivalente a la solución para equilibrar el canvas físico
    fun getTiltAngle(eq: Equation, xValue: Float): Float {
        val leftWeight = eq.leftX * xValue + eq.leftConst
        val rightWeight = eq.rightX * xValue + eq.rightConst
        return (leftWeight - rightWeight) * 5f // Factor de inclinación
    }

    fun loadEquation(equation: Equation) {
        _uiState.value = HangerState(equation = equation)
    }

    fun removeConstant(amount: Int) {
        _uiState.update { state ->
            val newEq = state.equation.copy(
                leftConst = state.equation.leftConst - amount,
                rightConst = state.equation.rightConst - amount
            )
            state.copy(
                equation = newEq,
                history = state.history + "history_sub:$amount"
            )
        }
        checkSolved()
    }

    fun resolveZeroPairs(isLeft: Boolean) {
        _uiState.update { state ->
            val eq = state.equation
            val newEq = if (isLeft) {
                // Lógica simplificada: si hay positivos y negativos, se cancelan mutuamente
                // Implementación compleja requeriría modelos separados para globos y pesas
                eq 
            } else eq
            state.copy(equation = newEq)
        }
    }

    fun divideGroups(divisor: Int) {
        _uiState.update { state ->
            val eq = state.equation
            if (eq.leftX % divisor == 0 && eq.rightConst % divisor == 0) {
                state.copy(
                    equation = eq.copy(leftX = eq.leftX / divisor, rightConst = eq.rightConst / divisor),
                    history = state.history + "history_div:$divisor"
                )
            } else state
        }
        checkSolved()
    }

    private fun checkSolved() {
        val eq = _uiState.value.equation
        if (eq.leftX == 1 && eq.leftConst == 0 && eq.rightX == 0) {
            _uiState.update { it.copy(isSolved = true) }
        }
    }
}