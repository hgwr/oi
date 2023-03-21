package jp.moreslowly.oi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class FullMemberException extends RuntimeException {
  public FullMemberException(String message) {
    super(message);
  }
}
