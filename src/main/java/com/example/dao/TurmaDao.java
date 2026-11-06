package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Turma;
import com.example.util.Conexao;

/**
 * DAO de Turma.
 * Responsavel pelas operacoes de banco de dados das turmas.
 */
public class TurmaDao {

    private static final String SQL_LISTAR =
            "SELECT * FROM turma ORDER BY nome";

    private static final String SQL_BUSCAR =
            "SELECT * FROM turma WHERE id = ?";

    private static final String SQL_INSERIR =
            "INSERT INTO turma (nome, curso, ano, periodo, imagem, ativa) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE turma SET nome = ?, curso = ?, ano = ?, periodo = ?, " +
            "imagem = ?, ativa = ? WHERE id = ?";

    private static final String SQL_DELETAR =
            "DELETE FROM turma WHERE id = ?";

    public List<Turma> listar() throws SQLException {
        List<Turma> turmas = new ArrayList<>();
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                turmas.add(mapear(rs));
            }
        }

        return turmas;
    }

    public Turma buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Turma inserir(Turma turma) throws SQLException {
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(
                SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, turma.getNome());
            ps.setString(2, turma.getCurso());

            if (turma.getAno() != null) {
                ps.setInt(3, turma.getAno());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, turma.getPeriodo());
            ps.setString(5, turma.getImagem());
            ps.setBoolean(6, Boolean.TRUE.equals(turma.getAtiva()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    turma.setId(keys.getInt(1));
                }
            }
        }

        return turma;
    }

    public boolean atualizar(Turma turma) throws SQLException {
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, turma.getNome());
            ps.setString(2, turma.getCurso());

            if (turma.getAno() != null) {
                ps.setInt(3, turma.getAno());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, turma.getPeriodo());
            ps.setString(5, turma.getImagem());
            ps.setBoolean(6, Boolean.TRUE.equals(turma.getAtiva()));
            ps.setInt(7, turma.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id) throws SQLException {
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Turma mapear(ResultSet rs) throws SQLException {
        Turma turma = new Turma();

        turma.setId(rs.getInt("id"));
        turma.setNome(rs.getString("nome"));
        turma.setCurso(rs.getString("curso"));

        int ano = rs.getInt("ano");
        turma.setAno(rs.wasNull() ? null : ano);

        turma.setPeriodo(rs.getString("periodo"));
        turma.setImagem(rs.getString("imagem"));
        turma.setAtiva(rs.getBoolean("ativa"));

        return turma;
    }
}
