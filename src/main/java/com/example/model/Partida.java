package com.example.model;

import java.util.Arrays;
import java.util.List;

public class Partida {

    public static final String AGENDADA = "AGENDADA";
    public static final String EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String FINALIZADA = "FINALIZADA";
    public static final String CANCELADA = "CANCELADA";

    public static final List<String> STATUS_VALIDOS =
            Arrays.asList(AGENDADA, EM_ANDAMENTO, FINALIZADA, CANCELADA);

    public static final String FASE_PADRAO = "AVULSO";
    public static final List<String> FASES_VALIDAS =
            Arrays.asList("AVULSO", "GRUPOS", "QUARTAS", "SEMI", "FINAL");

    private Integer id;
    private Integer modalidadeId;
    private Integer timeAId;
    private Integer timeBId;
    private Integer placarA;
    private Integer placarB;
    private String data;
    private String hora;
    private String local;
    private String fase;
    private String status;

    
    private String modalidadeNome;
    private String timeANome;
    private String timeBNome;

    public Partida() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getModalidadeId() { return modalidadeId; }
    public void setModalidadeId(Integer modalidadeId) { this.modalidadeId = modalidadeId; }

    public Integer getTimeAId() { return timeAId; }
    public void setTimeAId(Integer timeAId) { this.timeAId = timeAId; }

    public Integer getTimeBId() { return timeBId; }
    public void setTimeBId(Integer timeBId) { this.timeBId = timeBId; }

    public Integer getPlacarA() { return placarA; }
    public void setPlacarA(Integer placarA) { this.placarA = placarA; }

    public Integer getPlacarB() { return placarB; }
    public void setPlacarB(Integer placarB) { this.placarB = placarB; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getModalidadeNome() { return modalidadeNome; }
    public void setModalidadeNome(String modalidadeNome) { this.modalidadeNome = modalidadeNome; }

    public String getTimeANome() { return timeANome; }
    public void setTimeANome(String timeANome) { this.timeANome = timeANome; }

    public String getTimeBNome() { return timeBNome; }
    public void setTimeBNome(String timeBNome) { this.timeBNome = timeBNome; }
}
