package by.bsuir.ksis.kursovoi.http;

public
class HttpResponse {
    private final String status;
    private String headers;
    private String content;

    public HttpResponse(String responseString) {
        status = responseString.substring(responseString.indexOf(" ") + 1, responseString.indexOf("\r\n"));

        if (status.equalsIgnoreCase("200 ok") || status.equalsIgnoreCase("201 created")) {
            headers = responseString.substring(responseString.indexOf("\r\n") + 1, responseString.indexOf("\r\n\r\n"));
            content = responseString.substring(responseString.indexOf("\r\n\r\n"));
        }
    }

    public String getStatus() {
        return status;
    }

    public String getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        String repr = status;

        if (headers != null) {
            repr += headers;
        }

        if (content != null) {
            repr += content;
        }

        return repr;
    }
}