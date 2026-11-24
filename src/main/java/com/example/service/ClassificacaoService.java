package com.example.service;

import com.example.model.Classificacao;
import com.example.model.Partida;
import com.example.model.Time;

import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ClassificacaoService {

    public List<Classificacao> calcular(List<Time> timesAprovados, List<Partida> partidas,
                                        int pontosVitoria, int pontosEmpate, int pontosDerrota) {

        Map<Integer, Classificacao> linhas = new LinkedHashMap<>();
        for (Time t : timesAprovados) {
            linhas.put(t.getId(), new Classificacao(t.getId(), t.getNome(), t.getTurmaNome()));
        }

        for (Partida p : partidas) {
            if (!Partida.FINALIZADA.equals(p.getStatus())) {
                continue;
            }
            Classificacao a = linhas.get(p.getTimeAId());
            Classificacao b = linhas.get(p.getTimeBId());
            if (a == null || b == null) {
                continue; // time sem inscricao aprovada: jogo nao conta
            }
            int golsA = p.getPlacarA() == null ? 0 : p.getPlacarA();
            int golsB = p.getPlacarB() == null ? 0 : p.getPlacarB();
            a.registrar(golsA, golsB, pontosVitoria, pontosEmpate, pontosDerrota);
            b.registrar(golsB, golsA, pontosVitoria, pontosEmpate, pontosDerrota);
        }

        List<Classificacao> tabela = new ArrayList<>(linhas.values());
        Collator collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        collator.setStrength(Collator.PRIMARY); // ignora maiusculas e acentos

        tabela.sort((x, y) -> {
            if (x.getPontos() != y.getPontos()) {
                return Integer.compare(y.getPontos(), x.getPontos());
            }
            if (x.getVitorias() != y.getVitorias()) {
                return Integer.compare(y.getVitorias(), x.getVitorias());
            }
            if (x.getSaldoGols() != y.getSaldoGols()) {
                return Integer.compare(y.getSaldoGols(), x.getSaldoGols());
            }
            return collator.compare(x.getTime(), y.getTime());
        });

        for (int i = 0; i < tabela.size(); i++) {
            tabela.get(i).setPosicao(i + 1);
        }
        return tabela;
    }
}
