package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    }
}
