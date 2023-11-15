import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.*;
import java.util.*;
import java.util.zip.*;

public class Client implements Runnable {
  private final String ip;
  private final int port;
  private final Path dirpath;

  public Client(String ip, int port, Path dirpath) {
    this.ip = ip;
    this.port = port;
    this.dirpath = dirpath;
  }

  @Override
  public void run() {
    List<Path> filepaths = new ArrayList<>();
    try (DirectoryStream<Path> dir = Files.newDirectoryStream(dirpath)) {
      for (Path path : dir) {
        if (!Files.isDirectory(path)) {
          filepaths.add(path);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    try (Socket socket = new Socket(ip, port);
        OutputStream out = new GZIPOutputStream(socket.getOutputStream())) {

      byte[] nfiles = ByteBuffer.allocate(Integer.BYTES)
          .putInt(filepaths.size())
          .array();

      out.write(nfiles);

      for (Path path : filepaths) {
        byte[] name = path.getFileName()
            .toString()
            .getBytes(StandardCharsets.UTF_8);

        byte[] namesize = ByteBuffer.allocate(Integer.BYTES)
            .putInt(name.length)
            .array();

        byte[] contentsize = ByteBuffer.allocate(Long.BYTES)
            .putLong(Files.size(path))
            .array();

        byte[] content = Files.readAllBytes(path);

        out.write(namesize);
        out.write(name);
        out.write(contentsize);
        out.write(content);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    int port = Integer.parseUnsignedInt(args[0]);

    Path dirpath = Paths.get(args[1]);

    Client client = new Client("localhost", port, dirpath);
    client.run();
  }
}
