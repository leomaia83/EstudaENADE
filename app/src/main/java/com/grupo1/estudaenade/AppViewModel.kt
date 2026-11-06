package com.grupo1.estudaenade

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.grupo1.estudaenade.data.AvaliacaoSus
import com.grupo1.estudaenade.data.Componente
import com.grupo1.estudaenade.data.Dados
import com.grupo1.estudaenade.data.Modo
import com.grupo1.estudaenade.data.Perfil
import com.grupo1.estudaenade.data.PostForum
import com.grupo1.estudaenade.data.Questao
import com.grupo1.estudaenade.data.Resposta
import com.grupo1.estudaenade.data.SeedData
import com.grupo1.estudaenade.data.Simulado
import com.grupo1.estudaenade.data.StatusConteudo
import com.grupo1.estudaenade.data.Storage
import com.grupo1.estudaenade.data.Usuario
import java.util.Calendar
import java.util.UUID

/** Telas empilháveis (navegação com botão Voltar). */
sealed class Tela {
    data object Login : Tela()
    data object Cadastro : Tela()
    data object Onboarding : Tela()
    data object Principal : Tela()
    data class Resolver(val ids: List<String>, val indice: Int) : Tela()
    data class SimuladoExec(val simId: String) : Tela()
    data class SimuladoResultado(val simId: String) : Tela()
    data class Forum(val questaoId: String) : Tela()
    data class EditarQuestao(val questaoId: String?) : Tela()
    data object AvaliacaoSus : Tela()
    data object ResultadosSus : Tela()
}

enum class Aba(val rotulo: String) {
    INICIO("Início"), QUESTOES("Questões"), SIMULADO("Simulado"),
    DESEMPENHO("Evolução"), GESTAO("Gestão"), RELATORIOS("Relatórios"), PERFIL("Perfil")
}

data class Stat(val total: Int, val acertos: Int) {
    val taxa: Float get() = if (total == 0) 0f else acertos.toFloat() / total
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = Storage(app)

    var dados by mutableStateOf(storage.carregar() ?: SeedData.gerar())
        private set

    val pilha = mutableStateListOf<Tela>()
    var abaAtual by mutableStateOf(Aba.INICIO)
    var tamanhoFonte by mutableStateOf(17f)
    var mensagem by mutableStateOf<String?>(null)

    // Filtros do banco de questões (mantidos entre navegações)
    var filtroCurso by mutableStateOf<String?>(null)
    var filtroComponente by mutableStateOf<Componente?>(null)
    var filtroAno by mutableStateOf<Int?>(null)
    var filtroDisciplina by mutableStateOf<String?>(null)
    var filtroBusca by mutableStateOf("")
    var somenteErradas by mutableStateOf(false)

    init {
        val u = usuario
        pilha += when {
            u == null -> Tela.Login
            !u.onboardingConcluido -> Tela.Onboarding
            else -> Tela.Principal
        }
        filtroCurso = u?.curso
        verificarSimuladosExpirados()
    }

    val usuario: Usuario? get() = dados.usuarios.firstOrNull { it.id == dados.sessaoUsuarioId }
    val telaAtual: Tela get() = pilha.last()

    private fun atualizar(novo: Dados) {
        dados = novo
        storage.salvar(novo)
    }

    fun avisar(texto: String) { mensagem = texto }

    // ------------------------------------------------------------------ Navegação
    fun navegar(t: Tela) { pilha += t }
    fun substituir(t: Tela) { pilha.removeAt(pilha.lastIndex); pilha += t }
    fun voltar(): Boolean = if (pilha.size > 1) { pilha.removeAt(pilha.lastIndex); true } else false
    private fun reiniciarEm(t: Tela) { pilha.clear(); pilha += t }

