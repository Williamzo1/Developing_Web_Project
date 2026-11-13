package vn.vcraft.backend.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.vcraft.backend.repository.AccountRepository;

@Configuration
@Profile({"local", "demo"})
class DemoAccounts {
    @Bean
    @Order(20)
    ApplicationRunner seedDemoAccounts(AccountRepository accounts, BCryptPasswordEncoder passwords) {
        return args -> {
            String hash = passwords.encode("VcraftDemo17!");
            accounts.seedAccount("buyer", hash, "BUYER");
            accounts.seedAccount("admin", hash, "ADMIN");
            accounts.seedArtisan("xuong-men-lam", hash);
            accounts.seedArtisan("xuong-dat-nung", hash);
        };
    }
}
