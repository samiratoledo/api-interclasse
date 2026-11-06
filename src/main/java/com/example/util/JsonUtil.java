package com.example.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Auxiliares de JSON para as controllers.
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /** Envia um objeto como resposta JSON (200). */
    public static void enviar(HttpServletResponse response, Object objeto) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(objeto));
        out.flush();
    }

    /** Envia uma mensagem de sucesso (ex: "Time removido"). */
    public static void enviarMensagem(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(new Mensagem(mensagem)));
        out.flush();
    }

    /** Envia uma mensagem de erro com o status HTTP informado. */
    public static void enviarErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(new Erro(mensagem)));
        out.flush();
    }

    /** Le o corpo da requisicao e converte para o tipo informado. */
    public static <T> T ler(HttpServletRequest request, Class<T> classe) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = reader.readLine()) != null) {
            sb.append(linha);
        }
        if (sb.length() == 0) {
            return null;
        }
        return gson.fromJson(sb.toString(), classe);
    }

    /** Objeto simples de retorno de erro. */
    public static class Erro {
        private String erro;

        public Erro(String erro) {
            this.erro = erro;
        }

        public String getErro() {
            return erro;
        }
    }

    /** Objeto simples de retorno de mensagem. */
    public static class Mensagem {
        private String mensagem;

        public Mensagem(String mensagem) {
            this.mensagem = mensagem;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}
