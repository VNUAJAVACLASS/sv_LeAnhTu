package vnua.fita.tthieu.springboot.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO trả về chi tiết đơn hàng (1 đơn có nhiều sách)
 */
public class OrderDetailResponse {
    
    // Thông tin đơn hàng
    private Long orderId;
    private LocalDate ngayDat;
    private Integer trangThai;
    private String tenTrangThai;
    private Double tongTien;
    
    // Thông tin người đặt
    private Long userId;
    private String username;
    private String soDienThoai;
    
    // Danh sách sách trong đơn
    private List<BookInOrder> books;
    
    // Inner class: thông tin từng sách
    public static class BookInOrder {
        private Long bookId;
        private String tenSach;
        private Integer soLuong;
        private Double giaTaiThoiDiem;
        private Double thanhTien; // giaTaiThoiDiem * soLuong
        
        public BookInOrder() {}
        
        public BookInOrder(Long bookId, String tenSach, Integer soLuong, Double giaTaiThoiDiem) {
            this.bookId = bookId;
            this.tenSach = tenSach;
            this.soLuong = soLuong;
            this.giaTaiThoiDiem = giaTaiThoiDiem;
            this.thanhTien = giaTaiThoiDiem * soLuong;
        }
        
        // Getters & Setters
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        
        public String getTenSach() { return tenSach; }
        public void setTenSach(String tenSach) { this.tenSach = tenSach; }
        
        public Integer getSoLuong() { return soLuong; }
        public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
        
        public Double getGiaTaiThoiDiem() { return giaTaiThoiDiem; }
        public void setGiaTaiThoiDiem(Double giaTaiThoiDiem) { this.giaTaiThoiDiem = giaTaiThoiDiem; }
        
        public Double getThanhTien() { return thanhTien; }
        public void setThanhTien(Double thanhTien) { this.thanhTien = thanhTien; }
    }
    
    // Constructors
    public OrderDetailResponse() {}
    
    // Getters & Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public LocalDate getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDate ngayDat) { this.ngayDat = ngayDat; }
    
    public Integer getTrangThai() { return trangThai; }
    public void setTrangThai(Integer trangThai) { 
        this.trangThai = trangThai;
        this.tenTrangThai = getTrangThaiText(trangThai);
    }
    
    public String getTenTrangThai() { return tenTrangThai; }
    
    public Double getTongTien() { return tongTien; }
    public void setTongTien(Double tongTien) { this.tongTien = tongTien; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    
    public List<BookInOrder> getBooks() { return books; }
    public void setBooks(List<BookInOrder> books) { this.books = books; }
    
    // Helper method
    private String getTrangThaiText(Integer trangThai) {
        switch (trangThai) {
            case 1: return "Chờ xác nhận";
            case 2: return "Đã xác nhận";
            case 3: return "Đang giao";
            case 4: return "Đã giao";
            case 5: return "Đã hủy";
            case 6: return "Đã trả hàng";
            default: return "Không xác định";
        }
    }
}