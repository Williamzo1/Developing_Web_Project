package vn.vcraft.backend.config;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.vcraft.backend.service.LocalMediaStorage;
import vn.vcraft.backend.service.MediaStorage;

/**
 * Lưu ảnh trên đĩa (giả định Q-13) và phục vụ GET /media/** như tệp tĩnh. Tên tệp chứa UUID nên
 * không đổi nội dung, có thể cache một năm.
 */
@Configuration
class MediaConfig implements WebMvcConfigurer {
  private final LocalMediaStorage storage;

  MediaConfig(@Value("${media.storage-path:./media-data}") String storagePath) {
    this.storage = new LocalMediaStorage(Path.of(storagePath));
  }

  @Bean
  MediaStorage mediaStorage() {
    return storage;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
            .addResourceHandler("/media/**")
            .addResourceLocations(resourceLocation())
            .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
  }
  private String resourceLocation() {
    String uri = storage.root().toUri().toString();
    return uri.endsWith("/") ? uri : uri + "/";
  }
}
