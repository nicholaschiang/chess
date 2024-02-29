package service;

public class ExceptionWithStatusCode extends Exception {
  private int statusCode;

  public ExceptionWithStatusCode(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
