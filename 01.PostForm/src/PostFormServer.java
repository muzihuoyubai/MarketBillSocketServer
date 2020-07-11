import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class PostFormServer extends Thread {

  Socket connectedClient = null;
  BufferedReader inFromClient = null;
  DataOutputStream outToClient = null;


  public PostFormServer(Socket client) {
    connectedClient = client;
  }

  public void sendResponse(int statusCode, String responseString, boolean isFile) throws Exception {

    String statusLine = null;
    String serverdetails = "Server: Java HTTPServer";
    String contentLengthLine = null;
    String fileName = null;
    String contentTypeLine = "Content-Type: text/html" + "\r\n";
    FileInputStream fin = null;

    if (statusCode == 200) {
      statusLine = "HTTP/1.1 200 OK" + "\r\n";
    } else {
      statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
    }

    if (isFile) {
      fileName = responseString;
      fin = new FileInputStream(fileName);
      contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
      if (!fileName.endsWith(".htm") && !fileName.endsWith(".html")) {
        contentTypeLine = "Content-Type: \r\n";
      }
    }

    outToClient.writeBytes(statusLine);
    outToClient.writeBytes(serverdetails);
    outToClient.writeBytes(contentTypeLine);
    outToClient.writeBytes(contentLengthLine);
    outToClient.writeBytes("Connection: close\r\n");
    outToClient.writeBytes("\r\n");

    if (isFile) {
      sendFile(fin, outToClient);
    } else {
      outToClient.writeBytes(responseString);
    }

    outToClient.close();
  }

  public void sendFile(FileInputStream fin, DataOutputStream out) throws Exception {
    byte[] buffer = new byte[1024];
    int bytesRead;

    while ((bytesRead = fin.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
    }
    fin.close();
  }

  static String readLine(InputStream r) throws IOException {
    // HTTP carries both textual and binary elements.
    // Not using BufferedReader.readLine() so it does
    // not "steal" bytes from BufferedInputStream...

    // HTTP itself only allows 7bit ASCII characters
    // in headers, but some header values may be
    // further encoded using RFC 2231 or 5987 to
    // carry Unicode characters ...

    // InputStreamReader r = new InputStreamReader(in);
    StringBuilder sb = new StringBuilder();
    char c;
    int i;
    while ((i = r.read()) >= 0) {
      c = (char) i;
      if (c == '\n') {
        break;
      }
      if (c == '\r') {
        c = (char) r.read();
        if ((c == '\n')) {
          break;
        }
        sb.append('\r');
      }
      sb.append(c);
    }
    // if (sb.length() > 0) {
    return sb.toString();
    // } else {
    //   return null;
    // }
  }

  public static void main(String args[]) throws Exception {

    ServerSocket Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));
    System.out.println("TCPServer Waiting for client on port 5000");

    while (true) {
      Socket clientSocket = Server.accept();
      InputStream is = clientSocket.getInputStream();
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      String line;
      line = in.readLine();
      String request_method = line;
      System.out.println("HTTP-HEADER: " + line);
      line = "";
      // looks for post data
      int postDataI = -1;
      while ((line = in.readLine()) != null && (line.length() != 0)) {
        System.out.println("HTTP-HEADER: " + line);
        if (line.indexOf("Content-Length:") > -1) {
          postDataI = new Integer(
              line.substring(
                  line.indexOf("Content-Length:") + 16,
                  line.length())).intValue();
        }
      }
      String postData = "";
      // read the post data
      if (postDataI > 0) {
        char[] charArray = new char[postDataI];
        in.read(charArray, 0, postDataI);
        postData = new String(charArray);
      }
      out.println("HTTP/1.0 200 OK");
      out.println("Content-Type: text/html; charset=utf-8");
      out.println("Server: MINISERVER");
      // this blank line signals the end of the headers
      out.println("");
      // Send the HTML page
      out.println("<H1>Welcome to the Mini Server</H1>");
      out.println("<H2>Request Method->" + request_method + "</H2>");
      out.println("<H2>Post->" + postData + "</H2>");
      out.println("<form name=\"input\" action=\"form_submited\" method=\"post\">");
      out.println(
          "Username: <input type=\"text\" name=\"user\"><input type=\"submit\" value=\"Submit\"></form>");
      out.close();
      clientSocket.close();
    }
  }
}