package vn.vcraft.backend.service;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import vn.vcraft.backend.dto.WorkshopUpdateRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.vcraft.backend.dto.NewArtisanRequest;
import vn.vcraft.backend.dto.WorkshopResponse;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.AccountRepository;
import vn.vcraft.backend.repository.WorkshopRepository;
import static org.springframework.http.HttpStatus.*;

@Service
@Validated
public class WorkshopService {
    private final WorkshopRepository workshops;
    private final AccountRepository accounts;
    private final BCryptPasswordEncoder passwords;

    public WorkshopService(WorkshopRepository workshops, AccountRepository accounts, BCryptPasswordEncoder passwords) {
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
        return workshops.approve(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy xưởng."));
    }

    public WorkshopResponse rename(Actor actor, long id, @NotNull @Valid WorkshopUpdateRequest input) {
        if (!Long.valueOf(id).equals(actor.workshop())) {
            throw new ResponseStatusException(FORBIDDEN, "Bạn chỉ được sửa xưởng của mình.");
        }
        return workshops.rename(id, input.name())
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Xưởng chưa được duyệt."));
    }

}
