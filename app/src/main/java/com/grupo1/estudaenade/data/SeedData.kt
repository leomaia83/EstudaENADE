package com.grupo1.estudaenade.data

import kotlin.random.Random

/**
 * Dados de demonstração do protótipo.
 * As questões são ORIGINAIS, escritas no formato do ENADE para fins de prototipação;
 * na versão final seriam substituídas pelas provas oficiais publicadas pelo INEP.
 */
object SeedData {

    private const val DIA = 86_400_000L
    private const val FG = "Todos"

    private fun q(
        id: String, ano: Int, curso: String, comp: Componente, disciplina: String,
        enunciado: String, alts: List<String>, correta: Int, comentario: String
    ) = Questao(
        id = id, ano = ano, curso = curso, componente = comp, disciplina = disciplina,
        enunciado = enunciado, alternativas = alts, correta = correta, comentario = comentario,
        autorComentario = "Prof. Roberto Lima", comentarioVerificado = true, autorId = "u_prof"
    )

    val questoes: List<Questao> = listOf(
        // ---------------- FORMAÇÃO GERAL (todos os cursos) ----------------
        q("fg1", 2023, FG, Componente.FORMACAO_GERAL, "Sustentabilidade",
            "A Agenda 2030 da ONU estabelece 17 Objetivos de Desenvolvimento Sustentável (ODS). Uma universidade que implanta coleta seletiva, reduz o consumo de papel com processos digitais e promove campanhas de economia de energia contribui mais diretamente para qual ODS?",
            listOf("ODS 2 – Fome zero e agricultura sustentável", "ODS 12 – Consumo e produção responsáveis", "ODS 14 – Vida na água", "ODS 16 – Paz, justiça e instituições eficazes", "ODS 3 – Saúde e bem-estar"),
            1, "As ações descritas (coleta seletiva, redução de papel e economia de energia) tratam do uso racional de recursos e da redução de resíduos, que é o núcleo do ODS 12. Os demais ODS tratam de alimentação (2), oceanos (14), instituições (16) e saúde (3)."),
        q("fg2", 2022, FG, Componente.FORMACAO_GERAL, "Ética e Cidadania",
            "Segundo a Lei Geral de Proteção de Dados (Lei nº 13.709/2018), informações sobre origem racial ou étnica, convicção religiosa, opinião política, saúde e vida sexual de uma pessoa são classificadas como:",
            listOf("dados anonimizados", "dados públicos", "dados pessoais sensíveis", "dados cadastrais", "metadados"),
            2, "O art. 5º, II, da LGPD define esses dados como 'dados pessoais sensíveis', que exigem tratamento com proteção reforçada. Dados anonimizados são os que não permitem identificar o titular."),
        q("fg3", 2021, FG, Componente.FORMACAO_GERAL, "Interpretação de Dados",
            "Em uma turma, 40% dos alunos estudam pela manhã e 60% à noite. Entre os alunos da manhã, 75% foram aprovados; entre os da noite, 50%. Qual é o percentual de aprovados na turma inteira?",
            listOf("55%", "60%", "62,5%", "65%", "125%"),
            1, "É uma média ponderada: 0,40 × 75% + 0,60 × 50% = 30% + 30% = 60%. A média simples (62,5%) estaria errada porque os grupos têm tamanhos diferentes."),
        q("fg4", 2024, FG, Componente.FORMACAO_GERAL, "Diversidade e Inclusão",
            "No contexto da educação superior inclusiva, a chamada acessibilidade atitudinal refere-se:",
            listOf("à adaptação de rampas, elevadores e banheiros", "à eliminação de preconceitos, estigmas e discriminações nas relações entre as pessoas", "à disponibilização de softwares leitores de tela", "à tradução de materiais didáticos para Libras", "à adequação do mobiliário das salas de aula"),
            1, "A acessibilidade atitudinal trata do comportamento das pessoas: combater preconceitos e estigmas. Rampas e mobiliário são acessibilidade arquitetônica; leitores de tela e Libras são acessibilidade comunicacional/instrumental."),
        q("fg5", 2023, FG, Componente.FORMACAO_GERAL, "Tecnologia e Sociedade",
            "Diante do aumento da desinformação nas redes sociais, qual prática é mais eficaz para verificar a confiabilidade de uma notícia antes de compartilhá-la?",
            listOf("Verificar quantas vezes a notícia já foi compartilhada", "Consultar a fonte original e comparar com veículos confiáveis e agências de checagem", "Confiar na notícia se ela foi enviada por um conhecido", "Ler apenas o título para formar uma opinião rápida", "Verificar se a notícia contém imagens ou vídeos"),
            1, "Popularidade, quem enviou e a presença de imagens não garantem veracidade. A checagem da fonte original e a comparação com veículos e agências de verificação é a prática recomendada de letramento midiático."),
        q("fg6", 2022, FG, Componente.FORMACAO_GERAL, "Interpretação de Dados",
            "Um produto custava R$ 200,00. Ele sofreu um aumento de 10% e, no mês seguinte, um desconto de 10% sobre o novo preço. Qual é o preço final?",
            listOf("R$ 200,00", "R$ 198,00", "R$ 202,00", "R$ 180,00", "R$ 220,00"),
            1, "Após o aumento: 200 × 1,10 = R$ 220,00. Após o desconto: 220 × 0,90 = R$ 198,00. Aumento e desconto de mesmo percentual não se anulam, pois incidem sobre bases diferentes."),

        // ---------------- COMPUTAÇÃO ----------------
        q("c1", 2021, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Engenharia de Software",
            "No framework Scrum, qual papel é responsável por maximizar o valor do produto e gerenciar o Product Backlog?",
            listOf("Scrum Master", "Product Owner", "Developers", "Stakeholders", "Gerente de Projetos"),
            1, "Segundo o Guia do Scrum, o Product Owner é responsável por maximizar o valor do produto e pela gestão do Product Backlog. O Scrum Master garante que o Scrum seja compreendido e aplicado; os Developers criam o incremento."),
        q("c2", 2022, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Banco de Dados",
            "Em SQL, qual cláusula é utilizada para filtrar os grupos formados por GROUP BY a partir de funções de agregação, como COUNT ou SUM?",
            listOf("WHERE", "ORDER BY", "HAVING", "DISTINCT", "JOIN"),
            2, "WHERE filtra linhas antes do agrupamento; HAVING filtra grupos depois do GROUP BY e pode usar funções de agregação. Ex.: SELECT curso, COUNT(*) FROM aluno GROUP BY curso HAVING COUNT(*) > 30."),
        q("c3", 2023, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Algoritmos e Estruturas de Dados",
            "Qual é a complexidade de tempo, no pior caso, da busca binária em um vetor ordenado com n elementos?",
            listOf("O(1)", "O(log n)", "O(n)", "O(n log n)", "O(n²)"),
            1, "A cada comparação a busca binária descarta metade do vetor. O número máximo de passos é proporcional a log₂ n, logo O(log n)."),
        q("c4", 2021, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Redes de Computadores",
            "Qual protocolo da camada de transporte oferece entrega confiável, ordenada e orientada à conexão?",
            listOf("UDP", "IP", "TCP", "ICMP", "ARP"),
            2, "O TCP estabelece conexão (three-way handshake), confirma recebimento e reordena segmentos. O UDP é não orientado à conexão; IP, ICMP e ARP não são protocolos da camada de transporte."),
        q("c5", 2022, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Engenharia de Software",
            "Qual dos itens a seguir é um exemplo de requisito NÃO funcional?",
            listOf("O sistema deve permitir o cadastro de alunos", "O sistema deve gerar o boletim do aluno", "O sistema deve responder às consultas em até 2 segundos", "O sistema deve permitir filtrar questões por ano", "O usuário deve poder redefinir sua senha"),
            2, "Requisitos não funcionais descrevem qualidades do sistema (desempenho, segurança, usabilidade). Tempo de resposta é desempenho. As demais alternativas descrevem funcionalidades, ou seja, requisitos funcionais."),
        q("c6", 2023, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Programação Orientada a Objetos",
            "O princípio da orientação a objetos que oculta os detalhes internos de um objeto, expondo apenas uma interface pública para interação, é chamado de:",
            listOf("Herança", "Polimorfismo", "Encapsulamento", "Sobrecarga", "Recursão"),
            2, "Encapsulamento protege o estado interno do objeto (atributos privados) e expõe apenas métodos públicos. Herança reaproveita comportamento; polimorfismo permite tratar objetos diferentes pela mesma interface."),
        q("c7", 2024, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Banco de Dados",
            "Uma tabela relacional está na Primeira Forma Normal (1FN) quando:",
            listOf("não possui dependências transitivas", "todos os seus atributos são atômicos, sem grupos repetitivos", "não possui dependências parciais da chave", "possui apenas uma chave estrangeira", "todos os seus atributos fazem parte da chave primária"),
            1, "A 1FN exige atributos atômicos (indivisíveis) e ausência de grupos repetitivos. Eliminar dependências parciais é exigência da 2FN e eliminar dependências transitivas, da 3FN."),
        q("c8", 2024, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Segurança da Informação",
            "O ataque que insere comandos maliciosos em campos de entrada de uma aplicação para manipular as consultas feitas ao banco de dados é conhecido como:",
            listOf("Phishing", "DDoS", "SQL Injection", "Ransomware", "Man-in-the-middle"),
            2, "SQL Injection explora entradas não validadas concatenadas em consultas SQL. A principal defesa é o uso de consultas parametrizadas (prepared statements) e a validação das entradas."),
        q("c9", 2023, "Computação", Componente.CONHECIMENTO_ESPECIFICO, "Engenharia de Software",
            "Qual diagrama da UML representa as funcionalidades de um sistema do ponto de vista dos atores que interagem com ele?",
            listOf("Diagrama de Classes", "Diagrama de Sequência", "Diagrama de Casos de Uso", "Diagrama de Implantação", "Diagrama de Estados"),
            2, "O Diagrama de Casos de Uso mostra atores e as funcionalidades (casos de uso) que eles utilizam. É o diagrama comportamental mais usado na fase de requisitos."),

        // ---------------- ADMINISTRAÇÃO ----------------
        q("a1", 2021, "Administração", Componente.CONHECIMENTO_ESPECIFICO, "Gestão de Pessoas",
            "Segundo a hierarquia de necessidades de Maslow, qual nível está na base da pirâmide?",
            listOf("Autorrealização", "Estima", "Sociais", "Fisiológicas", "Segurança"),
            3, "Da base para o topo: fisiológicas, segurança, sociais, estima e autorrealização. As necessidades fisiológicas (alimentação, sono, abrigo) são as mais básicas."),
        q("a2", 2022, "Administração", Componente.CONHECIMENTO_ESPECIFICO, "Marketing",
            "Os 4 Ps do composto de marketing (mix de marketing), propostos por McCarthy, são:",
            listOf("Produto, Preço, Praça e Promoção", "Planejamento, Pessoas, Processos e Produto", "Preço, Prazo, Pessoas e Promoção", "Produto, Processo, Prazo e Publicidade", "Praça, Pesquisa, Preço e Parceria"),
            0, "O mix de marketing clássico é formado por Produto, Preço, Praça (distribuição) e Promoção (comunicação)."),
        q("a3", 2023, "Administração", Componente.CONHECIMENTO_ESPECIFICO, "Finanças",
            "Um investimento de R$ 1.000,00 aplicado a juros compostos de 10% ao ano, por 2 anos, resulta em um montante de:",
            listOf("R$ 1.200,00", "R$ 1.210,00", "R$ 1.100,00", "R$ 1.221,00", "R$ 1.020,00"),
            1, "M = C × (1 + i)ⁿ = 1.000 × 1,10² = 1.000 × 1,21 = R$ 1.210,00. Em juros simples o resultado seria R$ 1.200,00."),
        q("a4", 2024, "Administração", Componente.CONHECIMENTO_ESPECIFICO, "Estratégia",
            "Na análise SWOT, os quadrantes 'Oportunidades' e 'Ameaças' referem-se a:",
            listOf("fatores internos controláveis pela organização", "fatores do ambiente externo à organização", "metas financeiras de curto prazo", "indicadores de gestão de pessoas", "processos produtivos da empresa"),
            1, "Forças e Fraquezas são fatores internos; Oportunidades e Ameaças vêm do ambiente externo (mercado, concorrência, legislação, economia)."),
        q("a5", 2022, "Administração", Componente.CONHECIMENTO_ESPECIFICO, "Gestão de Pessoas",
            "Qual é o principal objetivo da avaliação de desempenho 360 graus?",
            listOf("Permitir que apenas o gestor direto avalie o colaborador", "Obter feedback de múltiplas fontes, como gestores, pares, subordinados, clientes e autoavaliação", "Definir automaticamente o salário do colaborador", "Substituir os programas de treinamento", "Medir exclusivamente a produtividade financeira"),
            1, "A avaliação 360° reúne percepções de todos os que se relacionam com o avaliado, reduzindo o viés de uma única fonte de avaliação."),

        // ---------------- ENGENHARIA ----------------
        q("e1", 2021, "Engenharia", Componente.CONHECIMENTO_ESPECIFICO, "Física e Mecânica",
            "Um corpo de massa 10 kg está sujeito a uma aceleração de 2 m/s². Qual é a força resultante que atua sobre ele?",
            listOf("5 N", "12 N", "20 N", "50 N", "200 N"),
            2, "Pela 2ª Lei de Newton, F = m × a = 10 kg × 2 m/s² = 20 N."),
        q("e2", 2022, "Engenharia", Componente.CONHECIMENTO_ESPECIFICO, "Resistência dos Materiais",
            "A tensão normal média em uma barra submetida a carga axial é calculada pela razão entre:",
            listOf("a força normal e a área da seção transversal", "o momento fletor e o comprimento da barra", "a deformação e a força aplicada", "a área da seção e a força normal", "a massa e o volume da barra"),
            0, "σ = N / A: força normal dividida pela área da seção transversal. A unidade no SI é o pascal (N/m²)."),
        q("e3", 2023, "Engenharia", Componente.CONHECIMENTO_ESPECIFICO, "Eletricidade",
            "Um resistor de 100 Ω é submetido a uma tensão de 12 V. Qual é a corrente elétrica que o atravessa?",
            listOf("0,012 A", "0,12 A", "1,2 A", "8,33 A", "1.200 A"),
            1, "Pela Lei de Ohm, I = V / R = 12 V / 100 Ω = 0,12 A (120 mA)."),
        q("e4", 2024, "Engenharia", Componente.CONHECIMENTO_ESPECIFICO, "Gestão de Projetos",
            "No método do caminho crítico (CPM), o caminho crítico de um projeto é:",
            listOf("o caminho de menor duração entre o início e o fim", "a sequência de atividades com folga zero que determina a duração total do projeto", "o caminho que consome mais recursos financeiros", "o conjunto das atividades de maior custo unitário", "o conjunto das atividades opcionais do projeto"),
            1, "O caminho crítico é o caminho mais longo da rede; suas atividades não têm folga, então qualquer atraso nelas atrasa o projeto inteiro."),
        q("e5", 2023, "Engenharia", Componente.CONHECIMENTO_ESPECIFICO, "Física e Mecânica",
            "Qual é a energia cinética de um corpo de 2 kg que se move a 3 m/s?",
            listOf("3 J", "6 J", "9 J", "18 J", "12 J"),
            2, "Ec = m × v² / 2 = 2 × 3² / 2 = 2 × 9 / 2 = 9 J.")
    )

