package vn.vcraft.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkshopResponse(long id, String name, String slug, Boolean approved, String username) {}