    fun abasDoPerfil(p: Perfil): List<Aba> = when (p) {
        Perfil.ESTUDANTE -> listOf(Aba.INICIO, Aba.QUESTOES, Aba.SIMULADO, Aba.DESEMPENHO, Aba.PERFIL)
        Perfil.PROFESSOR, Perfil.ADMIN -> listOf(Aba.INICIO, Aba.QUESTOES, Aba.SIMULADO, Aba.GESTAO, Aba.PERFIL)
        Perfil.COORDENADOR -> listOf(Aba.INICIO, Aba.QUESTOES, Aba.SIMULADO, Aba.RELATORIOS, Aba.PERFIL)
    }

    // ------------------------------------------------------------------ US01 · Autenticação
    fun emailValido(email: String) = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$").matches(email.trim())

    fun login(email: String, senha: String): String? {
        if (!emailValido(email)) return "Digite um e-mail válido (ex.: nome@aluno.edu.br)."
        if (senha.isBlank()) return "Digite sua senha."
        val u = dados.usuarios.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return "Não encontramos uma conta com este e-mail."
        if (u.senha != senha) return "Senha incorreta. Tente novamente."
        entrar(u)
        return null
    }

    fun entrar(u: Usuario) {
        atualizar(dados.copy(sessaoUsuarioId = u.id))
        filtroCurso = u.curso
        filtroAno = null; filtroDisciplina = null; filtroComponente = null; filtroBusca = ""; somenteErradas = false
        abaAtual = Aba.INICIO
        reiniciarEm(if (u.onboardingConcluido) Tela.Principal else Tela.Onboarding)
        verificarSimuladosExpirados()
    }

    fun cadastrar(nome: String, email: String, senha: String, perfil: Perfil): String? {
        if (nome.trim().length < 3) return "Informe seu nome completo."
        if (!emailValido(email)) return "Digite um e-mail válido."
        if (senha.length < 6) return "A senha deve ter pelo menos 6 caracteres."
        if (dados.usuarios.any { it.email.equals(email.trim(), true) }) return "Já existe uma conta com este e-mail."
        val u = Usuario(
            id = "u_" + UUID.randomUUID().toString().take(8), nome = nome.trim(), email = email.trim().lowercase(),
            senha = senha, perfil = perfil, curso = null, anoEnade = null, onboardingConcluido = false
        )
        atualizar(dados.copy(usuarios = dados.usuarios + u))
        entrar(u)
        return null
    }

    fun concluirOnboarding(curso: String, ano: Int, meta: Int) {
        val u = usuario ?: return
        val novo = u.copy(curso = curso, anoEnade = ano, metaDiariaMin = meta, onboardingConcluido = true)
        atualizar(dados.copy(usuarios = dados.usuarios.map { if (it.id == u.id) novo else it }))
        filtroCurso = curso
        reiniciarEm(Tela.Principal)
    }

    fun atualizarPerfil(curso: String, ano: Int, meta: Int) {
        val u = usuario ?: return
        val novo = u.copy(curso = curso, anoEnade = ano, metaDiariaMin = meta)
        atualizar(dados.copy(usuarios = dados.usuarios.map { if (it.id == u.id) novo else it }))
        filtroCurso = curso
        avisar("Perfil atualizado")
    }

    fun sair() {
        atualizar(dados.copy(sessaoUsuarioId = null))
        reiniciarEm(Tela.Login)
    }

    fun restaurarDemo() {
        atualizar(SeedData.gerar().copy(avaliacoes = dados.avaliacoes))
        reiniciarEm(Tela.Login)
        avisar("Dados de demonstração restaurados (avaliações SUS preservadas)")
    }

    // ------------------------------------------------------------------ US02 · Banco de questões
    fun questao(id: String): Questao? = dados.questoes.firstOrNull { it.id == id }

    val questoesPublicadas: List<Questao> get() = dados.questoes.filter { it.status == StatusConteudo.PUBLICADA }

    fun ultimaResposta(questaoId: String, uid: String? = usuario?.id): Resposta? =
        dados.respostas.filter { it.usuarioId == uid && it.questaoId == questaoId }.maxByOrNull { it.timestamp }

