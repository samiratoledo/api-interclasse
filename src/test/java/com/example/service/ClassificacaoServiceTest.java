package com.example.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.model.Classificacao;
import com.example.model.Partida;
import com.example.model.Time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;


public class ClassificacaoServiceTest {

    private final ClassificacaoService service = new ClassificacaoService();

    private Time time(int id, String nome) {
        Time t = new Time();
        t.setId(id);
        t.setNome(nome);
        t.setTurmaNome("Turma " + id);
        return t;
    }

    private Partida jogo(int a, int placarA, int b, int placarB, String status) {
        Partida p = new Partida();
        p.setTimeAId(a);
        p.setTimeBId(b);
        p.setPlacarA(placarA);
        p.setPlacarB(placarB);
        p.setStatus(status);
        return p;
    }

    private List<Classificacao> calcular(List<Time> times, Partida... partidas) {
        return service.calcular(times, new ArrayList<>(Arrays.asList(partidas)), 3, 1, 0);
    }

    @Test
    public void vitoriaDaTresPontosEDerrotaZero() {
        List<Classificacao> t = calcular(Arrays.asList(time(1, "Alfa"), time(2, "Beta")),
                jogo(1, 2, 2, 0, Partida.FINALIZADA));

        assertEquals("Alfa", t.get(0).getTime());
        assertEquals(3, t.get(0).getPontos());
        assertEquals(1, t.get(0).getVitorias());
        assertEquals(0, t.get(1).getPontos());
        assertEquals(1, t.get(1).getDerrotas());
        assertEquals(1, t.get(0).getPosicao());
        assertEquals(2, t.get(1).getPosicao());
    }

    @Test
    public void empateDaUmPontoParaCada() {
        List<Classificacao> t = calcular(Arrays.asList(time(1, "Alfa"), time(2, "Beta")),
                jogo(1, 1, 2, 1, Partida.FINALIZADA));

        assertEquals(1, t.get(0).getPontos());
        assertEquals(1, t.get(1).getPontos());
        assertEquals(1, t.get(0).getEmpates());
    }

    @Test
    public void pontuacaoVemDaConfiguracao() {
        List<Classificacao> t = service.calcular(
                Arrays.asList(time(1, "Alfa"), time(2, "Beta")),
                Arrays.asList(jogo(1, 3, 2, 0, Partida.FINALIZADA)),
                5, 2, 1);

        assertEquals(5, t.get(0).getPontos());
        assertEquals(1, t.get(1).getPontos()); // derrota valendo 1
    }

    @Test
    public void partidaNaoFinalizadaNaoContaPontos() {
        List<Classificacao> t = calcular(Arrays.asList(time(1, "Alfa"), time(2, "Beta")),
                jogo(1, 5, 2, 0, Partida.EM_ANDAMENTO),
                jogo(1, 5, 2, 0, Partida.AGENDADA),
                jogo(1, 5, 2, 0, Partida.CANCELADA));

        assertEquals(0, t.get(0).getPontos());
        assertEquals(0, t.get(0).getJogos());
    }

    @Test
    public void timeSemInscricaoAprovadaNaoApareceNemContaJogo() {
        List<Classificacao> t = calcular(Arrays.asList(time(1, "Alfa"), time(2, "Beta")),
                jogo(1, 1, 3, 0, Partida.FINALIZADA));

        assertEquals(2, t.size());
        assertEquals(0, t.get(0).getJogos());
        assertEquals(0, t.get(0).getPontos());
    }

    @Test
    public void timeAprovadoSemJogosApareceZerado() {
        List<Classificacao> t = calcular(Arrays.asList(time(1, "Alfa"), time(2, "Beta"), time(3, "Gama")));

        assertEquals(3, t.size());
        assertEquals(0, t.get(2).getPontos());
    }

