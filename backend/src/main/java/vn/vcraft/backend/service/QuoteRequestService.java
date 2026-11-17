package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static vn.vcraft.backend.model.RequestStatus.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.*;
import vn.vcraft.backend.repository.*;

@Service
@Validated
@Transactional(isolation = Isolation.READ_COMMITTED)
public class QuoteRequestService {
  private static final Logger log = LoggerFactory.getLogger(QuoteRequestService.class);
  // Every state mutation is routed through transition(); controllers cannot set status.
  private static final Map<RequestStatus, Set<RequestStatus>> TRANSITIONS =
      Map.of(
          DRAFT,
          Set.of(SENT),
          SENT,
          Set.of(BIDDING, CANCELLED),
          BIDDING,
          Set.of(BIDDING, SENT, ACCEPTED, CANCELLED),
          ACCEPTED,
          Set.of(IN_PRODUCTION),
          IN_PRODUCTION,
          Set.of(DELIVERED));
  private final QuoteRequestRepository requests;
  private final QuoteRepository quotes;
  private final QuoteEventRepository events;
  private final QuoteMatchingRepository matching;

  public QuoteRequestService(
      QuoteRequestRepository requests,
      QuoteRepository quotes,
      QuoteEventRepository events,
      QuoteMatchingRepository matching) {
    this.requests = requests;
    this.quotes = quotes;
    this.events = events;
    this.matching = matching;
  }

  private ApiException missing() {
    return new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
  }

  private ApiException conflict(String code, String message) {
    return new ApiException(CONFLICT, code, message);
  }

  private void require(boolean condition, String message) {
    if (!condition) throw new ApiException(BAD_REQUEST, "REQUEST_INCOMPLETE", message);
  }

  private void version(Long actual, Long expected) {
    require(expected != null, "Vui lòng gửi version của dữ liệu đang xem.");
    if (!Objects.equals(actual, expected))
      throw conflict("STALE_DATA", "Dữ liệu đã thay đổi, vui lòng tải lại.");
  }

  private QuoteRequest locked(long id) {
    return requests.lockById(id).orElseThrow(this::missing);
  }

  private QuoteRequest owned(Actor actor, long id, boolean lock) {
    QuoteRequest r = lock ? locked(id) : requests.findById(id).orElseThrow(this::missing);
    if (!"BUYER".equals(actor.role()) || !Objects.equals(r.getBuyerAccountId(), actor.id()))
      throw missing();
    return r;
  }

  private void artisan(Actor actor) {
    if (!"ARTISAN".equals(actor.role())
        || actor.workshop() == null
        || !matching.approved(actor.workshop())) throw missing();
  }

  private void state(QuoteRequest r, RequestStatus... allowed) {
    if (!Arrays.asList(allowed).contains(r.getStatus()))
      throw conflict("INVALID_STATE_TRANSITION", "Thao tác không phù hợp trạng thái hiện tại.");
  }

  private void transition(QuoteRequest r, RequestStatus next, Actor actor, String note) {
    RequestStatus before = r.getStatus();
    if (!TRANSITIONS.getOrDefault(before, Set.of()).contains(next))
      throw conflict("INVALID_STATE_TRANSITION", "Không thể chuyển trạng thái này.");
    r.setStatus(next);
    r.setUpdatedAt(Instant.now());
    event(r, actor, before, next, note);
    log.info(
        "Quote request {} changed {} -> {} by account {}", r.getId(), before, next, actor.id());
  }

  private void event(
      QuoteRequest r, Actor actor, RequestStatus before, RequestStatus next, String note) {
    QuoteRequestEvent e = new QuoteRequestEvent();
    e.setRequestId(r.getId());
    e.setActorAccountId(actor.id());
    e.setFromStatus(before);
    e.setToStatus(next);
    e.setNote(note);
    e.setCreatedAt(Instant.now());
    events.save(e);
  }

  private String clean(String s) {
    return s == null ? null : s.strip();
  }

