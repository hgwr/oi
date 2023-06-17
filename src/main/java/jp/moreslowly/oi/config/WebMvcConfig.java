package jp.moreslowly.oi.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    // viteとの開発用に、3000からのアクセスを許可する。
    registry.addMapping("/api/**")
        .allowedMethods("*")
        .allowedOrigins("http://localhost:3000",
            "http://127.0.0.1:5173",
            "http://localhost:5173",
            "http://192.168.1.175:5173");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/index.html")
        .addResourceLocations("classpath:/static/")
        .setCacheControl(CacheControl.maxAge(0, TimeUnit.SECONDS).cachePrivate());
  }
}
