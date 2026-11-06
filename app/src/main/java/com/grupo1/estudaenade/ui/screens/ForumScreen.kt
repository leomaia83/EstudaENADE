@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.Perfil
import com.grupo1.estudaenade.data.PostForum
import com.grupo1.estudaenade.ui.components.BarraTopo
import com.grupo1.estudaenade.ui.components.DialogoConfirmacao
import com.grupo1.estudaenade.ui.components.EstadoVazio
import com.grupo1.estudaenade.ui.components.StatusPill
import com.grupo1.estudaenade.ui.theme.TipoStatus
import java.util.concurrent.TimeUnit

/** US05 · Fórum colaborativo por questão: publicar, votar (upvote) e denunciar. */
@Composable
fun ForumScreen(vm: AppViewModel, tela: Tela.Forum) {
    val q = vm.questao(tela.questaoId)
    if (q == null) { if (vm.telaAtual == tela) vm.voltar(); return }
    val posts = vm.postsDa(q.id)
    var texto by remember { mutableStateOf("") }
    var denunciar by remember { mutableStateOf<PostForum?>(null) }
    val uid = vm.usuario?.id

    Scaffold(
        topBar = { BarraTopo("Fórum da questão", onVoltar = { vm.voltar() }) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding().imePadding(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = texto, onValueChange = { texto = it },
                        placeholder = { Text("Escreva sua dúvida ou explicação") },
                        modifier = Modifier.weight(1f), maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(onClick = { if (vm.publicar(q.id, texto)) texto = "" }, enabled = texto.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Publicar")
                    }
                }
            }
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) {
                        CabecalhoQuestao(q)
                        Spacer(Modifier.height(8.dp))
                        Text(q.enunciado, maxLines = 4, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (posts.isEmpty()) {
                item { EstadoVazio(Icons.Filled.Face, "Nenhum comentário ainda", "Seja o primeiro a compartilhar uma dúvida ou explicação.") }
            }
            items(posts, key = { it.id }) { p ->
                CartaoPost(
                    p, votou = uid != null && uid in p.votantes, meu = p.autorId == uid,
                    admin = vm.usuario?.perfil == Perfil.ADMIN,
                    onVotar = { vm.alternarVoto(p.id) }, onDenunciar = { denunciar = p },
                    onModerar = { remover -> vm.moderarPost(p.id, remover) }
                )
            }
        }
    }

    denunciar?.let { p ->
        DialogoConfirmacao(
            "Denunciar mensagem?", "A mensagem de ${p.autorNome} será ocultada e enviada para análise da moderação.", "Denunciar",
            destrutivo = true, onConfirmar = { vm.denunciar(p.id); denunciar = null }, onCancelar = { denunciar = null }
        )
    }
}

fun tempoRelativo(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    val min = TimeUnit.MILLISECONDS.toMinutes(diff)
    return when {
        min < 1 -> "agora"
        min < 60 -> "há $min min"
        min < 1440 -> "há ${min / 60} h"
        else -> "há ${min / 1440} dias"
    }
}

@Composable
private fun CartaoPost(
    p: PostForum, votou: Boolean, meu: Boolean, admin: Boolean,
    onVotar: () -> Unit, onDenunciar: () -> Unit, onModerar: (Boolean) -> Unit
) {
    val prof = p.autorPerfil == Perfil.PROFESSOR
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (prof) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Text(p.autorNome.filter { it.isUpperCase() }.take(2), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.autorNome, style = MaterialTheme.typography.titleSmall)
                    Text(tempoRelativo(p.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (prof) StatusPill("Professor", TipoStatus.SUCESSO, Icons.Filled.CheckCircle)
                if (p.denunciado) StatusPill("Denunciado", TipoStatus.ALERTA, Icons.Filled.Warning)
            }
            Spacer(Modifier.height(8.dp))
            Text(p.texto, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onVotar) {
                    Icon(Icons.Filled.ThumbUp, null, tint = if (votou) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (votou) "Útil · ${p.votos}" else "Útil · ${p.votos}", fontWeight = if (votou) FontWeight.Bold else FontWeight.Normal)
                }
                Spacer(Modifier.weight(1f))
                if (admin && p.denunciado) {
                    TextButton(onClick = { onModerar(false) }) { Text("Manter") }
                    TextButton(onClick = { onModerar(true) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Remover") }
                } else if (!meu && !p.denunciado) {
                    TextButton(onClick = onDenunciar) { Text("Denunciar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}
