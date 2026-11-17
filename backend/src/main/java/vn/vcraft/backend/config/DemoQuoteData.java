package vn.vcraft.backend.config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@Profile({"local", "demo"})
class DemoQuoteData {
  @Bean
  @Order(30)
  ApplicationRunner seedQuoteOptions(DataSource ds) {
    return args ->
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/quotes.sql")).execute(ds);
  }
}
