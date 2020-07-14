package club.banyuan.http;

import club.banyuan.entity.Provider;
import club.banyuan.entity.User;
import club.banyuan.exception.RequestException;
import club.banyuan.service.ProviderService;
import club.banyuan.service.UserService;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class HttpServer extends Thread {

  private Socket connectedClient;

  private ProviderService providerService = new ProviderService();
  private UserService userService = new UserService();


  public HttpServer(Socket client) {
    connectedClient = client;
  }

  public static void main(String[] args) throws Exception {

    ServerSocket Server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));
    System.out.println("TCPServer Waiting for client on port 5000");

    while (true) {
      Socket clientSocket = Server.accept();
      HttpServer postFormServer = new HttpServer(clientSocket);
      postFormServer.start();
    }
  }

  @Override
  public void run() {
    DataOutputStream out = null;
    try {
      out = new DataOutputStream(connectedClient.getOutputStream());
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
        } else {
          throw new RequestException(HttpStatus.NOT_FOUND);
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
              responseJson(out, providerService.getProviderList());
            } else {
              Provider provider = JSONObject.parseObject(payload, Provider.class);
              responseJson(out, providerService.getProviderList(provider));
            }
          }
          break;
          case "/server/provider/modify": {
            Map<String, String> formData = request.getFormData();
            Provider provider = JSONObject
                .parseObject(JSONObject.toJSONString(formData), Provider.class);
            if (provider.getId() == 0) {
              providerService.addProvider(provider);
            } else {
              providerService.updateProvider(provider);
            }
            responseRedirect(out, request, "/provider_list.html");
          }
          break;
          case "/server/provider/get": {
            Provider provider = request.getJsonData(Provider.class);
            Provider providerById = providerService.getProviderById(provider.getId());
            responseJson(out, providerById);
          }
          break;
          case "/server/provider/delete": {
            Provider provider = request.getJsonData(Provider.class);
            providerService.deleteProviderById(provider.getId());
            responseOk(out);
          }
          break;
          case "/server/user/list": {
            String payload = request.getPayload();
            if (payload == null) {
              responseJson(out, userService.getUserList());
            } else {
              User user = JSONObject.parseObject(payload, User.class);
              responseJson(out, userService.getUserList(user));
            }
          }
          break;
          case "/server/user/modify": {
            Map<String, String> formData = request.getFormData();
            User user = JSONObject
                .parseObject(JSONObject.toJSONString(formData), User.class);
            if (user.getId() == 0) {
              userService.addUser(user);
            } else {
              userService.updateUser(user);
            }
            responseRedirect(out, request, "/user_list.html");
          }
          break;
          case "/server/user/get": {
            User user = request.getJsonData(User.class);
            User userById = userService.getUserById(user.getId());
            responseJson(out, userById);
          }
          break;
          case "/server/user/delete": {
            User user = request.getJsonData(User.class);
            userService.deleteUserById(user.getId());
            responseOk(out);
          }
          break;
        }
      }

    } catch (Exception e) {
      try {
        HttpStatus status;
        if (e instanceof RequestException) {
          RequestException req = (RequestException) e;
          status = req.getStatus();
        } else {
          status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (out != null) {
          out.writeBytes("HTTP/1.1 " + status.response());
          out.writeBytes("\r\n");
        }
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    } finally {
      try {
        connectedClient.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private static void responseOk(DataOutputStream out) throws IOException {
    out.writeBytes("HTTP/1.1 200 OK");
    out.writeBytes("\r\n");
  }

  private void responseRedirect(DataOutputStream out, MbsRequest request, String s)
      throws IOException {
    out.writeBytes("HTTP/1.1 302 Found");
    out.writeBytes("\r\n");
    out.writeBytes("Location: " + "http://" + request.getHost() + s);
    out.writeBytes("\r\n");
  }

  private void responseJson(DataOutputStream out, Object obj) throws IOException {
    String data = JSONObject.toJSONString(obj);
    responseJson(out, data);
  }

  private void responseJson(DataOutputStream out, String s) throws IOException {
    String data = s;
    responseOk(out);
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

    InputStream resourceAsStream = HttpServer.class.getClassLoader()
        .getResourceAsStream("pages/" + path);

    if (resourceAsStream == null) {
      throw new RequestException(HttpStatus.NOT_FOUND);
    }
    /*
    返回顺序不能错
    状态
    Server
    Content-type
    content-length
     */
    responseOk(out);
    if (path.endsWith(".html")) {
      out.writeBytes("Content-Type: text/html; charset=utf-8");
      out.writeBytes("\r\n");
    }
    // css文件必须给定文件长度，并且不能用Reader readLine不然长度和字节数对不上
    out.writeBytes(("Content-Length: " + resourceAsStream.available()));
    out.writeBytes("\r\n");

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