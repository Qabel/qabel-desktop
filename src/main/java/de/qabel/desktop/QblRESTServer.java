package de.qabel.desktop;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import de.qabel.core.config.ResourceActor;
import de.qabel.core.drop.DropActor;
import de.qabel.core.module.ModuleManager;

public class QblRESTServer implements Runnable {

    private int port;

    public ResourceActor getResourceActor() {
        return resourceActor;
    }

    public DropActor getDropActor() {
        return dropActor;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public HttpServer getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    private ResourceActor resourceActor;
    private DropActor dropActor;
    private ModuleManager moduleManager;
    private HttpServer server;

    public QblRESTServer(int port, ResourceActor resourceActor, DropActor dropActor, ModuleManager moduleManager) {
        this.port = port;
        this.resourceActor = resourceActor;
        this.dropActor = dropActor;
        this.moduleManager = moduleManager;
    }

    @Override
    public void run() {
        server = null;
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

    public void stop() {
        if (server != null) {
            // delay until stopping = 0
            server.stop(0);
        }
    }
}