  private void apply(QuoteRequest r, QuoteRequestInput i) {
    if (i.categoryId() != null)
      require(matching.categoryExists(i.categoryId()), "Phân loại không tồn tại.");
    if (i.targetWorkshopId() != null)
      require(
          matching.approved(i.targetWorkshopId()),
          "Hộ được chỉ định chưa được duyệt hoặc không tồn tại.");
    require(
        i.mode() != RequestMode.OPEN || i.targetWorkshopId() == null,
        "Yêu cầu mở không chỉ định hộ.");
    List<Long> tags = i.tagIds() == null ? List.of() : i.tagIds();
    require(new HashSet<>(tags).size() == tags.size(), "Tag không được trùng.");
    tags.forEach(t -> require(matching.tagExists(t), "Tag không tồn tại."));
    r.setCategoryId(i.categoryId());
    r.setTitle(clean(i.title()));
    r.setQuantity(i.quantity());
    r.setUnit(clean(i.unit()));
    r.setSpec(clean(i.spec()));
    r.setNote(clean(i.note()));
    r.setDesiredBy(i.desiredBy());
    r.setBiddingDeadline(i.biddingDeadline());
    r.setMode(i.mode());
    r.setTargetWorkshopId(i.targetWorkshopId());
    r.setBuyerContactName(clean(i.buyerContactName()));
    r.setBuyerContactEmail(clean(i.buyerContactEmail()));
    r.setBuyerContactPhone(clean(i.buyerContactPhone()));
    r.setUpdatedAt(Instant.now());
    requests.saveAndFlush(r); // create FK parent before JDBC tag inserts, same Spring transaction
    matching.replaceTags(r.getId(), tags);
  }

  public QuoteRequestResponse create(Actor actor, @NotNull @Valid QuoteRequestInput input) {
    if (!"BUYER".equals(actor.role())) throw missing();
    QuoteRequest r = new QuoteRequest();
    r.setBuyerAccountId(actor.id());
    r.setStatus(DRAFT);
    r.setCreatedAt(Instant.now());
    apply(r, input);
    event(r, actor, null, DRAFT, "Tạo yêu cầu nháp.");
    return response(r, actor);
  }

  public QuoteRequestResponse update(
      Actor actor, long id, @NotNull @Valid QuoteRequestInput input) {
    QuoteRequest r = owned(actor, id, true);
    state(r, DRAFT);
    version(r.getVersion(), input.version());
    apply(r, input);
    return response(r, actor);
  }

  public void delete(Actor actor, long id, long expectedVersion) {
    QuoteRequest r = owned(actor, id, true);
    state(r, DRAFT);
    version(r.getVersion(), expectedVersion);
    requests.delete(r);
  }

  public QuoteRequestResponse send(Actor actor, long id, @NotNull @Valid VersionInput input) {
    QuoteRequest r = owned(actor, id, true);
    state(r, DRAFT);
    version(r.getVersion(), input.version());
    require(
        r.getCategoryId() != null
            && r.getTitle() != null
            && !r.getTitle().isBlank()
            && r.getQuantity() != null
            && r.getUnit() != null
            && !r.getUnit().isBlank()
            && r.getSpec() != null
            && !r.getSpec().isBlank()
            && r.getDesiredBy() != null,
        "Cần phân loại, tiêu đề, số lượng, đơn vị, yêu cầu kỹ thuật và ngày mong muốn.");
    require(
        !r.getDesiredBy().isBefore(LocalDate.now(ZoneOffset.UTC)),
        "Ngày mong muốn không được trong quá khứ (UTC).");
    require(
        r.getBuyerContactName() != null
            && !r.getBuyerContactName().isBlank()
            && ((r.getBuyerContactEmail() != null && !r.getBuyerContactEmail().isBlank())
                || r.getBuyerContactPhone() != null),
        "Cần tên liên hệ và email hoặc điện thoại của Buyer.");
    if (r.getMode() == RequestMode.TARGETED)
      require(
          r.getTargetWorkshopId() != null && matching.approved(r.getTargetWorkshopId()),
          "Cần chỉ định đúng một hộ đã được duyệt.");
    else require(!matching.tags(id).isEmpty(), "Yêu cầu mở phải có ít nhất một tag.");
    require(
        r.getBiddingDeadline() == null || r.getBiddingDeadline().isAfter(Instant.now()),
        "Hạn nhận báo giá phải ở tương lai khi gửi.");
    transition(r, SENT, actor, "Buyer gửi yêu cầu.");
    return response(r, actor);
  }

