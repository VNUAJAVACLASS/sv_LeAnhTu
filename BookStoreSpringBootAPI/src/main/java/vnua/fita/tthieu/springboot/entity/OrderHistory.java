package vnua.fita.tthieu.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

// Bảng order_history: lưu lịch sử đơn hàng
// Không ràng buộc với user để tránh mất dữ liệu khi xóa user
@Entity
@Table(name = "order_history")
public class OrderHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Không dùng @ManyToOne để tránh ràng buộc, chỉ lưu userId
    private Long userId;
    
    private String username;
    
    private String gmail;
    
    private String phone;
    
    // Không ràng buộc với book, chỉ lưu bookId
    private Long bookId;
    
    @Column(nullable = false)
    private String tenSach;
    
    @Column(nullable = false)
    private Integer soLuong;
    
    // Giá tại thời điểm đặt hàng
    @Column(nullable = false)
    private Double priceAtOrder;
    
    private LocalDate ngayDat;
    
    private LocalDate ngayNhan;
    
    // Constructors
    public OrderHistory() {}
    
    public OrderHistory(Long userId, String username, String gmail, String phone,
                       Long bookId, String tenSach, Integer soLuong, Double priceAtOrder) {
        this.userId = userId;
        this.username = username;
        this.gmail = gmail;
        this.phone = phone;
        this.bookId = bookId;
        this.tenSach = tenSach;
        this.soLuong = soLuong;
        this.priceAtOrder = priceAtOrder;
        this.ngayDat = LocalDate.now();
    }
    
    // Getters & Setters
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getGmail() {
        return gmail;
    }
    
    public void setGmail(String gmail) {
        this.gmail = gmail;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getTenSach() {
        return tenSach;
    }
    
    public void setTenSach(String tenSach) {
        this.tenSach = tenSach;
    }
    
    public Integer getSoLuong() {
        return soLuong;
    }
    
    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
    
    public Double getPriceAtOrder() {
        return priceAtOrder;
    }
    
    public void setPriceAtOrder(Double priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }
    
    public LocalDate getNgayDat() {
        return ngayDat;
    }
    
    public void setNgayDat(LocalDate ngayDat) {
        this.ngayDat = ngayDat;
    }
    
    public LocalDate getNgayNhan() { return ngayNhan; }
    
    public void setNgayNhan(LocalDate ngayNhan) { this.ngayNhan = ngayNhan; }
}