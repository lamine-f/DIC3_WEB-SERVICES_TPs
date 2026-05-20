package esp.dgi.ws.soap.auth.server;

import jakarta.xml.ws.Endpoint;

public class ServerApp {

    public static void main(String[] args) throws InterruptedException {
        String port = envOr("SERVICE_PORT", "9001");
        String address = "http://0.0.0.0:" + port + "/auth";

        Endpoint.publish(address, new AuthServiceImpl());
        System.out.println("[soap-auth-java] AuthService running at " + address);
        System.out.println("[soap-auth-java] WSDL: " + address + "?wsdl");

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.out.println("[soap-auth-java] shutting down")));

        Thread.currentThread().join();
    }

    private static String envOr(String key, String def) {
        String v = System.getenv(key);
        return v == null || v.isEmpty() ? def : v;
    }
}
