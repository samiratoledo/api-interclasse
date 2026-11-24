package com.example.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class Permissao {

    public static boolean exigirLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!SessaoUtil.estaLogado(request)) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_UNAUTHORIZED, "faca login para continuar");
            return false;
        }
        return true;
    }

    public static boolean exigirOrganizador(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!exigirLogin(request, response)) {
            return false;
        }
        if (!SessaoUtil.ehOrganizador(request)) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_FORBIDDEN, "acesso restrito a organizadores");
            return false;
        }
        return true;
    }
}
