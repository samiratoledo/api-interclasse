package com.example.controller;

import com.example.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Raiz da API. Apenas um resumo das rotas disponiveis, util para testar
 * se o servidor subiu (GET /api).
 */
@WebServlet("/")
public class ApiController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("nome", "API Interclasse ETEC");
        resumo.put("versao", "0.1.0");
        resumo.put("descricao", "Sistema de organizacao do interclasse de final de ano");

        Map<String, Object> rotas = new LinkedHashMap<>();
        rotas.put("GET /api", "Resumo da API");
        rotas.put("POST /api/auth/registrar", "Cadastro de participante");
        rotas.put("POST /api/auth/registrar-organizador", "Cadastro de organizador (chave mestra)");
        rotas.put("POST /api/auth/login", "Login");
        rotas.put("POST /api/auth/logout", "Logout");
        rotas.put("GET /api/auth/me", "Usuario logado");
        rotas.put("GET /api/times", "Lista de times");
        rotas.put("GET /api/times?id=1", "Detalhe de um time");
        rotas.put("POST /api/times", "Cria um time");
        rotas.put("PUT /api/times?id=1", "Atualiza um time");
        rotas.put("DELETE /api/times?id=1", "Remove um time");
        resumo.put("rotas", rotas);

        JsonUtil.enviar(response, resumo);
    }
}
