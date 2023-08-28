# screenmatch

## Projeto

Desenvolvimento de projeto durante o treinamento da ALURA para Sprint Application
Aplicação desenvolvida utiliza a implementação da interface CommandLineRunner, para construção de projetos Sprint Sem Web.

## Protocolo de comunicação

Utilizando JSON como protocolo de comunicação entre a API utilizada e o backend implementado.
Projeto utilizou jackson-databind para realizar a desserialização do JSON

```
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

## Modelo

Modelo de dados (pacote model) estruturado inicialmente para utilização de records como representantes dos dados.
O mapeamento utilizado nas propriedades do JSON foi de @JsonAlias pois o projeto apenas recebe os dados da API.
Poderíamos realizar o mapeamento utilizando @JsonProperties, mas, como não vamos fazer o caminho de volta (enviar dados), optou-se pela primeira anotação.
A record também foi marcada com
```
@JsonIgnoreProperties(ignoreUnknown = true)
```
para evitar que propriedades do JSON não mapeadas, gerassem erro na conversão. 

## Conversor de dados

A conversão de dados JSON para records e classes foi realizada por meio do ConverterDados. 
A mesma possui uma instância de ObjectMapper e realizar a conversão mediante informamos o JSON e a classe/record destino da conversão. 
A implementação do obter dados foi realizada de forma a permitir qualquer conversão de tipos, conforme abaixo:

```
@Override
public <T> T obterDados(String json, Class<T> classe) {
    try {
        return mapper.readValue(json, classe);
    } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
    }
}
```

## API para consumo

Foi escolhida a API https://www.omdbapi.com/ para consumo dos dados. 
O consumo da API foi implementado utilizando HttpRequest e HttpResponse, conforme abaixo:

```
public String obterDados(String endereco) {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endereco))
            .build();
    HttpResponse<String> response = null;
    try {
        response = client
                .send(request, HttpResponse.BodyHandlers.ofString());
  
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
  
    String json = response.body();
    return json;
}
```

## Passos e Funcionalidades

### Consumo inicial da API

Foi realizada uma consulta de forma simples e direta da API e os dados foram impressos no console da aplicação
```
List<DadosTemporada> temporadas = new ArrayList<>();
for (int i = 1; i < dados.totalTemporadas(); i++) {
    var enderecoTemporadaAPI = ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY;
    json = this.consumo.obterDados(enderecoTemporadaAPI);
    DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
    temporadas.add(dadosTemporada);
}
temporadas.forEach(System.out::println);
```

### Primeira utilização do Lambda

A implementação da iteração anterior foi melhorada e simplificada através do uso de lambda

```
temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
```

### Reunindo todos os episódios numa lista condensada

Foi necessário criar uma lista condensada com todos os episódios de todas as temporadas. 
Para isso foi utilizado o .flatMap e o .collect(Collectos.toList()) conforme abaixo.
Essa lista será utilizada para filtros, mapeamentos e estatísticas.

```
List<DadosEpisodio> dadosEpisodios = temporadas.stream()
        .flatMap(t -> t.episodios().stream()) 
        .collect(Collectors.toList());
```

### Obtendo os TOP episódios

É utilizado um .filter para eliminar os episódios da lista que não possuem avaliação.
Posteriormente a lista resultante é ordenada de forma reversa utilizando o .sorted
É obtido então a quantidade desejada, utilizando o .limit
Por fim, é realizado uma transformação simples para obter o titulo em caixa alta, utilizando o .map
Todo o resultado é impresso utilizando o .forEach com a chamada ao System.out::println

```
dadosEpisodios.stream()
        .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
        .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
        .limit(5)
        .map(e -> e.titulo().toUpperCase())
        .forEach(System.out::println);
```

### Enriquecendo o modelo de episódio com a informação de temporada

Para que a informação de temporada estivesse dentro do episódio, foi necessário criar uma classe Episodio contendo esta informação.
Para popular essa nova estrutura foi utilizada a lista condensada, obtida com o .flatMap
Durante a iteração dessa lista, a mesma foi mapeada para a nova estrutura utilizando .map e criando o novo objeto com dados de episodio e temporada.
Ao final, é apresentada a lista com .collect(Collectors.toList()) e impresso utilizando o .forEach

```
List<Episodio> episodios = temporadas.stream()
        .flatMap(t -> t.episodios().stream()
                .map(de -> new Episodio(t.numero(), de))
        ).collect(Collectors.toList());
episodios.forEach(System.out::println);
```

### Implementando sistema de busca por nome (título)

Foi realizada uma busca na lista de Episodios com a informação de Temporada. 
Essa busca foi realizada utilizando .filter e comparando o título com .contains 
O retorno, Optional, foi impresso de acordo com a presença ou não do retorno

```
System.out.println("Informe o título do episódio que deseja buscar");
var trechoTitulo = leitura.nextLine();
Optional<Episodio> episodioBuscado = episodios.stream()
        .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
        .findFirst();
if (episodioBuscado.isPresent()) {
    System.out.println("Episódio encontrado!");
    System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
} else {
    System.out.println("Episódio não encontrado");
}
```

### Obtendo episódios a partir de uma determinada data

Para execitar o filtro por data, foi implementada a funcionalidade de exibir episódios a partir de um ano informado.
A lista de episodios foi utilizada, por meio de .filter para comparar com a data informada, construída a partir do ano.
A comparação de datas foi realizada utilizando .isAfter

```
System.out.println("A partir de que ano  você deseja ver os episódios");
var ano = this.leitura.nextInt();
this.leitura.nextLine();

LocalDate dataBusca = LocalDate.of(ano, 1, 1);

DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
episodios.stream()
        .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
        .forEach(e -> System.out.println(
                "Temporada: " + e.getTemporada() +
                        " Episódio: " + e.getTitulo() +
                        " Data Lançamento: " + e.getDataLancamento().format(formatador)
        ));
```

Validações das conversões de data e double precisaram ser inseridas no construtor de Episodio
```
try {
    this.avaliacao = Double.valueOf(dadosEpisodio.avaliacao());
} catch (NumberFormatException e) {
    this.avaliacao = 0.0;
}
try {
    this.dataLancamento = LocalDate.parse(dadosEpisodio.dataLancamento());
} catch (DateTimeParseException e) {
    this.dataLancamento = null;
}
```

### Estatítica básica - média

Foi implementada a estatística de média utilizando a lista condensada.
Foram filtrados as avaliações acima de zero com .filter
Foi utilizado o agrupamento com Collectors.groupingBy e Collectors.averagingDouble para construir um HashMap de Temporada > Média 

```
Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
        .filter(e -> e.getAvaliacao() > 0.0)
        .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));
System.out.println(avaliacoesPorTemporada);
```

### Utilização de Estatísticas padrão com DoubleSummaryStatistics

Por fim, foi utilizada a implementação padrão de EStatísticas através de .summarizingDouble

```
DoubleSummaryStatistics est = episodios.stream()
        .filter(e -> e.getAvaliacao() > 0.0)
        .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
System.out.println("Média: " + est.getAverage());
System.out.println("Melhor episódio: " + est.getMax());
System.out.println("Pior episódio: " + est.getMin());
System.out.println("Quantidade: " + est.getCount());
```






