package com.example.model;

public class Inscricao{
    private int id;
    private int idTime;
    private int idModalidade;
    public enum StatusInscricao {
    	PENDENTE, CONFIRMADA, NEGADA
    };

    private StatusInscricao status;

    public Inscricao() {}
    public Inscricao(int id, int idTime, int idModalidade, StatusInscricao status){
        this.id = id; 
        this.idTime = idTime;
        this.idModalidade = idModalidade;
        this.status = status;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getIdModalidade() {
        return idModalidade;
    }
    public void setIdModalidade(int idModalidade) {
        this.idModalidade = idModalidade;
    }
    public int getIdTime() {
        return idTime;
    }
    public void setIdTime(int idTime) {
        this.idTime = idTime;
    }
    public StatusInscricao getStatus() {
        return status;
    }
    public void setStatus(StatusInscricao status) {
        this.status = status;
    }
}