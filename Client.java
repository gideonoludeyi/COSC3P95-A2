import java.net.*;
import java.io.*;

public class Client implements Runnable {
  private final String ip;
  private final int port;
  private final InputStream in;

  public Client(String ip, int port, InputStream in) {
    this.ip = ip;
    this.port = port;
    this.in = in;
  }

  @Override
  public void run() {
    try (
        Socket socket = new Socket(ip, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
      in.transferTo(socket.getOutputStream());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    int port = Integer.parseUnsignedInt(args[0]);

    InputStream in = new FileInputStream(args[1]);

    Client client = new Client("localhost", port, in);
    client.run();
  }
}