  public QuoteRequestResponse cancel(Actor actor, long id, @NotNull @Valid VersionInput input) {
    QuoteRequest r = owned(actor, id, true);
    state(r, SENT, BIDDING);
    version(r.getVersion(), input.version());
    for (Quote q : quotes.findByRequestIdOrderByIdAsc(id))
      if (q.getStatus() == QuoteStatus.SUBMITTED) {
        q.setStatus(QuoteStatus.NOT_SELECTED);
        q.setUpdatedAt(Instant.now());
      }
    transition(r, CANCELLED, actor, "Buyer hủy yêu cầu.");
    return response(r, actor);
  }

  @Transactional(readOnly = true)
  public PageResponse<QuoteRequestSummary> buyerList(Actor actor, PageQuery query) {
    return PageResponse.from(
        requests.findByBuyerAccountId(actor.id(), page(query)).map(this::summary));
  }

  @Transactional(readOnly = true)
  public PageResponse<QuoteRequestSummary> artisanList(Actor actor, PageQuery query) {
    artisan(actor);
    var ids = matching.visiblePage(actor.workshop(), query.getPage(), query.getSize());
    Map<Long, QuoteRequest> byId = new HashMap<>();
    requests.findAllById(ids.getContent()).forEach(r -> byId.put(r.getId(), r));
    return PageResponse.from(ids.map(id -> summary(byId.get(id))));
  }

  private Pageable page(PageQuery q) {
    return PageRequest.of(q.getPage(), q.getSize(), Sort.by("id").descending());
  }

  private QuoteRequestSummary summary(QuoteRequest r) {
    return new QuoteRequestSummary(
        r.getId(),
        r.getTitle(),
        r.getMode(),
        r.getStatus(),
        r.getQuantity(),
        r.getUnit(),
        r.getBiddingDeadline(),
        r.getUpdatedAt(),
        r.getVersion());
  }

  private QuoteRequest visible(Actor actor, long id) {
    if ("BUYER".equals(actor.role())) return owned(actor, id, false);
    artisan(actor);
    QuoteRequest r = requests.findById(id).orElseThrow(this::missing);
    boolean prior = quotes.findByRequestIdAndWorkshopId(id, actor.workshop()).isPresent();
    boolean targeted =
        r.getMode() == RequestMode.TARGETED
            && Objects.equals(r.getTargetWorkshopId(), actor.workshop())
            && r.getStatus() != DRAFT;
    boolean open =
        (r.getStatus() == SENT || r.getStatus() == BIDDING)
            && matching.isEligible(id, actor.workshop());
    if (!prior && !targeted && !open) throw missing();
    return r;
  }

  @Transactional(readOnly = true)
  public QuoteRequestResponse detail(Actor actor, long id) {
    return response(visible(actor, id), actor);
  }

  @Transactional(readOnly = true)
  public List<QuoteResponse> quoteList(Actor actor, long id) {
    return response(visible(actor, id), actor).quotes();
  }

  @Transactional(readOnly = true)
  public List<QuoteEventResponse> history(Actor actor, long id) {
    visible(actor, id);
    boolean buyer = "BUYER".equals(actor.role());
    return events.findByRequestIdOrderByIdAsc(id).stream()
        .map(
            e ->
                new QuoteEventResponse(
                    e.getId(),
                    buyer || Objects.equals(e.getActorAccountId(), actor.id())
                        ? e.getActorAccountId()
                        : null,
                    e.getFromStatus(),
                    e.getToStatus(),
                    e.getNote(),
                    e.getCreatedAt()))
        .toList();
  }

