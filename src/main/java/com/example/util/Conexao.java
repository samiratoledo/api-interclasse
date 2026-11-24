package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Conexao {

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
                            criado_em   TEXT    NOT NULL DEFAULT (datetime('now')),
                            turma_id    INTEGER REFERENCES turma(id)
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

                """
                        CREATE TABLE IF NOT EXISTS partida (
                            id              INTEGER PRIMARY KEY AUTOINCREMENT,
                            modalidade_id   INTEGER NOT NULL REFERENCES modalidade(id),
                            time1_id       INTEGER NOT NULL REFERENCES time(id),
                            time2_id       INTEGER NOT NULL REFERENCES time(id),
                            placar_a        INTEGER NOT NULL DEFAULT 0 CHECK (placar_a >= 0),
                            placar_b        INTEGER NOT NULL DEFAULT 0 CHECK (placar_b >= 0),
                            data            TEXT,
                            hora            TEXT,
                            local           TEXT,
                            fase            TEXT    NOT NULL DEFAULT 'AVULSO'
                                            CHECK (fase IN ('AVULSO', 'GRUPOS', 'QUARTAS', 'SEMI', 'FINAL')),
                            status          TEXT    NOT NULL DEFAULT 'AGENDADA'
                                            CHECK (status IN ('AGENDADA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA')),
                            criado_em       TEXT    NOT NULL DEFAULT (datetime('now')),
                            CHECK (time_a_id <> time_b_id)
                        )
                        """,
                "CREATE INDEX IF NOT EXISTS idx_partida_modalidade ON partida(modalidade_id, status)",
                """
                        CREATE TABLE IF NOT EXISTS configuracao (
                            chave   TEXT PRIMARY KEY,
                            valor   TEXT NOT NULL
                        )
                        """,
                "INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PONTOS_VITORIA', '3')",
                "INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PONTOS_EMPATE', '1')",
                "INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PONTOS_DERROTA', '0')",
                "INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('PERMITE_EMPATE', '1')",

                        """
                        CREATE TABLE IF NOT EXISTS inscricao (
                            id              INTEGER PRIMARY KEY AUTOINCREMENT,
                            idModalidade   INTEGER NOT NULL REFERENCES modalidade(id),
                            idTime         INTEGER NOT NULL REFERENCES time(id),
                            status         TEXT    NOT NULL DEFAULT 'PENDENTE'
                                           CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'NEGADA')),
                            dataInscricao  TEXT
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

        migrarTabelaTime(conn);
    }

   
    private static void migrarTabelaTime(Connection conn) throws SQLException {
        boolean temTurmaId = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(time)")) {
            while (rs.next()) {
                if ("turma_id".equalsIgnoreCase(rs.getString("name"))) {
                    temTurmaId = true;
                }
            }
        }

        try (Statement stmt = conn.createStatement()) {
            if (!temTurmaId) {
                stmt.execute("ALTER TABLE time ADD COLUMN turma_id INTEGER REFERENCES turma(id)");
            }
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_time_turma ON time(turma_id)");
        }
    }
}
