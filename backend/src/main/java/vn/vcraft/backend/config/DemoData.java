package vn.vcraft.backend.config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@Profile({"local", "demo"})
class DemoData {
    @Bean @org.springframework.core.annotation.Order(10)
    ApplicationRunner seedCatalog(DataSource dataSource) {
        return args -> new ResourceDatabasePopulator(new ClassPathResource("db/seed/catalog.sql"))
                .execute(dataSource);
    }
}
