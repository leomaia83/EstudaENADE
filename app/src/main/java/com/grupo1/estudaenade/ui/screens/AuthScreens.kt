@file:OptIn(ExperimentalMaterial3Api::class)

package com.grupo1.estudaenade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grupo1.estudaenade.AppViewModel
import com.grupo1.estudaenade.Tela
import com.grupo1.estudaenade.data.Cursos
import com.grupo1.estudaenade.data.Perfil
import com.grupo1.estudaenade.ui.components.BarraTopo
import com.grupo1.estudaenade.ui.components.Fluxo

@Composable
private fun Marca() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier.size(72.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp)) }
        Spacer(Modifier.height(12.dp))
        Text("Estuda ENADE", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Plataforma colaborativa de estudos para o ENADE",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
        )
    }
}

/** US01 · Login e cadastro institucional. */
@Composable
fun LoginScreen(vm: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var dialogoSso by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Marca()
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it; erro = null },
            label = { Text("E-mail institucional") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            singleLine = true, isError = erro != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = senha, onValueChange = { senha = it; erro = null },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                TextButton(onClick = { mostrarSenha = !mostrarSenha }) { Text(if (mostrarSenha) "Ocultar" else "Mostrar") }
            },
            visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true, isError = erro != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        if (erro != null) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(erro!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { erro = vm.login(email, senha) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Entrar", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { dialogoSso = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Entrar com conta institucional (SSO)")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { vm.navegar(Tela.Cadastro) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Não tem conta? Criar conta")
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        Text("Contas de demonstração", style = MaterialTheme.typography.titleSmall)
        Text("Toque para preencher (senha: 123456)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Fluxo(espacoH = 8.dp, espacoV = 4.dp) {
            listOf(
                "Estudante" to "lucas@aluno.edu.br", "Professor" to "roberto@prof.edu.br",
                "Coordenação" to "clara@coord.edu.br", "Admin" to "matheus@admin.edu.br"
            ).forEach { (rotulo, mail) ->
                AssistChip(onClick = { email = mail; senha = "123456"; erro = null }, label = { Text(rotulo) })
            }
        }
    }

    if (dialogoSso) {
        AlertDialog(
            onDismissRequest = { dialogoSso = false },
            title = { Text("Login institucional (simulação)") },
            text = {
                Column {
                    Text("No produto final, este botão abre o login único da faculdade. No protótipo, escolha uma conta:")
                    Spacer(Modifier.height(8.dp))
                    vm.dados.usuarios.filter { it.id in setOf("u_lucas", "u_prof", "u_coord", "u_admin") }.forEach { u ->
                        TextButton(onClick = { dialogoSso = false; vm.entrar(u) }, modifier = Modifier.fillMaxWidth()) {
                            Text("${u.nome} · ${u.perfil.rotulo}", modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { dialogoSso = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun CadastroScreen(vm: AppViewModel) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf(Perfil.ESTUDANTE) }
    var erro by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BarraTopo("Criar conta", onVoltar = { vm.voltar() }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            OutlinedTextField(nome, { nome = it; erro = null }, label = { Text("Nome completo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                email, { email = it; erro = null }, label = { Text("E-mail institucional") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                senha, { senha = it; erro = null }, label = { Text("Senha (mín. 6 caracteres)") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text("Eu sou", style = MaterialTheme.typography.titleSmall)
            Fluxo(espacoH = 8.dp) {
                listOf(Perfil.ESTUDANTE, Perfil.PROFESSOR, Perfil.COORDENADOR).forEach { p ->
                    FilterChip(selected = perfil == p, onClick = { perfil = p }, label = { Text(p.rotulo) })
                }
            }
            if (erro != null) {
                Spacer(Modifier.height(8.dp))
                Text(erro!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = { erro = vm.cadastrar(nome, email, senha, perfil) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Criar conta e continuar")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Seus dados são protegidos conforme a LGPD e usados apenas para personalizar seus estudos.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Primeiro acesso: confirma curso e ano previsto do ENADE (critério de aceitação da US01). */
@Composable
fun OnboardingScreen(vm: AppViewModel) {
    val u = vm.usuario
    var curso by remember { mutableStateOf(u?.curso ?: Cursos.lista.first()) }
    var ano by remember { mutableStateOf(u?.anoEnade ?: 2026) }
    var meta by remember { mutableStateOf(u?.metaDiariaMin ?: 30) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Olá, ${u?.primeiroNome ?: ""}!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Vamos personalizar seus estudos. Leva menos de 1 minuto.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        SeletorPerfilEstudo(curso, { curso = it }, ano, { ano = it }, meta, { meta = it })
        Spacer(Modifier.height(32.dp))
        Button(onClick = { vm.concluirOnboarding(curso, ano, meta) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Começar a estudar")
        }
    }
}

@Composable
fun SeletorPerfilEstudo(
    curso: String, onCurso: (String) -> Unit,
    ano: Int, onAno: (Int) -> Unit,
    meta: Int, onMeta: (Int) -> Unit
) {
    Text("1. Qual é o seu curso?", style = MaterialTheme.typography.titleMedium)
    Fluxo(espacoH = 8.dp) {
        Cursos.lista.forEach { c -> FilterChip(selected = curso == c, onClick = { onCurso(c) }, label = { Text(c) }) }
    }
    Spacer(Modifier.height(16.dp))
    Text("2. Em que ano você fará o ENADE?", style = MaterialTheme.typography.titleMedium)
    Fluxo(espacoH = 8.dp) {
        listOf(2026, 2027, 2028, 2029).forEach { a -> FilterChip(selected = ano == a, onClick = { onAno(a) }, label = { Text("$a") }) }
    }
    Spacer(Modifier.height(16.dp))
    Text("3. Meta diária de estudo", style = MaterialTheme.typography.titleMedium)
    Fluxo(espacoH = 8.dp) {
        listOf(10, 30, 60).forEach { m ->
            FilterChip(selected = meta == m, onClick = { onMeta(m) }, label = { Text(if (m == 60) "1 hora/dia" else "$m min/dia") })
        }
    }
}
