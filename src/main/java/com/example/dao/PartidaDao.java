package com.example.dao;

import com.example.model.Partida;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class PartidaDao {

    private static final String SQL_BASE =
            "SELECT p.id, p.modalidade_id, p.time1_id, p.time2_id, p.placar_a, p.placar_b, " +
            "p.data, p.hora, p.local, p.fase, p.status, " +
            "m.nome AS modalidade_nome, ta.nome AS time_a_nome, tb.nome AS time_b_nome " +
            "FROM partida p " +
            "JOIN modalidade m ON m.id = p.modalidade_id " +
            "JOIN time ta ON ta.id = p.time1_id " +
            "JOIN time tb ON tb.id = p.time2_id ";

    private static final String SQL_ORDEM = "ORDER BY p.data IS NULL, p.data, p.hora, p.id";

    private static final String SQL_BUSCAR = SQL_BASE + "WHERE p.id = ?";

    private static final String SQL_INSERIR =
            "INSERT INTO partida (modalidade_id, time1_id, time2_id, data, hora, local, fase, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'AGENDADA')";

    // Placar so muda enquanto a partida nao terminou; ao lancar placar ela passa a EM_ANDAMENTO.
    private static final String SQL_PLACAR =
            "UPDATE partida SET placar_a = ?, placar_b = ?, status = 'EM_ANDAMENTO' " +
            "WHERE id = ? AND status IN ('AGENDADA', 'EM_ANDAMENTO')";

    private static final String SQL_REAGENDAR =
            "UPDATE partida SET data = ?, hora = ?, local = ?, fase = ? " +
            "WHERE id = ? AND status = 'AGENDADA'";

    private static final String SQL_FINALIZAR =
            "UPDATE partida SET status = 'FINALIZADA' " +
            "WHERE id = ? AND status IN ('AGENDADA', 'EM_ANDAMENTO')";

    private static final String SQL_CANCELAR =
            "UPDATE partida SET status = 'CANCELADA' " +
            "WHERE id = ? AND status IN ('AGENDADA', 'EM_ANDAMENTO')";

    private static final String SQL_DELETAR =
            "DELETE FROM partida WHERE id = ? AND status <> 'FINALIZADA'";

    /** Lista partidas. Qualquer filtro pode ser null (sem filtro). */
    public List<Partida> listar(Integer modalidadeId, String status, String data) throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (modalidadeId != null) {
            sql.append("AND p.modalidade_id = ? ");
            parametros.add(modalidadeId);
        }
        if (status != null) {
            sql.append("AND p.status = ? ");
            parametros.add(status);
        }
        if (data != null) {
            sql.append("AND p.data = ? ");
            parametros.add(data);
        }
        sql.append(SQL_ORDEM);

        List<Partida> partidas = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    partidas.add(mapear(rs));
                }
            }
        }
        return partidas;
    }

    public Partida buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Partida inserir(Partida partida) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, partida.getModalidadeId());
            ps.setInt(2, partida.getTimeAId());
            ps.setInt(3, partida.getTimeBId());
            ps.setString(4, partida.getData());
            ps.setString(5, partida.getHora());
            ps.setString(6, partida.getLocal());
            ps.setString(7, partida.getFase());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    partida.setId(keys.getInt(1));
                }
            }
        }
        return buscarPorId(partida.getId()); // devolve ja com os nomes preenchidos
    }

    /** Altera data, hora, local e fase (so enquanto AGENDADA). */
    public boolean reagendar(Partida partida) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_REAGENDAR)) {
            ps.setString(1, partida.getData());
            ps.setString(2, partida.getHora());
            ps.setString(3, partida.getLocal());
            ps.setString(4, partida.getFase());
            ps.setInt(5, partida.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Atualiza o placar. Retorna false se a partida nao existe ou ja terminou/foi cancelada. */
    public boolean atualizarPlacar(int id, int placarA, int placarB) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_PLACAR)) {
            ps.setInt(1, placarA);
            ps.setInt(2, placarB);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean finalizar(int id) throws SQLException {
        return executarPorId(SQL_FINALIZAR, id);
    }

    public boolean cancelar(int id) throws SQLException {
        return executarPorId(SQL_CANCELAR, id);
    }

    /** Remove a partida (partida finalizada nao e removida: alteraria a classificacao). */
    public boolean deletar(int id) throws SQLException {
        return executarPorId(SQL_DELETAR, id);
    }

    private boolean executarPorId(String sql, int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Partida mapear(ResultSet rs) throws SQLException {
        Partida p = new Partida();
        p.setId(rs.getInt("id"));
        p.setModalidadeId(rs.getInt("modalidade_id"));
        p.setTimeAId(rs.getInt("time1_id"));
        p.setTimeBId(rs.getInt("time2_id"));
        p.setPlacarA(rs.getInt("placar_a"));
        p.setPlacarB(rs.getInt("placar_b"));
        p.setData(rs.getString("data"));
        p.setHora(rs.getString("hora"));
        p.setLocal(rs.getString("local"));
        p.setFase(rs.getString("fase"));
        p.setStatus(rs.getString("status"));
        p.setModalidadeNome(rs.getString("modalidade_nome"));
        p.setTimeANome(rs.getString("time_a_nome"));
        p.setTimeBNome(rs.getString("time_b_nome"));
        return p;
    }
}