    @Test
    public void maisPontosFicaNaFrente() {
        List<Classificacao> t = calcular(
                Arrays.asList(time(1, "Zeta"), time(2, "Beta"), time(3, "Gama"), time(4, "Delta")),
                jogo(1, 1, 3, 0, Partida.FINALIZADA),   // Zeta vence Gama
                jogo(1, 0, 4, 2, Partida.FINALIZADA),   // Zeta perde p/ Delta
                jogo(2, 0, 3, 0, Partida.FINALIZADA),   // Beta empata
                jogo(2, 1, 4, 1, Partida.FINALIZADA),   // Beta empata
                jogo(2, 2, 1, 2, Partida.FINALIZADA));  // Beta empata com Zeta (+1 pt p/ Zeta)

        Classificacao zeta = porNome(t, "Zeta");
        Classificacao beta = porNome(t, "Beta");
        assertEquals(4, zeta.getPontos());
        assertEquals(3, beta.getPontos());
        assertTrue(zeta.getPosicao() < beta.getPosicao());
    }

    @Test
    public void desempatePorVitoriasVemAntesDoSaldo() {
        List<Classificacao> t = calcular(
                Arrays.asList(time(1, "Zeta"), time(2, "Beta"), time(3, "Gama"),
                        time(4, "Delta"), time(5, "Eta")),
                jogo(1, 1, 3, 0, Partida.FINALIZADA),   // Zeta vence Gama
                jogo(1, 0, 4, 4, Partida.FINALIZADA),   // Zeta perde de Delta
                jogo(2, 0, 3, 0, Partida.FINALIZADA),   // Beta empata
                jogo(2, 1, 4, 1, Partida.FINALIZADA),   // Beta empata
                jogo(2, 2, 5, 2, Partida.FINALIZADA));  // Beta empata

        Classificacao zeta = porNome(t, "Zeta");
        Classificacao beta = porNome(t, "Beta");
        assertEquals(3, zeta.getPontos());
        assertEquals(3, beta.getPontos());
        assertEquals(-3, zeta.getSaldoGols());
        assertEquals(0, beta.getSaldoGols());
        assertTrue("mais vitorias deve ficar na frente",
                zeta.getPosicao() < beta.getPosicao());
    }

    @Test
    public void desempatePorSaldoDeGols() {
        List<Classificacao> t = calcular(
                Arrays.asList(time(1, "Alfa"), time(2, "Beta"), time(3, "Gama"), time(4, "Delta")),
                jogo(1, 1, 3, 0, Partida.FINALIZADA),   // Alfa +1
                jogo(2, 4, 4, 0, Partida.FINALIZADA));  // Beta +4

        assertEquals("Beta", t.get(0).getTime());
        assertEquals("Alfa", t.get(1).getTime());
        assertEquals(4, t.get(0).getSaldoGols());
    }

    @Test
    public void desempateFinalPorNomeAlfabetico() {
        List<Classificacao> t = calcular(Arrays.asList(time(1, "Zeta"), time(2, "alfa"), time(3, "Beta")));

        assertEquals("alfa", t.get(0).getTime());
        assertEquals("Beta", t.get(1).getTime());
        assertEquals("Zeta", t.get(2).getTime());
    }

    @Test
    public void golsProContraESaldoSaoSomados() {
        List<Classificacao> t = calcular(Arrays.asList(time(1, "Alfa"), time(2, "Beta")),
                jogo(1, 3, 2, 1, Partida.FINALIZADA),
                jogo(2, 2, 1, 0, Partida.FINALIZADA));

        Classificacao alfa = porNome(t, "Alfa");
        assertEquals(3, alfa.getGolsPro());
        assertEquals(3, alfa.getGolsContra());
        assertEquals(0, alfa.getSaldoGols());
        assertEquals(2, alfa.getJogos());
    }

    private Classificacao porNome(List<Classificacao> tabela, String nome) {
        for (Classificacao c : tabela) {
            if (c.getTime().equals(nome)) {
                return c;
            }
        }
        throw new AssertionError("time nao encontrado: " + nome);
    }
}
