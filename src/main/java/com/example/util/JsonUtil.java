
package com.example.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(java.time.LocalDate.class,
            (com.google.gson.JsonSerializer<java.time.LocalDate>) (d, t, c) ->
                new com.google.gson.JsonPrimitive(d.toString()))
        .registerTypeAdapter(java.time.LocalDate.class,
            (com.google.gson.JsonDeserializer<java.time.LocalDate>) (j, t, c) ->
                java.time.LocalDate.parse(j.getAsString()))
        .create();

    /** Envia um objeto como resposta JSON (200). */
    public static void enviar(HttpServletResponse response, Object objeto) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(objeto));
        out.flush();
    }

    /** Envia uma mensagem de sucesso. */
    public static void enviarMensagem(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(new Mensagem(mensagem)));
        out.flush();
    }

    /** Envia uma mensagem de erro. */
    public static void enviarErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(new Erro(mensagem)));
        out.flush();
    }

    /** Lê o corpo da requisição e converte para o tipo informado. */
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

    public static class Erro {
        private String erro;

        public Erro(String erro) {
            this.erro = erro;
        }

        public String getErro() {
            return erro;
        }
    }

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
