# FilmeDaVez 🎬

Aplicativo Android desenvolvido em **Kotlin e Jetpack Compose** para ajudar a escolher o próximo filme para assistir.

## Sobre o projeto

O FilmeDaVez surgiu de uma situação bem comum entre meus amigos: passar mais tempo tentando decidir qual filme assistir do que realmente assistindo alguma coisa.

A ideia foi criar um aplicativo onde pudéssemos selecionar nossos gêneros favoritos, informar temas que gostaríamos de encontrar e deixar o app buscar opções compatíveis. Depois disso, basta sortear um filme.

Desenvolvi o projeto principalmente para colocar em prática meus estudos de desenvolvimento Android, trabalhando com consumo de APIs, arquitetura MVVM, programação assíncrona, gerenciamento de estados e construção de interfaces com Jetpack Compose.

Ao longo do desenvolvimento, o projeto também passou a envolver desafios de desempenho e tratamento de erros, principalmente para conseguir realizar pesquisas em um catálogo grande sem deixar o usuário esperando muito tempo.

## Como funciona

O funcionamento é dividido em algumas etapas:

1. **Seleção de gêneros:** o usuário escolhe um ou mais gêneros, como ação, suspense, comédia ou ficção científica.
2. **Palavras-chave:** é possível adicionar até cinco temas, como "assassino", "viagem no tempo" ou "inteligência artificial". Essa etapa é opcional.
3. **Filtros adicionais:** o usuário pode definir avaliação mínima e máxima no TMDB, ano de lançamento, duração e país de origem.
4. **Pesquisa:** o aplicativo consulta a API do TMDB, reúne filmes compatíveis e organiza os resultados conforme as preferências escolhidas.
5. **Sorteio:** com os resultados encontrados, o usuário pode sortear um filme e, quando houver mais opções, realizar novos sorteios.
6. **Detalhes:** o filme sorteado é apresentado com pôster, título, sinopse, avaliação TMDB, lançamento, duração e gêneros.
7. **Onde assistir:** o aplicativo consulta as opções cadastradas para o Brasil, incluindo streaming por assinatura, aluguel, compra e modalidades gratuitas, quando disponíveis.

A pesquisa retorna **até 10 filmes compatíveis**. Quando encontra somente um, não é necessário realizar um sorteio entre opções. Quando não encontra nenhum, o aplicativo informa o usuário.

O objetivo não é limitar as recomendações aos filmes disponíveis em serviços de streaming. Filmes antigos sem disponibilidade digital cadastrada também podem ser sugeridos, já que a ideia é ajudar a descobrir algo interessante para assistir ou pesquisar depois.

## Tecnologias utilizadas

| Tecnologia | Aplicação no projeto |
|---|---|
| Kotlin | Linguagem principal do aplicativo |
| Jetpack Compose | Construção das interfaces de forma declarativa |
| Material Design 3 | Componentes e padronização visual |
| MVVM | Separação entre interface, estados e lógica de apresentação |
| ViewModel | Gerenciamento das operações e estados das telas |
| StateFlow | Atualização reativa das informações exibidas |
| Kotlin Coroutines | Execução assíncrona das pesquisas e controle de cancelamento |
| Retrofit | Comunicação com a API REST do TMDB |
| Gson | Conversão das respostas JSON em objetos Kotlin |
| OkHttp | Configuração do cliente HTTP e autenticação |
| Coil | Carregamento de pôsteres e logotipos das plataformas |
| Gradle | Configuração de dependências e compilação |
| Git e GitHub | Versionamento e acompanhamento da evolução do projeto |

