package vn.vcraft.backend.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.file.Path;
@Configuration
public class MediaResourceConfig implements WebMvcConfigurer {
    private final Path root;
    public MediaResourceConfig(@Value("${media.storage-path:/tmp/vcraft-media}") String path) { root=Path.of(path).toAbsolutePath().normalize(); }
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uri=root.toUri().toString();
        registry.addResourceHandler("/media/**").addResourceLocations(uri.endsWith("/") ? uri : uri+"/");
    }
}
