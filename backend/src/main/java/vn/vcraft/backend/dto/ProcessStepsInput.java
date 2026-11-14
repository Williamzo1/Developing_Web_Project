package vn.vcraft.backend.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProcessStepsInput(
        @NotNull(message = "Vui lòng gửi danh sách công đoạn.")
        @Size(max = 100, message = "Sản phẩm có tối đa 100 công đoạn.")
        List<@NotNull(message = "Công đoạn không được để trống.") @Valid ProcessStepInput> steps
) {}
