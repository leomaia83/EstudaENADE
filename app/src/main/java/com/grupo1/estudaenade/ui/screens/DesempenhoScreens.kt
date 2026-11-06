@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.Componente
import com.grupo1.estudaenade.data.Cursos
import com.grupo1.estudaenade.ui.components.*
import com.grupo1.estudaenade.ui.theme.TipoStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** US06 · Dashboard visual de desempenho do estudante. */
@Composable
fun DesempenhoTab(vm: AppViewModel) {
    val u = vm.usuario ?: return
    val st = vm.stat(vm.respostasDe(u.id))
    val porDisc = vm.statPorDisciplina(u.id, u.curso)
    val media = vm.mediaCursoPorDisciplina(u.curso, u.id)
    val respondidas = porDisc.filter { it.value.total > 0 }
    val hist = vm.historicoSimulados(u.id)
    val fmt = remember { SimpleDateFormat("dd/MM", Locale.forLanguageTag("pt-BR")) }
    val comp = vm.statPorComponente(setOf(u.id))
    val alunosCurso = vm.alunosDoCurso(u.curso).map { it.id }.toSet() - u.id
    val compCurso = vm.statPorComponente(alunosCurso)

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Meu desempenho")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CartaoMetrica("${st.total}", "Questões", Icons.Filled.Done, Modifier.weight(1f))
                CartaoMetrica("${(st.taxa * 100).toInt()}%", "Acertos", Icons.Filled.CheckCircle, Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                CartaoMetrica("${hist.size}", "Simulados", Icons.Filled.DateRange, Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            }

            if (st.total == 0) {
                EstadoVazio(Icons.Filled.Star, "Sem dados ainda", "Responda algumas questões para ver seus gráficos de desempenho.") {
                    Button(onClick = { val ids = vm.treinoRapido(); if (ids.isNotEmpty()) vm.navegar(Tela.Resolver(ids, 0)) }) { Text("Fazer treino rápido") }
                }
            } else {

            TituloSecao("Desempenho por tópico")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    val chaves = porDisc.keys.toList().take(8)
                    if (chaves.size >= 3) {
                        GraficoRadar(chaves, chaves.map { porDisc[it]?.taxa ?: 0f }, chaves.map { media[it] ?: 0f })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(horizontal = 8.dp)) {
                        Legenda(MaterialTheme.colorScheme.primary, "Você")
                        Legenda(MaterialTheme.colorScheme.tertiary, "Média do curso")
                    }
                    Spacer(Modifier.height(8.dp))
                    // Tabela com os mesmos números do gráfico (acessível, não depende só de cor/forma)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("Tópico", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("Você", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(56.dp))
                        Text("Curso", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(56.dp))
                    }
                    porDisc.forEach { (d, sd) ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text(d, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(if (sd.total == 0) "—" else "${(sd.taxa * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(56.dp))
                            Text("${((media[d] ?: 0f) * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(56.dp))
                        }
                    }
                }
            }

            TituloSecao("Evolução nos simulados")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    if (hist.isEmpty()) Text("Faça um simulado para acompanhar sua evolução.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else GraficoLinha(hist.takeLast(8).map { it.nota }, hist.takeLast(8).map { fmt.format(Date(it.inicio)) })
                }
            }

            TituloSecao("Formação Geral x Conhecimento Específico")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Componente.entries.forEach { c ->
                        val meu = comp[c]
                        BarraPercentual(c.rotulo, meu?.taxa ?: 0f, detalhe = "curso: ${((compCurso[c]?.taxa ?: 0f) * 100).toInt()}%")
                    }
                }
            }

            val atencao = respondidas.filter { it.value.taxa < 0.6f }.keys
            if (atencao.isNotEmpty()) {
                TituloSecao("Pontos de atenção")
                atencao.forEach { d ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(d, style = MaterialTheme.typography.titleSmall)
                                StatusPill("${((respondidas[d]?.taxa ?: 0f) * 100).toInt()}% de acertos", TipoStatus.ALERTA, Icons.Filled.Warning)
                            }
                            FilledTonalButton(onClick = {
                                vm.limparFiltros(); vm.filtroCurso = u.curso; vm.filtroDisciplina = d
                                val ids = vm.filtrar().map { it.id }
                                if (ids.isNotEmpty()) vm.navegar(Tela.Resolver(ids, 0))
                            }) { Text("Treinar") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "A média do curso é calculada com os estudantes de demonstração cadastrados no protótipo.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** US07 · Relatórios de acompanhamento da turma (Coordenação). */
@Composable
fun RelatoriosTab(vm: AppViewModel) {
    val u = vm.usuario ?: return
    var curso by remember { mutableStateOf(u.curso ?: Cursos.lista.first()) }
    val alunos = vm.alunosDoCurso(curso)
    val ids = alunos.map { it.id }.toSet()
    val comSimulado = alunos.count { a -> vm.historicoSimulados(a.id).isNotEmpty() }
    val adesao = if (alunos.isEmpty()) 0f else comSimulado.toFloat() / alunos.size
    val comp = vm.statPorComponente(ids)
    val media = vm.mediaCursoPorDisciplina(curso, null)
    val ctx = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        BarraTopo("Relatório da turma", acoes = {
            TextButton(onClick = {
                val csv = buildString {
                    appendLine("Relatório Estuda ENADE;Curso: $curso")
                    appendLine("Disciplina;Taxa de acerto da turma")
                    media.forEach { (d, t) -> appendLine("$d;${(t * 100).toInt()}%") }
                    Componente.entries.forEach { c -> appendLine("${c.rotulo};${((comp[c]?.taxa ?: 0f) * 100).toInt()}%") }
                    appendLine("Adesão a simulados;${(adesao * 100).toInt()}% ($comSimulado de ${alunos.size})")
                }
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Relatório ENADE - $curso")
                    putExtra(Intent.EXTRA_TEXT, csv)
                }
                ctx.startActivity(Intent.createChooser(i, "Exportar relatório"))
            }) { Text("Exportar") }
        })
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Fluxo(espacoH = 8.dp) {
                Cursos.lista.forEach { c -> FilterChip(curso == c, { curso = c }, { Text(c) }) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CartaoMetrica("${alunos.size}", "Estudantes", Icons.Filled.Person, Modifier.weight(1f))
                CartaoMetrica("${(adesao * 100).toInt()}%", "Adesão a simulados", Icons.Filled.DateRange, Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
            }
            TituloSecao("Formação Geral x Conhecimento Específico")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Componente.entries.forEach { c -> BarraPercentual(c.rotulo, comp[c]?.taxa ?: 0f, detalhe = "${comp[c]?.total ?: 0} respostas") }
                }
            }
            TituloSecao("Áreas com maior índice de erros")
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    media.entries.sortedBy { it.value }.forEach { (d, t) ->
                        BarraPercentual(d, t, cor = if (t < 0.6f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            TituloSecao("Estudantes")
            alunos.forEach { a ->
                val s = vm.stat(vm.respostasDe(a.id))
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(a.nome, modifier = Modifier.weight(1f))
                    Text("${vm.historicoSimulados(a.id).size} simulados  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    StatusPill("${(s.taxa * 100).toInt()}%", tipoNota((s.taxa * 100).toInt()))
                }
                HorizontalDivider()
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
