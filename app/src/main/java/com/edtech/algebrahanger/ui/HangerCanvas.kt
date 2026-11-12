package com.edtech.algebrahanger.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.edtech.algebrahanger.models.HangerItem
import com.edtech.algebrahanger.models.HangerSide
import com.edtech.algebrahanger.models.HangerState
import com.edtech.algebrahanger.models.ItemType
import com.edtech.algebrahanger.viewmodel.HangerViewModel
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Estructura de posición en pantalla para un elemento físico sobre el lienzo.
 */
data class ItemScreenPos(
    val item: HangerItem,
    val center: Offset,
    val isBalloon: Boolean
)

/**
 * Representa una partícula visual para la animación de aniquilación de pares cero.
 */
data class ZeroPairParticle(
    val position: Offset,
    val color: Color,
    val progress: Float
)

@Composable
fun HangerCanvas(
    state: HangerState,
    viewModel: HangerViewModel,
    modifier: Modifier = Modifier
) {
    // FÍSICA DIDÁCTICA:
    // La inclinación de la balanza refleja la diferencia de momento de fuerza (torque).
    // Usamos animateFloatAsState con un resorte (spring) de amortiguación media para que oscile
    // de forma orgánica y dinámica, igual que una balanza física en el aula.
    val targetTilt = viewModel.getTiltAngle(state)
    val tiltAngle by animateFloatAsState(
        targetValue = targetTilt,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "hangerSpringTilt"
    )

    // Estado de arrastre táctil (Drag and Drop)
    var draggedItemId by remember { mutableStateOf<String?>(null) }
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    var hoveredTargetId by remember { mutableStateOf<String?>(null) }

    // Lista de posiciones calculadas en el frame para colisiones
    val currentItemPositions = remember { mutableStateMapOf<String, ItemScreenPos>() }

    // Animación de partículas cuando se aniquila un par cero
    var particlesOrigin by remember { mutableStateOf<Offset?>(null) }
    val particleAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Detectar si hubo aniquilación reciente para detonar partículas
    LaunchedEffect(state.zeroPairCountCelebrated) {
        if (state.zeroPairCountCelebrated > 0 && particlesOrigin != null) {
            particleAnim.snapTo(0f)
            particleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.leftItems, state.rightItems) {
                // GESTOS TÁCTILES:
                // Permite pulsar directamente un elemento para retirarlo, facilitando la accesibilidad.
                detectTapGestures(
                    onTap = { tapOffset ->
                        val hit = currentItemPositions.values.find { (it.center - tapOffset).getDistance() < 42f }
                        if (hit != null) {
                            viewModel.onItemRemoved(hit.item)
                        }
                    }
                )
            }
            .pointerInput(state.leftItems, state.rightItems) {
                // DRAG & DROP DIDÁCTICO:
                // Arrastra un globo sobre una pesa para crear un par cero, o arrastra fuera para retirar.
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val hit = currentItemPositions.values.find { (it.center - startOffset).getDistance() < 46f }
                        if (hit != null) {
                            draggedItemId = hit.item.id
                            dragDelta = Offset.Zero
                            hoveredTargetId = null
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragDelta += dragAmount

                        val currentDragged = draggedItemId?.let { currentItemPositions[it] }
                        if (currentDragged != null) {
                            val activeCenter = currentDragged.center + dragDelta

                            // Buscar posible candidato a Par Cero en el mismo lado
                            val candidate = currentItemPositions.values.find { other ->
                                other.item.id != currentDragged.item.id &&
                                        other.item.side == currentDragged.item.side &&
                                        other.item.type == currentDragged.item.type &&
                                        other.item.isNegative != currentDragged.item.isNegative &&
                                        (other.center - activeCenter).getDistance() < 55f
                            }
                            hoveredTargetId = candidate?.item?.id
                        }
                    },
                    onDragEnd = {
                        val draggedId = draggedItemId
                        val targetId = hoveredTargetId
                        val draggedPos = draggedId?.let { currentItemPositions[it] }

                        if (draggedId != null && targetId != null && draggedPos != null) {
                            // COLISIÓN DE PAR CERO EXITOSA:
                            // Se encontraron un globo negativo y una pesa positiva del mismo tipo.
                            particlesOrigin = draggedPos.center + dragDelta
                            viewModel.resolveZeroPair(draggedPos.item.side, draggedId, targetId)
                        } else if (draggedPos != null && dragDelta.getDistance() > 70f) {
                            // ARRASTRE FUERA DEL COLGADOR:
                            // El estudiante sacó el objeto de la balanza para retirarlo.
                            viewModel.onItemRemoved(draggedPos.item)
                        }

                        draggedItemId = null
                        dragDelta = Offset.Zero
                        hoveredTargetId = null
                    },
                    onDragCancel = {
                        draggedItemId = null
                        dragDelta = Offset.Zero
                        hoveredTargetId = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Centro del fulcro (punto de apoyo superior)
            val pivotX = canvasW * 0.5f
            val pivotY = canvasH * 0.28f
            val barHalfLength = canvasW * 0.36f

            // 1. Dibujar Soporte y Fulcro (Base y Techo)
            drawCeilingHookAndFulcrum(pivotX, pivotY)

            // 2. Calcular los extremos girados de la barra según tiltAngle
            val rad = Math.toRadians(tiltAngle.toDouble())
            val cosT = cos(rad).toFloat()
            val sinT = sin(rad).toFloat()

            val leftAttachment = Offset(pivotX - barHalfLength * cosT, pivotY - barHalfLength * sinT)
            val rightAttachment = Offset(pivotX + barHalfLength * cosT, pivotY + barHalfLength * sinT)

            // 3. Dibujar la Barra Principal con rotación
            rotate(degrees = tiltAngle, pivot = Offset(pivotX, pivotY)) {
                // Barra de madera/metal
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF64748B), Color(0xFF334155), Color(0xFF1E293B))
                    ),
                    topLeft = Offset(pivotX - barHalfLength - 10f, pivotY - 8f),
                    size = Size(barHalfLength * 2f + 20f, 16f),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Detalles de remaches y pivote central
                drawCircle(color = Color(0xFFF1F5F9), radius = 10f, center = Offset(pivotX, pivotY))
                drawCircle(color = Color(0xFF0F172A), radius = 6f, center = Offset(pivotX, pivotY))

                // Enganches de los extremos
                drawCircle(color = Color(0xFFF59E0B), radius = 8f, center = Offset(pivotX - barHalfLength, pivotY))
                drawCircle(color = Color(0xFFF59E0B), radius = 8f, center = Offset(pivotX + barHalfLength, pivotY))
            }

            // Longitud de las cuerdas que sostienen las bandejas (cuelgan verticalmente por gravedad)
            val trayDrop = 55f
            val leftTrayCenter = Offset(leftAttachment.x, leftAttachment.y + trayDrop)
            val rightTrayCenter = Offset(rightAttachment.x, rightAttachment.y + trayDrop)

            // Cuerdas de suspensión verticales
            drawLine(
                color = Color(0xFF64748B),
                start = leftAttachment,
                end = leftTrayCenter,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF64748B),
                start = rightAttachment,
                end = rightTrayCenter,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Bandejas de suspensión
            drawTrayPlatform(leftTrayCenter)
            drawTrayPlatform(rightTrayCenter)

            // 4. Renderizar Elementos (Pesas hacia abajo, Globos hacia arriba)
            val positionsSnapshot = mutableMapOf<String, ItemScreenPos>()

            // Renderizar lado izquierdo
            renderSideItems(
                side = HangerSide.LEFT,
                trayCenter = leftTrayCenter,
                items = state.leftItems,
                draggedItemId = draggedItemId,
                dragDelta = dragDelta,
                hoveredTargetId = hoveredTargetId,
                positionsMap = positionsSnapshot
            )

            // Renderizar lado derecho
            renderSideItems(
                side = HangerSide.RIGHT,
                trayCenter = rightTrayCenter,
                items = state.rightItems,
                draggedItemId = draggedItemId,
                dragDelta = dragDelta,
                hoveredTargetId = hoveredTargetId,
                positionsMap = positionsSnapshot
            )

            // Actualizar el mapa de posiciones para hit testing
            currentItemPositions.clear()
            currentItemPositions.putAll(positionsSnapshot)

            // 5. Dibujar partículas de aniquilación si la animación está activa
            if (particleAnim.value in 0.01f..0.99f && particlesOrigin != null) {
                drawZeroPairBurst(particlesOrigin!!, particleAnim.value)
            }
        }
    }
}

