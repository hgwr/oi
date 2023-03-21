package jp.moreslowly.oi.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Override
    public void addCorsMappings(CorsRegistry registry) {
        // viteとの開発用に、3000からのアクセスを許可する。
        registry.addMapping("/api/**")
                .allowedMethods("*")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:5173", "http://192.168.1.175:5173");
    }
}
