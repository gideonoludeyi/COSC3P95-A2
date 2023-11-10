import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.*;
import java.util.concurrent.*;

public class Server implements Runnable {
  private final ForkJoinPool pool = ForkJoinPool.commonPool();
  private final int port;
  private final Path outdir;

  public Server(int port, Path outdir) {
    this.port = port;
    this.outdir = outdir;
  }

  @Override
  public void run() {
    System.out.println("Listening...");
    try (ServerSocket socket = new ServerSocket(port)) {
      while (!socket.isClosed()) {
        Socket client = socket.accept();
        pool.execute(() -> process(client));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      System.out.println("Closed");
    }
  }

  private void process(Socket client) {
    try (client) {
      InputStream in = client.getInputStream();

      int namesize = ByteBuffer.wrap(in.readNBytes(Integer.BYTES))
          .getInt();
      String filename = new String(in.readNBytes(namesize), StandardCharsets.UTF_8);

      String filepath = outdir.resolve(filename).toString();
      try (FileOutputStream out = new FileOutputStream(filepath)) {
        long contentsize = in.transferTo(out);
        System.out.printf("Wrote %s bytes to %s%n", contentsize, filepath);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Path outdir = Paths.get("tmp");

    Server server = new Server(9090, outdir);
    server.run();
  }
}