/**
 * Dibuja el gancho del techo y el fulcro triangular con sombra.
 */
private fun DrawScope.drawCeilingHookAndFulcrum(pivotX: Float, pivotY: Float) {
    // Cable hacia el techo
    drawLine(
        color = Color(0xFF475569),
        start = Offset(pivotX, 0f),
        end = Offset(pivotX, pivotY - 20f),
        strokeWidth = 4f
    )

    // Argolla superior
    drawCircle(
        color = Color(0xFF334155),
        radius = 16f,
        center = Offset(pivotX, pivotY - 20f),
        style = Stroke(width = 5f)
    )

    // Triángulo del fulcro
    val trianglePath = Path().apply {
        moveTo(pivotX, pivotY)
        lineTo(pivotX - 22f, pivotY - 32f)
        lineTo(pivotX + 22f, pivotY - 32f)
        close()
    }
    drawPath(path = trianglePath, color = Color(0xFF1E293B))
}

/**
 * Dibuja la barra de la bandeja donde cuelgan y flotan los elementos.
 */
private fun DrawScope.drawTrayPlatform(center: Offset) {
    val trayWidth = 140f
    val trayHeight = 8f

    // Barra horizontal de la bandeja
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(center.x - trayWidth / 2, center.y - trayHeight / 2),
        size = Size(trayWidth, trayHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Anillo de sujeción central
    drawCircle(color = Color(0xFFF59E0B), radius = 5f, center = center)
}

/**
 * Renderiza los elementos de un lado organizados didácticamente:
 * - Globos (-): Flotan arriba con una cuerda amarrada a la bandeja.
 * - Pesas (+): Cuelgan abajo con una cuerda amarrada a la bandeja.
 */
private fun DrawScope.renderSideItems(
    side: HangerSide,
    trayCenter: Offset,
    items: List<HangerItem>,
    draggedItemId: String?,
    dragDelta: Offset,
    hoveredTargetId: String?,
    positionsMap: MutableMap<String, ItemScreenPos>
) {
    val balloons = items.filter { it.isNegative }
    val weights = items.filter { !it.isNegative }

    // Distribución geométrica de los globos (Hacia arriba)
    layoutAndDrawItemList(
        items = balloons,
        trayCenter = trayCenter,
        isBalloon = true,
        draggedItemId = draggedItemId,
        dragDelta = dragDelta,
        hoveredTargetId = hoveredTargetId,
        positionsMap = positionsMap
    )

    // Distribución geométrica de las pesas (Hacia abajo)
    layoutAndDrawItemList(
        items = weights,
        trayCenter = trayCenter,
        isBalloon = false,
        draggedItemId = draggedItemId,
        dragDelta = dragDelta,
        hoveredTargetId = hoveredTargetId,
        positionsMap = positionsMap
    )
}

private fun DrawScope.layoutAndDrawItemList(
    items: List<HangerItem>,
    trayCenter: Offset,
    isBalloon: Boolean,
    draggedItemId: String?,
    dragDelta: Offset,
    hoveredTargetId: String?,
    positionsMap: MutableMap<String, ItemScreenPos>
) {
    if (items.isEmpty()) return

    val itemSize = 42f
    val spacingX = 48f
    val itemsPerRow = 4

    items.forEachIndexed { index, item ->
        val col = index % itemsPerRow
        val row = index / itemsPerRow
        val totalInThisRow = min(items.size - row * itemsPerRow, itemsPerRow)

        val rowStartX = trayCenter.x - ((totalInThisRow - 1) * spacingX) / 2f
        val restX = rowStartX + col * spacingX

        // Distancia vertical desde la bandeja:
        // Los globos flotan hacia arriba (-Y), las pesas cuelgan hacia abajo (+Y)
        val restY = if (isBalloon) {
            trayCenter.y - 45f - (row * 50f)
        } else {
            trayCenter.y + 45f + (row * 50f)
        }

        val isBeingDragged = (item.id == draggedItemId)
        val activeCenter = if (isBeingDragged) {
            Offset(restX + dragDelta.x, restY + dragDelta.y)
        } else {
            Offset(restX, restY)
        }

        // Registrar posición para detección de colisiones
        positionsMap[item.id] = ItemScreenPos(item, activeCenter, isBalloon)

        // Cuerda de sujeción (conecta el elemento con la bandeja)
        if (!isBeingDragged) {
            drawLine(
                color = if (isBalloon) Color(0xFF94A3B8) else Color(0xFF64748B),
                start = Offset(restX, trayCenter.y),
                end = Offset(restX, if (isBalloon) restY + itemSize / 2 else restY - itemSize / 2),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Si este elemento es el objetivo hovered de un par cero, dibujar aura brillante
        if (item.id == hoveredTargetId) {
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.5f),
                radius = itemSize * 0.9f,
                center = activeCenter
            )
            drawCircle(
                color = Color(0xFFFFEB3B),
                radius = itemSize * 0.75f,
                center = activeCenter,
                style = Stroke(width = 4f)
            )
        }

        // Dibujar el elemento físico correspondiente
        drawVisualElement(
            item = item,
            center = activeCenter,
            size = itemSize,
            isDragged = isBeingDragged
        )
    }
}

/**
 * Dibuja el elemento según su naturaleza didáctica:
 * - VARIABLE_X Positiva: Cuadrado azul (+x) colgando.
 * - VARIABLE_X Negativa: Cuadrado rojo translúcido (-x) como globo.
 * - CONSTANT_1 Positiva: Círculo ámbar/naranja (+1) como pesa sólida.
 * - CONSTANT_1 Negativa: Círculo morado (-1) como globo flotante con brillo esférico.
 */
private fun DrawScope.drawVisualElement(
    item: HangerItem,
    center: Offset,
    size: Float,
    isDragged: Boolean
) {
    val half = size / 2
    val shadowOffset = if (isDragged) 8f else 3f

    // Sombra sutil para elevación
    drawCircle(
        color = Color.Black.copy(alpha = if (isDragged) 0.35f else 0.15f),
        radius = half + 2f,
        center = Offset(center.x, center.y + shadowOffset)
    )

    when (item.type) {
        ItemType.VARIABLE_X -> {
            // CUADRADOS PARA VARIABLES 'x'
            val fillColor = if (item.isNegative) {
                Color(0xFFEF4444) // Rojo vibrante para -x (globo)
            } else {
                Color(0xFF2563EB) // Azul real para +x (pesa)
            }

            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(fillColor, fillColor.copy(alpha = 0.85f)),
                    start = Offset(center.x - half, center.y - half),
                    end = Offset(center.x + half, center.y + half)
                ),
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(size, size),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Borde delimitador nítido (estilo GeoGebra)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(size, size),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.5f)
            )

            // Si es globo, dibujar nudo inferior
            if (item.isNegative) {
                drawBalloonKnot(Offset(center.x, center.y + half + 2f), fillColor)
            }

            // Texto "x" o "-x"
            val label = if (item.isNegative) "-x" else "x"
            drawLabelText(label, center, Color.White, textSize = 22f)
        }

        ItemType.CONSTANT_1 -> {
            // CÍRCULOS PARA CONSTANTES '1'
            val fillColor = if (item.isNegative) {
                Color(0xFF8B5CF6) // Morado/Púrpura para -1 (globo)
            } else {
                Color(0xFFF59E0B) // Ámbar/Naranja para +1 (pesa)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(fillColor.copy(alpha = 0.95f), fillColor),
                    center = Offset(center.x - half * 0.3f, center.y - half * 0.3f),
                    radius = half * 1.2f
                ),
                radius = half,
                center = center
            )

            // Borde nítido
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = half,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Brillo especular blanco en globos para dar volumen 3D
            if (item.isNegative) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = half * 0.28f,
                    center = Offset(center.x - half * 0.35f, center.y - half * 0.35f)
                )
                drawBalloonKnot(Offset(center.x, center.y + half + 2f), fillColor)
            }

            // Texto "1" o "-1"
            val label = if (item.isNegative) "-1" else "1"
            drawLabelText(label, center, Color.White, textSize = 20f)
        }
    }
}

