# Estuda ENADE — Protótipo do MVP (Entregável 7)

App Android (Kotlin + Jetpack Compose + Material 3), 100% offline, que implementa o MVP
da Plataforma Colaborativa de Estudos para o ENADE.

## Como rodar
1. Abra esta pasta no Android Studio (File > Open).
2. Aguarde o Gradle sincronizar e clique em Run (emulador ou celular com Android 8+).

## Contas de demonstração (senha 123456)
| Perfil | E-mail |
|---|---|
| Estudante | lucas@aluno.edu.br |
| Professor | roberto@prof.edu.br |
| Coordenação | clara@coord.edu.br |
| Administrador | matheus@admin.edu.br |

## Rastreabilidade (User Story → tela)
| US | Prioridade | Onde está no app |
|---|---|---|
| US01 Login e cadastro | Must | Login, Criar conta, SSO simulado, Primeiro acesso (curso/ano/meta) |
| US02 Filtro do banco | Must | Aba Questões: filtros combinados por curso, componente, ano, disciplina, busca, "só as que errei" |
| US03 Simulado cronometrado | Must | Aba Simulado: cronômetro ocultável, mapa de questões, salvamento automático, finalização por tempo |
| US04 Gabarito comentado | Must | Prática: feedback imediato; Simulado: gabarito só no resultado |
| US08 Cadastro de conteúdo | Must | Aba Gestão (professor/admin): editor de questão + fila de moderação |
| US05 Fórum por questão | Should | Botão "Discutir com colegas": publicar, votar "Útil", denunciar |
| US06 Dashboard | Should | Aba Desempenho: radar por tópico x média do curso, evolução, FG x CE |
| US07 Relatórios | Should | Aba Relatórios (coordenação): adesão, áreas com mais erros, exportação |

## Validação com usuários
Perfil > "Aplicar avaliação de usabilidade (SUS)": checklist das 5 tarefas do roteiro
de teste + questionário SUS de 10 itens. Os resultados ficam em "Resultados da validação",
com exportação em CSV.

As questões incluídas são exemplos originais no formato do ENADE (para prototipação).
