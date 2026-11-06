@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.Cursos
import com.grupo1.estudaenade.ui.components.BarraTopo
import com.grupo1.estudaenade.ui.components.Fluxo
import com.grupo1.estudaenade.ui.components.DialogoConfirmacao
import com.grupo1.estudaenade.ui.components.StatusPill
import com.grupo1.estudaenade.ui.components.TituloSecao
import com.grupo1.estudaenade.ui.theme.TipoStatus
import com.grupo1.estudaenade.ui.theme.corStatus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class OpcaoSimulado(val titulo: String, val qtd: Int, val segundos: Int, val descricao: String)

private val opcoes = listOf(
    OpcaoSimulado("Mini simulado", 5, 5 * 360, "5 questões · 30 min"),
    OpcaoSimulado("Simulado padrão", 10, 10 * 360, "10 questões · 1 h"),
    OpcaoSimulado("Simulado completo", 20, 20 * 360, "20 questões · 2 h"),
    OpcaoSimulado("Demonstração do cronômetro", 5, 60, "5 questões · 1 min (mostra a finalização automática)")
)

/** US03 · Configuração e histórico de simulados. */
@Composable
fun SimuladoTab(vm: AppViewModel) {
    val u = vm.usuario ?: return
    val ativo = vm.simuladoAtivo()
    var curso by remember { mutableStateOf(u.curso ?: Cursos.lista.first()) }
    var opcao by remember { mutableStateOf(opcoes[1]) }
    var confirmarNovo by remember { mutableStateOf(false) }
    val historico = vm.historicoSimulados().reversed()
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Simulados")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            if (ativo != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Você tem um simulado em andamento", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text("${ativo.respostas.size} de ${ativo.questaoIds.size} respondidas · progresso salvo", color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Button(onClick = { vm.navegar(Tela.SimuladoExec(ativo.id)) }) { Text("Retomar") }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Text(
                "Treine nas mesmas condições da prova: tempo oficial proporcional (6 min por questão), 25% de Formação Geral e 75% de Conhecimento Específico. O gabarito só aparece no final.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TituloSecao("Curso")
            Fluxo(espacoH = 8.dp) {
                Cursos.lista.forEach { c -> FilterChip(curso == c, { curso = c }, { Text(c) }) }
            }
            TituloSecao("Formato")
            opcoes.forEach { o ->
                val sel = o == opcao
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { opcao = o }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = sel, onClick = { opcao = o })
                        Column {
                            Text(o.titulo, style = MaterialTheme.typography.titleMedium)
                            Text(o.descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { if (ativo != null) confirmarNovo = true else vm.iniciarSimulado(curso, opcao.qtd, opcao.segundos) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Iniciar simulado") }

            TituloSecao("Histórico")
            if (historico.isEmpty()) {
                Text("Você ainda não fez simulados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            historico.forEach { s ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { vm.navegar(Tela.SimuladoResultado(s.id)) }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${s.questaoIds.size} questões · ${fmt.format(Date(s.inicio))}", style = MaterialTheme.typography.titleSmall)
                            Text("${s.acertos} acertos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill("${s.nota}%", tipoNota(s.nota))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmarNovo) {
        DialogoConfirmacao(
            "Iniciar novo simulado?", "O simulado em andamento será finalizado e corrigido com as respostas atuais.", "Finalizar e iniciar novo",
            onConfirmar = { confirmarNovo = false; vm.iniciarSimulado(curso, opcao.qtd, opcao.segundos) },
            onCancelar = { confirmarNovo = false }
        )
    }
}

fun tipoNota(n: Int) = when {
    n >= 70 -> TipoStatus.SUCESSO
    n >= 50 -> TipoStatus.ALERTA
    else -> TipoStatus.ERRO
}

/** US03 · Execução: cronômetro ocultável, salvamento automático a cada resposta e finalização por tempo. */
@Composable
fun SimuladoExecScreen(vm: AppViewModel, tela: Tela.SimuladoExec) {
    val s = vm.simulado(tela.simId)
    if (s == null) { if (vm.telaAtual == tela) vm.voltar(); return }
    if (s.finalizado) {
        LaunchedEffect(s.id) { if (vm.telaAtual == tela) vm.substituir(Tela.SimuladoResultado(s.id)) }
        return
    }
    var agora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var mostrarTempo by remember { mutableStateOf(true) }
    var confirmarFim by remember { mutableStateOf(false) }
    var salvoEm by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(s.id) {
        while (true) {
            agora = System.currentTimeMillis()
            if (agora >= s.prazo) { vm.finalizarSimulado(s.id, porTempo = true); break }
            delay(500)
        }
    }

    val restante = (s.prazo - agora) / 1000
    val qid = s.questaoIds[s.indiceAtual]
    val q = vm.questao(qid) ?: return
    val marcada = s.respostas[qid]
    val pouco = restante <= 60

    Scaffold(
        topBar = {
            BarraTopo("Simulado · ${s.indiceAtual + 1}/${s.questaoIds.size}", onVoltar = { vm.voltar() }) {
                if (mostrarTempo) {
                    StatusPill(formatarTempo(restante), if (pouco) TipoStatus.ERRO else TipoStatus.INFO, Icons.Filled.DateRange)
                }
                TextButton(onClick = { mostrarTempo = !mostrarTempo }) { Text(if (mostrarTempo) "Ocultar" else "Ver tempo") }
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).navigationBarsPadding()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { vm.irParaQuestao(s.id, s.indiceAtual - 1) },
                            enabled = s.indiceAtual > 0, modifier = Modifier.weight(1f).height(50.dp)
                        ) { Text("Anterior") }
                        if (s.indiceAtual < s.questaoIds.lastIndex) {
                            Button(onClick = { vm.irParaQuestao(s.id, s.indiceAtual + 1) }, modifier = Modifier.weight(1f).height(50.dp)) { Text("Próxima") }
                        } else {
                            Button(onClick = { confirmarFim = true }, modifier = Modifier.weight(1f).height(50.dp)) { Text("Entregar") }
                        }
                    }
                }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Mapa de questões: navegação rápida e visão do que falta
            Fluxo(espacoH = 6.dp, espacoV = 6.dp) {
                s.questaoIds.forEachIndexed { i, id ->
                    val atual = i == s.indiceAtual
                    val feita = s.respostas.containsKey(id)
                    Box(
                        Modifier.size(34.dp)
                            .background(
                                when {
                                    atual -> MaterialTheme.colorScheme.primary
                                    feita -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }, CircleShape
                            )
                            .clickable { vm.irParaQuestao(s.id, i) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${i + 1}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = when {
                                atual -> MaterialTheme.colorScheme.onPrimary
                                feita -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${s.respostas.size} de ${s.questaoIds.size} respondidas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                if (salvoEm != null) {
                    Icon(Icons.Filled.Check, null, tint = corStatus(TipoStatus.SUCESSO).texto, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Salvo automaticamente", style = MaterialTheme.typography.bodySmall, color = corStatus(TipoStatus.SUCESSO).texto)
                }
            }
            Spacer(Modifier.height(12.dp))
            CabecalhoQuestao(q)
            Spacer(Modifier.height(12.dp))
            Text(q.enunciado, fontSize = vm.tamanhoFonte.sp, lineHeight = (vm.tamanhoFonte * 1.5f).sp)
            Spacer(Modifier.height(16.dp))
            q.alternativas.forEachIndexed { i, alt ->
                Alternativa(i, alt, if (marcada == i) EstadoAlt.SELECIONADA else EstadoAlt.NORMAL, vm.tamanhoFonte) {
                    vm.responderSimulado(s.id, qid, i)
                    salvoEm = System.currentTimeMillis()
                }
                Spacer(Modifier.height(8.dp))
            }
            Row { ControleFonte(vm) }
        }
    }

    if (confirmarFim) {
        val faltam = s.questaoIds.size - s.respostas.size
        DialogoConfirmacao(
            "Entregar simulado?",
            if (faltam > 0) "Ainda há $faltam questões sem resposta. Elas serão contadas como erradas." else "Todas as questões foram respondidas. Deseja ver seu resultado?",
            "Entregar",
            onConfirmar = { confirmarFim = false; vm.finalizarSimulado(s.id) },
            onCancelar = { confirmarFim = false }
        )
    }
}

/** Resultado: nota, revisão e gabarito comentado liberado somente após a entrega (critério da US04). */
@Composable
fun SimuladoResultadoScreen(vm: AppViewModel, tela: Tela.SimuladoResultado) {
    val s = vm.simulado(tela.simId)
    if (s == null) { if (vm.telaAtual == tela) vm.voltar(); return }
    var aberta by remember { mutableStateOf<String?>(null) }
    val duracao = ((s.fim ?: s.inicio) - s.inicio) / 1000

    Scaffold(topBar = { BarraTopo("Resultado do simulado", onVoltar = { vm.voltar() }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            val c = corStatus(tipoNota(s.nota))
            Card(colors = CardDefaults.cardColors(containerColor = c.fundo), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${s.nota}%", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = c.texto)
                    Text("${s.acertos} de ${s.questaoIds.size} questões corretas", color = c.texto, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        (if (s.finalizadoPorTempo) "Finalizado automaticamente ao esgotar o tempo · " else "") + "tempo usado: ${formatarTempo(duracao)}",
                        color = c.texto, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            TituloSecao("Revisão das questões")
            Text("Toque em uma questão para ver o gabarito comentado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            s.questaoIds.mapIndexedNotNull { i, qid -> vm.questao(qid)?.let { Triple(i, qid, it) } }.forEach { (i, qid, q) ->
                val resp = s.respostas[qid]
                val certo = resp == q.correta
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { aberta = if (aberta == qid) null else qid }) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}. ${q.disciplina}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            when {
                                resp == null -> StatusPill("Em branco", TipoStatus.NEUTRO)
                                certo -> StatusPill("Acertou", TipoStatus.SUCESSO, Icons.Filled.Check)
                                else -> StatusPill("Errou", TipoStatus.ERRO, Icons.Filled.Close)
                            }
                        }
                        Text(
                            "Sua resposta: ${resp?.let { letra(it) } ?: "—"} · Correta: ${letra(q.correta)}",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (aberta == qid) {
                            Spacer(Modifier.height(8.dp))
                            Text(q.enunciado, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            GabaritoComentado(q, vm.tamanhoFonte)
                            TextButton(onClick = { vm.navegar(Tela.Forum(q.id)) }) { Text("Abrir fórum desta questão") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
