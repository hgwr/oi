package jp.moreslowly.oi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableContentException extends RuntimeException {

  public static final String ROOM_IS_NOT_FOUND = "Room is not found.";

  public UnprocessableContentException(String message) {
    super(message);
  }
}
