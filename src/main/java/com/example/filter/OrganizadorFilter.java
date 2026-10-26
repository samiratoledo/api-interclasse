package com.example.filter;

import com.example.util.JsonUtil;
import com.example.util.SessaoUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro que protege as rotas /api/admin/*.
 * Somente usuarios do tipo ORGANIZADOR (logados com chave mestra) acessam essas rotas.
 */
@WebFilter("/api/admin/*")
public class OrganizadorFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!SessaoUtil.estaLogado(request)) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_UNAUTHORIZED, "faca login para continuar");
            return;
        }

        if (!SessaoUtil.ehOrganizador(request)) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_FORBIDDEN, "acesso restrito a organizadores");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Nada a configurar
    }

    @Override
    public void destroy() {
        // Nada a liberar
    }
}