  public QuoteResponse submit(Actor actor, long requestId, @NotNull @Valid QuoteInput input) {
    artisan(actor);
    QuoteRequest r = locked(requestId);
    state(r, SENT, BIDDING);
    if (!matching.isEligible(requestId, actor.workshop())) throw missing();
    if (r.getBiddingDeadline() != null && !r.getBiddingDeadline().isAfter(Instant.now()))
      throw conflict("BIDDING_CLOSED", "Đã hết hạn nhận báo giá mới.");
    Quote q = quotes.findByRequestIdAndWorkshopId(requestId, actor.workshop()).orElse(null);
    if (q != null && q.getStatus() != QuoteStatus.WITHDRAWN)
      throw conflict("QUOTE_ALREADY_EXISTS", "Hộ đã có báo giá, hãy sửa báo giá hiện tại.");
    if (q == null) {
      q = new Quote();
      q.setRequestId(requestId);
      q.setWorkshopId(actor.workshop());
      q.setCreatedAt(Instant.now());
    } else version(q.getVersion(), input.version());
    applyQuote(q, r, input);
    q.setStatus(QuoteStatus.SUBMITTED);
    quotes.saveAndFlush(q);
    transition(r, BIDDING, actor, "Hộ gửi báo giá.");
    return quoteResponse(q);
  }

  private void applyQuote(Quote q, QuoteRequest r, QuoteInput i) {
    BigDecimal total = i.unitPrice().multiply(BigDecimal.valueOf(r.getQuantity()));
    require(total.precision() - total.scale() <= 13, "Tổng giá vượt giới hạn lưu trữ.");
    q.setUnitPrice(i.unitPrice());
    q.setTotalPrice(total);
    q.setLeadTimeDays(i.leadTimeDays());
    q.setNote(clean(i.note()));
    q.setUpdatedAt(Instant.now());
  }

  private Quote ownQuote(Actor actor, long id) {
    Quote q = quotes.findById(id).orElseThrow(this::missing);
    if (!Objects.equals(q.getWorkshopId(), actor.workshop())) throw missing();
    return q;
  }

  private QuoteRequest parentOf(long quoteId) {
    return locked(quotes.requestIdOf(quoteId).orElseThrow(this::missing));
  }

  public QuoteResponse edit(Actor actor, long quoteId, @NotNull @Valid QuoteInput input) {
    artisan(actor);
    QuoteRequest r = parentOf(quoteId);
    Quote q = ownQuote(actor, quoteId);
    state(r, BIDDING);
    if (q.getStatus() != QuoteStatus.SUBMITTED)
      throw conflict("INVALID_STATE_TRANSITION", "Báo giá không còn được sửa.");
    version(q.getVersion(), input.version());
    applyQuote(q, r, input);
    quotes.flush();
    transition(r, BIDDING, actor, "Hộ cập nhật báo giá.");
    return quoteResponse(q);
  }

  public QuoteResponse withdraw(Actor actor, long quoteId, @NotNull @Valid VersionInput input) {
    artisan(actor);
    QuoteRequest r = parentOf(quoteId);
    Quote q = ownQuote(actor, quoteId);
    state(r, BIDDING);
    if (q.getStatus() != QuoteStatus.SUBMITTED)
      throw conflict("INVALID_STATE_TRANSITION", "Báo giá không còn được rút.");
    version(q.getVersion(), input.version());
    q.setStatus(QuoteStatus.WITHDRAWN);
    q.setUpdatedAt(Instant.now());
    quotes.flush();
    transition(
        r,
        quotes.existsByRequestIdAndStatus(r.getId(), QuoteStatus.SUBMITTED) ? BIDDING : SENT,
        actor,
        "Hộ rút báo giá.");
    return quoteResponse(q);
  }

  @Transactional(readOnly = true)
  public QuoteResponse quoteDetail(Actor actor, long quoteId) {
    Quote q = quotes.findById(quoteId).orElseThrow(this::missing);
    if ("BUYER".equals(actor.role())) owned(actor, q.getRequestId(), false);
    else {
      artisan(actor);
      ownQuote(actor, quoteId);
    }
    return quoteResponse(q);
  }

  public QuoteRequestResponse accept(
      Actor actor, long requestId, long quoteId, @NotNull @Valid VersionInput input) {
    QuoteRequest r =
        owned(actor, requestId, true); // SELECT ... FOR UPDATE: serialize ALL child mutations
    state(r, BIDDING);
    Quote selected = quotes.findById(quoteId).orElseThrow(this::missing);
    if (!Objects.equals(selected.getRequestId(), requestId)) throw missing();
    if (selected.getStatus() != QuoteStatus.SUBMITTED)
      throw conflict("INVALID_STATE_TRANSITION", "Báo giá không còn hiệu lực.");
    version(selected.getVersion(), input.version()); // Buyer accepts exactly the version displayed
    for (Quote q : quotes.findByRequestIdOrderByIdAsc(requestId))
      if (q.getStatus() == QuoteStatus.SUBMITTED) {
        q.setStatus(q.getId().equals(quoteId) ? QuoteStatus.ACCEPTED : QuoteStatus.NOT_SELECTED);
        q.setUpdatedAt(Instant.now());
      }
    quotes.flush();
    r.setAcceptedQuoteId(quoteId);
    transition(r, ACCEPTED, actor, "Buyer chấp nhận một báo giá.");
    return response(r, actor);
  }

