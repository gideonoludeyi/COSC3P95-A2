import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.*;

public class Client implements Runnable {
  private final String ip;
  private final int port;
  private final Path path;

  public Client(String ip, int port, Path path) {
    this.ip = ip;
    this.port = port;
    this.path = path;
  }

  @Override
  public void run() {
    try (Socket socket = new Socket(ip, port)) {
      byte[] filename = path.getFileName()
          .toString()
          .getBytes(StandardCharsets.UTF_8);

      byte[] namesize = ByteBuffer.allocate(Integer.BYTES)
          .putInt(filename.length)
          .array();

      byte[] content = Files.readAllBytes(path);

      OutputStream out = socket.getOutputStream();
      out.write(namesize);
      out.write(filename);
      out.write(content);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    int port = Integer.parseUnsignedInt(args[0]);

    Path p = Paths.get(args[1]);

    Client client = new Client("localhost", port, p);
    client.run();
  }
}