    fun filtrar(
        curso: String? = filtroCurso, comp: Componente? = filtroComponente, ano: Int? = filtroAno,
        disc: String? = filtroDisciplina, busca: String = filtroBusca, erradas: Boolean = somenteErradas
    ): List<Questao> = questoesPublicadas.filter { q ->
        q.pertenceAoCurso(curso) &&
            (comp == null || q.componente == comp) &&
            (ano == null || q.ano == ano) &&
            (disc == null || q.disciplina == disc) &&
            (busca.isBlank() || q.enunciado.contains(busca, true) || q.disciplina.contains(busca, true)) &&
            (!erradas || ultimaResposta(q.id)?.correta == false)
    }.sortedWith(compareBy<Questao> { it.componente.ordinal }.thenByDescending { it.ano })

    fun disciplinasDisponiveis(): List<String> =
        filtrar(disc = null, erradas = false).map { it.disciplina }.distinct().sorted()

    fun anosDisponiveis(): List<Int> = questoesPublicadas.map { it.ano }.distinct().sorted()

    fun limparFiltros() {
        filtroComponente = null; filtroAno = null; filtroDisciplina = null; filtroBusca = ""; somenteErradas = false
    }

    // ------------------------------------------------------------------ US04 · Prática + gabarito
    fun responderPratica(questaoId: String, alternativa: Int): Boolean {
        val u = usuario ?: return false
        val q = questao(questaoId) ?: return false
        val certo = alternativa == q.correta
        atualizar(dados.copy(respostas = dados.respostas + Resposta(u.id, q.id, alternativa, certo, Modo.PRATICA, System.currentTimeMillis())))
        return certo
    }

    fun treinoRapido(): List<String> {
        val u = usuario ?: return emptyList()
        val respondidas = dados.respostas.filter { it.usuarioId == u.id }.map { it.questaoId }.toSet()
        val pool = questoesPublicadas.filter { it.pertenceAoCurso(u.curso) }
        val novas = pool.filter { it.id !in respondidas }.shuffled()
        return (novas + pool.filter { it.id in respondidas }.shuffled()).take(5).map { it.id }
    }

    fun idsErradas(): List<String> {
        val u = usuario ?: return emptyList()
        return questoesPublicadas.filter { it.pertenceAoCurso(u.curso) && ultimaResposta(it.id)?.correta == false }.map { it.id }
    }

    // ------------------------------------------------------------------ US03 · Simulado cronometrado
    fun simuladoAtivo(): Simulado? {
        val u = usuario ?: return null
        return dados.simulados.lastOrNull { it.usuarioId == u.id && !it.finalizado }
    }

    fun simulado(id: String): Simulado? = dados.simulados.firstOrNull { it.id == id }

    fun iniciarSimulado(curso: String?, quantidade: Int, duracaoSeg: Int) {
        val u = usuario ?: return
        simuladoAtivo()?.let { finalizarSimulado(it.id) }
        val pool = questoesPublicadas.filter { it.pertenceAoCurso(curso) }
        val fg = pool.filter { it.componente == Componente.FORMACAO_GERAL }.shuffled()
        val ce = pool.filter { it.componente == Componente.CONHECIMENTO_ESPECIFICO }.shuffled()
        // Proporção do ENADE: 25% Formação Geral, 75% Conhecimento Específico
        val nFg = (quantidade / 4).coerceAtLeast(1).coerceAtMost(fg.size)
        val escolhidas = (fg.take(nFg) + ce.take(quantidade - nFg)).let {
            if (it.size < quantidade) it + (pool - it.toSet()).shuffled().take(quantidade - it.size) else it
        }
        val s = Simulado(
            id = "s_" + UUID.randomUUID().toString().take(8), usuarioId = u.id,
            questaoIds = escolhidas.map { it.id }, respostas = emptyMap(),
            inicio = System.currentTimeMillis(), duracaoSeg = duracaoSeg
        )
        atualizar(dados.copy(simulados = dados.simulados + s))
        navegar(Tela.SimuladoExec(s.id))
    }

    private fun atualizarSimulado(id: String, f: (Simulado) -> Simulado) {
        atualizar(dados.copy(simulados = dados.simulados.map { if (it.id == id) f(it) else it }))
    }

