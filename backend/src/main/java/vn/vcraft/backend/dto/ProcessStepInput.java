package vn.vcraft.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProcessStepInput(
        @NotBlank(message = "Vui lòng nhập tên công đoạn.")
        @Size(max = 160, message = "Tên công đoạn tối đa 160 ký tự.") String name,
        @Size(max = 10000, message = "Mô tả tối đa 10.000 ký tự.") String description
) {}
