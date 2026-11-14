package vn.vcraft.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.QuoteData.*;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.QuoteRepository;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static vn.vcraft.backend.dto.QuoteData.State.*;

@Service
@Transactional(readOnly = true)
public class QuoteRequestService {
    private static final Map<State, Set<State>> TRANSITIONS = Map.of(
            DRAFT, Set.of(SENT), SENT, Set.of(BIDDING, CANCELLED), BIDDING, Set.of(ACCEPTED, CANCELLED),
            ACCEPTED, Set.of(IN_PRODUCTION), IN_PRODUCTION, Set.of(DELIVERED));
    private final QuoteRepository quotes;

    public QuoteRequestService(QuoteRepository quotes) { this.quotes = quotes; }

    public List<Request> buyerList(Actor actor) { return quotes.buyerRequests(actor.id()); }

    public List<Request> artisanList(Actor actor) { return quotes.artisanRequests(actor.workshop()); }

    public Details buyerDetail(Actor actor, long id) {
        var request = buyer(actor, id, false);
        return new Details(request, quotes.quotes(id), quotes.events(id));
    }

    public Details artisanDetail(Actor actor, long id) {
        var request = artisan(actor, id, false);
        return new Details(request, quotes.ownQuote(id, actor.workshop()), quotes.events(id));
    }

    @Transactional
    public Details create(Actor actor, Draft input) {
        long id = quotes.create(actor.id(), category(input.category()), input);
        quotes.event(id, actor.id(), null, DRAFT);
        return buyerDetail(actor, id);
    }

    @Transactional
    public Details update(Actor actor, long id, Draft input) {
        var request = buyer(actor, id, true);
        requireState(request.status() == DRAFT);
        if (!Objects.equals(request.version(), input.version())) throw stale();
        quotes.update(id, category(input.category()), input);
        return buyerDetail(actor, id);
    }

    @Transactional
    public void delete(Actor actor, long id) {
        requireState(buyer(actor, id, true).status() == DRAFT);
        quotes.delete(id);
    }

    @Transactional
    public Details send(Actor actor, long id, Target input) {
        var request = buyer(actor, id, true);
        requireState(request.status() == DRAFT);
        Long target = quotes.targetId(input.workshop());
        if (target == null) throw new ApiException(BAD_REQUEST, "INVALID_WORKSHOP", "Xưởng không khả dụng.");
        requireBiddingDeadline(request);
        transition(actor, request, SENT, target, null);
        return buyerDetail(actor, id);
    }

    @Transactional
    public Details cancel(Actor actor, long id) {
        var request = buyer(actor, id, true);
        transition(actor, request, CANCELLED, request.targetWorkshopId(), null);
        quotes.closeQuotes(id, null);
        return buyerDetail(actor, id);
    }

    @Transactional
    public Details submit(Actor actor, long id, Offer input) {
        var request = artisan(actor, id, true);
        requireState(request.status() == SENT || request.status() == BIDDING);
        requireBiddingDeadline(request);
        quotes.submit(id, actor.workshop(), input);
        if (request.status() == SENT) transition(actor, request, BIDDING, request.targetWorkshopId(), null);
        return artisanDetail(actor, id);
    }

    @Transactional
    public Details accept(Actor actor, long id, long quoteId, Selection input) {
        var request = buyer(actor, id, true);
        requireState(request.status() == BIDDING);
        var quote = quotes.quotes(id).stream().filter(item -> item.id() == quoteId)
                .findFirst().orElseThrow(QuoteRequestService::missing);
        requireState(quote.status().equals("SUBMITTED"));
        if (!quote.updatedAt().isEqual(input.quoteUpdatedAt())) throw stale();
        quotes.closeQuotes(id, quoteId);
        transition(actor, request, ACCEPTED, request.targetWorkshopId(), quoteId);
        return buyerDetail(actor, id);
    }

    @Transactional
    public Details progress(Actor actor, long id, State next) {
        var request = artisan(actor, id, true);
        transition(actor, request, next, request.targetWorkshopId(), request.acceptedQuoteId());
        return artisanDetail(actor, id);
    }

    private void transition(Actor actor, Request request, State next, Long target, Long accepted) {
        requireState(TRANSITIONS.getOrDefault(request.status(), Set.of()).contains(next));
        quotes.transition(request.id(), next, target, accepted);
        quotes.event(request.id(), actor.id(), request.status(), next);
    }

    private Request buyer(Actor actor, long id, boolean lock) {
        return quotes.buyerRequest(id, actor.id(), lock).orElseThrow(QuoteRequestService::missing);
    }

    private Request artisan(Actor actor, long id, boolean lock) {
        return quotes.artisanRequest(id, actor.workshop(), lock).orElseThrow(QuoteRequestService::missing);
    }

    private long category(String slug) {
        Long id = quotes.categoryId(slug);
        if (id == null) throw new ApiException(BAD_REQUEST, "INVALID_CATEGORY", "Phân loại sản phẩm không tồn tại.");
        return id;
    }

    private static void requireBiddingDeadline(Request request) {
        if (request.biddingDeadline() != null && !request.biddingDeadline().isAfter(OffsetDateTime.now())) {
            throw new ApiException(CONFLICT, "BIDDING_CLOSED", "Đã hết hạn nhận báo giá.");
        }
    }

    private static void requireState(boolean allowed) {
        if (!allowed) throw new ApiException(CONFLICT, "INVALID_STATE_TRANSITION", "Yêu cầu đã đổi trạng thái. Vui lòng tải lại.");
    }

    private static ApiException stale() {
        return new ApiException(CONFLICT, "STALE_DATA", "Dữ liệu đã thay đổi. Vui lòng tải lại.");
    }

    private static ApiException missing() {
        return new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy yêu cầu hoặc báo giá.");
    }
}
