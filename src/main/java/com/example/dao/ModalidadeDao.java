package com.example.dao;

import com.example.model.Modalidade;
import com.example.util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Modalidade.
 * Responsavel pelas operacoes de banco de dados das modalidades.
 */
public class ModalidadeDao {

    private static final String SQL_LISTAR =
            "SELECT * FROM modalidade ORDER BY nome";

    private static final String SQL_BUSCAR =
            "SELECT * FROM modalidade WHERE id = ?";

    private static final String SQL_INSERIR =
            "INSERT INTO modalidade (nome, descricao, categoria, " +
            "minimo_atletas, maximo_atletas, icone, ativo) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE modalidade SET nome = ?, descricao = ?, categoria = ?, " +
            "minimo_atletas = ?, maximo_atletas = ?, icone = ?, ativo = ? " +
            "WHERE id = ?";

    private static final String SQL_DELETAR =
            "DELETE FROM modalidade WHERE id = ?";

    public List<Modalidade> listar() throws SQLException {
        List<Modalidade> modalidades = new ArrayList<>();
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modalidades.add(mapear(rs));
            }
        }

        return modalidades;
    }

    public Modalidade buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Modalidade inserir(Modalidade modalidade) throws SQLException {
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(
                SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, modalidade.getNome());
            ps.setString(2, modalidade.getDescricao());
            ps.setString(3, modalidade.getCategoria());
            ps.setInt(4, modalidade.getMinimoAtletas());
            ps.setInt(5, modalidade.getMaximoAtletas());
            ps.setString(6, modalidade.getIcone());
            ps.setBoolean(7, Boolean.TRUE.equals(modalidade.getAtivo()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    modalidade.setId(keys.getInt(1));
                }
            }
        }

        return modalidade;
    }

    public boolean atualizar(Modalidade modalidade) throws SQLException {
        Connection conn = Conexao.getConexao();

        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, modalidade.getNome());
            ps.setString(2, modalidade.getDescricao());
            ps.setString(3, modalidade.getCategoria());
            ps.setInt(4, modalidade.getMinimoAtletas());
            ps.setInt(5, modalidade.getMaximoAtletas());
            ps.setString(6, modalidade.getIcone());
            ps.setBoolean(7, Boolean.TRUE.equals(modalidade.getAtivo()));
            ps.setInt(8, modalidade.getId());

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

    private Modalidade mapear(ResultSet rs) throws SQLException {
        Modalidade modalidade = new Modalidade();

        modalidade.setId(rs.getInt("id"));
        modalidade.setNome(rs.getString("nome"));
        modalidade.setDescricao(rs.getString("descricao"));
        modalidade.setCategoria(rs.getString("categoria"));
        modalidade.setMinimoAtletas(rs.getInt("minimo_atletas"));
        modalidade.setMaximoAtletas(rs.getInt("maximo_atletas"));
        modalidade.setIcone(rs.getString("icone"));
        modalidade.setAtivo(rs.getBoolean("ativo"));

        return modalidade;
    }
}
