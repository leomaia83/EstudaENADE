package com.grupo1.estudaenade.data

/**
 * Modelo de domínio da Plataforma Colaborativa de Estudos para o ENADE.
 * Rastreabilidade: RF01–RF10 (Entregável 4) e US01–US09 (Entregável 5).
 */

enum class Perfil(val rotulo: String) {
    ESTUDANTE("Estudante"),
    PROFESSOR("Professor"),
    COORDENADOR("Coordenador"),
    ADMIN("Administrador")
}

enum class Componente(val rotulo: String, val sigla: String) {
    FORMACAO_GERAL("Formação Geral", "FG"),
    CONHECIMENTO_ESPECIFICO("Conhecimento Específico", "CE")
}

enum class StatusConteudo { PUBLICADA, PENDENTE }

enum class Modo { PRATICA, SIMULADO }

object Cursos {
    const val TODOS = "Todos"
    val lista = listOf("Computação", "Administração", "Engenharia")
}

data class Usuario(
    val id: String,
    val nome: String,
    val email: String,
    val senha: String,
    val perfil: Perfil,
    val curso: String?,
    val anoEnade: Int?,
    val metaDiariaMin: Int = 30,
    val onboardingConcluido: Boolean = false
) {
    val primeiroNome: String get() = nome.trim().split(" ").firstOrNull { it.isNotBlank() && !it.endsWith(".") } ?: nome
}

data class Questao(
    val id: String,
    val ano: Int,
    /** Curso da questão; "Todos" para Formação Geral (comum a todos os cursos). */
    val curso: String,
    val componente: Componente,
    val disciplina: String,
    val enunciado: String,
    val alternativas: List<String>,
    val correta: Int,
    val comentario: String,
    val autorComentario: String,
    val comentarioVerificado: Boolean,
    val status: StatusConteudo = StatusConteudo.PUBLICADA,
    val autorId: String? = null
) {
    fun pertenceAoCurso(c: String?): Boolean =
        c == null || c == Cursos.TODOS || curso == Cursos.TODOS || curso == c
}

data class Resposta(
    val usuarioId: String,
    val questaoId: String,
    val alternativa: Int,
    val correta: Boolean,
    val modo: Modo,
    val timestamp: Long
)

data class Simulado(
    val id: String,
    val usuarioId: String,
    val questaoIds: List<String>,
    val respostas: Map<String, Int>,
    val inicio: Long,
    val duracaoSeg: Int,
    val indiceAtual: Int = 0,
    val finalizado: Boolean = false,
    val fim: Long? = null,
    val acertos: Int = 0,
    val finalizadoPorTempo: Boolean = false
) {
    val prazo: Long get() = inicio + duracaoSeg * 1000L
    val nota: Int get() = if (questaoIds.isEmpty()) 0 else (acertos * 100) / questaoIds.size
}

data class PostForum(
    val id: String,
    val questaoId: String,
    val autorId: String,
    val autorNome: String,
    val autorPerfil: Perfil,
    val texto: String,
    val votantes: Set<String> = emptySet(),
    val denunciado: Boolean = false,
    val timestamp: Long
) {
    val votos: Int get() = votantes.size
}

/** Avaliação de usabilidade (System Usability Scale) coletada na validação com usuários. */
data class AvaliacaoSus(
    val id: String,
    val participante: String,
    val perfil: String,
    val respostas: List<Int>,
    val tarefasConcluidas: List<Boolean>,
    val comentario: String,
    val timestamp: Long
) {
    val score: Double
        get() {
            if (respostas.size != 10) return 0.0
            var soma = 0
            respostas.forEachIndexed { i, r -> soma += if (i % 2 == 0) r - 1 else 5 - r }
            return soma * 2.5
        }
}

data class Dados(
    val usuarios: List<Usuario>,
    val questoes: List<Questao>,
    val respostas: List<Resposta>,
    val simulados: List<Simulado>,
    val posts: List<PostForum>,
    val avaliacoes: List<AvaliacaoSus>,
    val sessaoUsuarioId: String?
)

object Sus {
    val afirmacoes = listOf(
        "Eu acho que gostaria de usar este sistema com frequência.",
        "Eu achei o sistema desnecessariamente complexo.",
        "Eu achei o sistema fácil de usar.",
        "Eu acho que precisaria do apoio de uma pessoa com conhecimentos técnicos para usar o sistema.",
        "Eu acho que as várias funções do sistema estão muito bem integradas.",
        "Eu acho que o sistema apresenta muita inconsistência.",
        "Eu imagino que as pessoas aprenderão a usar este sistema rapidamente.",
        "Eu achei o sistema atrapalhado de usar.",
        "Eu me senti confiante ao usar o sistema.",
        "Eu precisei aprender várias coisas novas antes de conseguir usar o sistema."
    )

    /** Tarefas do roteiro de teste de usabilidade (uma por user story Must Have). */
    val tarefas = listOf(
        "T1 · Entrar no app e concluir o primeiro acesso (US01)",
        "T2 · Filtrar questões de Formação Geral de um ano específico (US02)",
        "T3 · Responder uma questão e ler o gabarito comentado (US04)",
        "T4 · Iniciar um simulado, sair do app e retomá-lo (US03)",
        "T5 · Publicar uma dúvida no fórum de uma questão (US05)"
    )

    fun classificacao(score: Double): String = when {
        score >= 85 -> "Excelente"
        score >= 73 -> "Bom"
        score >= 68 -> "Acima da média"
        score >= 52 -> "Aceitável"
        else -> "Precisa melhorar"
    }
}
