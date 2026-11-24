package com.example.dao;

import com.example.model.Inscricao;
import com.example.model.Inscricao.StatusInscricao;
import com.example.util.Conexao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class InscricaoDao{
    private static final String SQL_LISTAR = "SELECT * FROM inscricao ORDER BY id";
    private static final String SQL_BUSCAR = "SELECT * FROM inscricao WHERE id = ?";
    private static final String SQL_INSERIR = "INSERT INTO inscricao (modalidade_id, time_id, status, data_inscricao) VALUES (?, ?, ?, ?)";
    private static final String SQL_ATUALIZAR = "UPDATE inscricao SET modalidade_id = ?, time_id = ?,status = ?, data_inscricao = ? WHERE id = ?";
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
            StatusInscricao status = inscricao.getStatus() != null ? inscricao.getStatus() : StatusInscricao.PENDENTE;
            inscricao.setStatus(status);
            ps.setString(3, status.name());
            LocalDate data = inscricao.getDataInscricao() != null ? inscricao.getDataInscricao() : LocalDate.now();
            ps.setString(4, data.toString());
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
            LocalDate data = inscricao.getDataInscricao() != null ? 
            inscricao.getDataInscricao() : LocalDate.now();
            ps.setString(4, data.toString());
            ps.setInt(5, inscricao.getId());
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

    public List<Inscricao> listarModalidade(int idModalidade) throws SQLException {
        List<Inscricao> lista = new ArrayList<>();
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM inscricao WHERE idModalidade = ? AND status = 'CONFIRMADA'")) {
            ps.setInt(1, idModalidade);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public boolean atualizarStatus(int id, StatusInscricao novoStatus) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE inscricao SET status = ? WHERE id = ?")) {
            ps.setString(1, novoStatus.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean timeTemInscricaoAtiva(int idTime) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM inscricao WHERE idTime = ? AND status = 'CONFIRMADA'")) {
            ps.setInt(1, idTime);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean estaConfirmado(int idTime, int idModalidade) throws SQLException {
        Connection conn = Conexao.getConexao();
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM inscricao WHERE idTime = ? AND idModalidade = ? AND status = 'CONFIRMADA'")) {
            ps.setInt(1, idTime);
            ps.setInt(2, idModalidade);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Inscricao mapear(ResultSet rs) throws SQLException {
        Inscricao inscricao = new Inscricao();
        inscricao.setId(rs.getInt("id"));
        inscricao.setIdModalidade(rs.getInt("idModalidade"));
        inscricao.setIdTime(rs.getInt("idTime"));
        inscricao.setStatus(StatusInscricao.valueOf(rs.getString("status")));
        String data = rs.getString("dataInscricao");
        if (data != null) {
            inscricao.setDataInscricao(LocalDate.parse(data));
        }
        return inscricao;
    }
}
