package com.example.model;

/**
 * Entidade Time (exemplo). Serve de modelo para os alunos entenderem o padrão.
 */
public class Time {

    private Integer id;
    private String nome;
    private String cor;
    private String responsavel;
    private String contato;

    public Time() {
    }

    public Time(String nome, String cor, String responsavel, String contato) {
        this.nome = nome;
        this.cor = cor;
        this.responsavel = responsavel;
        this.contato = contato;
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

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }
}