  public QuoteRequestResponse start(Actor actor, long id, @NotNull @Valid VersionInput input) {
    return progress(actor, id, input, ACCEPTED, IN_PRODUCTION);
  }

  public QuoteRequestResponse deliver(Actor actor, long id, @NotNull @Valid VersionInput input) {
    return progress(actor, id, input, IN_PRODUCTION, DELIVERED);
  }

  private QuoteRequestResponse progress(
      Actor actor, long id, VersionInput input, RequestStatus before, RequestStatus next) {
    artisan(actor);
    QuoteRequest r = locked(id);
    if (r.getAcceptedQuoteId() == null) {
      if (r.getMode() == RequestMode.TARGETED
              && Objects.equals(r.getTargetWorkshopId(), actor.workshop())
          || quotes.findByRequestIdAndWorkshopId(id, actor.workshop()).isPresent())
        throw conflict("INVALID_STATE_TRANSITION", "Yêu cầu chưa chọn báo giá.");
      throw missing();
    }
    Quote q = quotes.findById(r.getAcceptedQuoteId()).orElseThrow(this::missing);
    if (!Objects.equals(q.getWorkshopId(), actor.workshop())) throw missing();
    state(r, before);
    version(r.getVersion(), input.version());
    transition(
        r,
        next,
        actor,
        next == DELIVERED ? "Hộ xác nhận đã giao." : "Hộ xác nhận bắt đầu sản xuất.");
    return response(r, actor);
  }

  private QuoteResponse quoteResponse(Quote q) {
    return new QuoteResponse(
        q.getId(),
        q.getRequestId(),
        q.getWorkshopId(),
        q.getUnitPrice(),
        q.getTotalPrice(),
        q.getLeadTimeDays(),
        q.getNote(),
        q.getStatus(),
        q.getVersion(),
        q.getCreatedAt(),
        q.getUpdatedAt(),
        matching.contact(q.getWorkshopId()));
  }

  private QuoteRequestResponse response(QuoteRequest r, Actor actor) {
    requests.flush();
    boolean buyer = "BUYER".equals(actor.role());
    List<Quote> visible =
        buyer
            ? quotes.findByRequestIdOrderByIdAsc(r.getId())
            : quotes
                .findByRequestIdAndWorkshopId(r.getId(), actor.workshop())
                .map(List::of)
                .orElse(List.of());
    List<QuoteResponse> qs = visible.stream().map(this::quoteResponse).toList();
    boolean ownAccepted = visible.stream().anyMatch(q -> q.getStatus() == QuoteStatus.ACCEPTED);
    ContactResponse contact =
        buyer || !qs.isEmpty()
            ? new ContactResponse(
                r.getBuyerContactName(), r.getBuyerContactEmail(), r.getBuyerContactPhone())
            : null;
    return new QuoteRequestResponse(
        r.getId(),
        r.getCategoryId(),
        r.getTitle(),
        r.getQuantity(),
        r.getUnit(),
        r.getSpec(),
        r.getNote(),
        r.getDesiredBy(),
        r.getBiddingDeadline(),
        r.getMode(),
        r.getTargetWorkshopId(),
        matching.tags(r.getId()),
        r.getStatus(),
        buyer || ownAccepted ? r.getAcceptedQuoteId() : null,
        r.getVersion(),
        r.getCreatedAt(),
        r.getUpdatedAt(),
        contact,
        qs);
  }

  @Transactional(readOnly = true)
  public List<QuoteCategoryResponse> categories() {
    return matching.categoryOptions();
  }

  @Transactional(readOnly = true)
  public List<QuoteTagResponse> tags() {
    return matching.tagOptions();
  }
}
