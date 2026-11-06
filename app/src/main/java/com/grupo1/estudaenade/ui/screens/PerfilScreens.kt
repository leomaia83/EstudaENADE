@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.AvaliacaoSus
import com.grupo1.estudaenade.data.Cursos
import com.grupo1.estudaenade.data.Perfil
import com.grupo1.estudaenade.data.Sus
import com.grupo1.estudaenade.ui.components.*
import com.grupo1.estudaenade.ui.theme.TipoStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PerfilTab(vm: AppViewModel) {
    val u = vm.usuario ?: return
    var editar by remember { mutableStateOf(false) }
    var curso by remember(u.id) { mutableStateOf(u.curso ?: Cursos.lista.first()) }
    var ano by remember(u.id) { mutableStateOf(u.anoEnade ?: 2026) }
    var meta by remember(u.id) { mutableStateOf(u.metaDiariaMin) }
    var confirmarReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Perfil") {
            TextButton(onClick = { vm.sair() }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null); Spacer(Modifier.width(4.dp)); Text("Sair")
            }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Text(u.nome.filter { it.isUpperCase() }.take(2), color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(u.nome, style = MaterialTheme.typography.titleLarge)
                    Text(u.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    StatusPill(u.perfil.rotulo, TipoStatus.INFO)
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Preferências de estudo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            if (editar) vm.atualizarPerfil(curso, ano, meta)
                            editar = !editar
                        }) { Text(if (editar) "Salvar" else "Editar") }
                    }
                    if (editar) {
                        SeletorPerfilEstudo(curso, { curso = it }, ano, { ano = it }, meta, { meta = it })
                    } else {
                        LinhaInfo("Curso", u.curso ?: "—")
                        LinhaInfo("ENADE previsto", u.anoEnade?.toString() ?: "—")
                        LinhaInfo("Meta diária", "${u.metaDiariaMin} min")
                    }
                }
            }

            TituloSecao("Validação do protótipo")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(4.dp)) {
                    ListItem(
                        headlineContent = { Text("Aplicar avaliação de usabilidade (SUS)") },
                        supportingContent = { Text("Questionário padrão de 10 itens + checklist de tarefas") },
                        leadingContent = { Icon(Icons.Filled.Create, null) },
                        trailingContent = { Icon(Icons.Filled.KeyboardArrowRight, null) }
                    )
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { vm.navegar(Tela.AvaliacaoSus) }) { Text("Nova avaliação") }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    ListItem(
                        headlineContent = { Text("Resultados da validação") },
                        supportingContent = {
                            val av = vm.dados.avaliacoes
                            Text(if (av.isEmpty()) "Nenhuma avaliação registrada" else "${av.size} participantes · SUS médio ${"%.1f".format(av.map { it.score }.average())}")
                        },
                        leadingContent = { Icon(Icons.Filled.Star, null) }
                    )
                    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { vm.navegar(Tela.ResultadosSus) }) { Text("Ver resultados") }
                    }
                }
            }

            TituloSecao("Conta")
            OutlinedButton(onClick = { confirmarReset = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Refresh, null); Spacer(Modifier.width(8.dp)); Text("Restaurar dados de demonstração")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.sair() }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) { Icon(Icons.AutoMirrored.Filled.ExitToApp, null); Spacer(Modifier.width(8.dp)); Text("Sair") }
            Spacer(Modifier.height(16.dp))
            Text(
                "Estuda ENADE · protótipo do MVP (Entregável 7)\nProjeto de Extensão · Requisitos e Modelagem de Sistemas · 2026.2\nQuestões de demonstração elaboradas no formato do ENADE.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmarReset) {
        DialogoConfirmacao(
            "Restaurar dados de demonstração?",
            "Respostas, simulados, posts e contas criadas serão apagados e o app voltará ao estado inicial. As avaliações SUS coletadas serão mantidas.",
            "Restaurar", destrutivo = true,
            onConfirmar = { confirmarReset = false; vm.restaurarDemo() }, onCancelar = { confirmarReset = false }
        )
    }
}

@Composable
private fun LinhaInfo(rotulo: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(rotulo, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, fontWeight = FontWeight.SemiBold)
    }
}