A fonte de dados utilizada é a **[API do The Movie Database (TMDB)](https://developer.themoviedb.org/)**.

## Arquitetura e organização do código

Utilizei o padrão **MVVM (Model-View-ViewModel)** para manter uma separação entre a interface e as operações de busca.

A organização principal ficou assim:

- **`MainActivity.kt`** - controla o fluxo entre as telas e o estado das preferências do usuário.
- **`ui/screens`** - contém as telas de seleção de gêneros, palavras-chave, filtros, detalhes e disponibilidade dos filmes.
- **`ui/viewmodel/FilmesViewModel.kt`** - gerencia os estados da pesquisa, carregamento, resultados, erros e cancelamento.
- **`data/repository/FilmesRepository.kt`** - concentra a busca, aplicação de critérios, organização e classificação dos filmes.
- **`data/remote`** - contém o cliente Retrofit, a interface da API e o paginador.
- **`data/remote/dto`** - reúne os modelos utilizados para interpretar as respostas da API.
- **`model`** - contém filtros e regras para análise de palavras-chave e sinopses.
- **`ui/theme`** - centraliza as cores e configurações visuais.
- **`ui/components`** - contém componentes reutilizáveis, incluindo a animação de progresso da pesquisa.

O uso de `StateFlow` e dos estados definidos na ViewModel permite que a interface reaja ao andamento das operações, apresentando carregamento, sucesso, ausência de resultados ou mensagens de erro conforme necessário.

## O que estudei e apliquei

### 1. Consumo de APIs REST

Um dos principais objetivos foi aprofundar meu conhecimento em integração com APIs.

Utilizei Retrofit e Gson para consultar os endpoints do TMDB, receber informações em JSON e convertê-las para modelos Kotlin.

Durante o projeto, trabalhei com pesquisas de filmes, palavras-chave, detalhes, datas de lançamento e disponibilidade em plataformas.

Também precisei lidar com respostas incompletas da API. Em uma das etapas, por exemplo, encontrei um erro causado por uma lista de gêneros nula durante a classificação dos resultados. A correção envolveu tratar esses dados de forma defensiva, evitando que informações ausentes interrompessem toda a pesquisa.

### 2. Programação assíncrona com Coroutines

A pesquisa foi uma das partes que mais exigiram ajustes.

Inicialmente, o aplicativo consultava um grande número de páginas do TMDB de forma sequencial. Isso ampliava o número de filmes considerados, mas fazia a busca demorar demais.

Para melhorar esse comportamento, desenvolvi uma estratégia de **paginação inteligente**, que consulta páginas distribuídas pelo catálogo em vez de percorrer todas as páginas uma a uma.

Também utilizei Coroutines com requisições em pequenos lotes paralelos, mantendo um limite de concorrência para não sobrecarregar a API.

Nos testes realizados após essa otimização, pesquisas que antes demoravam vários minutos passaram a levar aproximadamente **5 a 10 segundos**, dependendo das preferências utilizadas e da conexão.

Para evitar pesquisas excessivamente longas, também implementei limites de tempo e aproveitamento dos candidatos que já foram coletados.

### 3. Pesquisa por palavras-chave e classificação de filmes

Queria que o aplicativo fosse além de uma pesquisa simples por gênero.

Por isso, implementei uma lógica que considera palavras-chave cadastradas no TMDB e também informações presentes nas sinopses.

Criei um processo de normalização dos termos, tratando diferenças de escrita e algumas expressões relacionadas em português e inglês.

Os filmes encontrados recebem uma pontuação baseada na compatibilidade com os gêneros e temas escolhidos. A avaliação TMDB e a quantidade de votos também participam da ordenação.

Essa foi uma parte interessante para estudar regras de negócio, processamento de listas, filtros e organização de resultados.

### 4. Otimização e tratamento de falhas

Durante o desenvolvimento, precisei trabalhar com situações que nem sempre aparecem nos primeiros testes, como respostas HTTP 429, falhas temporárias de conexão e cancelamento de requisições.

Implementei tentativas controladas para algumas falhas de rede e respeitei o intervalo de espera informado pela API quando aplicável.

Também organizei os estados de carregamento e erro para que o aplicativo consiga informar ao usuário o que está acontecendo.

Ainda existem melhorias planejadas nessa área, principalmente para preservar o progresso de uma pesquisa quando a conexão é interrompida.

### 5. Filtros de lançamento

Outra preocupação foi evitar recomendar filmes que ainda não estrearam ou que estejam em período exclusivamente cinematográfico.

Adicionei um filtro de data para evitar estreias futuras e uma verificação complementar dos lançamentos recentes registrados no Brasil.

Essa verificação considera as modalidades de lançamento cadastradas no TMDB e procura preservar filmes que já chegaram aos formatos domésticos.

É uma aproximação baseada nos dados disponíveis na API, não uma confirmação em tempo real da programação dos cinemas.

Mantive os filmes antigos no catálogo mesmo quando não possuem informações de streaming, aluguel ou compra.

### 6. Interfaces com Jetpack Compose

Todo o aplicativo foi construído utilizando **Jetpack Compose**, sem layouts XML.

Durante o desenvolvimento, trabalhei com composição de telas, componentes reutilizáveis, gerenciamento de estado, validação de campos e navegação.

Também criei um tema visual escuro com cores centralizadas no Material Design 3, utilizando laranja para as ações principais e tons de azul e verde para informações complementares.

Uma das partes visuais que desenvolvi foi uma animação de carregamento inspirada em uma película de cinema, desenhada com `Canvas` e animada conforme o progresso da pesquisa.

Esse processo me ajudou a entender melhor como organizar interfaces e reaproveitar componentes sem precisar repetir o mesmo código em várias telas.

## Testes e validação

Durante o desenvolvimento, realizei testes manuais em um dispositivo Android físico, utilizando o Android Studio para acompanhar a execução e analisar erros pelo Logcat.

Também distribuí uma versão de teste para alguns amigos, que utilizaram o aplicativo em seus próprios celulares.

Esse retorno ajudou a identificar melhorias importantes, principalmente no tempo necessário para encontrar filmes.

Os principais fluxos validados foram seleção de preferências, aplicação de filtros, pesquisa, sorteio, carregamento dos detalhes e consulta às plataformas.

Até o momento, a validação foi principalmente manual. Ainda pretendo evoluir a cobertura de testes automatizados.

## APK para testes

O FilmeDaVez possui uma versão beta preparada para quem quiser conhecer e testar o aplicativo sem precisar abrir o projeto no Android Studio.

A versão **Beta 1.1** foi gerada como APK release assinado e testada em um dispositivo Android físico, com as principais funcionalidades validadas.

### Download

**[Baixar FilmeDaVez - GitHub Releases](https://github.com/joaogeovani/FilmeDaVez/releases)**

### Como instalar

1. Acesse a página de Releases pelo link acima.
2. Selecione a versão **FilmeDaVez - Beta 1.1**.
3. Baixe o arquivo `app-release.apk` disponível em **Assets**.
4. Abra o arquivo no seu celular Android.
5. Caso o Android solicite, autorize a instalação de aplicativos dessa origem.
6. Conclua a instalação e abra o FilmeDaVez.

**Requisitos:**

- Android 8.0 (API 26) ou superior.
- Conexão com a internet para realizar consultas à API do TMDB.

O aplicativo não exige cadastro nem conta de usuário.

### Sobre a versão beta

Esta versão inclui seleção de gêneros, pesquisa por palavras-chave, filtros personalizados, busca otimizada, sorteio de filmes, informações detalhadas e consulta de disponibilidade em plataformas no Brasil.

Uma das principais melhorias foi a otimização da pesquisa, reduzindo significativamente o tempo necessário para encontrar filmes compatíveis.

Por ser um projeto pessoal de estudos, ainda existem melhorias planejadas, principalmente relacionadas à continuidade de pesquisas após perda de conexão e ao funcionamento em segundo plano.

O objetivo desta versão é disponibilizar uma experiência funcional e apresentar na prática os conhecimentos aplicados durante o desenvolvimento.

## Executando o projeto no Android Studio

Para executar o projeto localmente:

1. Clone o repositório com `git clone https://github.com/joaogeovani/FilmeDaVez.git`.
2. Abra o projeto no Android Studio.
3. Gere um **API Read Access Token** na sua conta do [TMDB](https://www.themoviedb.org/settings/api).
4. Crie um arquivo chamado `tmdb.properties` na raiz do projeto.
5. Adicione ao arquivo a propriedade `TMDB_READ_TOKEN=SEU_TOKEN_TMDB`.
6. Aguarde a sincronização do Gradle e execute o aplicativo em um dispositivo Android compatível.

O projeto utiliza esse arquivo para configurar o token durante a compilação. Ele está incluído no `.gitignore` e não deve ser enviado ao repositório.

Essa configuração evita publicar a credencial diretamente no código-fonte, mas não impede que ela seja extraída de um APK compilado. Para uma distribuição pública de maior escala, esse ponto exige uma revisão de segurança.

## Próximas melhorias

O FilmeDaVez continua sendo um projeto de estudos e ainda tenho algumas melhorias que gostaria de implementar:

- Preservar o progresso da pesquisa em situações de perda de conexão.
- Estudar armazenamento local e possibilidades de funcionamento offline.
- Melhorar o comportamento do aplicativo quando fica muito tempo em segundo plano.
- Ampliar os testes automatizados.
- Continuar ajustando a experiência de uso com base nos feedbacks recebidos.

Neste momento, minha prioridade é manter uma versão beta funcional, com uma pesquisa rápida e uma interface simples de utilizar.

## Sobre os dados

Os filmes, avaliações, pôsteres e informações de disponibilidade são obtidos por meio da API do **The Movie Database (TMDB)**.

As opções de onde assistir são apresentadas com base nas informações de provedores disponibilizadas pelo TMDB, com atribuição ao JustWatch. Essas informações podem variar conforme o país e mudar com o tempo.

Este projeto utiliza a API do TMDB, mas não é endossado nem certificado pelo TMDB.

## Autor

**João Geovani de Camargo**

Sou estudante de Engenharia de Software e desenvolvi o FilmeDaVez como projeto pessoal para praticar desenvolvimento Android e aprofundar conhecimentos em Kotlin, Jetpack Compose e integração com APIs.

O código e a evolução do projeto estão disponíveis aqui no GitHub.

[Meu perfil no GitHub](https://github.com/joaogeovani)