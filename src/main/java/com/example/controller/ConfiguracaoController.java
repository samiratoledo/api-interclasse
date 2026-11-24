package com.example.controller;

import com.example.dao.ConfiguracaoDao;
import com.example.model.Configuracao;
import com.example.util.JsonUtil;
import com.example.util.Permissao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;


@WebServlet("/configuracoes")
public class ConfiguracaoController extends HttpServlet {

    private ConfiguracaoDao dao = new ConfiguracaoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirLogin(request, response)) {
            return;
        }
        try {
            JsonUtil.enviar(response, dao.listar());
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirOrganizador(request, response)) {
            return;
        }
        try {
            Configuracao config = JsonUtil.ler(request, Configuracao.class);
            if (config == null || config.getChave() == null || config.getValor() == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "chave e valor sao obrigatorios");
                return;
            }

            String chave = config.getChave().trim().toUpperCase();
            String valor = config.getValor().trim();

            if (!ConfiguracaoDao.CHAVES_VALIDAS.contains(chave)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                        "chave invalida: use " + String.join(", ", ConfiguracaoDao.CHAVES_VALIDAS));
                return;
            }

            int numero;
            try {
                numero = Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "valor deve ser um numero inteiro");
                return;
            }

            if (ConfiguracaoDao.PERMITE_EMPATE.equals(chave)) {
                if (numero != 0 && numero != 1) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                            "PERMITE_EMPATE deve ser 0 ou 1");
                    return;
                }
            } else if (numero < 0) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "pontos nao podem ser negativos");
                return;
            }

            dao.salvar(chave, String.valueOf(numero));
            JsonUtil.enviar(response, dao.listar());

        } catch (com.google.gson.JsonParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "JSON invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
