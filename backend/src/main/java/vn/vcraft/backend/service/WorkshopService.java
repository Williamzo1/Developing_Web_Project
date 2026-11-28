package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.*;
import vn.vcraft.backend.validation.SlugUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class WorkshopService {

  private final WorkshopRepository workshops;
  private final AccountRepository accounts;
  private final BCryptPasswordEncoder passwords;
  private final WorkshopHourRepository hourRepo;
  private final MediaRepository mediaRepo;
  private final ProductService products;
  private final BatchService batches;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  public WorkshopService(WorkshopRepository workshops, AccountRepository accounts,
                         BCryptPasswordEncoder passwords,
                         WorkshopHourRepository hourRepo, MediaRepository mediaRepo, ProductService products, BatchService batches) {
    this.workshops = workshops;
    this.accounts  = accounts;
    this.passwords = passwords;
    this.hourRepo  = hourRepo;
    this.mediaRepo = mediaRepo;
    this.products = products;
    this.batches = batches;
  }

  // ----------------------------------------------------------------
  // ADMIN — giữ nguyên
  // ----------------------------------------------------------------

  public List<WorkshopResponse> list() {
    return workshops.list().stream()
            .map(r -> new WorkshopResponse(r.id(), r.name(), r.slug(),
                    r.approved(), r.username(), r.lockedAt()))
            .toList();
  }

  @Transactional
  public WorkshopResponse provision(@NotNull @Valid NewArtisanRequest input) {
    // Sinh slug từ tên xưởng
    String slug = SlugUtils.uniqueSlug(input.workshopName(),
            s -> workshops.existsBySlug(s));
    String hash = passwords.encode(input.password());
    var workshop = workshops.createPending(slug, input.workshopName().strip());
    accounts.addArtisan(input.username(), hash, workshop.id());
    return workshop;
  }

  @Transactional
  public WorkshopResponse approve(long id) {
    return workshops.approve(id)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                    "Không tìm thấy xưởng."));
  }

  @Transactional
  public WorkshopResponse lock(long id, String reason) {
    workshops.lock(id, reason);
    accounts.revokeAllSessionsForWorkshop(id);
    return workshops.list().stream()
            .filter(r -> r.id() == id)
            .findFirst()
            .map(r -> new WorkshopResponse(r.id(), r.name(), r.slug(),
                    r.approved(), r.username(), r.lockedAt()))
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                    "Không tìm thấy xưởng."));
  }

  @Transactional
  public void unlock(long id) {
    workshops.unlock(id);
  }

  // ----------------------------------------------------------------
  // ARTISAN — rename/contact (giữ nguyên pattern cũ)
  // ----------------------------------------------------------------

  @Transactional
  public WorkshopResponse rename(Actor actor, long id,
                                 @NotNull @Valid WorkshopUpdateRequest input) {
    if (!Long.valueOf(id).equals(actor.workshop())) {
      throw new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
    }
    return workshops.rename(id, input.name())
            .orElseThrow(() -> new ApiException(FORBIDDEN, "FORBIDDEN",
                    "Xưởng chưa được duyệt."));
  }

  @Transactional
  public ContactResponse updateContact(Actor actor, long id,
                                       @NotNull @Valid WorkshopContactInput input) {
    if (!"ARTISAN".equals(actor.role()) || !Long.valueOf(id).equals(actor.workshop())) {
      throw new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
    }
    if ((input.email() == null || input.email().isBlank()) && input.phone() == null) {
      throw new ApiException(BAD_REQUEST, "REQUEST_INCOMPLETE",
              "Cần email hoặc điện thoại để liên hệ.");
    }
    return workshops.updateContact(id, input.email(), input.phone())
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                    "Không tìm thấy tài nguyên."));
  }

  // ----------------------------------------------------------------
  // PUBLIC
  // ----------------------------------------------------------------

  public PageResponse<WorkshopSummaryResponse> listPublic(PageQuery query) {
    int limit  = query.getSize();
    int offset = query.getPage() * limit;
    long total = workshops.countPublic();
    List<WorkshopSummaryResponse> items = workshops.listPublic(limit, offset, baseUrl)
            .stream()
            .map(w -> new WorkshopSummaryResponse(
                    w.id(), w.slug(), w.name(), w.description(),
                    w.coverImage(),
                    w.acceptsVisitors(),
                    null)) // openNow: skip để tiết kiệm N query; trả null ở list
            .toList();
    return PageResponse.of(items, query.getPage(), limit, total);
  }

  public WorkshopDetailResponse getBySlug(String slug) {
    var row = workshops.findBySlug(slug)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                    "Không tìm thấy xưởng."));
    return toDetailResponse(row);
  }

  public WorkshopDetailResponse getMyWorkshop(Actor actor) {
    long workshopId = requireWorkshopId(actor);
    var row = workshops.findByWorkshopId(workshopId)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                    "Không tìm thấy xưởng."));
    return toDetailResponse(row);
  }

  public List<MapPinResponse> mapPins() {
    List<MapPinResponse> pins = workshops.mapPins(baseUrl);
    // Tính openNow cho từng ghim
    return pins.stream().map(pin -> new MapPinResponse(
            pin.id(), pin.slug(), pin.name(),
            pin.latitude(), pin.longitude(),
            pin.thumbnail(),
            pin.acceptsVisitors(),
            hourRepo.isOpenNow(pin.id()),  // 1 query/pin — chấp nhận được với số lượng nhỏ
            pin.address()
    )).toList();
  }

  // ----------------------------------------------------------------
  // ARTISAN — Hồ sơ
  // ----------------------------------------------------------------

  @Transactional
  public WorkshopDetailResponse updateProfile(Actor actor,
                                              @NotNull @Valid WorkshopProfileInput input) {
    long workshopId = requireWorkshopId(actor);

    // Kiểm tra cover_media_id thuộc người dùng
    if (input.coverMediaId() != null) {
      var media = mediaRepo.findById(input.coverMediaId())
              .orElseThrow(() -> new ApiException(BAD_REQUEST, "INVALID_INPUT",
                      "Ảnh bìa không hợp lệ."));
      if (media.ownerAccountId() != actor.id()) {
        throw new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
      }
    }

    // Kiểm tra tọa độ — phải có cả hai hoặc không cái nào
    if ((input.latitude() == null) != (input.longitude() == null)) {
      throw new ApiException(BAD_REQUEST, "VALIDATION_FAILED",
              "Vĩ độ và kinh độ phải được cung cấp cùng nhau.");
    }

    workshops.updateProfile(workshopId,
            input.name() != null ? input.name().strip() : null,
            input.description(),
            input.familyHistory(),
            input.address(),
            input.latitude(),
            input.longitude(),
            input.contactEmail(),
            input.phone(),
            input.facebookUrl(),
            input.acceptsVisitors() != null && input.acceptsVisitors(),
            input.coverMediaId());

    // Cập nhật ảnh xưởng nếu có
    if (input.mediaIds() != null) {
      validateMediaOwnership(actor.id(), input.mediaIds());
      mediaRepo.replaceLinks("WORKSHOP", workshopId, input.mediaIds());
    }

    return getMyWorkshop(actor);
  }

  @Transactional
  public void updateHours(Actor actor, List<WorkshopHourInput> hours) {
    long workshopId = requireWorkshopId(actor);
    if (hours.stream().map(WorkshopHourInput::dayOfWeek).distinct().count()!=hours.size())
      throw new ApiException(BAD_REQUEST,"VALIDATION_FAILED","Mỗi thứ chỉ khai báo một khung giờ.");
    // Validate giờ: open < close (đã có CHECK trong DB, nhưng validate sớm cho UX tốt hơn)
    for (var h : hours) {
      if (java.time.LocalTime.parse(h.openTime()).compareTo(java.time.LocalTime.parse(h.closeTime())) >= 0) {
        throw new ApiException(BAD_REQUEST, "VALIDATION_FAILED",
                "Giờ mở cửa phải trước giờ đóng cửa (thứ " + h.dayOfWeek() + ").");
      }
    }
    hourRepo.replaceHours(workshopId, hours);
  }

  @Transactional
  public void addClosure(Actor actor, WorkshopClosureInput input) {
    long workshopId = requireWorkshopId(actor);
    if (input.closedOn().isBefore(LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")))) {
      throw new ApiException(BAD_REQUEST, "VALIDATION_FAILED",
              "Ngày đóng cửa không thể là ngày trong quá khứ.");
    }
    hourRepo.addClosure(workshopId, input.closedOn(), input.reason());
  }

  @Transactional
  public void deleteClosure(Actor actor, long closureId) {
    long workshopId = requireWorkshopId(actor);
    boolean deleted = hourRepo.deleteClosure(workshopId, closureId);
    if (!deleted) throw new ApiException(NOT_FOUND, "NOT_FOUND",
            "Không tìm thấy ngày đóng cửa.");
  }

  // ----------------------------------------------------------------
  // CERTIFICATES
  // ----------------------------------------------------------------

  public List<CertificateResponse> getCertificates(Actor actor) {
    long workshopId = requireWorkshopId(actor);
    return certificatesWithImages(workshopId);
  }

  @Transactional
  public CertificateResponse addCertificate(Actor actor,
                                            @NotNull @Valid CertificateInput input) {
    long workshopId = requireWorkshopId(actor);
    var cert = workshops.addCertificate(workshopId, input.name(),
            input.issuer(), input.issuedOn());

    if (input.mediaIds() != null && !input.mediaIds().isEmpty()) {
      validateMediaOwnership(actor.id(), input.mediaIds());
      mediaRepo.replaceLinks("CERTIFICATE", cert.id(), input.mediaIds());
    }
    return certificatesWithImages(workshopId).stream().filter(c -> c.id().equals(cert.id())).findFirst().orElseThrow();
  }

  @Transactional
  public void deleteCertificate(Actor actor, long certId) {
    long workshopId = requireWorkshopId(actor);
    if (workshops.findCertificates(workshopId).stream().noneMatch(c -> c.id().equals(certId)))
      throw new ApiException(NOT_FOUND,"NOT_FOUND","Không tìm thấy chứng nhận.");
    mediaRepo.replaceLinks("CERTIFICATE", certId, List.of());
    boolean deleted = workshops.deleteCertificate(workshopId, certId);
    if (!deleted) throw new ApiException(NOT_FOUND, "NOT_FOUND",
            "Không tìm thấy chứng nhận.");
  }

  // ----------------------------------------------------------------
  // Utility
  // ----------------------------------------------------------------

  private long requireWorkshopId(Actor actor) {
    if (actor.workshop() == null) {
      throw new ApiException(FORBIDDEN, "FORBIDDEN",
              "Tài khoản không có xưởng.");
    }
    return actor.workshop();
  }

  private void validateMediaOwnership(long accountId, List<Long> mediaIds) {
    for (long mediaId : mediaIds) {
      var media = mediaRepo.findById(mediaId)
              .orElseThrow(() -> new ApiException(BAD_REQUEST, "INVALID_INPUT",
                      "Ảnh id=" + mediaId + " không tồn tại."));
      if (media.ownerAccountId() != accountId) {
        throw new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
      }
    }
  }

  private WorkshopDetailResponse toDetailResponse(
          WorkshopRepository.WorkshopDetailRow row) {

    // Ảnh bìa
    MediaResponse coverImage = null;
    if (row.pathThumb() != null) {
      coverImage = new MediaResponse(0L,
              baseUrl + "/media/" + row.pathThumb(),
              baseUrl + "/media/" + row.pathMedium(),
              baseUrl + "/media/" + row.pathLarge(),
              row.width(), row.height());
    }

    // Ảnh xưởng (từ media_links)
    List<MediaResponse> images = mediaRepo.findByEntity("WORKSHOP", row.id(), baseUrl);

    // Chứng nhận
    List<CertificateResponse> certs = certificatesWithImages(row.id());

    // Giờ mở cửa
    List<WorkshopHourResponse> hours = hourRepo.findByWorkshop(row.id());

    // openNow
    Boolean openNow = hourRepo.isOpenNow(row.id());

    return new WorkshopDetailResponse(
            row.id(), row.slug(), row.name(),
            row.description(), row.familyHistory(), row.address(),
            row.latitude(), row.longitude(),
            row.contactEmail(), row.phone(), row.facebookUrl(),
            row.acceptsVisitors(), openNow,
            coverImage, images, certs, hours,
            products.pageFiltered(new PageQuery(), row.id(), null, null).items(),
            batches.listPublic(new PageQuery(), null, row.id(), null).items()
    );
  }
  private List<CertificateResponse> certificatesWithImages(long id) {
    return workshops.findCertificates(id).stream().map(c -> new CertificateResponse(c.id(), c.name(),
            c.issuer(), c.issuedOn(), c.createdAt(), mediaRepo.findByEntity("CERTIFICATE",c.id(),baseUrl))).toList();
  }
}
