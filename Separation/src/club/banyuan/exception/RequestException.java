package club.banyuan.exception;

import club.banyuan.http.HttpStatus;

public class RequestException extends RuntimeException {

  private HttpStatus status;

  public RequestException(HttpStatus status) {
    this.status = status;
  }

  public RequestException(HttpStatus status, String msg) {
    this(msg);
    this.status = status;
  }

  public RequestException() {
  }

  public RequestException(String message) {
    super(message);
  }

  public RequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public RequestException(Throwable cause) {
    super(cause);
  }

  public RequestException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public HttpStatus getStatus() {
    return status;
  }
}
