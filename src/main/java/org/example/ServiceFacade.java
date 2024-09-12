package org.example;


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServiceFacade {

    private static final String BROWSER_AGENT = "Mozilla/5.0";
    private static final String BASE_URL = "http://localhost:36001/compreflex?";

    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket server = new ServerSocket(36000);
        while (true) {
            try (Socket socket = server.accept();
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                System.out.println("Request received...");
                String request = in.readLine();
                if (request != null) {
                    URI reqUri = new URI(request.split(" ")[1]);
                    String response;
                    if (reqUri.getPath().startsWith("/computar")) {
                        response = callRestService(reqUri.getQuery());
                    } else if (reqUri.getPath().startsWith("/calculadora")) {
                        response = generateWebForm();
                    } else {
                        response = "Invalid request!";
                    }
                    out.println(response);
                }
            }
        }
    }

    private static String generateWebForm() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n\r\n"
                + "<!DOCTYPE html>"
                + "<html><head><title>Calculadora</title></head>"
                + "<body><h1>Calculadora</h1>"
                + "<form><label for=\"command\">Comando:</label>"
                + "<input type=\"text\" id=\"command\" value=\"sin\">"
                + "<label for=\"params\">Parametros:</label>"
                + "<input type=\"text\" id=\"params\" value=\"-3.67\">"
                + "<input type=\"button\" value=\"Calcular\" onclick=\"fetchResult()\">"
                + "</form><div id=\"result\"></div>"
                + getJavaScript()
                + "</body></html>";
    }

    private static String getJavaScript() {
        return "<script>"
                + "function fetchResult() {"
                + "let cmd = document.getElementById('command').value;"
                + "let params = document.getElementById('params').value;"
                + "let xhr = new XMLHttpRequest();"
                + "xhr.onload = function() { document.getElementById('result').innerHTML = this.responseText; };"
                + "xhr.open('GET', '/computar?comando=' + cmd + '(' + params + ')');"
                + "xhr.send();"
                + "}"
                + "</script>";
    }

    private static String callRestService(String params) throws IOException {
        URL serviceURL = new URL(BASE_URL + params);
        HttpURLConnection connection = (HttpURLConnection) serviceURL.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", BROWSER_AGENT);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n");
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            return "Failed to connect!";
        }
    }
}
