package com.example.dao;

import com.example.model.Time;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Time.
 * Responsavel pelas operacoes de banco de dados dos times, incluindo o
 * vinculo com a turma (coluna time.turma_id).
 */
public class TimeDao {

    // LEFT JOIN: times antigos (sem turma) continuam aparecendo na listagem.
    private static final String SQL_BASE =
            "SELECT t.id, t.nome, t.cor, t.responsavel, t.contato, t.turma_id, " +
            "tu.nome AS turma_nome " +
            "FROM time t LEFT JOIN turma tu ON tu.id = t.turma_id ";

    private static final String SQL_LISTAR = SQL_BASE + "ORDER BY t.nome";

    private static final String SQL_LISTAR_POR_TURMA =
            SQL_BASE + "WHERE t.turma_id = ? ORDER BY t.nome";

    private static final String SQL_BUSCAR = SQL_BASE + "WHERE t.id = ?";

    private static final String SQL_INSERIR =
            "INSERT INTO time (nome, cor, responsavel, contato, turma_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE time SET nome = ?, cor = ?, responsavel = ?, contato = ?, turma_id = ? WHERE id = ?";

    private static final String SQL_DELETAR = "DELETE FROM time WHERE id = ?";

    // Nome repetido (ignorando maiusculas/minusculas) na mesma turma. ignorarId = 0 ao inserir.
    private static final String SQL_EXISTE_NOME =
            "SELECT 1 FROM time WHERE LOWER(nome) = LOWER(?) AND turma_id = ? AND id <> ?";

    public List<Time> listar() throws SQLException {
        List<Time> times = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                times.add(mapear(rs));
            }
        }
        return times;
    }

    public List<Time> listarPorTurma(int turmaId) throws SQLException {
        List<Time> times = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR_POR_TURMA)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    times.add(mapear(rs));
                }
            }
        }
        return times;
    }

    public Time buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Time inserir(Time time) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, time.getNome());
            ps.setString(2, time.getCor());
            ps.setString(3, time.getResponsavel());
            ps.setString(4, time.getContato());
            definirTurma(ps, 5, time.getTurmaId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    time.setId(keys.getInt(1));
                }
            }
        }
        return buscarPorId(time.getId()); // devolve ja com turmaNome preenchido
    }

    public boolean atualizar(Time time) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, time.getNome());
            ps.setString(2, time.getCor());
            ps.setString(3, time.getResponsavel());
            ps.setString(4, time.getContato());
            definirTurma(ps, 5, time.getTurmaId());
            ps.setInt(6, time.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Remove o time. Se ele ja tiver inscricoes/partidas (FOREIGN KEY sem CASCADE),
     * o SQLite recusa e lanca SQLException: o controller trata isso como 409.
     */
    public boolean deletar(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean existeNomeNaTurma(String nome, int turmaId, int ignorarId) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_EXISTE_NOME)) {
            ps.setString(1, nome);
            ps.setInt(2, turmaId);
            ps.setInt(3, ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** True quando a excecao veio de uma violacao de chave estrangeira. */
    public static boolean ehViolacaoDeChave(SQLException e) {
        String msg = e.getMessage();
        return msg != null && msg.toUpperCase().contains("FOREIGN KEY");
    }

    private void definirTurma(PreparedStatement ps, int indice, Integer turmaId) throws SQLException {
        if (turmaId != null) {
            ps.setInt(indice, turmaId);
        } else {
            ps.setNull(indice, Types.INTEGER);
        }
    }

    private Time mapear(ResultSet rs) throws SQLException {
        Time time = new Time();
        time.setId(rs.getInt("id"));
        time.setNome(rs.getString("nome"));
        time.setCor(rs.getString("cor"));
        time.setResponsavel(rs.getString("responsavel"));
        time.setContato(rs.getString("contato"));

        int turmaId = rs.getInt("turma_id");
        time.setTurmaId(rs.wasNull() ? null : turmaId);
        time.setTurmaNome(rs.getString("turma_nome"));
        return time;
    }
}