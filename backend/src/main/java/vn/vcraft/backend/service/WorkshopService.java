package vn.vcraft.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
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
    public WorkshopResponse provision(NewArtisanRequest input) {
        if (!validName(input.workshopName()) || input.username() == null
                || !input.username().matches("[a-z0-9-]{3,60}")
                || input.password() == null || input.password().length() < 10
                || input.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(BAD_REQUEST, "Tên xưởng, tài khoản hoặc mật khẩu không hợp lệ.");
        }
        String hash = passwords.encode(input.password());
        var workshop = workshops.createPending(input.username(), input.workshopName().strip());
        accounts.addArtisan(input.username(), hash, workshop.id());
        return workshop;
    }

    public WorkshopResponse approve(long id) {
        return workshops.approve(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy xưởng."));
    }

    public WorkshopResponse rename(Actor actor, long id, String name) {
        if (!Long.valueOf(id).equals(actor.workshop())) {
            throw new ResponseStatusException(FORBIDDEN, "Bạn chỉ được sửa xưởng của mình.");
        }
        if (!validName(name)) {
            throw new ResponseStatusException(BAD_REQUEST, "Tên xưởng phải có từ 1 đến 160 ký tự.");
        }
        return workshops.rename(id, name)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Xưởng chưa được duyệt."));
    }

    private boolean validName(String name) {
        return name != null && !name.isBlank() && name.strip().length() <= 160;
    }
}
