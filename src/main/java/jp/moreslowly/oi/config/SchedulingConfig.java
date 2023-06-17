package jp.moreslowly.oi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import jp.moreslowly.oi.tasks.DealerManager;
import jp.moreslowly.oi.tasks.SweeperTask;

@Configuration
@EnableScheduling
public class SchedulingConfig {

  @Bean
  public DealerManager dealerManager() {
    return new DealerManager();
  }

  @Bean
  public SweeperTask sweeperTask() {
    return new SweeperTask();
  }
}
