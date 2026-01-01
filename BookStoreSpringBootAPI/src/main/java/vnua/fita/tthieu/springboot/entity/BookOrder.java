package vnua.fita.tthieu.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "book_order")
public class BookOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore // Tránh vòng lặp JSON
    @JoinColumn(name = "order_id", nullable = false)
    private OrderStatus order;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer soLuong; // Bắt buộc set khi tạo

    @Column(nullable = false)
    private Double giaTaiThoiDiem; // Giá tại thời điểm đặt

    // ===== Helper =====
    public Double getTongTien() {
        return soLuong * giaTaiThoiDiem;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public OrderStatus getOrder() { return order; }
    public void setOrder(OrderStatus order) { this.order = order; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public Double getGiaTaiThoiDiem() { return giaTaiThoiDiem; }
    public void setGiaTaiThoiDiem(Double giaTaiThoiDiem) { this.giaTaiThoiDiem = giaTaiThoiDiem; }
}
