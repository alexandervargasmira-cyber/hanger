package com.edtech.algebrahanger.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edtech.algebrahanger.models.Equation
import com.edtech.algebrahanger.models.HangerSide
import com.edtech.algebrahanger.models.HangerState
import com.edtech.algebrahanger.models.ItemType
import com.edtech.algebrahanger.viewmodel.HangerViewModel
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HangerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showCustomEquationDialog by remember { mutableStateOf(false) }
    var showLevelMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Balance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Selector de Nivel
                    Box {
                        FilledTonalButton(
                            onClick = { showLevelMenu = true },
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("level_selector_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.currentLevel > 0) {
                                    "${stringResource(R.string.level_1).substringBefore(":")} ${state.currentLevel} (${state.currentEquationIndex + 1}/10)"
                                } else {
                                    stringResource(R.string.custom_equation)
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        DropdownMenu(
                            expanded = showLevelMenu,
                            onDismissRequest = { showLevelMenu = false }
                        ) {
                            val levelNames = listOf(
                                1 to R.string.level_1,
                                2 to R.string.level_2,
                                3 to R.string.level_3,
                                4 to R.string.level_4,
                                5 to R.string.level_5
                            )
                            levelNames.forEach { (lvl, resId) ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (state.currentLevel == lvl) MaterialTheme.colorScheme.primary else Color.Transparent
                                        )
                                    },
                                    text = { Text(stringResource(resId)) },
                                    onClick = {
                                        viewModel.loadLevel(lvl, 0)
                                        showLevelMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Botón Ecuación Personalizada
                    Button(
                        onClick = { showCustomEquationDialog = true },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("open_custom_equation_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.custom_equation), style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Botón Reiniciar Ejercicio
                    IconButton(
                        onClick = { viewModel.resetCurrentEquation() },
                        modifier = Modifier.testTag("reset_equation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.reset_equation),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Selector de Idioma en Tiempo Real
                    LanguageSelector()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // PANEL IZQUIERDO: 60% - Balanza / Colgador Interactivo con Físicas
            Column(
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, top = 8.dp, end = 6.dp, bottom = 8.dp)
            ) {
                // Barra de Estado de Balance
                BalanceStatusBar(state = state, viewModel = viewModel)

                // Lienzo Interactivo del Colgador
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    HangerCanvas(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Leyenda Visual en la esquina superior izquierda
                    VisualLegend(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra Didáctica Inferior (Operaciones y Pares Cero)
                InteractiveActionTray(state = state, viewModel = viewModel)
            }

            // PANEL DERECHO: 40% - Representación Algebraica, Historial y Solución
            Column(
                modifier = Modifier
                    .weight(0.40f)
                    .fillMaxHeight()
                    .padding(start = 6.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tarjeta de la Ecuación Algebraica Actual
                EquationDisplayCard(state = state, viewModel = viewModel)

                // Tarjeta de Historial de Pasos Formales
                StepHistoryCard(
                    history = state.history,
                    modifier = Modifier.weight(1f)
                )

                // Notificación / Tarjeta de Éxito cuando se resuelve
                AnimatedVisibility(
                    visible = state.isSolved,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SolvedCelebrationCard(
                        equation = state.equation,
                        onNext = { viewModel.nextEquation() }
                    )
                }
            }
        }
    }

    if (showCustomEquationDialog) {
        CustomEquationDialog(
            onDismissRequest = { showCustomEquationDialog = false },
            onEquationCreated = { newEquation ->
                viewModel.loadCustomEquation(newEquation)
            }
        )
    }
}

/**
 * Muestra el estado físico de la balanza:
 * Equilibrado (verde) o Desequilibrado (ámbar/rojo con botón para balancear el otro lado).
 */
@Composable
private fun BalanceStatusBar(
    state: HangerState,
    viewModel: HangerViewModel
) {
    val isTilted = state.pendingImbalance != null

    Surface(
        color = if (isTilted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isTilted) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isTilted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isTilted) {
                        stringResource(R.string.status_tilted)
                    } else {
                        stringResource(R.string.status_balanced)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isTilted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (isTilted) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = { viewModel.cancelPendingImbalance() },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelSmall)
                    }
                    FilledTonalButton(
                        onClick = { viewModel.balanceOppositeSide() },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Equilibrar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

/**
 * Leyenda visual compacta que explica el significado didáctico de cada figura.
 */
@Composable
private fun VisualLegend(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(shape = "square", color = Color(0xFF2563EB), label = "+x")
            LegendItem(shape = "square", color = Color(0xFFEF4444), label = "-x")
            LegendItem(shape = "circle", color = Color(0xFFF59E0B), label = "+1")
            LegendItem(shape = "circle", color = Color(0xFF8B5CF6), label = "-1")
        }
    }
}

@Composable
private fun LegendItem(shape: String, color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = color,
                    shape = if (shape == "square") RoundedCornerShape(2.dp) else CircleShape
                )
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

/**
 * Bandeja de acciones interactivas:
 * - Botón para Dividir en Grupos (cuando la variable está aislada con un coeficiente > 1)
 * - Botones para añadir opuestos a ambos lados para cancelar globos negativos con pares cero.
 */
@Composable
private fun InteractiveActionTray(
    state: HangerState,
    viewModel: HangerViewModel
) {
    val eq = state.equation
    val canDivideLeft = eq.leftX > 1 && eq.leftConst == 0 && eq.rightX == 0 && eq.rightConst % eq.leftX == 0
    val canDivideRight = eq.rightX > 1 && eq.rightConst == 0 && eq.leftX == 0 && eq.leftConst % eq.rightX == 0

    val hasNegativeConstants = state.leftItems.any { it.type == ItemType.CONSTANT_1 && it.isNegative } ||
            state.rightItems.any { it.type == ItemType.CONSTANT_1 && it.isNegative }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Consejo pedagógico
            Text(
                text = if (hasNegativeConstants) {
                    stringResource(R.string.tip_zero_pair)
                } else {
                    stringResource(R.string.tip_balance)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de División en Grupos Iguales
            if (canDivideLeft) {
                Button(
                    onClick = { viewModel.divideGroups(eq.leftX) },
                    modifier = Modifier.testTag("divide_groups_button")
                ) {
                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.divide_groups, eq.leftX))
                }
            } else if (canDivideRight) {
                Button(
                    onClick = { viewModel.divideGroups(eq.rightX) },
                    modifier = Modifier.testTag("divide_groups_button")
                ) {
                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.divide_groups, eq.rightX))
                }
            } else if (hasNegativeConstants) {
                // Ayuda para crear Pares Cero: sumar +1 a ambos lados
                FilledTonalButton(
                    onClick = { viewModel.addOppositesToBothSides(ItemType.CONSTANT_1, addNegative = false) },
                    modifier = Modifier.testTag("add_opposites_button")
                ) {
                    Text("+1 en ambos lados", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Tarjeta de la ecuación matemática con controles de navegación.
 */
@Composable
private fun EquationDisplayCard(
    state: HangerState,
    viewModel: HangerViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.current_equation),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                // Navegación rápida entre ecuaciones
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { viewModel.prevEquation() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("prev_equation_button"),
                        enabled = state.currentLevel > 0
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.prev_equation),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextEquation() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("next_equation_button"),
                        enabled = state.currentLevel > 0
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.next_equation),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ecuación en formato algebraico destacado
            Text(
                text = state.equation.toDisplayString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * Historial de pasos didácticos. Mapea claves internas (ej. history_sub:2) a textos localizados.
 */
@Composable
private fun StepHistoryCard(
    history: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.step_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(history) { index, rawStep ->
                    val localizedText = formatHistoryStep(rawStep)
                    val isFirst = (index == 0)
                    val isEquation = rawStep.contains("=") && !rawStep.contains(":")

                    Surface(
                        color = if (isEquation) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = localizedText,
                                style = if (isEquation) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Traduce cadenas como "history_sub:2" a "Restar 2 en ambos lados".
 */
@Composable
private fun formatHistoryStep(raw: String): String {
    return when {
        raw.startsWith("history_sub:") -> {
            val amount = raw.substringAfter("history_sub:").toIntOrNull() ?: 1
            stringResource(R.string.history_sub, amount)
        }
        raw.startsWith("history_add:") -> {
            val amount = raw.substringAfter("history_add:").toIntOrNull() ?: 1
            stringResource(R.string.history_add, amount)
        }
        raw.startsWith("history_div:") -> {
            val divisor = raw.substringAfter("history_div:").toIntOrNull() ?: 1
            stringResource(R.string.history_div, divisor)
        }
        raw.startsWith("history_sub_x:") -> {
            val count = raw.substringAfter("history_sub_x:").toIntOrNull() ?: 1
            stringResource(R.string.history_sub_x, count)
        }
        raw.startsWith("history_zero_pair:") -> {
            val sideKey = raw.substringAfter("history_zero_pair:")
            val sideName = if (sideKey == "side_left") stringResource(R.string.side_left) else stringResource(R.string.side_right)
            stringResource(R.string.history_zero_pair, sideName)
        }
        else -> raw
    }
}

/**
 * Tarjeta de celebración y éxito cuando la incógnita queda aislada.
 */
@Composable
private fun SolvedCelebrationCard(
    equation: Equation,
    onNext: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White)
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.solved_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val value = if (equation.leftX == 1) equation.rightConst else equation.leftConst
                    Text(
                        text = stringResource(R.string.solved_solution, value.toString()),
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF047857)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(stringResource(R.string.next_equation), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Selector de Idioma con soporte de Per-App Language Preferences (Android 13+ y AppCompat).
 * Cambia el idioma en tiempo real sin reiniciar ni perder el estado del ViewModel.
 */
@Composable
fun LanguageSelector() {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("language_selector_button")
        ) {
            Text("🌐")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val languages = listOf(
                "en" to "English",
                "es" to "Español",
                "ru" to "Русский",
                "zh" to "中文"
            )
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                        expanded = false
                    }
                )
            }
        }
    }
}
