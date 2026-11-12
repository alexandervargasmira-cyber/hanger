package com.edtech.algebrahanger.viewmodel

import androidx.lifecycle.ViewModel
import com.edtech.algebrahanger.data.EquationRepository
import com.edtech.algebrahanger.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class HangerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HangerState(Equation()))
    val uiState: StateFlow<HangerState> = _uiState.asStateFlow()

    private var initialEquationForReset: Equation = Equation(1, 2, 0, 5)

    init {
        loadLevel(1, 0)
    }

    /**
     * Carga un nivel específico y el índice de ecuación (de 0 a 9).
     */
    fun loadLevel(level: Int, equationIndex: Int = 0) {
        val eq = EquationRepository.getEquation(level, equationIndex)
        initialEquationForReset = eq
        setupEquationState(eq, level = level, equationIndex = equationIndex)
    }

    /**
     * Carga una ecuación personalizada ingresada por el usuario.
     */
    fun loadCustomEquation(eq: Equation) {
        initialEquationForReset = eq
        setupEquationState(eq, level = 0, equationIndex = 0)
    }

    /**
     * Reinicia la ecuación actual a su estado original sin perder el progreso del nivel.
     */
    fun resetCurrentEquation() {
        setupEquationState(
            initialEquationForReset,
            level = _uiState.value.currentLevel,
            equationIndex = _uiState.value.currentEquationIndex
        )
    }

    fun nextEquation() {
        val currentLevel = _uiState.value.currentLevel
        val currentIndex = _uiState.value.currentEquationIndex
        if (currentLevel in 1..5) {
            val totalInLevel = EquationRepository.getTotalEquations(currentLevel)
            if (currentIndex + 1 < totalInLevel) {
                loadLevel(currentLevel, currentIndex + 1)
            } else if (currentLevel < 5) {
                loadLevel(currentLevel + 1, 0)
            }
        }
    }

    fun prevEquation() {
        val currentLevel = _uiState.value.currentLevel
        val currentIndex = _uiState.value.currentEquationIndex
        if (currentLevel in 1..5) {
            if (currentIndex > 0) {
                loadLevel(currentLevel, currentIndex - 1)
            } else if (currentLevel > 1) {
                val prevLevel = currentLevel - 1
                val lastIndex = EquationRepository.getTotalEquations(prevLevel) - 1
                loadLevel(prevLevel, lastIndex)
            }
        }
    }

    /**
     * Inicializa los elementos físicos discretos (pesos y globos) en ambos lados del colgador.
     */
    private fun setupEquationState(eq: Equation, level: Int, equationIndex: Int) {
        val leftItems = generatePhysicalItems(eq.leftX, eq.leftConst, HangerSide.LEFT)
        val rightItems = generatePhysicalItems(eq.rightX, eq.rightConst, HangerSide.RIGHT)

        _uiState.value = HangerState(
            equation = eq,
            leftItems = leftItems,
            rightItems = rightItems,
            pendingImbalance = null,
            history = listOf(eq.toDisplayString()),
            isSolved = checkIsSolved(eq),
            currentLevel = level,
            currentEquationIndex = equationIndex,
            statusFeedback = null
        )
    }

    /**
     * Desglosa algebraicamente las incógnitas y constantes en elementos individuales:
     * - Positivos: Pesas que cuelgan hacia abajo (isNegative = false)
     * - Negativos: Globos que tiran hacia arriba (isNegative = true)
     */
    private fun generatePhysicalItems(xCount: Int, constCount: Int, side: HangerSide): List<HangerItem> {
        val items = mutableListOf<HangerItem>()
        val sidePrefix = if (side == HangerSide.LEFT) "L" else "R"

        // Generar variables x
        val absX = abs(xCount)
        val isXNeg = xCount < 0
        for (i in 0 until absX) {
            items.add(
                HangerItem(
                    id = "${sidePrefix}_X_${if (isXNeg) "neg" else "pos"}_$i",
                    side = side,
                    type = ItemType.VARIABLE_X,
                    isNegative = isXNeg,
                    index = i
                )
            )
        }

        // Generar constantes 1
        val absC = abs(constCount)
        val isCNeg = constCount < 0
        for (i in 0 until absC) {
            items.add(
                HangerItem(
                    id = "${sidePrefix}_C_${if (isCNeg) "neg" else "pos"}_$i",
                    side = side,
                    type = ItemType.CONSTANT_1,
                    isNegative = isCNeg,
                    index = i
                )
            )
        }

        return items
    }

    /**
     * FÍSICA DIDÁCTICA DEL COLGADOR:
     * Calcula el ángulo de inclinación (-24° a +24°) en tiempo real.
     * La masa de 'x' equivale a la solución real de la ecuación.
     * Las pesas empujan hacia abajo (+), los globos tiran hacia arriba (-).
     */
    fun getTiltAngle(state: HangerState): Float {
        val solution = initialEquationForReset.calculateSolution() ?: 1f
        // Si x es negativo o 0 en el ejercicio, asignamos peso unitario positivo para la física de visualización
        val physicalXWeight = if (solution > 0f) solution else 2f

        var leftTorque = 0f
        for (item in state.leftItems) {
            val weight = if (item.type == ItemType.VARIABLE_X) physicalXWeight else 1f
            leftTorque += if (item.isNegative) -weight else weight
        }

        var rightTorque = 0f
        for (item in state.rightItems) {
            val weight = if (item.type == ItemType.VARIABLE_X) physicalXWeight else 1f
            rightTorque += if (item.isNegative) -weight else weight
        }

        val netDifference = leftTorque - rightTorque
        // Factor de inclinación con límite suave para una visualización natural
        val rawAngle = netDifference * 4.5f
        return rawAngle.coerceIn(-24f, 24f)
    }

    /**
     * MECÁNICA 1: PARES CERO (ZERO PAIRS)
     * Cuando un globo (-1 o -x) choca con una pesa (+1 o +x) en el mismo lado,
     * ambos se anulan mutuamente (materia y antimateria didáctica).
     */
    fun resolveZeroPair(side: HangerSide, item1Id: String, item2Id: String) {
        _uiState.update { state ->
            val list = if (side == HangerSide.LEFT) state.leftItems else state.rightItems
            val item1 = list.find { it.id == item1Id }
            val item2 = list.find { it.id == item2Id }

            if (item1 != null && item2 != null &&
                item1.type == item2.type &&
                item1.isNegative != item2.isNegative
            ) {
                // Son opuestos legítimos del mismo tipo en el mismo lado: aniquilar
                val updatedList = list.filter { it.id != item1Id && it.id != item2Id }
                val newEquation = recalculateNetEquation(
                    if (side == HangerSide.LEFT) updatedList else state.leftItems,
                    if (side == HangerSide.RIGHT) updatedList else state.rightItems
                )

                val sideNameKey = if (side == HangerSide.LEFT) "side_left" else "side_right"
                val newHistory = state.history + "history_zero_pair:$sideNameKey"

                val updatedState = state.copy(
                    leftItems = if (side == HangerSide.LEFT) updatedList else state.leftItems,
                    rightItems = if (side == HangerSide.RIGHT) updatedList else state.rightItems,
                    equation = newEquation,
                    history = newHistory,
                    zeroPairCountCelebrated = state.zeroPairCountCelebrated + 1,
                    statusFeedback = "zero_pair_annihilated"
                )
                checkSolved(updatedState)
            } else {
                state
            }
        }
    }

    /**
     * MECÁNICA 2: RETIRO TÁCTIL Y EQUILIBRIO DE OPERACIONES
     * Si el estudiante retira un elemento de un lado:
     * - Si la balanza estaba equilibrada, se inclina y queda pendiente el retiro equivalente.
     * - Si ya había un retiro pendiente en el lado opuesto y este elemento coincide,
     *   la balanza recupera el equilibrio perfecto y registra el paso formal.
     */
    fun onItemRemoved(item: HangerItem) {
        _uiState.update { state ->
            val currentPending = state.pendingImbalance

            if (currentPending == null) {
                // Inicia desequilibrio didáctico
                val newLeft = if (item.side == HangerSide.LEFT) state.leftItems - item else state.leftItems
                val newRight = if (item.side == HangerSide.RIGHT) state.rightItems - item else state.rightItems
                val newEq = recalculateNetEquation(newLeft, newRight)

                state.copy(
                    leftItems = newLeft,
                    rightItems = newRight,
                    equation = newEq,
                    pendingImbalance = PendingImbalance(
                        removedItemType = item.type,
                        isNegative = item.isNegative,
                        originSide = item.side
                    ),
                    statusFeedback = "unbalanced_remove_opposite"
                )
            } else if (currentPending.originSide != item.side &&
                currentPending.removedItemType == item.type &&
                currentPending.isNegative == item.isNegative
            ) {
                // Se completó la operación simétrica en ambos lados! Equilibrio restaurado
                val newLeft = if (item.side == HangerSide.LEFT) state.leftItems - item else state.leftItems
                val newRight = if (item.side == HangerSide.RIGHT) state.rightItems - item else state.rightItems
                val newEq = recalculateNetEquation(newLeft, newRight)

                val stepHistory = when {
                    item.type == ItemType.VARIABLE_X && !item.isNegative -> "history_sub_x:1"
                    item.type == ItemType.CONSTANT_1 && !item.isNegative -> "history_sub:1"
                    item.type == ItemType.CONSTANT_1 && item.isNegative -> "history_add:1"
                    else -> "history_sub:1"
                }

                val updated = state.copy(
                    leftItems = newLeft,
                    rightItems = newRight,
                    equation = newEq,
                    pendingImbalance = null,
                    history = state.history + stepHistory + newEq.toDisplayString(),
                    statusFeedback = "balanced_restored"
                )
                checkSolved(updated)
            } else {
                // El elemento no corresponde al balance esperado
                state
            }
        }
    }

    /**
     * Cancela o deshace un desequilibrio pendiente restaurando el elemento al lado original.
     */
    fun cancelPendingImbalance() {
        _uiState.update { state ->
            val pending = state.pendingImbalance ?: return@update state
            val restoredItem = HangerItem(
                id = "restored_${System.currentTimeMillis()}",
                side = pending.originSide,
                type = pending.removedItemType,
                isNegative = pending.isNegative
            )
            val newLeft = if (pending.originSide == HangerSide.LEFT) state.leftItems + restoredItem else state.leftItems
            val newRight = if (pending.originSide == HangerSide.RIGHT) state.rightItems + restoredItem else state.rightItems
            val newEq = recalculateNetEquation(newLeft, newRight)

            state.copy(
                leftItems = newLeft,
                rightItems = newRight,
                equation = newEq,
                pendingImbalance = null,
                statusFeedback = null
            )
        }
    }

    /**
     * Equilibra automáticamente el lado opuesto si el estudiante solicita igualar la operación.
     */
    fun balanceOppositeSide() {
        val pending = _uiState.value.pendingImbalance ?: return
        val targetSide = if (pending.originSide == HangerSide.LEFT) HangerSide.RIGHT else HangerSide.LEFT
        val targetList = if (targetSide == HangerSide.LEFT) _uiState.value.leftItems else _uiState.value.rightItems

        // Buscar un elemento coincidente en el lado destino
        val match = targetList.find { it.type == pending.removedItemType && it.isNegative == pending.isNegative }
        if (match != null) {
            onItemRemoved(match)
        }
    }

    /**
     * ACCIÓN DIDÁCTICA: Sumar opuestos a ambos lados para crear pares cero.
     * Ejemplo: Si hay -1 (globos), sumar +1 (pesas) a ambos lados para cancelar.
     */
    fun addOppositesToBothSides(type: ItemType, addNegative: Boolean) {
        _uiState.update { state ->
            val leftNew = HangerItem(
                id = "L_added_${System.currentTimeMillis()}",
                side = HangerSide.LEFT,
                type = type,
                isNegative = addNegative
            )
            val rightNew = HangerItem(
                id = "R_added_${System.currentTimeMillis() + 1}",
                side = HangerSide.RIGHT,
                type = type,
                isNegative = addNegative
            )

            val updatedLeft = state.leftItems + leftNew
            val updatedRight = state.rightItems + rightNew
            val newEq = recalculateNetEquation(updatedLeft, updatedRight)

            val stepText = if (type == ItemType.CONSTANT_1) {
                if (addNegative) "history_sub:1" else "history_add:1"
            } else {
                "history_sub_x:1"
            }

            state.copy(
                leftItems = updatedLeft,
                rightItems = updatedRight,
                equation = newEq,
                history = state.history + stepText + newEq.toDisplayString()
            )
        }
    }

    /**
     * MECÁNICA 3: DIVIDIR EN GRUPOS IGUALES
     * Cuando la ecuación tiene 'Nx = C' (o 'C = Nx') sin constantes aditivas,
     * permite dividir ambos lados por N para aislar una sola x.
     */
    fun divideGroups(divisor: Int) {
        if (divisor <= 0) return
        _uiState.update { state ->
            val eq = state.equation
            val isStandardLeft = eq.leftX > 0 && eq.leftConst == 0 && eq.rightX == 0 && eq.rightConst % divisor == 0
            val isStandardRight = eq.rightX > 0 && eq.rightConst == 0 && eq.leftX == 0 && eq.leftConst % divisor == 0

            if (isStandardLeft || isStandardRight) {
                val newEq = if (isStandardLeft) {
                    eq.copy(leftX = eq.leftX / divisor, rightConst = eq.rightConst / divisor)
                } else {
                    eq.copy(rightX = eq.rightX / divisor, leftConst = eq.leftConst / divisor)
                }

                val newLeft = generatePhysicalItems(newEq.leftX, newEq.leftConst, HangerSide.LEFT)
                val newRight = generatePhysicalItems(newEq.rightX, newEq.rightConst, HangerSide.RIGHT)

                val updatedState = state.copy(
                    equation = newEq,
                    leftItems = newLeft,
                    rightItems = newRight,
                    history = state.history + "history_div:$divisor" + newEq.toDisplayString(),
                    pendingImbalance = null
                )
                checkSolved(updatedState)
            } else {
                state
            }
        }
    }

    /**
     * Recalcula los totales algebraicos netos a partir de la lista de elementos en cada lado.
     */
    private fun recalculateNetEquation(left: List<HangerItem>, right: List<HangerItem>): Equation {
        var lx = 0
        var lc = 0
        for (item in left) {
            val sign = if (item.isNegative) -1 else 1
            if (item.type == ItemType.VARIABLE_X) lx += sign else lc += sign
        }

        var rx = 0
        var rc = 0
        for (item in right) {
            val sign = if (item.isNegative) -1 else 1
            if (item.type == ItemType.VARIABLE_X) rx += sign else rc += sign
        }

        return Equation(leftX = lx, leftConst = lc, rightX = rx, rightConst = rc)
    }

    private fun checkSolved(state: HangerState): HangerState {
        val solved = checkIsSolved(state.equation)
        return state.copy(isSolved = solved)
    }

    private fun checkIsSolved(eq: Equation): Boolean {
        return (eq.leftX == 1 && eq.leftConst == 0 && eq.rightX == 0) ||
                (eq.rightX == 1 && eq.rightConst == 0 && eq.leftX == 0)
    }
}
