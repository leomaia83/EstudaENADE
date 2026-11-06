@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo1.estudaenade.Aba
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.Perfil
import com.grupo1.estudaenade.ui.components.CartaoMetrica
import com.grupo1.estudaenade.ui.components.StatusPill
import com.grupo1.estudaenade.ui.components.TituloSecao
import com.grupo1.estudaenade.ui.theme.TipoStatus
import kotlinx.coroutines.delay

/** Estrutura principal com barra de navegação inferior (ações na zona do polegar, RNF01). */
@Composable
fun PrincipalScreen(vm: AppViewModel) {
    val u = vm.usuario ?: return
    val abas = vm.abasDoPerfil(u.perfil)
    if (vm.abaAtual !in abas) vm.abaAtual = Aba.INICIO

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                abas.forEach { aba ->
                    NavigationBarItem(
                        selected = vm.abaAtual == aba,
                        onClick = { vm.abaAtual = aba },
                        icon = { Icon(iconeAba(aba), contentDescription = null) },
                        label = { Text(aba.rotulo, maxLines = 1) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (vm.abaAtual) {
                Aba.INICIO -> InicioTab(vm)
                Aba.QUESTOES -> BancoQuestoesTab(vm)
                Aba.SIMULADO -> SimuladoTab(vm)
                Aba.DESEMPENHO -> DesempenhoTab(vm)
                Aba.GESTAO -> GestaoTab(vm)
                Aba.RELATORIOS -> RelatoriosTab(vm)
                Aba.PERFIL -> PerfilTab(vm)
            }
        }
    }
}

private fun iconeAba(a: Aba) = when (a) {
    Aba.INICIO -> Icons.Filled.Home
    Aba.QUESTOES -> Icons.Filled.Search
    Aba.SIMULADO -> Icons.Filled.DateRange
    Aba.DESEMPENHO -> Icons.Filled.Star
    Aba.GESTAO -> Icons.Filled.Build
    Aba.RELATORIOS -> Icons.AutoMirrored.Filled.List
    Aba.PERFIL -> Icons.Filled.Person
}

fun formatarTempo(seg: Long): String {
    val s = seg.coerceAtLeast(0)
    val h = s / 3600; val m = (s % 3600) / 60; val ss = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, ss) else "%02d:%02d".format(m, ss)
}

@Composable
fun InicioTab(vm: AppViewModel) {
    val u = vm.usuario ?: return
    val ativo = vm.simuladoAtivo()
    var agora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(ativo?.id) {
        while (ativo != null) {
            agora = System.currentTimeMillis()
            if (agora >= ativo.prazo) { vm.verificarSimuladosExpirados(); vm.avisar("Tempo esgotado: seu simulado foi corrigido automaticamente"); break }
            delay(1000)
        }
    }

    val rs = vm.respostasDe(u.id)
    val st = vm.stat(rs)
    val metaQuestoes = (u.metaDiariaMin / 3).coerceAtLeast(3)
    val hoje = vm.respondidasHoje(u.id)

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // Cabeçalho dinâmico
        Text("Olá, ${u.primeiroNome}!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                listOfNotNull(u.perfil.rotulo, u.curso, u.anoEnade?.let { "ENADE $it" }).joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(16.dp))

        // Card de ação rápida (Hero)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                if (ativo != null) {
                    val restante = (ativo.prazo - agora) / 1000
                    Text("Simulado em andamento", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Questão ${ativo.indiceAtual + 1} de ${ativo.questaoIds.size} · ${ativo.respostas.size} respondidas · ${formatarTempo(restante)} restantes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { vm.navegar(Tela.SimuladoExec(ativo.id)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary)
                    ) { Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Continuar de onde parei") }
                } else {
                    Text("Treino rápido de hoje", style = MaterialTheme.typography.titleLarge)
                    Text("5 questões do seu curso, com gabarito comentado na hora.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { val ids = vm.treinoRapido(); if (ids.isNotEmpty()) vm.navegar(Tela.Resolver(ids, 0)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary, contentColor = MaterialTheme.colorScheme.primary)
                    ) { Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Começar treino") }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (u.perfil == Perfil.ESTUDANTE) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CartaoMetrica("${vm.sequenciaDias(u.id)} dias", "Sequência de estudo", Icons.Filled.Favorite, Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                CartaoMetrica("${(st.taxa * 100).toInt()}%", "Acertos (${st.total} questões)", Icons.Filled.CheckCircle, Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Meta de hoje", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (hoje >= metaQuestoes) StatusPill("Meta batida", TipoStatus.SUCESSO, Icons.Filled.Check)
                        else Text("$hoje de $metaQuestoes questões", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { (hoje.toFloat() / metaQuestoes).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp))
                }
            }
            val erradas = vm.idsErradas()
            if (erradas.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                AcaoCard(
                    Icons.Filled.Refresh, "Revisar meus erros",
                    "${erradas.size} questões que você errou recentemente", "Revisar"
                ) { vm.navegar(Tela.Resolver(erradas, 0)) }
            }
        }

        if (u.perfil == Perfil.ADMIN || u.perfil == Perfil.PROFESSOR) {
            Spacer(Modifier.height(4.dp))
            val pend = vm.pendentes.size + if (u.perfil == Perfil.ADMIN) vm.denunciados.size else 0
            AcaoCard(
                Icons.Filled.Notifications, if (u.perfil == Perfil.ADMIN) "Fila de moderação" else "Gestão de conteúdo",
                if (u.perfil == Perfil.ADMIN) if (pend == 1) "1 item aguardando sua análise" else "$pend itens aguardando sua análise" else "${vm.pendentes.count { it.autorId == u.id }} questões suas aguardando aprovação",
                "Abrir"
            ) { vm.abaAtual = Aba.GESTAO }
        }
        if (u.perfil == Perfil.COORDENADOR) {
            Spacer(Modifier.height(4.dp))
            AcaoCard(Icons.AutoMirrored.Filled.List, "Relatório da turma", "Adesão aos simulados e áreas com mais erros em ${u.curso}", "Ver") { vm.abaAtual = Aba.RELATORIOS }
        }

        Spacer(Modifier.height(12.dp))
        TituloSecao("Você sabia?")
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp)) {
                Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.width(12.dp))
                Text(
                    "A prova do ENADE tem 40 questões: 10 de Formação Geral (comuns a todos os cursos) e 30 de Componente Específico da sua área.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun AcaoCard(icone: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, texto: String, botao: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(onClick = onClick) { Text(botao) }
        }
    }
}