    /** Salvamento automático a cada resposta (RNF04). */
    fun responderSimulado(simId: String, questaoId: String, alternativa: Int) {
        atualizarSimulado(simId) { it.copy(respostas = it.respostas + (questaoId to alternativa)) }
    }

    fun irParaQuestao(simId: String, indice: Int) {
        atualizarSimulado(simId) { it.copy(indiceAtual = indice.coerceIn(0, it.questaoIds.lastIndex)) }
    }

    fun finalizarSimulado(simId: String, porTempo: Boolean = false) {
        val s = simulado(simId) ?: return
        if (s.finalizado) return
        val agora = System.currentTimeMillis()
        val novas = s.questaoIds.mapNotNull { qid ->
            val alt = s.respostas[qid] ?: return@mapNotNull null
            val q = questao(qid) ?: return@mapNotNull null
            Resposta(s.usuarioId, qid, alt, alt == q.correta, Modo.SIMULADO, agora)
        }
        val fim = if (porTempo) s.prazo else agora
        val atualizado = s.copy(finalizado = true, fim = fim, acertos = novas.count { it.correta }, finalizadoPorTempo = porTempo)
        atualizar(dados.copy(
            simulados = dados.simulados.map { if (it.id == simId) atualizado else it },
            respostas = dados.respostas + novas
        ))
    }

    fun verificarSimuladosExpirados() {
        val agora = System.currentTimeMillis()
        dados.simulados.filter { !it.finalizado && agora >= it.prazo }.forEach { finalizarSimulado(it.id, porTempo = true) }
    }

    fun historicoSimulados(uid: String? = usuario?.id): List<Simulado> =
        dados.simulados.filter { it.usuarioId == uid && it.finalizado }.sortedBy { it.inicio }

    // ------------------------------------------------------------------ US05 · Fórum
    fun postsDa(questaoId: String): List<PostForum> {
        val admin = usuario?.perfil == Perfil.ADMIN
        return dados.posts.filter { it.questaoId == questaoId && (!it.denunciado || admin) }
            .sortedWith(compareByDescending<PostForum> { it.autorPerfil == Perfil.PROFESSOR }.thenByDescending { it.votos })
    }

    fun publicar(questaoId: String, texto: String): Boolean {
        val u = usuario ?: return false
        if (texto.isBlank()) return false
        val p = PostForum("p_" + UUID.randomUUID().toString().take(8), questaoId, u.id, u.nome, u.perfil, texto.trim(), timestamp = System.currentTimeMillis())
        atualizar(dados.copy(posts = dados.posts + p))
        return true
    }

    fun alternarVoto(postId: String) {
        val uid = usuario?.id ?: return
        atualizar(dados.copy(posts = dados.posts.map {
            if (it.id != postId) it else it.copy(votantes = if (uid in it.votantes) it.votantes - uid else it.votantes + uid)
        }))
    }

    fun denunciar(postId: String) {
        atualizar(dados.copy(posts = dados.posts.map { if (it.id == postId) it.copy(denunciado = true) else it }))
        avisar("Obrigado! A mensagem foi enviada para a moderação.")
    }

    fun moderarPost(postId: String, remover: Boolean) {
        atualizar(dados.copy(posts = if (remover) dados.posts.filterNot { it.id == postId }
        else dados.posts.map { if (it.id == postId) it.copy(denunciado = false) else it }))
        avisar(if (remover) "Mensagem removida" else "Mensagem mantida no fórum")
    }

    // ------------------------------------------------------------------ US08 · Gestão de conteúdo
    fun salvarQuestao(q: Questao) {
        val u = usuario ?: return
        val status = if (u.perfil == Perfil.ADMIN) StatusConteudo.PUBLICADA else StatusConteudo.PENDENTE
        val salva = q.copy(
            status = status,
            autorComentario = u.nome,
            comentarioVerificado = u.perfil == Perfil.PROFESSOR || q.comentarioVerificado,
            autorId = q.autorId ?: u.id
        )
        val existe = dados.questoes.any { it.id == q.id }
        atualizar(dados.copy(questoes = if (existe) dados.questoes.map { if (it.id == q.id) salva else it } else dados.questoes + salva))
        avisar(if (status == StatusConteudo.PUBLICADA) "Questão publicada" else "Questão enviada para aprovação do administrador")
    }

