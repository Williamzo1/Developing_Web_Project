package vn.vcraft.backend.security;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import vn.vcraft.backend.dto.ApiErrorResponse;

@Component
public class SecurityErrorWriter {
    private final ObjectMapper json;

    public SecurityErrorWriter(ObjectMapper json) {
        this.json = json;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.writeValueAsString(
                ApiErrorResponse.of(status, code, message, request.getRequestURI())));
    }
}
