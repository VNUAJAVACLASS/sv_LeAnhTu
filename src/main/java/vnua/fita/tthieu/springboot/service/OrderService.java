package vnua.fita.tthieu.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vnua.fita.tthieu.springboot.dto.OrderHistoryDetailResponse;
import vnua.fita.tthieu.springboot.dto.OrderItemRequest;
import vnua.fita.tthieu.springboot.entity.*;
import vnua.fita.tthieu.springboot.repository.*;

import java.time.LocalDate;
import java.util.List;
import vnua.fita.tthieu.springboot.dto.OrderDetailResponse;
import vnua.fita.tthieu.springboot.dto.OrderDetailResponse.BookInOrder;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderBookRepository orderStatusRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    /**
     * ✅ Tạo đơn hàng mới - FIXED
     * Xử lý đơn hàng có nhiều sách
     */
    @Transactional
    public OrderStatus createOrder(Long userId, List<OrderItemRequest> items) {

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Danh sách sách rỗng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        OrderStatus order = new OrderStatus();
        order.setUser(user);
        order.setSoDienThoaiUser(user.getSoDienThoai());
        order.setNgayDat(LocalDate.now());
        order.setTrangThai(1); // CHỜ XÁC NHẬN

        double tongTien = 0;

        for (OrderItemRequest item : items) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new RuntimeException("Sách không tồn tại"));

            if (item.getSoLuong() <= 0 || book.getSoLuong() < item.getSoLuong()) {
                throw new RuntimeException("Số lượng không hợp lệ: " + book.getTenSach());
            }

            BookOrder bo = new BookOrder();
            bo.setBook(book);
            bo.setSoLuong(item.getSoLuong());
            bo.setGiaTaiThoiDiem(book.getGia());

            order.addBookOrder(bo);

            tongTien += book.getGia() * item.getSoLuong();

            // Trừ kho
            book.setSoLuong(book.getSoLuong() - item.getSoLuong());
            bookRepository.save(book);
        }

        order.setTongGiaTien(tongTien);

        return orderStatusRepository.save(order);
    }


    /**
     * ✅ Cập nhật trạng thái đơn hàng - FIXED
     * Khi chuyển sang "ĐÃ GIAO" (4) -> Lưu vào order_history
     */
    @Transactional
    public OrderStatus updateOrderStatus(Long orderId, Integer trangThai) {
        // Validate trạng thái
        if (trangThai == null || trangThai < 1 || trangThai > 6) {
            throw new RuntimeException("Trạng thái không hợp lệ. Phải từ 1-6");
        }

        OrderStatus order = orderStatusRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        Integer currentStatus = order.getTrangThai();
        
        // Không cho phép cập nhật nếu đã hủy hoặc đã trả hàng
        if (currentStatus == 5 || currentStatus == 6) {
            throw new RuntimeException("Không thể cập nhật đơn hàng đã hủy hoặc đã trả hàng");
        }

        // ✅ Nếu chuyển sang "ĐÃ GIAO" (4) -> Lưu vào order_history
        if (trangThai == 4 && currentStatus != 4) {
            saveToOrderHistory(order);
        }

        // ✅ Nếu HỦY đơn (5) -> Hoàn lại số lượng sách
        if (trangThai == 5) {
            returnBookStock(order);
        }

        order.setTrangThai(trangThai);
        return orderStatusRepository.save(order);
    }

    /**
     * ✅ Lưu đơn hàng vào order_history khi giao thành công
     */
    private void saveToOrderHistory(OrderStatus order) {
        User user = order.getUser();
        
        // Lưu tất cả sách trong đơn vào history
        for (BookOrder bo : order.getBookOrders()) {
            OrderHistory history = new OrderHistory();
            history.setUserId(user.getId());
            history.setUsername(user.getUsername());
            history.setGmail(user.getGmail());
            history.setPhone(user.getSoDienThoai());
            history.setBookId(bo.getBook().getId());
            history.setTenSach(bo.getBook().getTenSach());
            history.setSoLuong(bo.getSoLuong());
            history.setPriceAtOrder(bo.getGiaTaiThoiDiem());
            history.setNgayDat(order.getNgayDat());
            history.setNgayNhan(LocalDate.now());
            
            orderHistoryRepository.save(history);
        }
    }

    /**
     * Hoàn lại số lượng sách khi hủy đơn
     */
    private void returnBookStock(OrderStatus order) {
        for (BookOrder bo : order.getBookOrders()) {
            Book book = bo.getBook();
            book.setSoLuong(book.getSoLuong() + bo.getSoLuong());
            bookRepository.save(book);
        }
    }

    /**
     * Lấy tất cả đơn hàng
     */
    public List<OrderStatus> getAllOrders() {
        return orderStatusRepository.findAll();
    }

    /**
     * ✅ Lấy đơn hàng theo user - FIXED
     */
    public List<OrderStatus> getOrdersByUser(Long userId) {
        return orderStatusRepository.findByUserId(userId);
    }

    /**
     * Lấy đơn hàng theo ID
     */
    public OrderStatus getOrderById(Long orderId) {
        return orderStatusRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
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
     * Xóa đơn hàng (chuyển sang trạng thái HỦY)
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        updateOrderStatus(orderId, 5);
    }
    
    /**
     * Lấy đơn hàng ĐANG XỬ LÝ của user (trạng thái 1-4)
     */
    public List<OrderStatus> getProcessingOrdersByUser(Long userId) {
        List<OrderStatus> allOrders = orderStatusRepository.findByUserId(userId);
        return allOrders.stream()
                .filter(order -> order.getTrangThai() >= 1 && order.getTrangThai() <= 4)
                .collect(Collectors.toList());
    }
    
    /**
     * ✅ Lấy chi tiết đơn hàng - FIXED
     */
    public OrderDetailResponse getOrderDetail(Long orderId) {
        OrderStatus order = orderStatusRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(order.getId());
        response.setNgayDat(order.getNgayDat());
        response.setTrangThai(order.getTrangThai());
        response.setTongTien(order.getTongGiaTien());
        response.setUserId(order.getUser().getId());
        response.setUsername(order.getUser().getUsername());
        response.setSoDienThoai(order.getSoDienThoaiUser());
        
        // Lấy danh sách sách từ BookOrder
        List<BookInOrder> bookList = order.getBookOrders().stream()
                .map(bo -> new BookInOrder(
                        bo.getBook().getId(),
                        bo.getBook().getTenSach(),
                        bo.getSoLuong(),
                        bo.getGiaTaiThoiDiem()
                ))
                .collect(Collectors.toList());
        
        response.setBooks(bookList);
        return response;
    }
}