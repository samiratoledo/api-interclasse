package com.example.controller;

import com.example.dao.TimeDao;
import com.example.dao.TurmaDao;
import com.example.model.Time;
import com.example.model.Turma;
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
 * Controller de Time (CRUD + vinculo com turma).
 *
 * Rotas:
 *   GET    /api/times              -> lista todos os times
 *   GET    /api/times?turmaId=1    -> lista os times de uma turma
 *   GET    /api/times?id=1         -> busca um time
 *   POST   /api/times              -> cria um time
 *   PUT    /api/times?id=1         -> atualiza um time
 *   DELETE /api/times?id=1         -> remove um time (409 se tiver inscricoes/partidas)
 *
 * Campos obrigatorios: nome, responsavel, turmaId.
 * Campos opcionais: cor, contato (se preenchido, deve ter 10 ou 11 digitos).
 */
@WebServlet("/times")
public class TimeController extends HttpServlet {

    private static final int NOME_MAX = 60;

    private TimeDao dao = new TimeDao();
    private TurmaDao turmaDao = new TurmaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            String turmaId = request.getParameter("turmaId");

            if (id != null) {
                Time time = dao.buscarPorId(Integer.parseInt(id));
                if (time == null) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time nao encontrado");
                    return;
                }
                JsonUtil.enviar(response, time);
                return;
            }

            List<Time> times;
            if (turmaId != null) {
                int turma = Integer.parseInt(turmaId);
                if (turmaDao.buscarPorId(turma) == null) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrada");
                    return;
                }
                times = dao.listarPorTurma(turma);
            } else {
                times = dao.listar();
            }
            JsonUtil.enviar(response, times);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id ou turmaId invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Time time = JsonUtil.ler(request, Time.class);
            if (time == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
                return;
            }

            normalizar(time);
            if (!validar(time, 0, true, response)) {
                return;
            }

            JsonUtil.enviar(response, dao.inserir(time));

        } catch (com.google.gson.JsonParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "JSON invalido");
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
            int timeId = Integer.parseInt(id);

            Time atual = dao.buscarPorId(timeId);
            if (atual == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time nao encontrado");
                return;
            }

            Time time = JsonUtil.ler(request, Time.class);
            if (time == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
                return;
            }
            time.setId(timeId);

            normalizar(time);
            // So exige turma ATIVA quando o time esta mudando de turma
            boolean mudouDeTurma = time.getTurmaId() == null || !time.getTurmaId().equals(atual.getTurmaId());
            if (!validar(time, timeId, mudouDeTurma, response)) {
                return;
            }

            dao.atualizar(time);
            JsonUtil.enviar(response, dao.buscarPorId(timeId));

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (com.google.gson.JsonParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "JSON invalido");
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
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time nao encontrado");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Time removido");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            if (TimeDao.ehViolacaoDeChave(e)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                        "Time possui inscricoes ou partidas vinculadas e nao pode ser removido");
            } else {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    /** Remove espacos extras e transforma texto vazio em null. */
    private void normalizar(Time time) {
        time.setNome(limpar(time.getNome()));
        time.setCor(limpar(time.getCor()));
        time.setResponsavel(limpar(time.getResponsavel()));
        time.setContato(limpar(time.getContato()));
    }

    private String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String s = valor.trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * Valida o time. Se algo estiver errado, ja envia a resposta de erro e devolve false.
     *
     * @param ignorarId        id do proprio time (0 no cadastro), para a checagem de nome repetido
     * @param exigirTurmaAtiva true no cadastro ou quando o time troca de turma
     */
    private boolean validar(Time time, int ignorarId, boolean exigirTurmaAtiva, HttpServletResponse response)
            throws IOException, SQLException {

        if (time.getNome() == null) {
            return erro(response, HttpServletResponse.SC_BAD_REQUEST, "nome e obrigatorio");
        }
        if (time.getNome().length() < 2 || time.getNome().length() > NOME_MAX) {
            return erro(response, HttpServletResponse.SC_BAD_REQUEST,
                    "nome deve ter entre 2 e " + NOME_MAX + " caracteres");
        }
        if (time.getResponsavel() == null) {
            return erro(response, HttpServletResponse.SC_BAD_REQUEST, "responsavel e obrigatorio");
        }
        if (time.getTurmaId() == null) {
            return erro(response, HttpServletResponse.SC_BAD_REQUEST, "turmaId e obrigatorio");
        }
        if (time.getContato() != null) {
            int digitos = time.getContato().replaceAll("\\D", "").length();
            if (digitos != 10 && digitos != 11) {
                return erro(response, HttpServletResponse.SC_BAD_REQUEST,
                        "contato invalido: informe telefone com DDD (10 ou 11 digitos)");
            }
        }

        Turma turma = turmaDao.buscarPorId(time.getTurmaId());
        if (turma == null) {
            return erro(response, HttpServletResponse.SC_NOT_FOUND, "Turma nao encontrada");
        }
        if (exigirTurmaAtiva && !Boolean.TRUE.equals(turma.getAtiva())) {
            return erro(response, HttpServletResponse.SC_BAD_REQUEST, "Turma inativa");
        }

        if (dao.existeNomeNaTurma(time.getNome(), time.getTurmaId(), ignorarId)) {
            return erro(response, HttpServletResponse.SC_CONFLICT, "Ja existe um time com esse nome nessa turma");
        }
        return true;
    }

    private boolean erro(HttpServletResponse response, int status, String mensagem) throws IOException {
        JsonUtil.enviarErro(response, status, mensagem);
        return false;
    }
}