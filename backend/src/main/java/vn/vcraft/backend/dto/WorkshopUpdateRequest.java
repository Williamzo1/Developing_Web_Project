package vn.vcraft.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkshopUpdateRequest(
        @NotBlank(message = "Vui lòng nhập tên xưởng.")
        @Size(max = 160, message = "Tên xưởng tối đa 160 ký tự.") String name
) {
    public WorkshopUpdateRequest {
        if (name != null) name = name.strip();
    }
}
