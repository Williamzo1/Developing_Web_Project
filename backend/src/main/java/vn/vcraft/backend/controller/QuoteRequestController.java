package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.QuoteData.*;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.QuoteRequestService;

@RestController
@RequestMapping("/api")
class QuoteRequestController {
    private static final String BUYER = "/buyer/quote-requests";
    private static final String ARTISAN = "/artisan/quote-requests";
    private final QuoteRequestService quotes;

    QuoteRequestController(QuoteRequestService quotes) { this.quotes = quotes; }

    @GetMapping(BUYER)
    List<Request> buyerList(@AuthenticationPrincipal Actor actor) { return quotes.buyerList(actor); }

    @GetMapping(BUYER + "/{id}")
    Details buyerDetail(@AuthenticationPrincipal Actor actor, @PathVariable long id) { return quotes.buyerDetail(actor, id); }

    @PostMapping(BUYER)
    @ResponseStatus(HttpStatus.CREATED)
    Details create(@AuthenticationPrincipal Actor actor, @Valid @RequestBody Draft input) { return quotes.create(actor, input); }

    @PutMapping(BUYER + "/{id}")
    Details update(@AuthenticationPrincipal Actor actor, @PathVariable long id, @Valid @RequestBody Draft input) {
        return quotes.update(actor, id, input);
    }

    @DeleteMapping(BUYER + "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Actor actor, @PathVariable long id) { quotes.delete(actor, id); }

    @PostMapping(BUYER + "/{id}/send")
    Details send(@AuthenticationPrincipal Actor actor, @PathVariable long id, @Valid @RequestBody Target input) {
        return quotes.send(actor, id, input);
    }

    @PostMapping(BUYER + "/{id}/cancel")
    Details cancel(@AuthenticationPrincipal Actor actor, @PathVariable long id) { return quotes.cancel(actor, id); }

    @GetMapping(BUYER + "/{id}/quotes")
    List<Quote> listQuotes(@AuthenticationPrincipal Actor actor, @PathVariable long id) { return quotes.buyerDetail(actor, id).quotes(); }

    @PostMapping(BUYER + "/{id}/quotes/{quoteId}/accept")
    Details accept(@AuthenticationPrincipal Actor actor, @PathVariable long id, @PathVariable long quoteId,
                   @Valid @RequestBody Selection input) { return quotes.accept(actor, id, quoteId, input); }

    @GetMapping(ARTISAN)
    List<Request> artisanList(@AuthenticationPrincipal Actor actor) { return quotes.artisanList(actor); }

    @GetMapping(ARTISAN + "/{id}")
    Details artisanDetail(@AuthenticationPrincipal Actor actor, @PathVariable long id) { return quotes.artisanDetail(actor, id); }

    @PutMapping(ARTISAN + "/{id}/quote")
    Details submit(@AuthenticationPrincipal Actor actor, @PathVariable long id, @Valid @RequestBody Offer input) {
        return quotes.submit(actor, id, input);
    }

    @PostMapping(ARTISAN + "/{id}/start-production")
    Details start(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
        return quotes.progress(actor, id, State.IN_PRODUCTION);
    }

    @PostMapping(ARTISAN + "/{id}/deliver")
    Details deliver(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
        return quotes.progress(actor, id, State.DELIVERED);
    }
}