    fun aprovarQuestao(id: String) {
        atualizar(dados.copy(questoes = dados.questoes.map { if (it.id == id) it.copy(status = StatusConteudo.PUBLICADA) else it }))
        avisar("Questão aprovada e publicada")
    }

    fun excluirQuestao(id: String) {
        atualizar(dados.copy(
            questoes = dados.questoes.filterNot { it.id == id },
            posts = dados.posts.filterNot { it.questaoId == id }
        ))
        avisar("Questão excluída")
    }

    val pendentes: List<Questao> get() = dados.questoes.filter { it.status == StatusConteudo.PENDENTE }
    val denunciados: List<PostForum> get() = dados.posts.filter { it.denunciado }

    // ------------------------------------------------------------------ US06/US07 · Desempenho e relatórios
    fun respostasDe(uid: String?): List<Resposta> = dados.respostas.filter { it.usuarioId == uid }

    fun stat(rs: List<Resposta>) = Stat(rs.size, rs.count { it.correta })

    fun statPorDisciplina(uid: String?, curso: String?): Map<String, Stat> {
        val rs = respostasDe(uid)
        return questoesPublicadas.filter { it.pertenceAoCurso(curso) }.map { it.disciplina }.distinct().sorted()
            .associateWith { d ->
                val ids = dados.questoes.filter { it.disciplina == d }.map { it.id }.toSet()
                stat(rs.filter { it.questaoId in ids })
            }
    }

    fun mediaCursoPorDisciplina(curso: String?, excluir: String?): Map<String, Float> {
        val alunos = dados.usuarios.filter { it.perfil == Perfil.ESTUDANTE && it.curso == curso && it.id != excluir }.map { it.id }.toSet()
        val rs = dados.respostas.filter { it.usuarioId in alunos }
        return questoesPublicadas.filter { it.pertenceAoCurso(curso) }.map { it.disciplina }.distinct().sorted()
            .associateWith { d ->
                val ids = dados.questoes.filter { it.disciplina == d }.map { it.id }.toSet()
                stat(rs.filter { it.questaoId in ids }).taxa
            }
    }

    fun statPorComponente(uids: Set<String>): Map<Componente, Stat> {
        val rs = dados.respostas.filter { it.usuarioId in uids }
        return Componente.entries.associateWith { c ->
            val ids = dados.questoes.filter { it.componente == c }.map { it.id }.toSet()
            stat(rs.filter { it.questaoId in ids })
        }
    }

    fun sequenciaDias(uid: String?): Int {
        val dias = respostasDe(uid).map { diaDe(it.timestamp) }.toSet()
        var d = diaDe(System.currentTimeMillis())
        if (d !in dias) d -= 1
        var n = 0
        while (d in dias) { n++; d -= 1 }
        return n
    }

    fun respondidasHoje(uid: String?): Int {
        val hoje = diaDe(System.currentTimeMillis())
        return respostasDe(uid).count { diaDe(it.timestamp) == hoje }
    }

    private fun diaDe(ts: Long): Long {
        val c = Calendar.getInstance().apply { timeInMillis = ts }
        val off = c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)
        return (ts + off) / 86_400_000L
    }

    fun alunosDoCurso(curso: String?): List<Usuario> =
        dados.usuarios.filter { it.perfil == Perfil.ESTUDANTE && it.curso == curso }

    // ------------------------------------------------------------------ Validação (SUS)
    fun salvarAvaliacao(a: AvaliacaoSus) {
        atualizar(dados.copy(avaliacoes = dados.avaliacoes + a))
    }

    fun excluirAvaliacao(id: String) {
        atualizar(dados.copy(avaliacoes = dados.avaliacoes.filterNot { it.id == id }))
    }

    fun novoId(prefixo: String) = prefixo + UUID.randomUUID().toString().take(8)
}
