package de.qabel.desktop;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.qabel.core.config.ResourceActor;
import de.qabel.core.drop.DropActor;
import de.qabel.core.module.ModuleManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QblRESTServer implements Runnable {    private static final String HOSTNAME = "localhost";

    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final int STATUS_OK = 200;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;

    private static final int NO_RESPONSE_LENGTH = -1;

    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String ALLOWED_METHODS = METHOD_GET + "," + METHOD_OPTIONS;

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

    /**
     * Base clase for REST handlers
     */
    abstract class SimpleHandler implements HttpHandler {

        private Map<String, List<String>> requestParameters;
        private Headers headers;
        private String requestMethod;

        @Override
        public void handle(HttpExchange he) throws IOException {


            try {
                headers = he.getResponseHeaders();
                requestMethod = he.getRequestMethod().toUpperCase();
                requestParameters = getRequestParameters(he.getRequestURI());
                headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                switch (requestMethod) {
                    case METHOD_GET:
                        get(he);
                        break;
                    default:
                        headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                        he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                        break;
                }
            } finally {
                he.close();
            }
        }

        public abstract void get(HttpExchange he) throws IOException;
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
        server.createContext("/status", new SimpleHandler() {
                    @Override
                    public void get(HttpExchange he) throws IOException {
                        final String responseBody = "{status: \"running\"}";
                        final byte[] rawResponseBody = responseBody.getBytes(CHARSET);
                        he.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                        he.getResponseBody().write(rawResponseBody);
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

    /**
     * Extract request parameter from a URL.
     * @param requestUri
     * @return Map of parameters
     * @throws UnsupportedEncodingException
     */
    private static Map<String, List<String>> getRequestParameters(final URI requestUri) throws UnsupportedEncodingException {
        final Map<String, List<String>> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery();
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]", -1);
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2);
                final String requestParameterName = decodeUrlComponent(requestParameter[0]);
                if (!requestParameters.containsKey(requestParameterName)) {
                    requestParameters.put(requestParameterName, new ArrayList<String>());
                }
                final String requestParameterValue = requestParameter.length > 1 ? decodeUrlComponent(requestParameter[1]) : null;
                requestParameters.get(requestParameterName).add(requestParameterValue);
            }
        }
        return requestParameters;
    }

    /**
     * Decode a url component
     * Always uses the constant CHARSET.
     * @param urlComponent
     * @return decoded url compoment
     * @throws UnsupportedEncodingException
     */
    private static String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(CHARSET + " is not a supported encoding");
        }
    }


}