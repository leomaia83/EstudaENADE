@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo1.estudaenade.ui.theme.TipoStatus
import com.grupo1.estudaenade.ui.theme.corStatus
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun BarraTopo(titulo: String, onVoltar: (() -> Unit)? = null, acoes: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (onVoltar != null) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = { acoes() }
    )
}

/** Pílula de status suave: sempre cor + texto (+ ícone opcional), nunca só cor. */
@Composable
fun StatusPill(texto: String, tipo: TipoStatus, icone: ImageVector? = null, modifier: Modifier = Modifier) {
    val c = corStatus(tipo)
    Row(
        modifier = modifier
            .background(c.fundo, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, tint = c.texto, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(texto, color = c.texto, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier, acao: @Composable () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        acao()
    }
}

@Composable
fun CartaoMetrica(valor: String, rotulo: String, icone: ImageVector, modifier: Modifier = Modifier, cor: Color = MaterialTheme.colorScheme.primary) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier.size(32.dp).background(cor.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall)
            Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EstadoVazio(icone: ImageVector, titulo: String, descricao: String, modifier: Modifier = Modifier, acoes: @Composable () -> Unit = {}) {
    Column(modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icone, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        acoes()
    }
}

/** Diálogo de confirmação; ações destrutivas usam o botão na cor de erro. */
@Composable
fun DialogoConfirmacao(
    titulo: String, texto: String, confirmar: String, destrutivo: Boolean = false,
    onConfirmar: () -> Unit, onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                colors = if (destrutivo) ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.textButtonColors()
            ) { Text(confirmar, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

/** Gráfico de teia (radar): desempenho do aluno x média do curso por tópico. */
@Composable
fun GraficoRadar(rotulos: List<String>, valores: List<Float>, comparativo: List<Float>, tamanho: Dp = 280.dp) {
    val corPrincipal = MaterialTheme.colorScheme.primary
    val corComparativo = MaterialTheme.colorScheme.tertiary
    val corGrade = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val corTexto = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    if (rotulos.size < 3) return
    Canvas(Modifier.fillMaxWidth().height(tamanho)) {
        val centro = Offset(size.width / 2, size.height / 2)
        val raio = min(size.width, size.height) / 2 * 0.62f
        val n = rotulos.size
        fun ponto(i: Int, f: Float): Offset {
            val ang = -Math.PI / 2 + 2 * Math.PI * i / n
            return Offset(centro.x + (raio * f * cos(ang)).toFloat(), centro.y + (raio * f * sin(ang)).toFloat())
        }
        for (nivel in 1..4) {
            val p = Path()
            for (i in 0 until n) { val pt = ponto(i, nivel / 4f); if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y) }
            p.close()
            drawPath(p, corGrade, style = Stroke(width = 1.dp.toPx()))
        }
        for (i in 0 until n) drawLine(corGrade, centro, ponto(i, 1f), strokeWidth = 1.dp.toPx())
        fun poligono(vs: List<Float>, cor: Color, preencher: Boolean) {
            val p = Path()
            vs.forEachIndexed { i, v -> val pt = ponto(i, v.coerceIn(0.03f, 1f)); if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y) }
            p.close()
            if (preencher) drawPath(p, cor.copy(alpha = 0.22f))
            drawPath(p, cor, style = Stroke(width = 2.5.dp.toPx()))
        }
        poligono(comparativo, corComparativo, false)
        poligono(valores, corPrincipal, true)
        valores.forEachIndexed { i, v -> drawCircle(corPrincipal, 4.dp.toPx(), ponto(i, v.coerceIn(0.03f, 1f))) }
        val paint = Paint().apply { color = corTexto; textSize = 11.dp.toPx(); isAntiAlias = true; textAlign = Paint.Align.CENTER }
        rotulos.forEachIndexed { i, r ->
            val pt = ponto(i, 1.22f)
            val linhas = quebrar(r, 14)
            linhas.forEachIndexed { j, l -> drawContext.canvas.nativeCanvas.drawText(l, pt.x, pt.y + j * 12.dp.toPx() - (linhas.size - 1) * 6.dp.toPx() + 4.dp.toPx(), paint) }
        }
    }
}

private fun quebrar(texto: String, max: Int): List<String> {
    val linhas = mutableListOf<String>()
    var atual = ""
    texto.split(" ").forEach { p ->
        if ((atual + " " + p).trim().length > max && atual.isNotEmpty()) { linhas += atual; atual = p } else atual = (atual + " " + p).trim()
    }
    if (atual.isNotEmpty()) linhas += atual
    return linhas
}

/** Gráfico de linha da evolução das notas (0–100). */
@Composable
fun GraficoLinha(valores: List<Int>, rotulos: List<String>, altura: Dp = 180.dp) {
    val cor = MaterialTheme.colorScheme.primary
    val corGrade = MaterialTheme.colorScheme.outlineVariant
    val corTexto = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    Canvas(Modifier.fillMaxWidth().height(altura)) {
        val esq = 32.dp.toPx(); val base = size.height - 22.dp.toPx(); val topo = 8.dp.toPx()
        val paint = Paint().apply { color = corTexto; textSize = 10.dp.toPx(); isAntiAlias = true }
        listOf(0, 50, 100).forEach { v ->
            val y = base - (base - topo) * v / 100f
            drawLine(corGrade, Offset(esq, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText("$v%", 0f, y + 4.dp.toPx(), paint)
        }
        if (valores.isEmpty()) return@Canvas
        val passo = if (valores.size == 1) 0f else (size.width - esq - 16.dp.toPx()) / (valores.size - 1)
        val pts = valores.mapIndexed { i, v ->
            Offset(esq + 8.dp.toPx() + passo * i, base - (base - topo) * v / 100f)
        }
        val p = Path()
        pts.forEachIndexed { i, pt -> if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y) }
        drawPath(p, cor, style = Stroke(width = 3.dp.toPx()))
        paint.textAlign = Paint.Align.CENTER
        pts.forEachIndexed { i, pt ->
            drawCircle(cor, 5.dp.toPx(), pt)
            drawContext.canvas.nativeCanvas.drawText(rotulos.getOrElse(i) { "" }, pt.x, size.height - 4.dp.toPx(), paint)
        }
    }
}

/** Barra horizontal de progresso com rótulo e percentual (usada em comparativos FG x CE). */
@Composable
fun BarraPercentual(rotulo: String, valor: Float, cor: Color = MaterialTheme.colorScheme.primary, detalhe: String? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(rotulo, style = MaterialTheme.typography.bodyMedium)
            Text("${(valor * 100).toInt()}%" + (detalhe?.let { "  ·  $it" } ?: ""), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(10.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))) {
            Box(Modifier.fillMaxWidth(valor.coerceIn(0f, 1f)).height(10.dp).background(cor, RoundedCornerShape(50)))
        }
    }
}

@Composable
fun Legenda(cor: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(cor, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Layout em fluxo (quebra os itens em linhas), usado para grupos de chips.
 * Implementação própria com Layout para não depender de APIs experimentais.
 */
@Composable
fun Fluxo(
    modifier: Modifier = Modifier,
    espacoH: Dp = 8.dp,
    espacoV: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content, modifier = modifier) { medidas, restricoes ->
        val h = espacoH.roundToPx()
        val v = espacoV.roundToPx()
        val livres = restricoes.copy(minWidth = 0, minHeight = 0)
        val placeaveis = medidas.map { it.measure(livres) }
        val larguraMax = restricoes.maxWidth
        val posicoes = ArrayList<Pair<Int, Int>>(placeaveis.size)
        var x = 0
        var y = 0
        var alturaLinha = 0
        var larguraUsada = 0
        placeaveis.forEach { p ->
            if (x > 0 && x + p.width > larguraMax) {
                x = 0
                y += alturaLinha + v
                alturaLinha = 0
            }
            posicoes += x to y
            x += p.width + h
            larguraUsada = maxOf(larguraUsada, x - h)
            alturaLinha = maxOf(alturaLinha, p.height)
        }
        val largura = if (restricoes.hasBoundedWidth) larguraMax else larguraUsada
        layout(largura.coerceAtLeast(restricoes.minWidth), (y + alturaLinha).coerceAtLeast(restricoes.minHeight)) {
            placeaveis.forEachIndexed { i, p -> p.placeRelative(posicoes[i].first, posicoes[i].second) }
        }
    }
}
