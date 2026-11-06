package com.grupo1.estudaenade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo1.estudaenade.ui.screens.*
import com.grupo1.estudaenade.ui.theme.EstudaEnadeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EstudaEnadeTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App(vm: AppViewModel = viewModel()) {
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(vm.mensagem) {
        vm.mensagem?.let { snackbar.showSnackbar(it); vm.mensagem = null }
    }
    // Ao voltar para o app, encerra simulados cujo tempo acabou enquanto ele estava fechado
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.verificarSimuladosExpirados() }

    BackHandler(enabled = vm.pilha.size > 1 || (vm.telaAtual == Tela.Principal && vm.abaAtual != Aba.INICIO)) {
        if (!vm.voltar()) vm.abaAtual = Aba.INICIO
    }

    Box(Modifier.fillMaxSize()) {
        val tela = vm.telaAtual
        val telaTopo = tela is Tela.Login || tela is Tela.Onboarding
        AnimatedContent(
            targetState = tela,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "navegacao",
            modifier = if (telaTopo) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier
        ) { t ->
            when (t) {
                Tela.Login -> LoginScreen(vm)
                Tela.Cadastro -> CadastroScreen(vm)
                Tela.Onboarding -> OnboardingScreen(vm)
                Tela.Principal -> PrincipalScreen(vm)
                is Tela.Resolver -> ResolverScreen(vm, t)
                is Tela.SimuladoExec -> SimuladoExecScreen(vm, t)
                is Tela.SimuladoResultado -> SimuladoResultadoScreen(vm, t)
                is Tela.Forum -> ForumScreen(vm, t)
                is Tela.EditarQuestao -> EditarQuestaoScreen(vm, t)
                Tela.AvaliacaoSus -> AvaliacaoSusScreen(vm)
                Tela.ResultadosSus -> ResultadosSusScreen(vm)
            }
        }
        SnackbarHost(
            snackbar,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 88.dp)
        )
    }
}
