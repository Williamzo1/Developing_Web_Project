package vn.vcraft.backend.controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.*;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/news")
public class NewsController {
    private final AdminService admin;
    public NewsController(AdminService admin) {
        this.admin = admin;
    }

    @GetMapping
    PageResponse<NewsResponse> list(@Valid @ModelAttribute PageQuery q,@RequestParam(required=false) String kind) { return admin.listNewsPublic(q,kind); }
    @GetMapping("/{slug}")
    NewsResponse get(@PathVariable String slug) { return admin.newsPublic(slug); }

}
