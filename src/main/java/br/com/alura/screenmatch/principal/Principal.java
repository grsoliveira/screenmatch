package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=d54a6b1c";

    public void exibeMenu() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = this.leitura.nextLine();
        var enderecoSerieAPI = ENDERECO + nomeSerie.replace(" ", "+") + API_KEY;
        var json = this.consumo.obterDados(enderecoSerieAPI);

        DadosSerie dados = this.conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i < dados.totalTemporadas(); i++) {
            var enderecoTemporadaAPI = ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY;
            json = this.consumo.obterDados(enderecoTemporadaAPI);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

//        for (int i = 0; i < dados.totalTemporadas(); i++){
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for(int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        //example streams
//        List<String> nomes = Arrays.asList("Elizabete", "Beatriz", "Alberto", "Danilo", "Caio");
//        nomes.stream()
//                .sorted() //ordenacao
//                .limit(3) //limite da quantidade retornada
//                .filter(n -> n.startsWith("B")) //filtrando de acordo com o critério passado
//                .map(n -> n.toUpperCase()) //aplicando uma transformação
//                .forEach(System.out::println);

        //criando uma lista única com todos os episodios
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()) //dentro da minha lista vou ter outra lista e vou puxar todas juntas
                .collect(Collectors.toList()); //toList() retorna uma lista estática, onde não podemos empilhar novas operações

        //imprimindo os TOP 5 episódios
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println("Primeiro filtro (N/A) " + e))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(e -> System.out.println("Segundo filtro (ordenacao por avaliacao) " + e))
                .limit(5)
//                .peek(e -> System.out.println("Terceiro filtro (limit) " + e))
                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Quarto filtro (mapeamento) " + e))
                .forEach(System.out::println);

        //iterando a lista flat para construir novos episodios (com numero da temporada)
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(de -> new Episodio(t.numero(), de))
                ).collect(Collectors.toList());
        episodios.forEach(System.out::println);

        //realizar de busca de exemplo
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


        //obtendo os episódios a partir de um ano selecionado pelo usuário
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



    }
}
