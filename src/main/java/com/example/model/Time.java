package com.example.model;

/**
 * Entidade Time.
 * Representa as equipes que disputam o Interclasse. Cada time pertence a uma turma.
 */
public class Time {

    private Integer id;
    private String nome;
    private String cor;
    private String responsavel;
    private String contato;
    private Integer turmaId;

    /** Somente leitura: preenchido pelo DAO (JOIN com turma) para facilitar a listagem. */
    private String turmaNome;

    public Time() {
    }

    public Time(String nome, String cor, String responsavel, String contato) {
        this.nome = nome;
        this.cor = cor;
        this.responsavel = responsavel;
        this.contato = contato;
    }

    public Time(String nome, String cor, String responsavel, String contato, Integer turmaId) {
        this(nome, cor, responsavel, contato);
        this.turmaId = turmaId;
    }

    public Integer getId() {return id;}

    public void setId(Integer id) {this.id = id;}

    public String getNome() {return nome;}

    public void setNome(String nome) {this.nome = nome;}

    public String getCor() {return cor;}
    
    public void setCor(String cor) {this.cor = cor;}

    public String getResponsavel() {return responsavel;}

    public void setResponsavel(String responsavel) {this.responsavel = responsavel;}

    public String getContato() {return contato;}

    public void setContato(String contato) {this.contato = contato;}

    public Integer getTurmaId() {return turmaId;}

    public void setTurmaId(Integer turmaId) {this.turmaId = turmaId;}

    public String getTurmaNome() {return turmaNome;}

    public void setTurmaNome(String turmaNome) {this.turmaNome = turmaNome;}
}
    