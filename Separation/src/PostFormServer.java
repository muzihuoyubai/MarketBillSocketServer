import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class PostFormServer extends Thread {

  Socket connectedClient = null;
  BufferedReader inFromClient = null;
  DataOutputStream outToClient = null;
  String CONTEXT_ROOT = "http://localhost:5000/";


  public PostFormServer(Socket client) {
    connectedClient = client;
  }

  public static void main(String args[]) throws Exception {

    ServerSocket Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));
    System.out.println("TCPServer Waiting for client on port 5000");

    while (true) {
      Socket clientSocket = Server.accept();
      PostFormServer postFormServer = new PostFormServer(clientSocket);
      postFormServer.run();

    }
  }

  @Override
  public void run() {
    try {
      DataOutputStream out = new DataOutputStream(connectedClient.getOutputStream());
      BufferedReader in = new BufferedReader(
          new InputStreamReader(connectedClient.getInputStream()));

      MbsRequest request = MbsRequest.parse(in);
      String path = request.getPath();
      if (request.isGet()) {
        if ("/".equals(path)) {
          responseFile("login.html", out);
        } else if (path.endsWith(".html") || path.startsWith("/css") || path
            .startsWith("/images")) {
          responseFile(path, out);
        }
      } else {
        switch (path) {
          case "/server/login":
            if (login(request)) {
              out.writeBytes("HTTP/1.1 302 Found");
              out.writeBytes("\r\n");
              out.writeBytes("Location: " + CONTEXT_ROOT + "bill_list.html");
              out.writeBytes("\r\n");
            }
            break;
        }
      }

      out.close();

      connectedClient.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean login(MbsRequest request) {
    return true;
  }

  private static void responseFile(String path, DataOutputStream out) throws IOException {

    InputStream resourceAsStream = PostFormServer.class.getClassLoader()
        .getResourceAsStream("pages/" + path);
    // BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));

    // String statusLine = "HTTP/1.1 200 OK" + "\r\n";
    /*
    返回顺序不能错
    状态
    Server
    Content-type
    content-length
     */
    out.writeBytes("HTTP/1.1 200 OK");
    out.writeBytes("Server: Java HTTPServer");
    out.writeBytes("\r\n");
    if (path.endsWith(".html")) {
      // out.write("Connection: close\r\n".getBytes());
      out.writeBytes("Content-Type: text/html; charset=utf-8");
      out.writeBytes("\r\n");
      out.writeBytes(("Content-Length: " + resourceAsStream.available()));
      out.writeBytes("\r\n");
    } else {
      // out.println("Content-Type: ");
      // css文件必须给定文件长度，并且不能用Reader readLine不然长度和字节数对不上
      out.writeBytes(("Content-Length: " + resourceAsStream.available()));
      out.writeBytes("\r\n");
    }

    // 空行表示返回头终止
    out.writeBytes("\r\n");

    byte[] buf = new byte[1024];

    int count = resourceAsStream.read(buf);
    while (count > 0) {
      out.write(buf, 0, count);
      count = resourceAsStream.read(buf);
    }

    // String line = reader.readLine();
    // while (line != null) {
    //   out.println(reader.readLine());
    //   line = reader.readLine();
    // }
    resourceAsStream.close();
    // String contentLengthLine = "Content-Length: " + fin.available() + "\r\n";
  }


}