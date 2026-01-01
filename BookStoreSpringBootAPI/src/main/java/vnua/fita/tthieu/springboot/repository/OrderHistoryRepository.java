package vnua.fita.tthieu.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnua.fita.tthieu.springboot.entity.OrderHistory;
import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    // Tìm lịch sử đơn hàng theo userId
    List<OrderHistory> findByUserId(Long userId);
}