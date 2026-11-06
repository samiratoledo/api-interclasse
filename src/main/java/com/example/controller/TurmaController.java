package com.example.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.dao.TurmaDao;
import com.example.model.Turma;
import com.example.util.JsonUtil;

/**
 * Controller de Turma com operacoes basicas de CRUD.
 *
 * Rotas:
 *   GET    /api/turmas      -> lista todas as turmas
 *   GET    /api/turmas?id=1 -> busca uma turma
 *   POST   /api/turmas      -> cria uma turma
 *   PUT    /api/turmas?id=1 -> atualiza uma turma
 *   DELETE /api/turmas?id=1 -> remove uma turma
 */
@WebServlet("/turmas")
public class TurmaController extends HttpServlet {

    private TurmaDao dao = new TurmaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String id = request.getParameter("id");

            if (id == null) {
                List<Turma> turmas = dao.listar();
                JsonUtil.enviar(response, turmas);
                return;
            }

            Turma turma = dao.buscarPorId(Integer.parseInt(id));

            if (turma == null) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrada");
                return;
            }

            JsonUtil.enviar(response, turma);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Turma turma = JsonUtil.ler(request, Turma.class);

            if (turma == null || turma.getNome() == null
                    || turma.getNome().isBlank()) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
                return;
            }

            if (turma.getAno() != null && turma.getAno() <= 0) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "ano invalido");
                return;
            }

            if (turma.getPeriodo() != null && !turma.getPeriodo().isBlank()
                    && !turma.getPeriodo().equals("manha")
                    && !turma.getPeriodo().equals("tarde")) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "periodo deve ser manha ou tarde");
                return;
            }

            if (turma.getAtiva() == null) {
                turma.setAtiva(true);
            }

            dao.inserir(turma);
            JsonUtil.enviar(response, turma);

        } catch (SQLException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String id = request.getParameter("id");

            if (id == null) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
                return;
            }

            Turma turma = JsonUtil.ler(request, Turma.class);

            if (turma == null || turma.getNome() == null
                    || turma.getNome().isBlank()) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
                return;
            }

            turma.setId(Integer.parseInt(id));

            if (turma.getAno() != null && turma.getAno() <= 0) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "ano invalido");
                return;
            }

            if (turma.getPeriodo() != null && !turma.getPeriodo().isBlank()
                    && !turma.getPeriodo().equals("manha")
                    && !turma.getPeriodo().equals("tarde")) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "periodo deve ser manha ou tarde");
                return;
            }

            if (turma.getAtiva() == null) {
                turma.setAtiva(true);
            }

            if (!dao.atualizar(turma)) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrada");
                return;
            }

            JsonUtil.enviar(response, turma);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String id = request.getParameter("id");

            if (id == null) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
                return;
            }

            if (!dao.deletar(Integer.parseInt(id))) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrada");
                return;
            }

            JsonUtil.enviarMensagem(response,
                    HttpServletResponse.SC_OK, "Turma removida");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}