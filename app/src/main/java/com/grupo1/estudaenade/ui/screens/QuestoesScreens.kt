@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.Componente
import com.grupo1.estudaenade.data.Cursos
import com.grupo1.estudaenade.data.Questao
import com.grupo1.estudaenade.ui.components.BarraTopo
import com.grupo1.estudaenade.ui.components.Fluxo
import com.grupo1.estudaenade.ui.components.EstadoVazio
import com.grupo1.estudaenade.ui.components.StatusPill
import com.grupo1.estudaenade.ui.theme.TipoStatus
import com.grupo1.estudaenade.ui.theme.corStatus

/** US02 · Banco de questões com filtros combinados (ano, curso, componente, disciplina). */
@Composable
fun BancoQuestoesTab(vm: AppViewModel) {
    val lista = vm.filtrar()
    val disciplinas = vm.disciplinasDisponiveis()
    var filtrosAbertos by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Banco de questões", acoes = {
            TextButton(onClick = { filtrosAbertos = !filtrosAbertos }) { Text(if (filtrosAbertos) "Ocultar filtros" else "Filtros") }
        })
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Column(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = vm.filtroBusca, onValueChange = { vm.filtroBusca = it },
                placeholder = { Text("Buscar por palavra ou disciplina") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (vm.filtroBusca.isNotEmpty()) IconButton(onClick = { vm.filtroBusca = "" }) { Icon(Icons.Filled.Clear, "Limpar busca") } },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            if (filtrosAbertos) {
                GrupoFiltro("Curso") {
                    (listOf(Cursos.TODOS) + Cursos.lista).forEach { c ->
                        val sel = (vm.filtroCurso ?: Cursos.TODOS) == c
                        FilterChip(sel, { vm.filtroCurso = if (c == Cursos.TODOS) null else c; vm.filtroDisciplina = null }, { Text(c) })
                    }
                }
                GrupoFiltro("Componente") {
                    FilterChip(vm.filtroComponente == null, { vm.filtroComponente = null }, { Text("Todos") })
                    Componente.entries.forEach { c ->
                        FilterChip(vm.filtroComponente == c, { vm.filtroComponente = c; vm.filtroDisciplina = null }, { Text(c.rotulo) })
                    }
                }
                GrupoFiltro("Ano da prova") {
                    FilterChip(vm.filtroAno == null, { vm.filtroAno = null }, { Text("Todos") })
                    vm.anosDisponiveis().forEach { a -> FilterChip(vm.filtroAno == a, { vm.filtroAno = a }, { Text("$a") }) }
                }
                GrupoFiltro("Disciplina") {
                    FilterChip(vm.filtroDisciplina == null, { vm.filtroDisciplina = null }, { Text("Todas") })
                    disciplinas.forEach { d -> FilterChip(vm.filtroDisciplina == d, { vm.filtroDisciplina = d }, { Text(d) }) }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Switch(checked = vm.somenteErradas, onCheckedChange = { vm.somenteErradas = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Só as que eu errei", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${lista.size} questões encontradas", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (lista.isNotEmpty()) {
                    Button(onClick = { vm.navegar(Tela.Resolver(lista.map { it.id }, 0)) }) {
                        Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(4.dp)); Text("Praticar")
                    }
                }
            }
        } }

        if (lista.isEmpty()) { item {
            EstadoVazio(
                Icons.Filled.Search, "Nenhuma questão com esses filtros",
                "Tente uma destas alternativas:"
            ) {
                Fluxo(espacoH = 8.dp) {
                    if (vm.filtroAno != null) AssistChip({ vm.filtroAno = null }, { Text("Remover filtro de ano") })
                    if (vm.filtroDisciplina != null) AssistChip({ vm.filtroDisciplina = null }, { Text("Todas as disciplinas") })
                    if (vm.somenteErradas) AssistChip({ vm.somenteErradas = false }, { Text("Incluir questões acertadas") })
                    if (vm.filtroBusca.isNotBlank()) AssistChip({ vm.filtroBusca = "" }, { Text("Limpar busca") })
                    AssistChip({ vm.limparFiltros() }, { Text("Limpar todos os filtros") })
                }
            } }
        } else {
                items(lista, key = { it.id }) { q ->
                    val idx = lista.indexOf(q)
                    Box(Modifier.padding(horizontal = 16.dp)) { CartaoQuestao(vm, q) { vm.navegar(Tela.Resolver(lista.map { it.id }, idx)) } }
                }
        }
        }
    }
}

