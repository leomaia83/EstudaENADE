package com.grupo1.estudaenade.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistência local (offline-first). Todo o estado é salvo no aparelho a cada alteração,
 * o que garante a retomada do simulado no ponto exato em que parou (RNF04).
 */
class Storage(context: Context) {

    private val prefs = context.getSharedPreferences("estuda_enade", Context.MODE_PRIVATE)

    fun carregar(): Dados? = try {
        prefs.getString(CHAVE, null)?.let { fromJson(JSONObject(it)) }
    } catch (e: Exception) {
        null
    }

    fun salvar(d: Dados) {
        prefs.edit().putString(CHAVE, toJson(d).toString()).apply()
    }

    companion object {
        private const val CHAVE = "estado_v1"

        private fun strList(a: JSONArray): List<String> = (0 until a.length()).map { a.getString(it) }
        private fun JSONObject.optStr(k: String): String? = if (isNull(k) || !has(k)) null else getString(k)

        fun toJson(d: Dados): JSONObject = JSONObject().apply {
            put("sessao", d.sessaoUsuarioId ?: JSONObject.NULL)
            put("usuarios", JSONArray(d.usuarios.map { u ->
                JSONObject().apply {
                    put("id", u.id); put("nome", u.nome); put("email", u.email); put("senha", u.senha)
                    put("perfil", u.perfil.name); put("curso", u.curso ?: JSONObject.NULL)
                    put("ano", u.anoEnade ?: JSONObject.NULL); put("meta", u.metaDiariaMin)
                    put("onb", u.onboardingConcluido)
                }
            }))
            put("questoes", JSONArray(d.questoes.map { q ->
                JSONObject().apply {
                    put("id", q.id); put("ano", q.ano); put("curso", q.curso); put("comp", q.componente.name)
                    put("disc", q.disciplina); put("enun", q.enunciado); put("alts", JSONArray(q.alternativas))
                    put("cor", q.correta); put("coment", q.comentario); put("autorC", q.autorComentario)
                    put("verif", q.comentarioVerificado); put("status", q.status.name)
                    put("autorId", q.autorId ?: JSONObject.NULL)
                }
            }))
            put("respostas", JSONArray(d.respostas.map { r ->
                JSONObject().apply {
                    put("u", r.usuarioId); put("q", r.questaoId); put("a", r.alternativa)
                    put("c", r.correta); put("m", r.modo.name); put("t", r.timestamp)
                }
            }))
            put("simulados", JSONArray(d.simulados.map { s ->
                JSONObject().apply {
                    put("id", s.id); put("u", s.usuarioId); put("qs", JSONArray(s.questaoIds))
                    put("resp", JSONObject().apply { s.respostas.forEach { (k, v) -> put(k, v) } })
                    put("ini", s.inicio); put("dur", s.duracaoSeg); put("idx", s.indiceAtual)
                    put("fin", s.finalizado); put("fim", s.fim ?: JSONObject.NULL); put("ac", s.acertos)
                    put("tempo", s.finalizadoPorTempo)
                }
            }))
            put("posts", JSONArray(d.posts.map { p ->
                JSONObject().apply {
                    put("id", p.id); put("q", p.questaoId); put("aId", p.autorId); put("aNome", p.autorNome)
                    put("aPerfil", p.autorPerfil.name); put("txt", p.texto); put("vot", JSONArray(p.votantes.toList()))
                    put("den", p.denunciado); put("t", p.timestamp)
                }
            }))
            put("avaliacoes", JSONArray(d.avaliacoes.map { a ->
                JSONObject().apply {
                    put("id", a.id); put("part", a.participante); put("perfil", a.perfil)
                    put("resp", JSONArray(a.respostas)); put("tarefas", JSONArray(a.tarefasConcluidas))
                    put("coment", a.comentario); put("t", a.timestamp)
                }
            }))
        }

        fun fromJson(o: JSONObject): Dados {
            val us = o.getJSONArray("usuarios")
            val qs = o.getJSONArray("questoes")
            val rs = o.getJSONArray("respostas")
            val ss = o.getJSONArray("simulados")
            val ps = o.getJSONArray("posts")
            val av = o.getJSONArray("avaliacoes")
            return Dados(
                usuarios = (0 until us.length()).map { i ->
                    val u = us.getJSONObject(i)
                    Usuario(
                        id = u.getString("id"), nome = u.getString("nome"), email = u.getString("email"),
                        senha = u.getString("senha"), perfil = Perfil.valueOf(u.getString("perfil")),
                        curso = u.optStr("curso"), anoEnade = if (u.isNull("ano")) null else u.getInt("ano"),
                        metaDiariaMin = u.getInt("meta"), onboardingConcluido = u.getBoolean("onb")
                    )
                },
                questoes = (0 until qs.length()).map { i ->
                    val q = qs.getJSONObject(i)
                    Questao(
                        id = q.getString("id"), ano = q.getInt("ano"), curso = q.getString("curso"),
                        componente = Componente.valueOf(q.getString("comp")), disciplina = q.getString("disc"),
                        enunciado = q.getString("enun"), alternativas = strList(q.getJSONArray("alts")),
                        correta = q.getInt("cor"), comentario = q.getString("coment"),
                        autorComentario = q.getString("autorC"), comentarioVerificado = q.getBoolean("verif"),
                        status = StatusConteudo.valueOf(q.getString("status")), autorId = q.optStr("autorId")
                    )
                },
                respostas = (0 until rs.length()).map { i ->
                    val r = rs.getJSONObject(i)
                    Resposta(
                        r.getString("u"), r.getString("q"), r.getInt("a"), r.getBoolean("c"),
                        Modo.valueOf(r.getString("m")), r.getLong("t")
                    )
                },
                simulados = (0 until ss.length()).map { i ->
                    val s = ss.getJSONObject(i)
                    val respObj = s.getJSONObject("resp")
                    val mapa = mutableMapOf<String, Int>()
                    respObj.keys().forEach { k -> mapa[k] = respObj.getInt(k) }
                    Simulado(
                        id = s.getString("id"), usuarioId = s.getString("u"), questaoIds = strList(s.getJSONArray("qs")),
                        respostas = mapa, inicio = s.getLong("ini"), duracaoSeg = s.getInt("dur"),
                        indiceAtual = s.getInt("idx"), finalizado = s.getBoolean("fin"),
                        fim = if (s.isNull("fim")) null else s.getLong("fim"), acertos = s.getInt("ac"),
                        finalizadoPorTempo = s.optBoolean("tempo", false)
                    )
                },
                posts = (0 until ps.length()).map { i ->
                    val p = ps.getJSONObject(i)
                    PostForum(
                        id = p.getString("id"), questaoId = p.getString("q"), autorId = p.getString("aId"),
                        autorNome = p.getString("aNome"), autorPerfil = Perfil.valueOf(p.getString("aPerfil")),
                        texto = p.getString("txt"), votantes = strList(p.getJSONArray("vot")).toSet(),
                        denunciado = p.getBoolean("den"), timestamp = p.getLong("t")
                    )
                },
                avaliacoes = (0 until av.length()).map { i ->
                    val a = av.getJSONObject(i)
                    val r = a.getJSONArray("resp")
                    val t = a.getJSONArray("tarefas")
                    AvaliacaoSus(
                        id = a.getString("id"), participante = a.getString("part"), perfil = a.getString("perfil"),
                        respostas = (0 until r.length()).map { r.getInt(it) },
                        tarefasConcluidas = (0 until t.length()).map { t.getBoolean(it) },
                        comentario = a.getString("coment"), timestamp = a.getLong("t")
                    )
                },
                sessaoUsuarioId = o.optStr("sessao")
            )
        }
    }
}
