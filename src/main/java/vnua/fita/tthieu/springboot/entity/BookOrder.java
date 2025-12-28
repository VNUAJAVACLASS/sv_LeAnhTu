package vnua.fita.tthieu.springboot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_order")
public class BookOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ với OrderStatus
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderStatus order; 

    // Quan hệ với Book
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // Số lượng sách trong đơn hàng
    @Column(nullable = false)
    private Integer soLuong;

    // Giá tại thời điểm đặt (không bị ảnh hưởng khi admin sửa giá sách)
    @Column(nullable = false)
    private Double giaTaiThoiDiem;

    // ===== Constructors =====
    
    public BookOrder() {}

    public BookOrder(OrderStatus order, Book book, Integer soLuong, Double giaTaiThoiDiem) {
        this.order = order;
        this.book = book;
        this.soLuong = soLuong;
        this.giaTaiThoiDiem = giaTaiThoiDiem;
    }

    // ===== Getters & Setters =====
    
    public Long getId() {
        return id;
    }

    public OrderStatus getOrder() {
        return order;
    }

    public void setOrder(OrderStatus order) {
        this.order = order;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Double getGiaTaiThoiDiem() {
        return giaTaiThoiDiem;
    }

    public void setGiaTaiThoiDiem(Double giaTaiThoiDiem) {
        this.giaTaiThoiDiem = giaTaiThoiDiem;
    }

    // ===== Helper Methods =====
    
    /**
     * Tính tổng tiền của book order này
     */
    public Double getTongTien() {
        return giaTaiThoiDiem * soLuong;
    }
}