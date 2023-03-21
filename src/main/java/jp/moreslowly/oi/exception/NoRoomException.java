package jp.moreslowly.oi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class NoRoomException extends RuntimeException {
  public NoRoomException(String message) {
    super(message);
  }
}
