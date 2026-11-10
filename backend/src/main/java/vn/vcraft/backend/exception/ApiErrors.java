package vn.vcraft.backend.exception;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String,String>> status(ResponseStatusException ex) {
        int code=ex.getStatusCode().value();
        return ResponseEntity.status(code).body(Map.of("code","HTTP_"+code,
            "message",ex.getReason()==null?"Không thể thực hiện yêu cầu.":ex.getReason()));
    }
    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.bind.MissingRequestValueException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<Map<String,String>> malformed() {
        return ResponseEntity.badRequest().body(Map.of("code","INVALID_INPUT","message","Kiểm tra định dạng dữ liệu gửi lên."));
    }
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String,String>> uploadLimit() {
        return ResponseEntity.status(413).body(Map.of("code","UPLOAD_TOO_LARGE","message","Ảnh vượt giới hạn tải lên."));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String,String>> integrity() {
        return ResponseEntity.status(409).body(Map.of("code","DATA_CONFLICT","message","Dữ liệu trùng hoặc vi phạm ràng buộc."));
    }
}
