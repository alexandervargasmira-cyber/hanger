// HangerCanvas.kt
package com.edtech.algebrahanger.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.edtech.algebrahanger.models.Equation
import com.edtech.algebrahanger.viewmodel.HangerViewModel

@Composable
fun HangerCanvas(equation: Equation, viewModel: HangerViewModel) {
    // Calculamos el valor real de X para simular el balance
    val trueXValue = if (equation.leftX != equation.rightX) {
        (equation.rightConst - equation.leftConst).toFloat() / (equation.leftX - equation.rightX)
    } else 1f

    val targetTilt = viewModel.getTiltAngle(equation, trueXValue)
    
    // Animación de físicas (Spring)
    val tiltAngle by animateFloatAsState(
        targetValue = targetTilt,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "hangerTilt"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerW = size.width / 2
        val centerH = size.height / 3
        val barWidth = size.width * 0.8f

        // Dibujar Fulcro (Triángulo central)
        drawCircle(color = Color.DarkGray, radius = 15f, center = Offset(centerW, centerH))

        // Rotar el lienzo basado en el desequilibrio
        rotate(degrees = tiltAngle, pivot = Offset(centerW, centerH)) {
            // Barra Principal
            drawLine(
                color = Color.Black,
                start = Offset(centerW - barWidth / 2, centerH),
                end = Offset(centerW + barWidth / 2, centerH),
                strokeWidth = 10f
            )

            // Lógica de dibujado de Pesas (Positivos, hacia abajo) y Globos (Negativos, hacia arriba)
            // Aquí se dibujarían los elementos (x y constantes) usando drawCircle / drawRect
            // Posición Izquierda: Offset(centerW - barWidth/2, centerH)
            // Posición Derecha: Offset(centerW + barWidth/2, centerH)
        }
    }
}