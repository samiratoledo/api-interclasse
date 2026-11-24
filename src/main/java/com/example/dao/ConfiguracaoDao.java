package com.example.dao;

import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfiguracaoDao {

    public static final String PONTOS_VITORIA = "PONTOS_VITORIA";
    public static final String PONTOS_EMPATE = "PONTOS_EMPATE";
    public static final String PONTOS_DERROTA = "PONTOS_DERROTA";
    public static final String PERMITE_EMPATE = "PERMITE_EMPATE";

    public static final List<String> CHAVES_VALIDAS =
            Arrays.asList(PONTOS_VITORIA, PONTOS_EMPATE, PONTOS_DERROTA, PERMITE_EMPATE);

    private static final String SQL_LISTAR = "SELECT chave, valor FROM configuracao ORDER BY chave";
    private static final String SQL_BUSCAR = "SELECT valor FROM configuracao WHERE chave = ?";
    private static final String SQL_SALVAR =
            "INSERT INTO configuracao (chave, valor) VALUES (?, ?) " +
            "ON CONFLICT(chave) DO UPDATE SET valor = excluded.valor";

    public Map<String, String> listar() throws SQLException {
        Map<String, String> mapa = new LinkedHashMap<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString("chave"), rs.getString("valor"));
            }
        }
        return mapa;
    }

    /** Valor da chave ou null se ela nao existir. */
    public String buscar(String chave) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, chave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("valor") : null;
            }
        }
    }

    /** Cria ou altera o valor de uma chave. */
    public void salvar(String chave, String valor) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SALVAR)) {
            ps.setString(1, chave);
            ps.setString(2, valor);
            ps.executeUpdate();
        }
    }

    /** Valor inteiro da chave; se faltar ou for invalido, devolve o padrao. */
    public int buscarInt(String chave, int padrao) throws SQLException {
        String valor = buscar(chave);
        if (valor == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    public int pontosVitoria() throws SQLException {
        return buscarInt(PONTOS_VITORIA, 3);
    }

    public int pontosEmpate() throws SQLException {
        return buscarInt(PONTOS_EMPATE, 1);
    }

    public int pontosDerrota() throws SQLException {
        return buscarInt(PONTOS_DERROTA, 0);
    }

    /** PERMITE_EMPATE = 1 (padrao) -> empate vale; 0 -> partida empatada nao pode ser finalizada. */
    public boolean permiteEmpate() throws SQLException {
        return buscarInt(PERMITE_EMPATE, 1) != 0;
    }
}
