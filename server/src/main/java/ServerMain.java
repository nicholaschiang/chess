import server.Server;

public class ServerMain {
  private static Server server;

  public static void main(String[] args) {
    try {
      server = new Server();
      var port = server.run(0);
      System.out.println("Started server! Visit: http://localhost:" + port);
    } catch (Throwable ex) {
      System.out.println("Failed to start server: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}
