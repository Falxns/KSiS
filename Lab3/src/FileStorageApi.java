import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FileStorageApi {
    private static final int API_PORT = 7777;
    private HttpServer server;

    public FileStorageApi() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", API_PORT), 0);
            System.out.println("Api started at:" + server.getAddress().getHostName() + ":" + server.getAddress().getPort());
            server.createContext("/Files", new ApiHandler());
            server.setExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(10));
            server.start();
        } catch (IOException ex) {
            System.out.println(this.toString() + ": " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        FileStorageApi api = new FileStorageApi();
    }
}
