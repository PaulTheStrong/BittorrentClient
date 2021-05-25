package by.bsuir.ksis.kursovoi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

public class HttpRequest {
    private final String host;
    private final int port;

    public HttpRequest(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HttpResponse get(String path) throws IOException {
        String request =
                "GET " + path + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "User-Agent: pavelTorrent/0001\r\n" +
                "Accept-Encoding: gzip\r\n" +
                "Connection: Close\r\n\r\n";
        System.out.println("[CLIENT]\n" + request);

        return getHttpResponse(request);
    }

    public HttpResponse post(String path, Map<String, String> parameters) throws IOException {
        String request = "POST " +
                path +
                " HTTP/1.0\r\n" +
                "Host: " + host + "\r\n" +
                "Accept: */*\r\n" +
                "Accept-Encoding: gzip, deflate\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n";

        String paramString = getParamString(parameters);
        request += "Content-Length: " + paramString.length() + "\r\n\r\n";
        request += paramString;
        System.out.println("[CLIENT]\n" + request);

        return getHttpResponse(request);
    }

    private String getParamString(Map<String, String> parameters) {
        String paramString = "";
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            paramString += param.getKey() + "=" + param.getValue() + "&";
        }
        paramString = paramString.substring(0, paramString.length() - 1);
        return paramString;
    }

    public HttpResponse head(String path) throws IOException {
        String request = "HEAD " +
                path +
                " HTTP/1.1\r\n" +
                "Accept: */*\r\n" +
                "Host: " +
                host +
                "\r\n" +
                "Connection: Close\r\n\r\n";
        System.out.println("[CLIENT]\n" + request);

        return getHttpResponse(request);
    }

    private HttpResponse getHttpResponse(String request) throws IOException {
        Socket socket = new Socket(host, port);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        out.write(request.getBytes());
        out.flush();

        StringBuilder responseBuffer = new StringBuilder();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, 4096)) != -1) {
            for (int i = 0; i < bytesRead; i++)
                responseBuffer.append((char) buffer[i]);
        }
        socket.close();

        return new HttpResponse(responseBuffer.toString());
    }
}
