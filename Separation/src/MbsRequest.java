import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    this.path = path;
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
}
