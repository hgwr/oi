package jp.moreslowly.oi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import jp.moreslowly.oi.repository.RoomRepository;
import jp.moreslowly.oi.tasks.DealerManager;

@Configuration
@EnableScheduling
public class SchedulingConfig {

  @Bean
  public DealerManager dealerManager() {
    return new DealerManager();
  }
}
