package vnua.fita.tthieu.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vnua.fita.tthieu.springboot.entity.OrderStatus;

import java.util.List;

/**
 * Repository cho OrderStatus (quản lý đơn hàng)
 * 
 * LƯU Ý: Vì OrderStatus có quan hệ @ManyToOne với User (không phải userId trực tiếp),
 * nên phải dùng "user.id" trong tên method thay vì "userId"
 */
@Repository
public interface OrderBookRepository extends JpaRepository<OrderStatus, Long> {
    
    /**
     * Tìm đơn hàng theo user.id (vì có quan hệ @ManyToOne với User)
     * Spring JPA sẽ tự động hiểu: user.id = userId
     */
    List<OrderStatus> findByUserId(Long userId);
    
    /**
     * Tìm đơn hàng theo trạng thái
     * @param trangThai 1=CHỜ_XÁC_NHẬN, 2=ĐÃ_XÁC_NHẬN, 3=ĐANG_GIAO, 4=ĐÃ_GIAO, 5=ĐÃ_HỦY, 6=ĐÃ_TRẢ_HÀNG
     */
    List<OrderStatus> findByTrangThai(Integer trangThai);
    
    /**
     * Tìm đơn hàng theo user.id và trạng thái
     */
    List<OrderStatus> findByUserIdAndTrangThai(Long userId, Integer trangThai);
    
    /**
     * Tìm đơn hàng theo user.id và trạng thái trong khoảng
     * Ví dụ: tìm đơn đang xử lý (trạng thái 1-4)
     */
    List<OrderStatus> findByUserIdAndTrangThaiBetween(Long userId, Integer minStatus, Integer maxStatus);
}