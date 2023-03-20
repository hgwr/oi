package jp.moreslowly.oi.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(value = { IllegalArgumentException.class })
  protected org.springframework.http.ResponseEntity<Object> handleIllegalArgumentException(
      RuntimeException ex) {
    return org.springframework.http.ResponseEntity.badRequest().body(ex.getMessage());
  }
}
