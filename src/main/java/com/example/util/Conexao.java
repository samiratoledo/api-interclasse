package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexão com o banco SQLite (arquivo único, sem servidor de banco).
 *
 * O banco e as tabelas são criados automaticamente na primeira conexão.
 */
public class Conexao {

    /**
     * Caminho do arquivo do banco. Pode ser alterado pela variável de ambiente
     * INTERCLASSE_DB.
     */
    private static final String URL = "jdbc:sqlite:" + caminhoBanco();

    private static Connection conexao = null;

    private static String caminhoBanco() {
        String env = System.getenv("INTERCLASSE_DB");
        if (env == null || env.isBlank()) {
            env = "interclasse.db";
        }
        return env;
    }

    public static Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver do SQLite nao encontrado", e);
            }
            conexao = DriverManager.getConnection(URL);
            criarTabelas(conexao);
        }
        return conexao;
    }

    /**
     * Script das tabelas usadas pela base. Alunos podem ampliar este script
     * conforme implementam as atividades do README.
     */
    private static void criarTabelas(Connection conn) throws SQLException {
        String[] scripts = {
                """
                        CREATE TABLE IF NOT EXISTS usuario (
                            id       INTEGER PRIMARY KEY AUTOINCREMENT,
                            nome     TEXT    NOT NULL,
                            email    TEXT    NOT NULL UNIQUE,
                            senha    TEXT    NOT NULL,
                            tipo     TEXT    NOT NULL DEFAULT 'PARTICIPANTE'
                                         CHECK (tipo IN ('PARTICIPANTE', 'ORGANIZADOR')),
                            criado_em TEXT    NOT NULL DEFAULT (datetime('now'))
                        )
                        """,
                """
                        CREATE TABLE IF NOT EXISTS chave_mestra (
                            id    INTEGER PRIMARY KEY AUTOINCREMENT,
                            chave TEXT NOT NULL UNIQUE
                        )
                        """,
                """
                        CREATE TABLE IF NOT EXISTS time (
                            id          INTEGER PRIMARY KEY AUTOINCREMENT,
                            nome        TEXT    NOT NULL,
                            cor         TEXT,
                            responsavel TEXT,
                            contato     TEXT,
                            criado_em   TEXT    NOT NULL DEFAULT (datetime('now'))
                        )
                        """,

                """
                        CREATE TABLE IF NOT EXISTS turma (
                            id        INTEGER PRIMARY KEY AUTOINCREMENT,
                            nome      TEXT    NOT NULL UNIQUE,
                            curso     TEXT,
                            ano       INTEGER,
                            periodo   TEXT,
                            imagem    TEXT,
                            ativa     INTEGER NOT NULL DEFAULT 1,
                            criado_em TEXT    NOT NULL DEFAULT (datetime('now'))
                        )
                        """,
                """
                        CREATE TABLE IF NOT EXISTS modalidade (
                            id              INTEGER PRIMARY KEY AUTOINCREMENT,
                            nome            TEXT    NOT NULL UNIQUE,
                            descricao       TEXT,
                            categoria       TEXT    NOT NULL DEFAULT 'misto'
                                            CHECK (categoria IN ('masculino', 'feminino', 'misto')),
                            minimo_atletas  INTEGER NOT NULL DEFAULT 1,
                            maximo_atletas  INTEGER NOT NULL DEFAULT 30,
                            icone           TEXT,
                            ativo           INTEGER NOT NULL DEFAULT 1,
                            criado_em       TEXT    NOT NULL DEFAULT (datetime('now'))
                        )
                        """,
                "INSERT OR IGNORE INTO chave_mestra (chave) VALUES ('ETEC-INTERCLASSE-2026')"
        };

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            for (String script : scripts) {
                stmt.execute(script);
            }
        }
    }
}