/**
 * Dibuja un nudo triangular debajo de los globos.
 */
private fun DrawScope.drawBalloonKnot(position: Offset, color: Color) {
    val knotPath = Path().apply {
        moveTo(position.x, position.y - 3f)
        lineTo(position.x - 4f, position.y + 4f)
        lineTo(position.x + 4f, position.y + 4f)
        close()
    }
    drawPath(path = knotPath, color = color)
}

/**
 * Renderiza el texto centrado usando el Paint nativo de Android.
 */
private fun DrawScope.drawLabelText(text: String, center: Offset, color: Color, textSize: Float) {
    val paint = Paint().apply {
        this.color = color.hashCode()
        this.textSize = textSize
        this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        this.textAlign = Paint.Align.CENTER
        this.isAntiAlias = true
    }
    // Ajuste de línea base vertical
    val yPos = center.y - (paint.descent() + paint.ascent()) / 2f
    drawContext.canvas.nativeCanvas.drawText(text, center.x, yPos, paint)
}

/**
 * Dibuja una animación de partículas en expansión al aniquilar un par cero.
 */
private fun DrawScope.drawZeroPairBurst(origin: Offset, progress: Float) {
    val maxRadius = 60f
    val currentRadius = maxRadius * progress
    val alpha = (1f - progress).coerceIn(0f, 1f)

    // Anillo exterior expansivo
    drawCircle(
        color = Color(0xFFFFD700).copy(alpha = alpha),
        radius = currentRadius,
        center = origin,
        style = Stroke(width = 4f * (1f - progress))
    )

    // Rayos / chispas didácticas
    val particleCount = 8
    for (i in 0 until particleCount) {
        val angle = (i * 2 * Math.PI / particleCount).toFloat()
        val dist = currentRadius * 0.85f
        val px = origin.x + cos(angle) * dist
        val py = origin.y + sin(angle) * dist

        drawCircle(
            color = if (i % 2 == 0) Color(0xFF60A5FA).copy(alpha = alpha) else Color(0xFFF43F5E).copy(alpha = alpha),
            radius = 5f * (1f - progress),
            center = Offset(px, py)
        )
    }
}
