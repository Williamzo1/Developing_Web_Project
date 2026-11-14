package vn.vcraft.backend.service;

import org.springframework.stereotype.Service;
import vn.vcraft.backend.dto.FeaturedContent.*;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.repository.FeaturedRepository;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class FeaturedService {
    private final FeaturedRepository featured;

    public FeaturedService(FeaturedRepository featured) { this.featured = featured; }

    public Input selected() { return featured.selected(); }

    public Home published() { return featured.published(); }

    public Input save(Input input) {
        if (!featured.save(input)) {
            throw new ApiException(BAD_REQUEST, "INVALID_FEATURED", "Chỉ chọn được xưởng, sản phẩm và lô đang công khai.");
        }
        return input;
    }
}
