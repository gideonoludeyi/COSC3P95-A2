import java.net.*;
import java.io.*;

public class Server implements Runnable {
  private final int port;

  public Server(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    System.out.println("Listening...");
    try (ServerSocket socket = new ServerSocket(port)) {
      while (!socket.isClosed()) {
        try (
            Socket client = socket.accept();
            OutputStream out = new FileOutputStream("file")) {
          client.getInputStream().transferTo(out);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      System.out.println("Closed");
    }
  }

  public static void main(String[] args) {
    Server server = new Server(9090);
    server.run();
  }
}
