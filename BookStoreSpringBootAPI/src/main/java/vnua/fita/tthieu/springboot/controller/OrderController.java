package vnua.fita.tthieu.springboot.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vnua.fita.tthieu.springboot.dto.CreateOrderRequest;
import vnua.fita.tthieu.springboot.dto.OrderDetailResponse;
import vnua.fita.tthieu.springboot.dto.OrderHistoryDetailResponse;
import vnua.fita.tthieu.springboot.dto.OrderItemRequest;
import vnua.fita.tthieu.springboot.entity.OrderHistory;
import vnua.fita.tthieu.springboot.entity.OrderStatus;
import vnua.fita.tthieu.springboot.entity.User;
import vnua.fita.tthieu.springboot.service.OrderService;
/**
 * Controller quản lý đơn hàng
 * 
 * Chức năng:
 * - Lấy đơn hàng (tất cả / theo user / theo trạng thái)
 * - Lấy lịch sử đơn hàng (tất cả / theo user)
 * - Tạo đơn hàng
 * - Cập nhật trạng thái đơn hàng (admin)
 * - Hủy đơn hàng (admin)
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Lấy tất cả đơn hàng (chỉ admin)
     * GET /api/orders
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<OrderStatus> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy đơn hàng theo ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderStatus order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy đơn hàng của một user
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        try {
            List<OrderStatus> orders = orderService.getOrdersByUser(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy đơn hàng theo trạng thái (chỉ admin)
     * GET /api/orders/status/{trangThai}
     */
    @GetMapping("/status/{trangThai}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable Integer trangThai) {
        try {
            List<OrderStatus> orders = orderService.getOrdersByStatus(trangThai);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả lịch sử đơn hàng (chỉ admin)
     * GET /api/orders/history
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAllOrderHistory() {
        try {
            List<OrderHistory> histories = orderService.getAllOrderHistory();
            return ResponseEntity.ok(histories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử đơn hàng theo userId
     * GET /api/orders/history/user/{userId}
     */
    @GetMapping("/history/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getOrderHistoryByUserId(@PathVariable Long userId) {
        try {
            List<OrderHistory> histories = orderService.getOrderHistoryByUserId(userId);
            return ResponseEntity.ok(histories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Tạo đơn hàng mới
     * POST /api/orders
     * Body: {"userId": 1, "bookId": 1, "soLuong": 2}
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest orderData) {
        try {
            Long userId = orderData.getUserId();
            List<OrderItemRequest> items = orderData.getItems();

            OrderStatus order = orderService.createOrder(userId, items);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }



    /**
     * Cập nhật trạng thái đơn hàng (chỉ admin)
     * PATCH /api/orders/{id}/status
     * Body: {"trangThai": 2}
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> statusData) {
        try {
            Integer newStatus = statusData.get("trangThai");
            OrderStatus order = orderService.updateOrderStatus(id, newStatus);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Hủy đơn hàng (chỉ admin)
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok("Đã hủy đơn hàng thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Lấy đơn hàng ĐANG XỬ LÝ theo userId (trạng thái 1-4)
     * GET /api/orders/processing/{userId}
     */
    @GetMapping("/processing/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getProcessingOrders(@PathVariable Long userId) {
        try {
            List<OrderStatus> orders = orderService.getProcessingOrdersByUser(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy LỊCH SỬ đơn hàng theo userId
     * GET /api/orders/history/{userId}
     */
    @GetMapping("/history/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getOrderHistory(@PathVariable Long userId) {
        try {
            List<OrderHistory> histories = orderService.getOrderHistoryByUserId(userId);
            return ResponseEntity.ok(histories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy chi tiết đơn hàng (bao gồm tất cả sách)
     * GET /api/orders/{id}/details
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        try {
            OrderDetailResponse response = orderService.getOrderDetail(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}

/*
 * ===== DANH SÁCH API ENDPOINTS =====
 * 
 * GET    /api/orders                       - Lấy tất cả đơn hàng (Admin only)
 * GET    /api/orders/{id}                  - Lấy đơn hàng theo ID
 * GET    /api/orders/user/{userId}         - Lấy đơn hàng theo user
 * GET    /api/orders/status/{trangThai}    - Lấy đơn hàng theo trạng thái (Admin only)
 * GET    /api/orders/history               - Lấy tất cả lịch sử (Admin only)
 * GET    /api/orders/history/user/{userId} - Lấy lịch sử theo user
 * POST   /api/orders                       - Tạo đơn hàng mới
 * PATCH  /api/orders/{id}/status           - Cập nhật trạng thái (Admin only)
 * DELETE /api/orders/{id}                  - Hủy đơn hàng (Admin only)
 * GET    /history/{id}/detail           - Lấy đơn hàng đã lưu vào lịch sur chi tiết theo id (all role)
 * 
 * ===== TRẠNG THÁI ĐƠN HÀNG =====
 * 1 = CHO_XAC_NHAN   - Chờ xác nhận
 * 2 = DA_XAC_NHAN    - Đã xác nhận
 * 3 = DANG_GIAO      - Đang giao
 * 4 = DA_GIAO        - Đã giao (tự động cập nhật ngày nhận)
 * 5 = DA_HUY         - Đã hủy (tự động hoàn lại hàng)
 * 6 = DA_TRA_HANG    - Đã trả hàng
 */