/** Instrumento de validação com usuários: SUS (Brooke, 1996) + checklist de tarefas do roteiro de teste. */
@Composable
fun AvaliacaoSusScreen(vm: AppViewModel) {
    var participante by remember { mutableStateOf("P${vm.dados.avaliacoes.size + 1}") }
    var perfil by remember { mutableStateOf(Perfil.ESTUDANTE.rotulo) }
    val respostas = remember { mutableStateListOf(*Array(10) { 0 }) }
    val tarefas = remember { mutableStateListOf(*Array(Sus.tarefas.size) { false }) }
    var comentario by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<AvaliacaoSus?>(null) }
    val faltam = respostas.count { it == 0 }

    Scaffold(topBar = { BarraTopo("Avaliação de usabilidade", onVoltar = { vm.voltar() }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).imePadding()) {
            Text(
                "Peça ao participante para realizar as tarefas abaixo sem ajuda. Marque as que ele concluiu sozinho e depois deixe-o responder as 10 afirmações.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(participante, { participante = it }, label = { Text("Identificação do participante") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Fluxo(espacoH = 8.dp) {
                listOf("Estudante", "Professor", "Coordenador").forEach { p -> FilterChip(perfil == p, { perfil = p }, { Text(p) }) }
            }

            TituloSecao("Tarefas concluídas sem ajuda")
            Sus.tarefas.forEachIndexed { i, t ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = tarefas[i], onCheckedChange = { tarefas[i] = it })
                    Text(t, style = MaterialTheme.typography.bodyMedium)
                }
            }

            TituloSecao("Questionário SUS")
            Text("1 = discordo totalmente · 5 = concordo totalmente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Sus.afirmacoes.forEachIndexed { i, a ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${i + 1}. $a", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { n ->
                                FilterChip(respostas[i] == n, { respostas[i] = n }, { Text("$n") })
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(comentario, { comentario = it }, label = { Text("Comentários e sugestões do participante") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val a = AvaliacaoSus(
                        vm.novoId("av_"), participante.ifBlank { "Anônimo" }, perfil,
                        respostas.toList(), tarefas.toList(), comentario.trim(), System.currentTimeMillis()
                    )
                    vm.salvarAvaliacao(a)
                    resultado = a
                },
                enabled = faltam == 0, modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(if (faltam == 0) "Salvar avaliação" else "Responda as $faltam afirmações restantes") }
            Spacer(Modifier.height(24.dp))
        }
    }

    resultado?.let { a ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Avaliação registrada") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("%.1f".format(a.score), fontSize = 44.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Pontuação SUS · ${Sus.classificacao(a.score)}")
                    Spacer(Modifier.height(4.dp))
                    Text("${a.tarefasConcluidas.count { it }} de ${a.tarefasConcluidas.size} tarefas concluídas sem ajuda", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { resultado = null; vm.substituir(Tela.ResultadosSus) }) { Text("Ver resultados") } },
            dismissButton = { TextButton(onClick = { resultado = null; vm.voltar() }) { Text("Fechar") } }
        )
    }
}

@Composable
fun ResultadosSusScreen(vm: AppViewModel) {
    val av = vm.dados.avaliacoes
    val ctx = LocalContext.current
    var excluir by remember { mutableStateOf<AvaliacaoSus?>(null) }
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("pt-BR")) }

    Scaffold(topBar = {
        BarraTopo("Resultados da validação", onVoltar = { vm.voltar() }) {
            if (av.isNotEmpty()) TextButton(onClick = {
                val csv = buildString {
                    append("participante;perfil;data;")
                    append((1..10).joinToString(";") { "Q$it" }); append(";")
                    append(Sus.tarefas.indices.joinToString(";") { "T${it + 1}" })
                    appendLine(";sus;classificacao;comentario")
                    av.forEach { a ->
                        append("${a.participante};${a.perfil};${fmt.format(Date(a.timestamp))};")
                        append(a.respostas.joinToString(";")); append(";")
                        append(a.tarefasConcluidas.joinToString(";") { if (it) "1" else "0" })
                        appendLine(";${"%.1f".format(a.score)};${Sus.classificacao(a.score)};${a.comentario.replace(";", ",").replace("\n", " ")}")
                    }
                }
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Validação SUS - Estuda ENADE")
                    putExtra(Intent.EXTRA_TEXT, csv)
                }
                ctx.startActivity(Intent.createChooser(i, "Exportar CSV"))
            }) { Text("Exportar CSV") }
        }
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (av.isEmpty()) {
                EstadoVazio(Icons.Filled.Star, "Nenhuma avaliação ainda", "Aplique o questionário SUS com usuários reais para registrar a validação do protótipo.") {
                    Button(onClick = { vm.substituir(Tela.AvaliacaoSus) }) { Text("Aplicar avaliação") }
                }
            } else {
                val media = av.map { it.score }.average()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CartaoMetrica("%.1f".format(media), "SUS médio · ${Sus.classificacao(media)}", Icons.Filled.Star, Modifier.weight(1f))
                    CartaoMetrica("${av.size}", "Participantes", Icons.Filled.Person, Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.height(8.dp))
                Text("Referência: pontuação SUS acima de 68 é considerada acima da média.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TituloSecao("Taxa de sucesso por tarefa")
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Sus.tarefas.forEachIndexed { i, t ->
                            val ok = av.count { it.tarefasConcluidas.getOrElse(i) { false } }
                            BarraPercentual(t, ok.toFloat() / av.size, cor = if (ok.toFloat() / av.size >= 0.8f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
                TituloSecao("Participantes")
                av.sortedByDescending { it.timestamp }.forEach { a ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("${a.participante} · ${a.perfil}", style = MaterialTheme.typography.titleSmall)
                                    Text(fmt.format(Date(a.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill("%.1f".format(a.score), if (a.score >= 68) TipoStatus.SUCESSO else if (a.score >= 52) TipoStatus.ALERTA else TipoStatus.ERRO)
                                IconButton(onClick = { excluir = a }) { Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
                            }
                            if (a.comentario.isNotBlank()) Text("“${a.comentario}”", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }

    excluir?.let { a ->
        DialogoConfirmacao(
            "Excluir avaliação?", "A avaliação de ${a.participante} será removida dos resultados.", "Excluir", destrutivo = true,
            onConfirmar = { vm.excluirAvaliacao(a.id); excluir = null }, onCancelar = { excluir = null }
        )
    }
}
