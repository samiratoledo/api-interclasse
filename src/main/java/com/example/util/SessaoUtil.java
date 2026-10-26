package com.example.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Auxiliares de sessao e senha.
 */
public class SessaoUtil {

    /** Atributo da sessao onde fica guardado o id do usuario logado. */
    public static final String ATRIBUTO_USUARIO = "usuarioId";
    /** Atributo da sessao onde fica guardado o tipo (PARTICIPANTE / ORGANIZADOR). */
    public static final String ATRIBUTO_TIPO = "usuarioTipo";

    /** Gera o hash SHA-256 da senha (nunca guardamos a senha em texto puro). */
    public static String hashSenha(String senha) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(senha.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }

    /** Usuario logado na sessao ou null se nao houver login. */
    public static Integer usuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute(ATRIBUTO_USUARIO);
        return (id instanceof Integer) ? (Integer) id : null;
    }

    /** Tipo do usuario logado ou null. */
    public static String tipoUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object tipo = session.getAttribute(ATRIBUTO_TIPO);
        return (tipo instanceof String) ? (String) tipo : null;
    }

    /** True quando o usuario logado e organizador. */
    public static boolean ehOrganizador(HttpServletRequest request) {
        return "ORGANIZADOR".equals(tipoUsuarioLogado(request));
    }

    /** True quando existe alguem logado. */
    public static boolean estaLogado(HttpServletRequest request) {
        return usuarioLogado(request) != null;
    }
}
