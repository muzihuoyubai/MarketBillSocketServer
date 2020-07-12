import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MbsRequest {

  private String method;
  private String path;
  private int contentLength;
  private String payload;
  private String host;


  public static MbsRequest parse(BufferedReader in) throws IOException {
    String line;
    line = in.readLine();
    MbsRequest mbsRequest = new MbsRequest();
    if (line == null) {
      return null;
    }
    // 提取前两位，获取请求方式和路径
    // POST / HTTP/1.1
    StringTokenizer stringTokenizer = new StringTokenizer(line);
    mbsRequest.setMethod(stringTokenizer.nextToken());
    mbsRequest.setPath(stringTokenizer.nextToken());
    while ((line = in.readLine()) != null && (line.length() != 0)) {
      if (line.contains("Content-Length:")) {
        mbsRequest.setContentLength(Integer.parseInt(line.replace("Content-Length: ", "")));
      }
      if (line.contains("Host:")) {
        mbsRequest.setHost(line.replace("Host: ", ""));
      }
    }
    int contentLength = mbsRequest.getContentLength();
    if (contentLength > 0) {
      char[] charArray = new char[contentLength];
      in.read(charArray, 0, contentLength);
      // Form提交时，中文字符串解码
      mbsRequest.setPayload(URLDecoder.decode(new String(charArray), StandardCharsets.UTF_8));
    }
    return mbsRequest;
  }

  public boolean isGet() {
    return "GET".equals(method);
  }

  public boolean isPost() {
    return "POST".equals(method);
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    if (path.contains("?")) {
      this.path = path.substring(0, path.indexOf("?"));
      this.payload = path.substring(path.indexOf("?") + 1);
    } else {
      this.path = path;
    }
  }

  public int getContentLength() {
    return contentLength;
  }

  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Map<String, String> getFormData() {
    StringTokenizer stringTokenizer = new StringTokenizer(payload, "&|=");
    Map<String, String> map = new HashMap<>();
    while (stringTokenizer.hasMoreTokens()) {
      map.put(stringTokenizer.nextToken(), stringTokenizer.nextToken());
    }
    return map;
  }

  public String getJsonData() {
    return payload;
  }

  // public static void main(String[] args) {
  //   String s = "flag=doAdd&proName=123&proDesc=123&phone=123&contact=123&button=提交";
  //   StringTokenizer stringTokenizer = new StringTokenizer(s, "&|=");
  //   System.out.println(stringTokenizer.nextToken());
  //   System.out.println(stringTokenizer.nextToken());
  //   System.out.println(stringTokenizer.nextToken());
  //   System.out.println(stringTokenizer.nextToken());
  // }
}
