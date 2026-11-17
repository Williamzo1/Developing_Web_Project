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
@RequestMapping("/api/buyer/quote-requests")
class BuyerQuoteController {
  private final QuoteRequestService service;

  BuyerQuoteController(QuoteRequestService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  QuoteRequestResponse create(
      @AuthenticationPrincipal Actor actor, @Valid @RequestBody QuoteRequestInput body) {
    return service.create(actor, body);
  }

  @GetMapping
  PageResponse<QuoteRequestSummary> list(
      @AuthenticationPrincipal Actor actor, @Valid @ModelAttribute PageQuery page) {
    return service.buyerList(actor, page);
  }

  @GetMapping("/{id}")
  QuoteRequestResponse detail(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    return service.detail(actor, id);
  }

  @PutMapping("/{id}")
  QuoteRequestResponse update(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody QuoteRequestInput body) {
    return service.update(actor, id, body);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody VersionInput body) {
    service.delete(actor, id, body.version());
  }

  @PostMapping("/{id}/send")
  QuoteRequestResponse send(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody VersionInput body) {
    return service.send(actor, id, body);
  }

  @PostMapping("/{id}/cancel")
  QuoteRequestResponse cancel(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @Valid @RequestBody VersionInput body) {
    return service.cancel(actor, id, body);
  }

  @GetMapping("/{id}/quotes")
  List<QuoteResponse> quotes(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    return service.quoteList(actor, id);
  }

  @GetMapping("/{id}/events")
  List<QuoteEventResponse> events(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    return service.history(actor, id);
  }

  @PostMapping("/{id}/quotes/{quoteId}/accept")
  QuoteRequestResponse accept(
      @AuthenticationPrincipal Actor actor,
      @PathVariable long id,
      @PathVariable long quoteId,
      @Valid @RequestBody VersionInput body) {
    return service.accept(actor, id, quoteId, body);
  }
}
