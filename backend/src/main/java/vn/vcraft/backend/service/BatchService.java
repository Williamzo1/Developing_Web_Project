package vn.vcraft.backend.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.BatchData.*;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.model.ProcessStep;
import vn.vcraft.backend.repository.BatchRepository;
import vn.vcraft.backend.repository.ProcessStepRepository;
import vn.vcraft.backend.security.BearerTokens;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional(readOnly = true)
public class BatchService {
    private final BatchRepository batches;
    private final ProcessStepRepository steps;

    public BatchService(BatchRepository batches, ProcessStepRepository steps) {
        this.batches = batches;
        this.steps = steps;
    }

    public PageResponse<Batch> published(PageQuery page, String product, String workshop, Kind kind) {
        return batches.published(page, product, workshop, kind);
    }

    public Details publicDetail(UUID code, String token) {
        if (token != null && token.length() > 100) throw missing();
        return details(batches.publicDetail(code, token == null ? null : BearerTokens.hash(token))
                .orElseThrow(BatchService::missing));
    }

    public PageResponse<Batch> ownList(Actor actor, PageQuery page) { return batches.ownList(actor.workshop(), page); }

    public Details ownDetail(Actor actor, long id) { return details(owned(actor, id, false)); }

    @Transactional
    public Details create(Actor actor, Input input) {
        long id = batches.create(actor.workshop(), UUID.randomUUID(), input);
        if (id == 0) throw missing();
        return ownDetail(actor, id);
    }

    @Transactional
    public Details update(Actor actor, long id, Input input) {
        owned(actor, id, true);
        if (!batches.update(id, actor.workshop(), input)) throw missing();
        return ownDetail(actor, id);
    }

    @Transactional
    public Details milestones(Actor actor, long id, Milestones input) {
        owned(actor, id, true);
        batches.replaceMilestones(id, input.items());
        return ownDetail(actor, id);
    }

    @Transactional
    public Share share(Actor actor, long id) {
        Batch batch = owned(actor, id, true);
        if (batch.visibility() != Visibility.PRIVATE || batch.hidden()) {
            throw new ApiException(CONFLICT, "BATCH_NOT_SHAREABLE", "Chỉ tạo liên kết riêng cho lô riêng tư chưa bị ẩn.");
        }
        String token = UUID.randomUUID().toString() + UUID.randomUUID();
        batches.setTokenHash(id, BearerTokens.hash(token));
        return new Share(token);
    }

    @Transactional
    public void revoke(Actor actor, long id) {
        owned(actor, id, true);
        batches.setTokenHash(id, null);
    }

    @Transactional
    public void hide(long id) {
        if (!batches.hide(id)) throw missing();
    }

    public Details adminDetail(long id) {
        return details(batches.adminDetail(id).orElseThrow(BatchService::missing));
    }

    private Batch owned(Actor actor, long id, boolean lock) {
        return batches.owned(id, actor.workshop(), lock).orElseThrow(BatchService::missing);
    }

    private Details details(Batch batch) {
        return new Details(batch, batches.milestones(batch.id()),
                steps.findByProductIdOrderByPosition(batch.productId()).stream().map(ProcessStep::response).toList());
    }

    private static ApiException missing() {
        return new ApiException(NOT_FOUND, "BATCH_NOT_FOUND", "Không tìm thấy lô hàng hoặc sản phẩm.");
    }
}
