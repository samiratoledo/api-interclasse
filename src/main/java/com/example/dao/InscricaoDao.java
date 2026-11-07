package com.example.dao;

import com.example.model.Inscricao;
import com.example.model.Inscricao.StatusInscricao;
import com.example.util.Conexao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class InscricaoDao{
    private static final String SQL_LISTAR = "SELECT * FROM inscricao ORDER BY nome";
    private static final String SQL_BUSCAR = "SELECT * FROM inscricao WHERE id = ?";
    private static final String SQL_INSERIR = "INSERT INTO inscricao (idModalidade, idTime, status) VALUES (?, ?, ?)";
    private static final String SQL_ATUALIZAR = "UPDATE inscricao SET idModalidade = ?, idTime = ?, status = ? WHERE id = ?";
    private static final String SQL_DELETAR = "DELETE FROM inscricao WHERE id = ?";

    public List<Inscricao> listar() throws SQLException {
        List<Inscricao> inscricoes = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                inscricoes.add(mapear(rs));
            }
        }
        return inscricoes;
    }

    public Inscricao buscarPorId(int id) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }
    public Inscricao inserir(Inscricao inscricao) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inscricao.getIdModalidade());
            ps.setInt(2, inscricao.getIdTime());
            ps.setString(3, inscricao.getStatus().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    inscricao.setId(keys.getInt(1));
                }
            }
        }
        return inscricao;
    }
    public boolean atualizar(Inscricao inscricao) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement(SQL_ATUALIZAR)) {
            ps.setInt(1, inscricao.getIdModalidade());
            ps.setInt(2, inscricao.getIdTime());
            ps.setString(3, inscricao.getStatus().toString());
            ps.setInt(4, inscricao.getId());
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

    private Inscricao mapear(ResultSet rs) throws SQLException {
        Inscricao inscricao = new Inscricao();
        inscricao.setId(rs.getInt("id"));
        inscricao.setIdModalidade(rs.getInt("idModalidade"));
        inscricao.setIdTime(rs.getInt("idTime"));
        inscricao.setStatus(StatusInscricao.valueOf(rs.getString("status")));
        return inscricao;
    }
}