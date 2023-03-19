package jp.moreslowly.oi.config;

import java.util.Optional;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {
  @Value("${ajp.secret}")
  private String ajpSecret;

  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {
    return server ->
      Optional.ofNullable(server)
        .ifPresent(s -> s.addAdditionalTomcatConnectors(redirectConnector()));
  }

  private Connector redirectConnector() {
    Connector connector = new Connector("AJP/1.3");
    connector.setScheme("http");
    connector.setPort(8009);
    connector.setAllowTrace(false);

    final AbstractAjpProtocol protocol = (AbstractAjpProtocol) connector.getProtocolHandler();
    connector.setSecure(true);
    protocol.setSecret(ajpSecret);
    return connector;
  }
}
