package vn.vcraft.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.vcraft.backend.model.QuoteRequestEvent;

public interface QuoteEventRepository extends JpaRepository<QuoteRequestEvent, Long> {
  List<QuoteRequestEvent> findByRequestIdOrderByIdAsc(Long requestId);
}
