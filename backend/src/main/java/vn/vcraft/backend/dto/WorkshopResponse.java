package vn.vcraft.backend.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkshopResponse(long id, String name, String slug, Boolean approved, String username, Instant lockedAt) {
    public WorkshopResponse(long id, String name, String slug, Boolean approved, String username) {
        this(id, name, slug, approved, username, null);
    }
}
