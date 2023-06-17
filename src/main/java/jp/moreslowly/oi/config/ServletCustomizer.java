package jp.moreslowly.oi.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ServletCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
  @Override
  public void customize(ConfigurableServletWebServerFactory factory) {
      // vue-routerをHistoryモードで動かすと、/以外が404となってしまうため、index.htmlに飛ばすように修正。
      ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
      factory.addErrorPages(error404Page);
  }
}
