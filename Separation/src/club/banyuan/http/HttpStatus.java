package club.banyuan.http;

public enum HttpStatus {
  OK(200, "OK", "请求已经成功处理"),
  FOUND(302, "Found", "请重新发送请求"),
  BAD_REQUEST(400, "Bad Request", "请求错误，请修正请求"),
  UNAUTHORIZED(401, "Unauthorized", "没有被授权或者授权已经失效"),
  FORBIDDEN(403, "Forbidden", "请求被理解，但是拒绝执行"),
  NOT_FOUND(404, "Not Found", "资源未找到"),
  INTERNAL_SERVER_ERROR(500, "Internal Server Error", "服务器内部错误"),
  ;

  private final int code;
  private final String reasonEN;
  private final String reasonCN;

  HttpStatus(int code, String reasonEN, String reasonCN) {
    this.code = code;
    this.reasonEN = reasonEN;
    this.reasonCN = reasonCN;
  }

  public int getCode() {
    return code;
  }

  public String getReasonEN() {
    return reasonEN;
  }

  public String getReasonCN() {
    return reasonCN;
  }

  public String response() {
    return code + " " + reasonEN;
  }
}
