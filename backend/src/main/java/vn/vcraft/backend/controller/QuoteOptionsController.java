package vn.vcraft.backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.service.QuoteRequestService;

@RestController
class QuoteOptionsController {
  private final QuoteRequestService service;

  QuoteOptionsController(QuoteRequestService service) {
    this.service = service;
  }

  @GetMapping("/api/quote-categories")
  List<QuoteCategoryResponse> categories() {
    return service.categories();
  }

  @GetMapping("/api/quote-tags")
  List<QuoteTagResponse> tags() {
    return service.tags();
  }
}
