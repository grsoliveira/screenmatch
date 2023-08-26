package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.service.ConsumoAPI;

import java.sql.SQLOutput;
import java.util.Scanner;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=d54a6b1c";

    public void exibeMenu() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = this.leitura.nextLine();
        var enderecoAPI = ENDERECO + nomeSerie.replace(" ", "+") + API_KEY;
        var json = this.consumo.obterDados(enderecoAPI);
    }
}
