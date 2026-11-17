package vn.vcraft.backend.exception;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.vcraft.backend.dto.ApiErrorResponse;

@RestControllerAdvice
class ApiErrors {
  private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiErrorResponse> business(ApiException error, HttpServletRequest request) {
    return response(error.getStatus().value(), error.getCode(), error.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
    Map<String, String> fields = new TreeMap<>();
                    (left, right) -> left.compareTo(right) <= 0 ? left : right));
  }

  @ExceptionHandler(ConstraintViolationException.class)
    Map<String, String> fields = new TreeMap<>();
              String path = field.getPropertyPath().toString();
              String name = path.substring(path.lastIndexOf('.') + 1);
              fields.putIfAbsent(name, field.getMessage());
            });
  }

  @ExceptionHandler(ResponseStatusException.class)
    int status = error.getStatusCode().value();
  }

  @ExceptionHandler({
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.bind.MissingRequestValueException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ApiErrorResponse> malformed(Exception error, HttpServletRequest request) {
    return response(400, "INVALID_INPUT", "Định dạng dữ liệu không hợp lệ.", request);
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ApiErrorResponse> method(
    var builder = ResponseEntity.status(405);
  }

  @ExceptionHandler({
    org.springframework.web.servlet.resource.NoResourceFoundException.class,
    org.springframework.web.servlet.NoHandlerFoundException.class
  })
  ResponseEntity<ApiErrorResponse> notFound(Exception error, HttpServletRequest request) {
    return response(404, "NOT_FOUND", "Không tìm thấy đường dẫn.", request);
  }

  @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
  ResponseEntity<ApiErrorResponse> upload(Exception error, HttpServletRequest request) {
    return response(413, "UPLOAD_TOO_LARGE", "Ảnh vượt giới hạn tải lên.", request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiErrorResponse> integrity(Exception error, HttpServletRequest request) {
    return response(409, "DATA_CONFLICT", "Dữ liệu trùng hoặc vi phạm ràng buộc.", request);
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  ResponseEntity<ApiErrorResponse> forbidden(Exception error, HttpServletRequest request) {
    return response(403, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác.", request);
  }

  @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
  ResponseEntity<ApiErrorResponse> mediaType(Exception error, HttpServletRequest request) {
    return response(415, "UNSUPPORTED_MEDIA_TYPE", "Kiểu dữ liệu không được hỗ trợ.", request);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiErrorResponse> unexpected(Exception error, HttpServletRequest request) {
    log.error("Unexpected request failure on {}", request.getRequestURI(), error);
    return response(500, "INTERNAL_ERROR", "Có lỗi hệ thống, vui lòng thử lại.", request);
  }

  }

  private String code(int status) {
    return switch (status) {
      case 400 -> "INVALID_INPUT";
      case 401 -> "UNAUTHENTICATED";
      case 403 -> "FORBIDDEN";
      case 404 -> "NOT_FOUND";
      case 409 -> "DATA_CONFLICT";
      default -> "REQUEST_FAILED";
    };
  }
}
