import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.nio.charset.*;
import java.nio.*;
import java.util.Base64;
import java.util.concurrent.*;
import java.util.zip.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Server implements Runnable {
  private final ForkJoinPool pool = ForkJoinPool.commonPool();
  private final int port;
  private final Path outdir;
  private final Cipher cipher;

  public Server(int port, Path outdir, Cipher cipher) {
    this.port = port;
    this.outdir = outdir;
    this.cipher = cipher;
  }

  @Override
  public void run() {
    System.out.println("Listening...");
    try (ServerSocket socket = new ServerSocket(port)) {
      while (!socket.isClosed()) {
        Socket client = socket.accept();
        pool.execute(() -> process(client));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void process(Socket client) {
    try (client;
        InputStream in = inputStream(client)) {

      int nfiles = ByteBuffer.wrap(in.readNBytes(Integer.BYTES))
          .getInt();

      for (int i = 0; i < nfiles; i++) {
        int namesize = ByteBuffer.wrap(in.readNBytes(Integer.BYTES))
            .getInt();

        String filename = new String(in.readNBytes(namesize), StandardCharsets.UTF_8);

        long contentsize = ByteBuffer.wrap(in.readNBytes(Long.BYTES))
            .getLong();

        Path filepath = outdir.resolve(filename);
        try (OutputStream out = new FileOutputStream(filepath.toString())) {
          long bytesremaining = contentsize;
          while (bytesremaining > 0) {
            byte[] buf = in.readNBytes(Math.min(1024, (int) bytesremaining));
            out.write(buf);
            bytesremaining -= buf.length;
          }
          System.out.printf("Wrote %s bytes to %s%n", contentsize, filepath);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private InputStream inputStream(Socket client) throws IOException {
    InputStream in = client.getInputStream();
    in = new GZIPInputStream(in); // Compression
    in = new CipherInputStream(in, cipher); // Encryption

    return in;
  }

  public static void main(String[] args) throws IOException, GeneralSecurityException {
    Path outdir = Files.createDirectories(Paths.get("tmp"));

    String encodedKey = Files.readString(Paths.get("key.txt"));
    SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(encodedKey), "AES");
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, key);

    Server server = new Server(9090, outdir, cipher);
    server.run();
  }
}
