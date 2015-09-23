package de.qabel.desktop;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

class QblRESTServer implements Runnable {

    private int port;

    public QblRESTServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(this.port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.setExecutor(null);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                String response = "Response";
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        server.start();

    }
}