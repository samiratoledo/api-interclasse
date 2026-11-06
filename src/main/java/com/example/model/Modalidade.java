package com.example.model;

/**
 * Entidade Modalidade.
 * Representa os esportes e jogos do Interclasse.
 */
public class Modalidade {

    private Integer id;
    private String nome;
    private String descricao;
    private String categoria;
    private Integer minimoAtletas;
    private Integer maximoAtletas;
    private String icone;
    private Boolean ativo;

    public Modalidade() {
    }

    public Modalidade(String nome, String descricao, String categoria,
            Integer minimoAtletas, Integer maximoAtletas,
            String icone, Boolean ativo) {
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.minimoAtletas = minimoAtletas;
        this.maximoAtletas = maximoAtletas;
        this.icone = icone;
        this.ativo = ativo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getMinimoAtletas() {
        return minimoAtletas;
    }

    public void setMinimoAtletas(Integer minimoAtletas) {
        this.minimoAtletas = minimoAtletas;
    }

    public Integer getMaximoAtletas() {
        return maximoAtletas;
    }

    public void setMaximoAtletas(Integer maximoAtletas) {
        this.maximoAtletas = maximoAtletas;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
