import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostFormServer extends Thread {

  Socket connectedClient = null;
  String CONTEXT_ROOT = "http://localhost:5000/";
  private static List<Provider> providerList = new ArrayList<>();
  private static int providerId = 1;


  public PostFormServer(Socket client) {
    connectedClient = client;
  }

  public static void main(String args[]) throws Exception {

    ServerSocket Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));
    System.out.println("TCPServer Waiting for client on port 5000");

    while (true) {
      Socket clientSocket = Server.accept();
      PostFormServer postFormServer = new PostFormServer(clientSocket);
      postFormServer.start();

    }
  }

  @Override
  public void run() {
    try {
      DataOutputStream out = new DataOutputStream(connectedClient.getOutputStream());
      BufferedReader in = new BufferedReader(
          new InputStreamReader(connectedClient.getInputStream()));

      MbsRequest request = MbsRequest.parse(in);
      if (request == null) {
        return;
      }
      String path = request.getPath();
      if (request.isGet()) {
        if ("/".equals(path)) {
          responseFile("login.html", out);
        } else if (path.contains(".html") || path.startsWith("/css") || path
            .startsWith("/images") || path
            .startsWith("/js")) {
          responseFile(path, out);
        }
      } else {
        switch (path) {
          case "/server/login":
            if (login(request)) {
              responseRedirect(out, request, "/bill_list.html");
            }
            break;
          case "/server/provider/list": {
            String payload = request.getPayload();
            if (payload == null) {
              responseJson(out, JSONObject.toJSONString(providerList));
            }
            Provider provider = JSONObject.parseObject(payload, Provider.class);
            List<Provider> rlt = providerList.stream().filter(t -> {
              if (provider == null) {
                return true;
              }
              String name = provider.getName();
              if (name != null && name.trim().length() > 0) {
                return t.getName().contains(name);
              } else {
                return true;
              }
            }).filter(t -> {
              if (provider == null) {
                return true;
              }
              String desc = provider.getDesc();
              if (desc != null && desc.trim().length() > 0) {
                return t.getDesc().contains(desc);
              } else {
                return true;
              }
            }).collect(Collectors.toList());
            responseJson(out, JSONObject.toJSONString(rlt));
          }
          break;
          case "/server/provider/modify": {
            Map<String, String> formData = request.getFormData();
            Provider provider = JSONObject
                .parseObject(JSONObject.toJSONString(formData), Provider.class);
            if (provider.getId() == 0) {
              provider.setId(providerId++);
              providerList.add(provider);
            } else {
              Optional<Provider> id = providerList.stream()
                  .filter(t -> t.getId() == provider.getId()).findFirst();
              if (id.isPresent()) {
                providerList.remove(id.get());
                providerList.add(provider);
                providerList.sort(Comparator.comparingInt(Provider::getId));
              } else {
                //TODO Exception
              }
            }

            responseRedirect(out, request, "/provider_list.html");
          }
          break;
          case "/server/provider/get": {
            String payload = request.getJsonData();
            Map<String, String> map = JSONObject
                .parseObject(payload, new TypeReference<>() {
                });
            Optional<Provider> id = providerList.stream()
                .filter(t -> t.getId() == Integer.parseInt(map.get("id"))).findFirst();
            if (id.isPresent()) {
              responseJson(out, JSONObject.toJSONString(id.get()));
            } else {
              responseJson(out, "{}");
            }
          }
          break;
          case "/server/provider/delete": {
            String payload = request.getJsonData();
            Map<String, String> map = JSONObject
                .parseObject(payload, new TypeReference<>() {
                });
            Optional<Provider> id = providerList.stream()
                .filter(t -> t.getId() == Integer.parseInt(map.get("id"))).findFirst();
            if (id.isPresent()) {
              providerList.remove(id.get());
              out.writeBytes("HTTP/1.1 200 OK");
              out.writeBytes("\r\n");
              out.writeBytes("Server: Java HTTPServer");
              out.writeBytes("\r\n");
            } else {
              // TODO exception
            }
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

  private void responseRedirect(DataOutputStream out, MbsRequest request, String s)
      throws IOException {
    out.writeBytes("HTTP/1.1 302 Found");
    out.writeBytes("\r\n");
    out.writeBytes("Location: " + "http://" + request.getHost() + s);
    out.writeBytes("\r\n");
  }

  private void responseJson(DataOutputStream out, String s) throws IOException {
    String data = s;
    out.writeBytes("HTTP/1.1 200 OK");
    out.writeBytes("\r\n");
    out.writeBytes("Server: Java HTTPServer");
    out.writeBytes("\r\n");
    out.writeBytes("Content-Type: application/json; charset=utf-8");
    out.writeBytes("\r\n");
    out.writeBytes(("Content-Length: " + data.getBytes().length));
    out.writeBytes("\r\n");
    out.writeBytes("\r\n");
    out.write(data.getBytes());
  }

  private boolean login(MbsRequest request) {
    return true;
  }

  private static void responseFile(String path, DataOutputStream out) throws IOException {

    InputStream resourceAsStream = PostFormServer.class.getClassLoader()
        .getResourceAsStream("pages/" + path);
    System.out.println(path);
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
    out.writeBytes("\r\n");
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

    resourceAsStream.close();
  }


}