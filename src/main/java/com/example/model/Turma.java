package com.example.model;

/**
 * Entidade Turma.
 * Representa as turmas participantes do Interclasse.
 */
public class Turma {

    private Integer id;
    private String nome;
    private String curso;
    private Integer ano;
    private String periodo;
    private String imagem;
    private Boolean ativa;

    public Turma() {
    }

    public Turma(String nome, String curso, Integer ano, String periodo,
                 String imagem, Boolean ativa) {
        this.nome = nome;
        this.curso = curso;
        this.ano = ano;
        this.periodo = periodo;
        this.imagem = imagem;
        this.ativa = ativa;
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

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }
}