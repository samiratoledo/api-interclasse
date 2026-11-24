package com.example.controller;

import com.example.dao.InscricaoDao;
import com.example.model.Inscricao;
import com.example.model.Inscricao.StatusInscricao;
import com.example.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Rotas:
 *   GET    /inscricoes                    -> lista todas
 *   GET    /inscricoes?id=1               -> busca uma
 *   GET    /inscricoes?modalidade_id=2     -> lista as CONFIRMADAS da modalidade
 *   POST   /inscricoes                    -> cria (status padrao: PENDENTE)
 *   PUT    /inscricoes?id=1               -> atualiza (corpo parcial aceito)
 *   PUT    /inscricoes?id=1&status=CONFIRMADA -> muda so o status
 *   DELETE /inscricoes?id=1               -> remove
 */
@WebServlet("/inscricoes")
public class InscricaoController extends HttpServlet {

    private InscricaoDao dao = new InscricaoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            String modalidade_id = request.getParameter("modalidade_id");

            if (id != null) {
                Inscricao inscricao = dao.buscarPorId(Integer.parseInt(id));
                if (inscricao == null) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                    return;
                }
                JsonUtil.enviar(response, inscricao);
                return;
            }

            List<Inscricao> lista = (modalidade_id != null)
                    ? dao.listarModalidade(Integer.parseInt(modalidade_id))
                    : dao.listar();
            JsonUtil.enviar(response, lista);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "parametro numerico invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Inscricao inscricao = JsonUtil.ler(request, Inscricao.class);

            if (inscricao == null || inscricao.getIdTime() <= 0 || inscricao.getIdModalidade() <= 0) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                        "time_id e modalidade_id sao obrigatorios");
                return;
            }

            if (dao.estaConfirmado(inscricao.getIdTime(), inscricao.getIdModalidade())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                        "time ja esta confirmado nessa modalidade");
                return;
            }

            //status oendente por padrao
            Inscricao criada = dao.inserir(inscricao);
            response.setStatus(HttpServletResponse.SC_CREATED);
            JsonUtil.enviar(response, criada);

        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            if (id == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
                return;
            }
            int idInscricao = Integer.parseInt(id);

            String statusParam = request.getParameter("status");
            if (statusParam != null) {
                StatusInscricao novoStatus = StatusInscricao.valueOf(statusParam.toUpperCase());
                if (!dao.atualizarStatus(idInscricao, novoStatus)) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                    return;
                }
                JsonUtil.enviar(response, dao.buscarPorId(idInscricao));
                return;
            }
            Inscricao existente = dao.buscarPorId(idInscricao);
            if (existente == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                return;
            }

            Inscricao dados = JsonUtil.ler(request, Inscricao.class);
            if (dados == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
                return;
            }

            if (dados.getIdModalidade() > 0) existente.setIdModalidade(dados.getIdModalidade());
            if (dados.getIdTime() > 0)       existente.setIdTime(dados.getIdTime());
            if (dados.getStatus() != null)   existente.setStatus(dados.getStatus());
            if (dados.getDataInscricao() != null) existente.setDataInscricao(dados.getDataInscricao());

            dao.atualizar(existente);
            JsonUtil.enviar(response, existente);

        } catch (NumberFormatException e) {          // nao esquecer, antes do ilegal pro parseInt nao falhar
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (IllegalArgumentException e) {       // StatusInscricao.valueOf falhou
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                    "status invalido (use PENDENTE, CONFIRMADA ou CANCELADA)");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            if (id == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
                return;
            }

            if (!dao.deletar(Integer.parseInt(id))) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Inscricao nao encontrada");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Inscricao removida");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}