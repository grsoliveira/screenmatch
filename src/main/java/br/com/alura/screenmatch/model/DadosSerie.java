package br.com.alura.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DadosSerie(@JsonAlias("Title") String titulo,
                         @JsonAlias("totalSeasons") Integer totalTemporadas,
                         @JsonAlias("imdbRating") String avaliacao) {

    //@JsonAlias pode receber diferentes valores separados por vírgula, para várias opções de mapeamento
    //@JsonProperty poderia ser utilizado quando vamos montar o JSON (caminho inverso)
}
