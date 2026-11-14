package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.NewsData.*;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.NewsService;

@RestController
@RequestMapping("/api")
class NewsController {
    private final NewsService news;

    NewsController(NewsService news) { this.news = news; }

    @GetMapping("/news")
    PageResponse<Summary> list(@Valid @ModelAttribute PageQuery page, @RequestParam(required = false) Kind kind) {
        return news.list(page, kind, false);
    }

    @GetMapping("/news/{slug}")
    News published(@PathVariable String slug) { return news.published(slug); }

    @GetMapping("/admin/news")
    PageResponse<Summary> manage(@Valid @ModelAttribute PageQuery page, @RequestParam(required = false) Kind kind) {
        return news.list(page, kind, true);
    }

    @GetMapping("/admin/news/{id}")
    News detail(@PathVariable long id) { return news.find(id); }

    @PostMapping("/admin/news")
    @ResponseStatus(HttpStatus.CREATED)
    News create(@AuthenticationPrincipal Actor actor, @Valid @RequestBody Input input) { return news.create(actor, input); }

    @PutMapping("/admin/news/{id}")
    News update(@PathVariable long id, @Valid @RequestBody Input input) { return news.update(id, input); }
}
