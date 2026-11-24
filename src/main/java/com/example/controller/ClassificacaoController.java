package com.example.controller;

import com.example.dao.ConfiguracaoDao;
import com.example.dao.InscricaoDao;
import com.example.dao.ModalidadeDao;
import com.example.dao.PartidaDao;
import com.example.dao.TimeDao;
import com.example.model.Classificacao;
import com.example.model.Inscricao;
import com.example.model.Modalidade;
import com.example.model.Partida;
import com.example.model.Time;
import com.example.service.ClassificacaoService;
import com.example.util.JsonUtil;
import com.example.util.Permissao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/classificacao")
public class ClassificacaoController extends HttpServlet {

    private ModalidadeDao modalidadeDao = new ModalidadeDao();
    private InscricaoDao inscricaoDao = new InscricaoDao();
    private TimeDao timeDao = new TimeDao();
    private PartidaDao partidaDao = new PartidaDao();
    private ConfiguracaoDao configuracaoDao = new ConfiguracaoDao();
    private ClassificacaoService service = new ClassificacaoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Permissao.exigirLogin(request, response)) {
            return;
        }
        try {
            String param = request.getParameter("modalidadeId");
            if (param == null || param.isBlank()) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "modalidadeId e obrigatorio");
                return;
            }
            int modalidadeId = Integer.parseInt(param.trim());

            Modalidade modalidade = modalidadeDao.buscarPorId(modalidadeId);
            if (modalidade == null) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "Modalidade nao encontrada");
                return;
            }

            // times aprovados = inscricoes CONFIRMADA da modalidade
            List<Time> aprovados = new ArrayList<>();
            for (Inscricao inscricao : inscricaoDao.listarModalidade(modalidadeId)) {
                Time time = timeDao.buscarPorId(inscricao.getIdTime());
                if (time != null) {
                    aprovados.add(time);
                }
            }

            List<Partida> finalizadas = partidaDao.listar(modalidadeId, Partida.FINALIZADA, null);

            List<Classificacao> tabela = service.calcular(aprovados, finalizadas,
                    configuracaoDao.pontosVitoria(),
                    configuracaoDao.pontosEmpate(),
                    configuracaoDao.pontosDerrota());

            JsonUtil.enviar(response, tabela);

        } catch (NumberFormatException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "modalidadeId invalido");
        } catch (SQLException e) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
