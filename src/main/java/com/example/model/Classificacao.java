package com.example.model;

/**
 * Uma linha da tabela de classificacao de uma modalidade.
 * Nao existe tabela no banco: e calculada a partir das partidas FINALIZADAS.
 */
public class Classificacao {

    private int posicao;
    private int timeId;
    private String time;
    private String turma;
    private int jogos;
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golsPro;
    private int golsContra;
    private int saldoGols;
    private int pontos;

    public Classificacao() {
    }

    public Classificacao(int timeId, String time, String turma) {
        this.timeId = timeId;
        this.time = time;
        this.turma = turma;
    }

    /** Registra o resultado de um jogo para este time. */
    public void registrar(int golsFeitos, int golsSofridos,
                          int pontosVitoria, int pontosEmpate, int pontosDerrota) {
        jogos++;
        golsPro += golsFeitos;
        golsContra += golsSofridos;
        saldoGols = golsPro - golsContra;
        if (golsFeitos > golsSofridos) {
            vitorias++;
            pontos += pontosVitoria;
        } else if (golsFeitos == golsSofridos) {
            empates++;
            pontos += pontosEmpate;
        } else {
            derrotas++;
            pontos += pontosDerrota;
        }
    }

    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; }

    public int getTimeId() { return timeId; }
    public String getTime() { return time; }
    public String getTurma() { return turma; }
    public int getJogos() { return jogos; }
    public int getVitorias() { return vitorias; }
    public int getEmpates() { return empates; }
    public int getDerrotas() { return derrotas; }
    public int getGolsPro() { return golsPro; }
    public int getGolsContra() { return golsContra; }
    public int getSaldoGols() { return saldoGols; }
    public int getPontos() { return pontos; }
}
