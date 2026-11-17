package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import vn.vcraft.backend.dto.NewArtisanRequest;
import vn.vcraft.backend.dto.WorkshopResponse;
import vn.vcraft.backend.dto.WorkshopUpdateRequest;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.AccountRepository;
import vn.vcraft.backend.repository.WorkshopRepository;

@Service
@Validated
public class WorkshopService {
  private final WorkshopRepository workshops;
  private final AccountRepository accounts;
  private final BCryptPasswordEncoder passwords;

  public WorkshopService(
      WorkshopRepository workshops, AccountRepository accounts, BCryptPasswordEncoder passwords) {
    this.workshops = workshops;
    this.accounts = accounts;
    this.passwords = passwords;
  }

  public List<WorkshopResponse> list() {
    return workshops.list();
  }

  @Transactional
  public WorkshopResponse provision(@NotNull @Valid NewArtisanRequest input) {
    String hash = passwords.encode(input.password());
    var workshop = workshops.createPending(input.username(), input.workshopName().strip());
    accounts.addArtisan(input.username(), hash, workshop.id());
    return workshop;
  }

  public WorkshopResponse approve(long id) {
    return workshops
        .approve(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy xưởng."));
  }

  public WorkshopResponse rename(
      Actor actor, long id, @NotNull @Valid WorkshopUpdateRequest input) {
    if (!Long.valueOf(id).equals(actor.workshop())) {
      throw new ResponseStatusException(FORBIDDEN, "Bạn chỉ được sửa xưởng của mình.");
    }
    return workshops
        .rename(id, input.name())
        .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Xưởng chưa được duyệt."));
  }

  public vn.vcraft.backend.dto.ContactResponse updateContact(
      Actor actor, long id, @NotNull @Valid vn.vcraft.backend.dto.WorkshopContactInput input) {
    if (!"ARTISAN".equals(actor.role()) || !Long.valueOf(id).equals(actor.workshop())) {
      throw new vn.vcraft.backend.exception.ApiException(
          NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
    }
    if ((input.email() == null || input.email().isBlank()) && input.phone() == null) {
      throw new vn.vcraft.backend.exception.ApiException(
          BAD_REQUEST, "REQUEST_INCOMPLETE", "Cần email hoặc điện thoại để liên hệ.");
    }
    return workshops
        .updateContact(id, input.email(), input.phone())
        .orElseThrow(
            () ->
                new vn.vcraft.backend.exception.ApiException(
                    NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên."));
  }
}
