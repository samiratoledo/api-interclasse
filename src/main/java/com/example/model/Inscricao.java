package com.example.model;

import java.time.LocalDate;

public class Inscricao{
    private int id;
    private int time_id;
    private int modalidade_id;
    private LocalDate data_inscricao;
    public enum StatusInscricao {
    	PENDENTE, CONFIRMADA, NEGADA
    };

    private StatusInscricao status;

    public Inscricao() {}
    public Inscricao(int id, int time_id, int modalidade_id, StatusInscricao status){
        this.id = id; 
        this.time_id = time_id;
        this.modalidade_id = modalidade_id;
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
    public void setIdModalidade(int modalidade_id) {
        this.modalidade_id = modalidade_id;
    }
    public int getIdTime() {
        return idTime;
    }
    public void setIdTime(int time_id) {
        this.time_id = time_id;
    }
    public LocalDate getDataInscricao(){
        return data_inscricao;
    }
    public void setDataInscricao(LocalDate data_inscricao){
        this.data_inscricao = data_inscricao;
    }
    public StatusInscricao getStatus() {
        return status;
    }
    public void setStatus(StatusInscricao status) {
        this.status = status;
    }
}
