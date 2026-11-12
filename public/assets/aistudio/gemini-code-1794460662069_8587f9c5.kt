// MainScreen.kt
package com.edtech.algebrahanger.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edtech.algebrahanger.viewmodel.HangerViewModel

@Composable
fun MainScreen(viewModel: HangerViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = { LanguageSelector() }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Panel Izquierdo: 60% (Colgador Interactivo)
            Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                HangerCanvas(equation = state.equation, viewModel = viewModel)
                
                // Bandeja Inferior (Acciones)
                if (state.equation.leftX > 0 && state.equation.leftConst == 0 && state.equation.rightX == 0) {
                    Button(
                        onClick = { viewModel.divideGroups(state.equation.leftX) },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    ) {
                        Text(stringResource(id = R.string.divide_groups, state.equation.leftX))
                    }
                }
            }

            // Panel Derecho: 40% (Ecuación e Historial)
            Column(
                modifier = Modifier.weight(0.4f).fillMaxHeight().padding(16.dp)
            ) {
                Text("Equación Actual:", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${state.equation.leftX}x + ${state.equation.leftConst} = ${state.equation.rightX}x + ${state.equation.rightConst}",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Historial de Pasos:", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(state.history) { step ->
                        Text("• $step", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelector() {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        TextButton(onClick = { expanded = true }) { Text("🌐 Idioma") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val languages = listOf("en" to "English", "es" to "Español", "ru" to "Русский", "zh" to "中文")
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