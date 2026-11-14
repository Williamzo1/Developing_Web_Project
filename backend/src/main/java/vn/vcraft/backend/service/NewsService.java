package vn.vcraft.backend.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.NewsData.*;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.NewsRepository;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class NewsService {
    private final NewsRepository news;

    public NewsService(NewsRepository news) { this.news = news; }

    public PageResponse<Summary> list(PageQuery page, Kind kind, boolean admin) { return news.list(page, kind, admin); }

    public News published(String slug) { return news.published(slug).orElseThrow(NewsService::missing); }

    public News find(long id) { return news.find(id).orElseThrow(NewsService::missing); }

    @Transactional
    public News create(Actor actor, Input input) { return news.insert(actor.id(), "tin-" + UUID.randomUUID(), input); }

    @Transactional
    public News update(long id, Input input) { return news.update(id, input).orElseThrow(NewsService::missing); }

    private static ApiException missing() {
        return new ApiException(NOT_FOUND, "NEWS_NOT_FOUND", "Không tìm thấy bài viết.");
    }
}
