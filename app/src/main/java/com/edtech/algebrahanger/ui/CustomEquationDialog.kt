package com.edtech.algebrahanger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.edtech.algebrahanger.models.Equation
import com.edtech.algebrahanger.utils.EquationParser
import com.example.R

@Composable
fun CustomEquationDialog(
    onDismissRequest: () -> Unit,
    onEquationCreated: (Equation) -> Unit
) {
    var formulaText by remember { mutableStateOf("2x + 3 = 11") }
    var errorMessageResId by remember { mutableStateOf<Int?>(null) }

    fun appendChar(char: String) {
        errorMessageResId = null
        // Evitar dobles espacios o signos repetidos ilógicos
        formulaText = if (formulaText.isEmpty() && char in listOf("+", "*", "/")) {
            formulaText
        } else {
            formulaText + char
        }
    }

    fun deleteLast() {
        errorMessageResId = null
        if (formulaText.isNotEmpty()) {
            formulaText = formulaText.dropLast(1).trimEnd()
        }
    }

    fun clearAll() {
        errorMessageResId = null
        formulaText = ""
    }

    fun trySubmit() {
        try {
            val parsed = EquationParser.parse(formulaText)
            onEquationCreated(parsed)
            onDismissRequest()
        } catch (e: IllegalArgumentException) {
            errorMessageResId = when (e.message) {
                "infinite_solutions" -> R.string.infinite_solutions
                "no_solution" -> R.string.no_solution
                else -> R.string.invalid_equation
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("custom_equation_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = stringResource(R.string.custom_equation_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pantalla de Previsualización de la Ecuación
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = if (errorMessageResId != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formulaText.ifEmpty { stringResource(R.string.custom_equation_preview, "...") },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (formulaText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Mensaje de Error / Retroalimentación Pedagógica
                if (errorMessageResId != null) {
                    Text(
                        text = stringResource(errorMessageResId!!),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        text = "Ejemplos: 3x + 2 = 14  |  2x - 3 = x + 5  |  4x = 12",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TECLADO ALGEBRAICO Y NUMÉRICO INTEGRADO
                // Diseñado para tabletas y smartphones en Landscape sin necesidad de teclado virtual nativo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fila 1: [ 7 ] [ 8 ] [ 9 ] [ + ] [ x ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("7", Modifier.weight(1f)) { appendChar("7") }
                        KeypadButton("8", Modifier.weight(1f)) { appendChar("8") }
                        KeypadButton("9", Modifier.weight(1f)) { appendChar("9") }
                        KeypadButton("+", Modifier.weight(1f), isOperator = true) { appendChar(" + ") }
                        KeypadButton("x", Modifier.weight(1f), isVariable = true) { appendChar("x") }
                    }

                    // Fila 2: [ 4 ] [ 5 ] [ 6 ] [ - ] [ = ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("4", Modifier.weight(1f)) { appendChar("4") }
                        KeypadButton("5", Modifier.weight(1f)) { appendChar("5") }
                        KeypadButton("6", Modifier.weight(1f)) { appendChar("6") }
                        KeypadButton("-", Modifier.weight(1f), isOperator = true) { appendChar(" - ") }
                        KeypadButton("=", Modifier.weight(1f), isEquals = true) { appendChar(" = ") }
                    }

                    // Fila 3: [ 1 ] [ 2 ] [ 3 ] [ 0 ] [ ⌫ ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadButton("1", Modifier.weight(1f)) { appendChar("1") }
                        KeypadButton("2", Modifier.weight(1f)) { appendChar("2") }
                        KeypadButton("3", Modifier.weight(1f)) { appendChar("3") }
                        KeypadButton("0", Modifier.weight(1f)) { appendChar("0") }
                        Button(
                            onClick = { deleteLast() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("key_backspace"),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.delete_char),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de Acción (Limpiar, Cancelar, Crear)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { clearAll() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("clear_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.clear))
                    }

                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("cancel_button")
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = { trySubmit() },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("create_equation_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.create), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false,
    isVariable: Boolean = false,
    isEquals: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        isVariable -> Color(0xFF2563EB)
        isEquals -> Color(0xFF10B981)
        isOperator -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isVariable -> Color.White
        isEquals -> Color.White
        isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(46.dp)
            .testTag("key_$label"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            fontSize = if (isVariable || isOperator || isEquals) 20.sp else 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
