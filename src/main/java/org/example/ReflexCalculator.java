package org.example;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;

public class ReflexCalculator {

    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket server = new ServerSocket(36001);
        while (true) {
            try (Socket socket = server.accept();
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println("Received a request...");
                String request = in.readLine();
                if (request != null) {
                    URI reqUri = new URI(request.split(" ")[1]);
                    String path = reqUri.getPath();
                    String query = reqUri.getQuery().replace("comando=", "");
                    if (path.startsWith("/ReflexCalculator")) {
                        String[] cmdParams = query.split("\\(");

                        String cmd = cmdParams[0];
                        double[] numbers = parseParams(cmdParams[1]);
                        processCommand(cmd, numbers);
                        String response = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"result\": " + Arrays.toString(numbers) + "}";
                        out.println(response);
                    }
                }
            }
        }
    }

    private static double[] parseParams(String params) {
        String[] parts = params.replace(")", "").split(",");
        return Arrays.stream(parts).mapToDouble(Double::parseDouble).toArray();
    }

    private static void processCommand(String command, double[] numbers) {
        try {
            Method method = Math.class.getMethod(command, double.class);
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = (double) method.invoke(null, numbers[i]);
            }
        } catch (Exception e) {
            boubleSort(numbers);
        }
    }

    private static void boubleSort(double[] arr) {
        for(int j =0;j < arr.length -1; j++ ) {
            for (int i = 0; i < arr.length; i++) {
                double temp = arr[i];
                if (arr[i] > arr[i + 1]) {
                    arr[i] = arr[i + 1];
                    arr[i + 1] = temp;
                }
            }
        }

    }

}
