package esp.dgi.ws.rest.auth.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {

    public static void main(String[] args) throws Exception {
        String port = envOr("SERVICE_PORT", "8081");
        URI baseUri = URI.create("http://0.0.0.0:" + port + "/");

        ResourceConfig rc = new ResourceConfig()
            .packages("esp.dgi.ws.rest.auth.server.resources",
                      "esp.dgi.ws.rest.auth.server.exceptions");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, rc, false);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[rest-auth-java] shutting down");
            server.shutdownNow();
        }));

        server.start();
        System.out.println("[rest-auth-java] AuthService REST running at " + baseUri);
        System.out.println("[rest-auth-java] endpoints: /register /login /logout /validate");

        Thread.currentThread().join();
    }

    private static String envOr(String key, String def) {
        String v = System.getenv(key);
        return v == null || v.isEmpty() ? def : v;
    }
}
