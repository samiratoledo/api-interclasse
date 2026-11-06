package com.example.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.dao.ModalidadeDao;
import com.example.model.Modalidade;
import com.example.util.JsonUtil;

/**
 * Controller de Modalidade com operacoes basicas de CRUD.
 *
 * Rotas:
 *   GET    /api/modalidades      -> lista modalidades
 *   GET    /api/modalidades?id=1 -> busca uma modalidade
 *   POST   /api/modalidades      -> cria uma modalidade
 *   PUT    /api/modalidades?id=1 -> atualiza uma modalidade
 *   DELETE /api/modalidades?id=1 -> remove uma modalidade
 */
@WebServlet("/modalidades")
public class ModalidadeController extends HttpServlet {

    private ModalidadeDao dao = new ModalidadeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String id = request.getParameter("id");

            if (id == null) {
                List<Modalidade> modalidades = dao.listar();
                JsonUtil.enviar(response, modalidades);
                return;
            }

            Modalidade modalidade = dao.buscarPorId(Integer.parseInt(id));

            if (modalidade == null) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Modalidade nao encontrada");
                return;
            }

            JsonUtil.enviar(response, modalidade);

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
            Modalidade modalidade = JsonUtil.ler(request, Modalidade.class);

            if (modalidade == null || modalidade.getNome() == null
                    || modalidade.getNome().isBlank()) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
                return;
            }

            if (modalidade.getCategoria() == null
                    || modalidade.getCategoria().isBlank()) {
                modalidade.setCategoria("misto");
            }

            if (!modalidade.getCategoria().equals("masculino")
                    && !modalidade.getCategoria().equals("feminino")
                    && !modalidade.getCategoria().equals("misto")) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "categoria deve ser masculino, feminino ou misto");
                return;
            }

            if (modalidade.getMinimoAtletas() == null) {
                modalidade.setMinimoAtletas(1);
            }

            if (modalidade.getMaximoAtletas() == null) {
                modalidade.setMaximoAtletas(30);
            }

            if (modalidade.getMinimoAtletas() < 1
                    || modalidade.getMaximoAtletas() < modalidade.getMinimoAtletas()) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "limites de atletas invalidos");
                return;
            }

            if (modalidade.getAtivo() == null) {
                modalidade.setAtivo(true);
            }

            dao.inserir(modalidade);
            JsonUtil.enviar(response, modalidade);

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

            Modalidade modalidade = JsonUtil.ler(request, Modalidade.class);

            if (modalidade == null || modalidade.getNome() == null
                    || modalidade.getNome().isBlank()) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
                return;
            }

            modalidade.setId(Integer.parseInt(id));

            if (modalidade.getCategoria() == null
                    || modalidade.getCategoria().isBlank()) {
                modalidade.setCategoria("misto");
            }

            if (!modalidade.getCategoria().equals("masculino")
                    && !modalidade.getCategoria().equals("feminino")
                    && !modalidade.getCategoria().equals("misto")) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "categoria deve ser masculino, feminino ou misto");
                return;
            }

            if (modalidade.getMinimoAtletas() == null) {
                modalidade.setMinimoAtletas(1);
            }

            if (modalidade.getMaximoAtletas() == null) {
                modalidade.setMaximoAtletas(30);
            }

            if (modalidade.getMinimoAtletas() < 1
                    || modalidade.getMaximoAtletas() < modalidade.getMinimoAtletas()) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "limites de atletas invalidos");
                return;
            }

            if (modalidade.getAtivo() == null) {
                modalidade.setAtivo(true);
            }

            if (!dao.atualizar(modalidade)) {
                JsonUtil.enviarErro(response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Modalidade nao encontrada");
                return;
            }

            JsonUtil.enviar(response, modalidade);

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
                        HttpServletResponse.SC_NOT_FOUND,
                        "Modalidade nao encontrada");
                return;
            }

            JsonUtil.enviarMensagem(response,
                    HttpServletResponse.SC_OK, "Modalidade removida");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}