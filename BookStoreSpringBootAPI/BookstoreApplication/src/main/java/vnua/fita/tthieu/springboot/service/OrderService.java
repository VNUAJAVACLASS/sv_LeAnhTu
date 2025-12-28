package vnua.fita.tthieu.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vnua.fita.tthieu.springboot.entity.*;
import vnua.fita.tthieu.springboot.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    /**
     * Tạo đơn hàng mới
     */
    @Transactional
    public OrderStatus createOrder(Long userId, Long bookId, Integer soLuong) {
        // ===== VALIDATE INPUT =====
        if (soLuong == null || soLuong <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        // Lấy user từ DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));

        // Lấy sách từ DB
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Sách không tồn tại với ID: " + bookId));

        // Kiểm tra tồn kho
        if (book.getSoLuong() < soLuong) {
            throw new RuntimeException("Không đủ sách trong kho. Còn lại: " + book.getSoLuong());
        }

        // ===== TẠO ORDERSTATUS =====
        OrderStatus order = new OrderStatus();
        order.setUser(user);
        order.setBook(book);  // SET BOOK (QUAN TRỌNG!)
        order.setSoDienThoaiUser(user.getSoDienThoai());
        order.setTrangThai(1); // 1 = CHỜ XÁC NHẬN
        
        // SET CÁC TRƯỜNG BỊ THIẾU
        order.setTenSach(book.getTenSach());
        order.setSoLuong(soLuong);
        order.setTongGiaTien(book.getGia() * soLuong);

        // ===== TẠO BOOKORDER =====
        BookOrder bookOrder = new BookOrder();
        bookOrder.setOrder(order);
        bookOrder.setBook(book);
        bookOrder.setSoLuong(soLuong);
        bookOrder.setGiaTaiThoiDiem(book.getGia());

        // Thêm BookOrder vào OrderStatus
        order.addBookOrder(bookOrder);

        // ===== TRỪ TỒN KHO =====
        book.setSoLuong(book.getSoLuong() - soLuong);
        bookRepository.save(book);

        // ===== LƯU VÀO ORDER_HISTORY =====
        OrderHistory history = new OrderHistory(
            user.getId(),
            user.getUsername(),
            user.getGmail(),
            user.getSoDienThoai(),
            book.getId(),
            book.getTenSach(),
            soLuong,
            book.getGia()
        );
        orderHistoryRepository.save(history);

        // ===== LƯU ORDERSTATUS =====
        return orderStatusRepository.save(order);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @Transactional
    public OrderStatus updateOrderStatus(Long orderId, Integer trangThai) {
        // Validate trạng thái
        if (trangThai == null || trangThai < 1 || trangThai > 6) {
            throw new RuntimeException("Trạng thái không hợp lệ. Phải từ 1-6");
        }

        // Tìm đơn hàng
        OrderStatus order = orderStatusRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại với ID: " + orderId));

        // Kiểm tra logic chuyển trạng thái
        Integer currentStatus = order.getTrangThai();
        
        // Không cho phép cập nhật nếu đơn đã hủy hoặc đã trả hàng
        if (currentStatus == 5 || currentStatus == 6) {
            throw new RuntimeException("Không thể cập nhật đơn hàng đã hủy hoặc đã trả hàng");
        }

        // Nếu chuyển sang trạng thái "ĐÃ GIAO" (4), cập nhật ngày nhận trong order_history
        if (trangThai == 4) {
            updateOrderHistoryDeliveryDate(order);
        }

        // Nếu HỦY đơn hàng (5), hoàn lại số lượng sách
        if (trangThai == 5) {
            returnBookStock(order);
        }

        // Cập nhật trạng thái
        order.setTrangThai(trangThai);
        
        return orderStatusRepository.save(order);
    }

    /**
     * Cập nhật ngày nhận trong order_history khi đơn được giao
     */
    private void updateOrderHistoryDeliveryDate(OrderStatus order) {
        List<OrderHistory> histories = orderHistoryRepository.findByUserId(order.getUser().getId());
        
        for (OrderHistory history : histories) {
            // Tìm history khớp với order này (cùng user, book, ngày đặt)
            if (history.getBookId().equals(order.getBook().getId()) 
                && history.getNgayDat().equals(order.getNgayDat())
                && history.getNgayNhan() == null) {
                
                history.setNgayNhan(LocalDate.now());
                orderHistoryRepository.save(history);
                break; // Chỉ cập nhật 1 record
            }
        }
    }

    /**
     * Hoàn lại số lượng sách khi hủy đơn
     */
    private void returnBookStock(OrderStatus order) {
        Book book = order.getBook();
        book.setSoLuong(book.getSoLuong() + order.getSoLuong());
        bookRepository.save(book);
    }

    /**
     * Lấy tất cả đơn hàng
     */
    public List<OrderStatus> getAllOrders() {
        return orderStatusRepository.findAll();
    }

    /**
     * Lấy đơn hàng theo user
     */
    public List<OrderStatus> getOrdersByUser(Long userId) {
        return orderStatusRepository.findByUserId(userId);
    }

    /**
     * Lấy đơn hàng theo ID
     */
    public OrderStatus getOrderById(Long orderId) {
        return orderStatusRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại với ID: " + orderId));
    }

    /**
     * Lấy đơn hàng theo trạng thái
     */
    public List<OrderStatus> getOrdersByStatus(Integer trangThai) {
        return orderStatusRepository.findByTrangThai(trangThai);
    }

    /**
     * Lấy tất cả lịch sử đơn hàng
     */
    public List<OrderHistory> getAllOrderHistory() {
        return orderHistoryRepository.findAll();
    }

    /**
     * Lấy lịch sử đơn hàng theo user
     */
    public List<OrderHistory> getOrderHistoryByUserId(Long userId) {
        return orderHistoryRepository.findByUserId(userId);
    }

    /**
     * Xóa đơn hàng (soft delete bằng cách chuyển sang trạng thái HỦY)
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        updateOrderStatus(orderId, 5); // 5 = ĐÃ HỦY
    }
}