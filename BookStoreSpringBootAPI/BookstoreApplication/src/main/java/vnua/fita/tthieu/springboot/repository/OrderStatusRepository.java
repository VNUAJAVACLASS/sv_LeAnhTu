package vnua.fita.tthieu.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnua.fita.tthieu.springboot.entity.OrderStatus;
import java.util.List;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
    // Tìm tất cả đơn hàng của một user
    List<OrderStatus> findByUserId(Long userId);
    
    // Tìm đơn hàng theo trạng thái
    List<OrderStatus> findByTrangThai(Integer trangThai);
    

    // Tìm theo nhiều trạng thái (phục vụ job 3 ngày)
    List<OrderStatus> findByTrangThaiIn(List<Integer> trangThaiList);
}