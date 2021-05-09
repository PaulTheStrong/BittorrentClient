package by.bsuir.ksis.kursovoi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {

    public static void main(String[] args) throws IOException {
        String host = "vk.com";
        int port = 80;
        Socket socket = new Socket(host, port);

        System.out.println("===== GET request =====");
        var request = new HttpRequest(host, 80);
        var response = request.get("/messages");
        System.out.println("[SERVER]\n" + response.toString());
        System.out.println("========================");

        System.out.println("===== HEAD request =====");
        var headRequest = new HttpRequest(host, 80);
        var pheadResponse = headRequest.head("/posts");
        System.out.println("[SERVER]\nStatus: " + pheadResponse.getStatus());
        System.out.println();
        System.out.println("Headers: " + pheadResponse.getHeaders());
        System.out.println();
        System.out.println("Content: " + pheadResponse.getContent());
        System.out.println("========================");

        var params = new HashMap<String, String>();
        params.put("userId", "2");
        params.put("title", "helloworld");
//        params.put("body", "my-dear-friend");

        System.out.println("===== POST request =====");
        var postRequest = new HttpRequest(host, 80);
        var postResponse = postRequest.post("/posts", params);
        System.out.println("[SERVER]\nStatus: " + postResponse.getStatus());
        System.out.println();
        System.out.println("Headers: " + postResponse.getHeaders());
        System.out.println();
        System.out.println("Content: " + postResponse.getContent());
        System.out.println("========================");
    }
}