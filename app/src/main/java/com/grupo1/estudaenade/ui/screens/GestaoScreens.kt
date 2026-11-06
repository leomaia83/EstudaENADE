@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.Componente
import com.grupo1.estudaenade.data.Cursos
import com.grupo1.estudaenade.data.Perfil
import com.grupo1.estudaenade.data.Questao
import com.grupo1.estudaenade.data.StatusConteudo
import com.grupo1.estudaenade.ui.components.*
import com.grupo1.estudaenade.ui.theme.TipoStatus

/** US08 · Área administrativa: questões, gabaritos e fila de moderação. */
@Composable
fun GestaoTab(vm: AppViewModel) {
    val u = vm.usuario ?: return
    val admin = u.perfil == Perfil.ADMIN
    var aba by remember { mutableIntStateOf(0) }
    var excluir by remember { mutableStateOf<Questao?>(null) }
    var rejeitar by remember { mutableStateOf<Questao?>(null) }
    val nModeracao = vm.pendentes.size + vm.denunciados.size

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { BarraTopo(if (admin) "Gestão de conteúdo" else "Minhas contribuições") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.navegar(Tela.EditarQuestao(null)) },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nova questão") }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (admin) {
                TabRow(selectedTabIndex = aba) {
                    Tab(aba == 0, { aba = 0 }, text = { Text("Questões (${vm.questoesPublicadas.size})") })
                    Tab(aba == 1, { aba = 1 }, text = { Text("Moderação ($nModeracao)") })
                }
            }
            if (!admin || aba == 0) {
                val lista = if (admin) vm.dados.questoes.filter { it.status == StatusConteudo.PUBLICADA }
                else vm.dados.questoes.filter { it.autorId == u.id }
                LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!admin) item {
                        Text(
                            "Questões e gabaritos que você cadastrar passam pela aprovação do administrador antes de serem publicados. Seus comentários recebem o selo de professor verificado.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(lista, key = { it.id }) { q ->
                        ItemGestao(q, onEditar = { vm.navegar(Tela.EditarQuestao(q.id)) }, onExcluir = if (admin) ({ excluir = q }) else null)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { TituloSecao("Questões aguardando aprovação (${vm.pendentes.size})") }
                    if (vm.pendentes.isEmpty()) item { Text("Nenhuma questão pendente.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(vm.pendentes, key = { "p" + it.id }) { q ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                CabecalhoQuestao(q)
                                Spacer(Modifier.height(6.dp))
                                Text(q.enunciado, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("Gabarito: ${letra(q.correta)} · enviada por ${q.autorComentario}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { rejeitar = q }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Rejeitar") }
                                    TextButton(onClick = { vm.navegar(Tela.EditarQuestao(q.id)) }) { Text("Editar") }
                                    Button(onClick = { vm.aprovarQuestao(q.id) }) { Text("Aprovar") }
                                }
                            }
                        }
                    }
                    item { TituloSecao("Mensagens denunciadas no fórum (${vm.denunciados.size})") }
                    if (vm.denunciados.isEmpty()) item { Text("Nenhuma denúncia pendente.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(vm.denunciados, key = { "d" + it.id }) { p ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.autorNome, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    StatusPill("Denunciado", TipoStatus.ALERTA, Icons.Filled.Warning)
                                }
                                Text(p.texto, style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { vm.navegar(Tela.Forum(p.questaoId)) }) { Text("Ver contexto") }
                                    TextButton(onClick = { vm.moderarPost(p.id, false) }) { Text("Manter") }
                                    TextButton(onClick = { vm.moderarPost(p.id, true) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Remover") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    excluir?.let { q ->
        DialogoConfirmacao(
            "Excluir questão?", "A questão de ${q.disciplina} (${q.ano}) e todas as mensagens do fórum dela serão removidas. Esta ação não pode ser desfeita.",
            "Excluir", destrutivo = true, onConfirmar = { vm.excluirQuestao(q.id); excluir = null }, onCancelar = { excluir = null }
        )
    }
    rejeitar?.let { q ->
        DialogoConfirmacao(
            "Rejeitar questão?", "A questão enviada por ${q.autorComentario} será descartada.",
            "Rejeitar", destrutivo = true, onConfirmar = { vm.excluirQuestao(q.id); rejeitar = null }, onCancelar = { rejeitar = null }
        )
    }
}

@Composable
private fun ItemGestao(q: Questao, onEditar: () -> Unit, onExcluir: (() -> Unit)?) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(q.componente.sigla, if (q.componente == Componente.FORMACAO_GERAL) TipoStatus.INFO else TipoStatus.NEUTRO)
                    if (q.status == StatusConteudo.PENDENTE) StatusPill("Aguardando aprovação", TipoStatus.ALERTA)
                    else StatusPill("Publicada", TipoStatus.SUCESSO, Icons.Filled.Check)
                }
                Spacer(Modifier.height(4.dp))
                Text("${q.ano} · ${if (q.curso == Cursos.TODOS) "Todos os cursos" else q.curso} · ${q.disciplina}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(q.enunciado, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, "Editar") }
            if (onExcluir != null) IconButton(onClick = onExcluir) { Icon(Icons.Filled.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

/** Cadastro/edição de questão com gabarito comentado (US08). */
@Composable
fun EditarQuestaoScreen(vm: AppViewModel, tela: Tela.EditarQuestao) {
    val original = tela.questaoId?.let { vm.questao(it) }
    val u = vm.usuario
    var curso by remember { mutableStateOf(original?.curso ?: u?.curso ?: Cursos.lista.first()) }
    var comp by remember { mutableStateOf(original?.componente ?: Componente.CONHECIMENTO_ESPECIFICO) }
    var ano by remember { mutableStateOf(original?.ano ?: 2025) }
    var disciplina by remember { mutableStateOf(original?.disciplina ?: "") }
    var enunciado by remember { mutableStateOf(original?.enunciado ?: "") }
    val alts = remember { mutableStateListOf(*(original?.alternativas ?: List(5) { "" }).toTypedArray()) }
    var correta by remember { mutableIntStateOf(original?.correta ?: 0) }
    var comentario by remember { mutableStateOf(original?.comentario ?: "") }
    var erro by remember { mutableStateOf<String?>(null) }

    fun salvar() {
        erro = when {
            disciplina.isBlank() -> "Informe a disciplina."
            enunciado.trim().length < 20 -> "O enunciado deve ter pelo menos 20 caracteres."
            alts.any { it.isBlank() } -> "Preencha as 5 alternativas."
            comentario.trim().length < 20 -> "Escreva o gabarito comentado (mín. 20 caracteres)."
            else -> null
        }
        if (erro != null) return
        val q = Questao(
            id = original?.id ?: vm.novoId("q_"), ano = ano,
            curso = if (comp == Componente.FORMACAO_GERAL) Cursos.TODOS else curso,
            componente = comp, disciplina = disciplina.trim(), enunciado = enunciado.trim(),
            alternativas = alts.map { it.trim() }, correta = correta, comentario = comentario.trim(),
            autorComentario = original?.autorComentario ?: (u?.nome ?: ""),
            comentarioVerificado = original?.comentarioVerificado ?: false,
            autorId = original?.autorId
        )
        vm.salvarQuestao(q)
        vm.voltar()
    }

    Scaffold(topBar = {
        BarraTopo(if (original == null) "Nova questão" else "Editar questão", onVoltar = { vm.voltar() }) {
            TextButton(onClick = { salvar() }) { Text("Salvar") }
        }
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).imePadding()) {
            Text("Componente", style = MaterialTheme.typography.titleSmall)
            Fluxo(espacoH = 8.dp) {
                Componente.entries.forEach { c -> FilterChip(comp == c, { comp = c }, { Text(c.rotulo) }) }
            }
            if (comp == Componente.CONHECIMENTO_ESPECIFICO) {
                Text("Curso", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                Fluxo(espacoH = 8.dp) {
                    Cursos.lista.forEach { c -> FilterChip(curso == c, { curso = c }, { Text(c) }) }
                }
            }
            Text("Ano da prova", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
            Fluxo(espacoH = 8.dp) {
                (2021..2025).forEach { a -> FilterChip(ano == a, { ano = a }, { Text("$a") }) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(disciplina, { disciplina = it }, label = { Text("Disciplina / tópico") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(enunciado, { enunciado = it }, label = { Text("Enunciado") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            TituloSecao("Alternativas (marque a correta)")
            alts.forEachIndexed { i, a ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = correta == i, onClick = { correta = i })
                    OutlinedTextField(a, { alts[i] = it }, label = { Text("Alternativa ${letra(i)}") }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(4.dp))
            }
            TituloSecao("Gabarito comentado")
            OutlinedTextField(
                comentario, { comentario = it }, minLines = 4, modifier = Modifier.fillMaxWidth(),
                label = { Text("Explique o raciocínio da resposta correta") },
                supportingText = { Text("Aceita fórmulas em texto (ex.: x², √2, Σ, ≤) e passo a passo.") }
            )
            if (erro != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(erro!!, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { salvar() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (u?.perfil == Perfil.ADMIN) "Salvar e publicar" else "Enviar para aprovação")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
