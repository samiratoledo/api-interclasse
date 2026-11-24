package com.example.controller;

import com.example.dao.ConfiguracaoDao;
import com.example.dao.InscricaoDao;
import com.example.dao.ModalidadeDao;
import com.example.dao.PartidaDao;
import com.example.dao.TimeDao;
import com.example.model.Modalidade;
import com.example.model.Partida;
import com.example.model.Time;
import com.example.util.JsonUtil;
import com.example.util.Permissao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


@WebServlet({"/partidas", "/partidas/*"})
public class PartidaController extends HttpServlet {

    private PartidaDao dao = new PartidaDao();
    private ModalidadeDao modalidadeDao = new ModalidadeDao();
    private TimeDao timeDao = new TimeDao();
    private InscricaoDao inscricaoDao = new InscricaoDao();
    private ConfiguracaoDao configuracaoDao = new ConfiguracaoDao();

    /** O HttpServlet 3.1 nao tem doPatch*/
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(request.getMethod())) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }

    /**GET */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirLogin(request, response)) {
            return;
        }
        try {
            Rota rota = lerRota(request);
            if (rota.acao != null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "rota nao encontrada");
                return;
            }

            if (rota.id != null) {
                Partida partida = dao.buscarPorId(rota.id);
                if (partida == null) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Partida nao encontrada");
                    return;
                }
                JsonUtil.enviar(response, partida);
                return;
            }

            Integer modalidadeId = null;
            String modalidadeParam = limpar(request.getParameter("modalidadeId"));
            if (modalidadeParam != null) {
                modalidadeId = Integer.parseInt(modalidadeParam);
            }

            String status = limpar(request.getParameter("status"));
            if (status != null) {
                status = status.toUpperCase();
                if (!Partida.STATUS_VALIDOS.contains(status)) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                            "status invalido: use " + String.join(", ", Partida.STATUS_VALIDOS));
                    return;
                }
            }

            String data = limpar(request.getParameter("data"));
            if (data != null) {
                try {
                    data = LocalDate.parse(data).toString();
                } catch (DateTimeParseException e) {
                    JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                            "data invalida: use yyyy-MM-dd");
                    return;
                }
            }

            JsonUtil.enviar(response, dao.listar(modalidadeId, status, data));

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id ou modalidadeId invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /** POST */

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirOrganizador(request, response)) {
            return;
        }
        try {
            Rota rota = lerRota(request);

            if ("finalizar".equals(rota.acao)) {
                finalizar(rota, response);
            } else if ("cancelar".equals(rota.acao)) {
                cancelar(rota, response);
            } else if (rota.acao == null && rota.id == null) {
                agendar(request, response);
            } else {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "rota nao encontrada");
            }

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (com.google.gson.JsonParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "JSON invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    
    private void agendar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Partida partida = JsonUtil.ler(request, Partida.class);
        if (partida == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
            return;
        }
        if (partida.getModalidadeId() == null || partida.getTimeAId() == null || partida.getTimeBId() == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                    "modalidadeId, timeAId e timeBId sao obrigatorios");
            return;
        }
        if (partida.getTimeAId().equals(partida.getTimeBId())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "os dois times devem ser diferentes");
            return;
        }

        normalizar(partida);
        if (!validarAgenda(partida, response)) {
            return;
        }

        Modalidade modalidade = modalidadeDao.buscarPorId(partida.getModalidadeId());
        if (modalidade == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Modalidade nao encontrada");
            return;
        }
        if (!Boolean.TRUE.equals(modalidade.getAtivo())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Modalidade inativa");
            return;
        }

        Time timeA = timeDao.buscarPorId(partida.getTimeAId());
        if (timeA == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time A nao encontrado");
            return;
        }
        Time timeB = timeDao.buscarPorId(partida.getTimeBId());
        if (timeB == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Time B nao encontrado");
            return;
        }

        if (!inscricaoDao.estaConfirmado(timeA.getId(), modalidade.getId())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                    "O time " + timeA.getNome() + " nao tem inscricao confirmada nessa modalidade");
            return;
        }
        if (!inscricaoDao.estaConfirmado(timeB.getId(), modalidade.getId())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                    "O time " + timeB.getNome() + " nao tem inscricao confirmada nessa modalidade");
            return;
        }

        JsonUtil.enviar(response, dao.inserir(partida));
    }

    /** POST /api/partidas?id=1/finalizar */
    private void finalizar(Rota rota, HttpServletResponse response) throws IOException, SQLException {
        Partida partida = buscarOuErro(rota, response);
        if (partida == null) {
            return;
        }
        if (!exigirEmAberto(partida, "finalizada", response)) {
            return;
        }

        // Empate configuravel: PERMITE_EMPATE = 0 bloqueia a finalizacao de partida empatada
        if (partida.getPlacarA().equals(partida.getPlacarB()) && !configuracaoDao.permiteEmpate()) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                    "Empate nao permitido: ajuste o placar antes de finalizar");
            return;
        }

        // A inscricao pode ter sido negada depois do agendamento
        if (!inscricaoDao.estaConfirmado(partida.getTimeAId(), partida.getModalidadeId())
                || !inscricaoDao.estaConfirmado(partida.getTimeBId(), partida.getModalidadeId())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                    "Um dos times nao tem mais inscricao confirmada nessa modalidade");
            return;
        }

        if (!dao.finalizar(partida.getId())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Partida nao pode ser finalizada");
            return;
        }
        JsonUtil.enviar(response, dao.buscarPorId(partida.getId()));
    }

    /** POST /api/partidas?id=1/cancelar */
    private void cancelar(Rota rota, HttpServletResponse response) throws IOException, SQLException {
        Partida partida = buscarOuErro(rota, response);
        if (partida == null) {
            return;
        }
        if (!exigirEmAberto(partida, "cancelada", response)) {
            return;
        }
        if (!dao.cancelar(partida.getId())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Partida nao pode ser cancelada");
            return;
        }
        JsonUtil.enviar(response, dao.buscarPorId(partida.getId()));
    }



    /** PUT /api/partidas?id=1 : reagenda. Campos nao enviados continuam como estao. */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirOrganizador(request, response)) {
            return;
        }
        try {
            Rota rota = lerRota(request);
            Partida atual = buscarOuErro(rota, response);
            if (atual == null) {
                return;
            }

            Partida novo = JsonUtil.ler(request, Partida.class);
            if (novo == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "corpo da requisicao invalido");
                return;
            }
            normalizar(novo);

            if (!Partida.AGENDADA.equals(atual.getStatus())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                        "So e possivel reagendar partida AGENDADA (status atual: " + atual.getStatus() + ")");
                return;
            }

            novo.setId(atual.getId());
            if (novo.getData() == null) novo.setData(atual.getData());
            if (novo.getHora() == null) novo.setHora(atual.getHora());
            if (novo.getLocal() == null) novo.setLocal(atual.getLocal());
            if (novo.getFase() == null) novo.setFase(atual.getFase());

            if (!validarAgenda(novo, response)) {
                return;
            }
            if (!dao.reagendar(novo)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Partida nao pode ser reagendada");
                return;
            }
            JsonUtil.enviar(response, dao.buscarPorId(atual.getId()));

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (com.google.gson.JsonParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "JSON invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }



    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirOrganizador(request, response)) {
            return;
        }
        try {
            Rota rota = lerRota(request);
            Partida atual = buscarOuErro(rota, response);
            if (atual == null) {
                return;
            }

            Partida corpo = JsonUtil.ler(request, Partida.class);
            if (corpo == null || (corpo.getPlacarA() == null && corpo.getPlacarB() == null)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                        "informe placarA e/ou placarB");
                return;
            }
            if ((corpo.getPlacarA() != null && corpo.getPlacarA() < 0)
                    || (corpo.getPlacarB() != null && corpo.getPlacarB() < 0)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                        "placar nao pode ser negativo");
                return;
            }

            if (Partida.FINALIZADA.equals(atual.getStatus())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                        "Partida finalizada: o placar nao pode mais ser alterado");
                return;
            }
            if (Partida.CANCELADA.equals(atual.getStatus())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                        "Partida cancelada: o placar nao pode ser alterado");
                return;
            }

            int placarA = corpo.getPlacarA() != null ? corpo.getPlacarA() : atual.getPlacarA();
            int placarB = corpo.getPlacarB() != null ? corpo.getPlacarB() : atual.getPlacarB();

            if (!dao.atualizarPlacar(atual.getId(), placarA, placarB)) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                        "Partida nao aceita mais alteracao de placar");
                return;
            }
            JsonUtil.enviar(response, dao.buscarPorId(atual.getId()));

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (com.google.gson.JsonParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "JSON invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirOrganizador(request, response)) {
            return;
        }
        try {
            Rota rota = lerRota(request);
            Partida atual = buscarOuErro(rota, response);
            if (atual == null) {
                return;
            }
            if (Partida.FINALIZADA.equals(atual.getStatus())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                        "Partida finalizada nao pode ser removida");
                return;
            }
            if (!dao.deletar(atual.getId())) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT, "Partida nao pode ser removida");
                return;
            }
            JsonUtil.enviarMensagem(response, HttpServletResponse.SC_OK, "Partida removida");

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private static class Rota {
        Integer id;
        String acao;
    }

    private Rota lerRota(HttpServletRequest request) {
        List<String> partes = new ArrayList<>();
        juntarPartes(partes, request.getPathInfo());
        juntarPartes(partes, request.getParameter("id"));

        Rota rota = new Rota();
        for (String parte : partes) {
            if ("finalizar".equalsIgnoreCase(parte) || "cancelar".equalsIgnoreCase(parte)) {
                rota.acao = parte.toLowerCase();
            } else {
                rota.id = Integer.parseInt(parte);
            }
        }
        return rota;
    }

    private void juntarPartes(List<String> partes, String texto) {
        if (texto == null) {
            return;
        }
        for (String parte : texto.split("/")) {
            if (!parte.isBlank()) {
                partes.add(parte.trim());
            }
        }
    }

    private Partida buscarOuErro(Rota rota, HttpServletResponse response) throws IOException, SQLException {
        if (rota.id == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id e obrigatorio");
            return null;
        }
        Partida partida = dao.buscarPorId(rota.id);
        if (partida == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Partida nao encontrada");
        }
        return partida;
    }

    /** Partida precisa estar AGEDADA ou EM_ANDAMENTO. Senao envia 409 e devolve false. */
    private boolean exigirEmAberto(Partida partida, String acaoPassado, HttpServletResponse response)
            throws IOException {
        if (Partida.FINALIZADA.equals(partida.getStatus()) || Partida.CANCELADA.equals(partida.getStatus())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_CONFLICT,
                    "Partida ja esta " + partida.getStatus() + " e nao pode ser " + acaoPassado);
            return false;
        }
        return true;
    }

    private void normalizar(Partida p) {
        p.setData(limpar(p.getData()));
        p.setHora(limpar(p.getHora()));
        p.setLocal(limpar(p.getLocal()));
        String fase = limpar(p.getFase());
        p.setFase(fase == null ? null : fase.toUpperCase());
    }

    private String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String s = valor.trim();
        return s.isEmpty() ? null : s;
    }

    private boolean validarAgenda(Partida p, HttpServletResponse response) throws IOException {
        try {
            if (p.getData() != null) {
                p.setData(LocalDate.parse(p.getData()).toString());
            }
        } catch (DateTimeParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "data invalida: use yyyy-MM-dd");
            return false;
        }
        try {
            if (p.getHora() != null) {
                p.setHora(LocalTime.parse(p.getHora()).toString());
            }
        } catch (DateTimeParseException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "hora invalida: use HH:mm");
            return false;
        }
        if (p.getFase() == null) {
            p.setFase(Partida.FASE_PADRAO);
        }
        if (!Partida.FASES_VALIDAS.contains(p.getFase())) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST,
                    "fase invalida: use " + String.join(", ", Partida.FASES_VALIDAS));
            return false;
        }
        return true;
    }
}