@Composable
private fun GrupoFiltro(titulo: String, conteudo: @Composable () -> Unit) {
    Text(titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    // Uma linha rolável por grupo: economiza altura e mantém a lista de questões visível
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { conteudo() }
}

@Composable
fun CartaoQuestao(vm: AppViewModel, q: Questao, onClick: () -> Unit) {
    val ult = vm.ultimaResposta(q.id)
    val nPosts = vm.postsDa(q.id).size
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(q.componente.sigla, if (q.componente == Componente.FORMACAO_GERAL) TipoStatus.INFO else TipoStatus.NEUTRO)
                Text("${q.ano} · ${q.disciplina}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                when (ult?.correta) {
                    true -> StatusPill("Acertei", TipoStatus.SUCESSO, Icons.Filled.Check)
                    false -> StatusPill("Errei", TipoStatus.ERRO, Icons.Filled.Close)
                    null -> {}
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(q.enunciado, maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
            if (nPosts > 0) {
                Spacer(Modifier.height(6.dp))
                Text("$nPosts comentários no fórum", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** US04 · Modo Prática Livre: resposta com feedback imediato e gabarito comentado. */
@Composable
fun ResolverScreen(vm: AppViewModel, tela: Tela.Resolver) {
    val qid = tela.ids.getOrNull(tela.indice)
    val q = qid?.let { vm.questao(it) }
    if (q == null) { if (vm.telaAtual == tela) vm.voltar(); return }

    var selecionada by remember(qid) { mutableStateOf<Int?>(null) }
    var respondida by remember(qid) { mutableStateOf(false) }
    val nPosts = vm.postsDa(q.id).size
    val ultimo = tela.indice == tela.ids.lastIndex
    val rolagem = rememberScrollState()
    var posFeedback by remember(qid) { mutableIntStateOf(0) }
    // Ao responder, rola até o feedback para o gabarito comentado ficar visível sem esforço
    LaunchedEffect(respondida) { if (respondida) { delay(150); rolagem.animateScrollTo(posFeedback) } }

    Scaffold(
        topBar = {
            BarraTopo("Questão ${tela.indice + 1} de ${tela.ids.size}", onVoltar = { vm.voltar() }) {
                ControleFonte(vm)
            }
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
                    if (!respondida) {
                        Button(
                            onClick = { selecionada?.let { vm.responderPratica(q.id, it); respondida = true } },
                            enabled = selecionada != null, modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Responder") }
                    } else {
                        Button(
                            onClick = { if (ultimo) vm.voltar() else vm.substituir(tela.copy(indice = tela.indice + 1)) },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text(if (ultimo) "Concluir treino" else "Próxima questão") }
                    }
                }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rolagem).padding(16.dp)) {
            LinearProgressIndicator(progress = { (tela.indice + 1f) / tela.ids.size }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            CabecalhoQuestao(q)
            Spacer(Modifier.height(12.dp))
            Text(q.enunciado, fontSize = vm.tamanhoFonte.sp, lineHeight = (vm.tamanhoFonte * 1.5f).sp)
            Spacer(Modifier.height(16.dp))
            q.alternativas.forEachIndexed { i, alt ->
                val estado = when {
                    !respondida -> if (selecionada == i) EstadoAlt.SELECIONADA else EstadoAlt.NORMAL
                    i == q.correta -> EstadoAlt.CORRETA
                    i == selecionada -> EstadoAlt.ERRADA
                    else -> EstadoAlt.NORMAL
                }
                Alternativa(i, alt, estado, vm.tamanhoFonte, enabled = !respondida) { selecionada = i }
                Spacer(Modifier.height(8.dp))
            }

            if (respondida) {
                val certo = selecionada == q.correta
                Spacer(Modifier.height(8.dp))
                val c = corStatus(if (certo) TipoStatus.SUCESSO else TipoStatus.ERRO)
                Row(Modifier.onGloballyPositioned { posFeedback = it.positionInParent().y.toInt() }.fillMaxWidth().background(c.fundo, RoundedCornerShape(12.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (certo) Icons.Filled.CheckCircle else Icons.Filled.Close, null, tint = c.texto)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (certo) "Você acertou!" else "Resposta incorreta. A correta é a letra ${letra(q.correta)}.",
                        color = c.texto, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(12.dp))
                GabaritoComentado(q, vm.tamanhoFonte)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { vm.navegar(Tela.Forum(q.id)) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Face, null); Spacer(Modifier.width(8.dp))
                    Text(if (nPosts > 0) "Discutir com colegas ($nPosts comentários)" else "Tirar dúvida no fórum")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ControleFonte(vm: AppViewModel) {
    TextButton(onClick = { vm.tamanhoFonte = (vm.tamanhoFonte - 2f).coerceAtLeast(13f) }) { Text("A-", fontWeight = FontWeight.Bold) }
    TextButton(onClick = { vm.tamanhoFonte = (vm.tamanhoFonte + 2f).coerceAtMost(25f) }) { Text("A+", fontWeight = FontWeight.Bold) }
}

@Composable
fun CabecalhoQuestao(q: Questao) {
    Fluxo(espacoH = 6.dp, espacoV = 4.dp) {
        StatusPill(q.componente.rotulo, if (q.componente == Componente.FORMACAO_GERAL) TipoStatus.INFO else TipoStatus.NEUTRO)
        StatusPill(q.disciplina, TipoStatus.NEUTRO)
        StatusPill("${q.ano}", TipoStatus.NEUTRO)
        if (q.curso != Cursos.TODOS) StatusPill(q.curso, TipoStatus.NEUTRO)
    }
}

@Composable
fun GabaritoComentado(q: Questao, fonte: Float) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gabarito comentado", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                if (q.comentarioVerificado) StatusPill("Verificado", TipoStatus.SUCESSO, Icons.Filled.CheckCircle)
            }
            Text("por ${q.autorComentario}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(8.dp))
            Text(q.comentario, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = (fonte - 1f).sp, lineHeight = ((fonte - 1f) * 1.5f).sp)
        }
    }
}

enum class EstadoAlt { NORMAL, SELECIONADA, CORRETA, ERRADA }

fun letra(i: Int) = ('A' + i).toString()

@Composable
fun Alternativa(indice: Int, texto: String, estado: EstadoAlt, fonte: Float, enabled: Boolean = true, onClick: () -> Unit) {
    val sucesso = corStatus(TipoStatus.SUCESSO)
    val erro = corStatus(TipoStatus.ERRO)
    val (fundo, borda, corTexto) = when (estado) {
        EstadoAlt.NORMAL -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.onSurface)
        EstadoAlt.SELECIONADA -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimaryContainer)
        EstadoAlt.CORRETA -> Triple(sucesso.fundo, sucesso.texto, sucesso.texto)
        EstadoAlt.ERRADA -> Triple(erro.fundo, erro.texto, erro.texto)
    }
    Surface(
        shape = RoundedCornerShape(14.dp), color = fundo,
        border = BorderStroke(if (estado == EstadoAlt.NORMAL) 1.dp else 2.dp, borda),
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).background(if (estado == EstadoAlt.NORMAL) MaterialTheme.colorScheme.surfaceVariant else borda, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (estado) {
                    EstadoAlt.CORRETA -> Icon(Icons.Filled.Check, "Correta", tint = fundo, modifier = Modifier.size(18.dp))
                    EstadoAlt.ERRADA -> Icon(Icons.Filled.Close, "Errada", tint = fundo, modifier = Modifier.size(18.dp))
                    else -> Text(
                        letra(indice), fontWeight = FontWeight.Bold,
                        color = if (estado == EstadoAlt.SELECIONADA) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(texto, color = corTexto, fontSize = (fonte - 1f).sp, lineHeight = ((fonte - 1f) * 1.4f).sp)
        }
    }
}

@Suppress("unused")
private val transparente = Color.Transparent
