package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.QuoteRequestService;

@RestController
@RequestMapping("/api/artisan")
class ArtisanQuoteController {
  private final QuoteRequestService service;

  ArtisanQuoteController(QuoteRequestService service) {
    this.service = service;
  }

  @GetMapping("/quote-requests")
  PageResponse<QuoteRequestSummary> list(
      @AuthenticationPrincipal Actor actor, @Valid @ModelAttribute PageQuery page) {
    return service.artisanList(actor, page);
  }

  @GetMapping("/quote-requests/{id}")
  QuoteRequestResponse detail(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    return service.detail(actor, id);
  }

  @GetMapping("/quote-requests/{id}/quotes")
  List<QuoteResponse> quotes(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    return service.quoteList(actor, id);
  }

  @GetMapping("/quote-requests/{id}/events")
  List<QuoteEventResponse> events(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    return service.history(actor, id);
  }

  @PostMapping("/quote-requests/{id}/quotes")
  @ResponseStatus(HttpStatus.CREATED)
  QuoteResponse submit(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody QuoteInput body) {
    return service.submit(actor, id, body);
  }

  @GetMapping("/quotes/{id}")
  QuoteResponse quote(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    return service.quoteDetail(actor, id);
  }

  @PutMapping("/quotes/{id}")
  QuoteResponse edit(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody QuoteInput body) {
    return service.edit(actor, id, body);
  }

  @PostMapping("/quotes/{id}/withdraw")
  QuoteResponse withdraw(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody VersionInput body) {
    return service.withdraw(actor, id, body);
  }

  @PostMapping("/quote-requests/{id}/start-production")
  QuoteRequestResponse start(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody VersionInput body) {
    return service.start(actor, id, body);
  }

  @PostMapping("/quote-requests/{id}/deliver")
  QuoteRequestResponse deliver(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody VersionInput body) {
    return service.deliver(actor, id, body);
  }
}