    /** Questão enviada por professor aguardando aprovação do administrador (fluxo de moderação, US08). */
    private val questaoPendente = Questao(
        id = "c10", ano = 2024, curso = "Computação", componente = Componente.CONHECIMENTO_ESPECIFICO,
        disciplina = "Engenharia de Software",
        enunciado = "Na técnica de priorização MoSCoW, os requisitos classificados como 'Should have' são aqueles:",
        alternativas = listOf("obrigatórios para que a entrega seja considerada um sucesso", "importantes, mas não críticos para a entrega atual", "desejáveis apenas se houver tempo e recursos sobrando", "explicitamente fora do escopo desta entrega", "já implementados em versões anteriores"),
        correta = 1,
        comentario = "Must = obrigatório; Should = importante, mas a entrega ainda funciona sem ele; Could = desejável; Won't = fora do escopo agora.",
        autorComentario = "Prof. Roberto Lima", comentarioVerificado = true,
        status = StatusConteudo.PENDENTE, autorId = "u_prof"
    )

    fun gerar(agora: Long = System.currentTimeMillis()): Dados {
        val usuarios = mutableListOf(
            Usuario("u_lucas", "Lucas Andrade", "lucas@aluno.edu.br", "123456", Perfil.ESTUDANTE, "Computação", 2026, 30, true),
            Usuario("u_prof", "Prof. Roberto Lima", "roberto@prof.edu.br", "123456", Perfil.PROFESSOR, "Computação", null, 30, true),
            Usuario("u_coord", "Profª. Clara Souza", "clara@coord.edu.br", "123456", Perfil.COORDENADOR, "Computação", null, 30, true),
            Usuario("u_admin", "Matheus Ribeiro", "matheus@admin.edu.br", "123456", Perfil.ADMIN, null, null, 30, true)
        )
        // Colegas fictícios (geram "média do curso" e relatórios da coordenação)
        val colegas = listOf(
            Triple("Ana Martins", "Computação", 0.78), Triple("Bruno Costa", "Computação", 0.55),
            Triple("Carla Nunes", "Computação", 0.66), Triple("Diego Alves", "Computação", 0.48),
            Triple("Elisa Rocha", "Computação", 0.72), Triple("Fernanda Dias", "Administração", 0.62),
            Triple("Gustavo Reis", "Administração", 0.51), Triple("Helena Prado", "Engenharia", 0.69),
            Triple("Igor Teles", "Engenharia", 0.57)
        )
        colegas.forEachIndexed { i, (nome, curso, _) ->
            val email = nome.lowercase().split(" ").first() + "@aluno.edu.br"
            usuarios += Usuario("u_col$i", nome, email, "123456", Perfil.ESTUDANTE, curso, 2026, 30, true)
        }

        val rnd = Random(42)
        val respostas = mutableListOf<Resposta>()
        val simulados = mutableListOf<Simulado>()

        fun responder(uid: String, q: Questao, habilidade: Double, ts: Long, modo: Modo): Resposta {
            val acerta = rnd.nextDouble() < habilidade
            val alt = if (acerta) q.correta else (q.correta + 1 + rnd.nextInt(4)) % 5
            return Resposta(uid, q.id, alt, acerta, modo, ts)
        }

        fun simuladoPassado(uid: String, curso: String, habilidade: Double, diasAtras: Int, n: Int) {
            val pool = questoes.filter { it.pertenceAoCurso(curso) }.shuffled(rnd).take(n)
            val inicio = agora - diasAtras * DIA
            val resp = pool.map { responder(uid, it, habilidade, inicio + 60_000L, Modo.SIMULADO) }
            respostas += resp
            simulados += Simulado(
                id = "s_${uid}_$diasAtras", usuarioId = uid, questaoIds = pool.map { it.id },
                respostas = resp.associate { it.questaoId to it.alternativa },
                inicio = inicio, duracaoSeg = n * 360, indiceAtual = n - 1, finalizado = true,
                fim = inicio + n * 200_000L, acertos = resp.count { it.correta }
            )
        }

        colegas.forEachIndexed { i, (_, curso, hab) ->
            val uid = "u_col$i"
            questoes.filter { it.pertenceAoCurso(curso) }.forEach { q ->
                if (rnd.nextDouble() < 0.8) respostas += responder(uid, q, hab, agora - rnd.nextInt(1, 25) * DIA, Modo.PRATICA)
            }
            if (i % 3 != 2) simuladoPassado(uid, curso, hab, rnd.nextInt(2, 20), 8)
        }

        // Histórico do Lucas: evolução ao longo de 3 semanas + sequência de estudo
        simuladoPassado("u_lucas", "Computação", 0.40, 20, 10)
        simuladoPassado("u_lucas", "Computação", 0.55, 13, 10)
        simuladoPassado("u_lucas", "Computação", 0.70, 6, 10)
        val lucasPratica = listOf("c1" to 2, "c2" to 2, "fg3" to 1, "c5" to 1, "fg1" to 1, "c7" to 0)
        lucasPratica.forEach { (qid, diasAtras) ->
            val q = questoes.first { it.id == qid }
            val acerta = qid != "fg3" && qid != "c7"
            val alt = if (acerta) q.correta else (q.correta + 2) % 5
            respostas += Resposta("u_lucas", qid, alt, acerta, Modo.PRATICA, agora - diasAtras * DIA - 3_600_000L)
        }

        val posts = listOf(
            PostForum("p1", "c1", "u_col0", "Ana Martins", Perfil.ESTUDANTE,
                "Dica: o Scrum Master não gerencia o backlog. Ele remove impedimentos e ajuda o time a aplicar o Scrum.",
                setOf("u_col1", "u_col2", "u_col3"), false, agora - 5 * DIA),
            PostForum("p2", "c1", "u_prof", "Prof. Roberto Lima", Perfil.PROFESSOR,
                "Exato! E desde o Guia do Scrum de 2020 o termo 'Development Team' foi substituído por 'Developers'. Vale atenção na prova.",
                setOf("u_col0", "u_col1", "u_col2", "u_col4", "u_lucas"), false, agora - 4 * DIA),
            PostForum("p3", "fg3", "u_col1", "Bruno Costa", Perfil.ESTUDANTE,
                "Por que não é 62,5%? Fiz a média entre 75 e 50.",
                setOf("u_col3"), false, agora - 3 * DIA),
            PostForum("p4", "fg3", "u_col2", "Carla Nunes", Perfil.ESTUDANTE,
                "Porque os grupos têm tamanhos diferentes! Tem que ponderar: 40% da turma × 75% + 60% × 50% = 60%.",
                setOf("u_col1", "u_col3", "u_col4", "u_lucas"), false, agora - 2 * DIA),
            PostForum("p5", "c3", "u_col3", "Diego Alves", Perfil.ESTUDANTE,
                "VENDO RESUMOS PRONTOS DO ENADE, CHAMA NO PRIVADO!!!",
                emptySet(), true, agora - DIA)
        )

        return Dados(
            usuarios = usuarios,
            questoes = questoes + questaoPendente,
            respostas = respostas,
            simulados = simulados,
            posts = posts,
            avaliacoes = emptyList(),
            sessaoUsuarioId = null
        )
    }
}
