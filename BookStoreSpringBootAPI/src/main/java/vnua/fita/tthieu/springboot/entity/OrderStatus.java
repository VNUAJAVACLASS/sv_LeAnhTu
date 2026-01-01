package vnua.fita.tthieu.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== USER =====
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== INFO =====
    @Column(name = "so_dien_thoai_user")
    private String soDienThoaiUser;

    @Column(name = "ngay_dat")
    private LocalDate ngayDat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "tong_gia_tien")
    private Double tongGiaTien;

    // ===== RELATION: 1 ORDER - N BOOK =====
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<BookOrder> bookOrders = new ArrayList<>();

    // ===== HELPER =====
    public void addBookOrder(BookOrder bo) {
        bookOrders.add(bo);
        bo.setOrder(this);
    }

    public void removeBookOrder(BookOrder bo) {
        bookOrders.remove(bo);
        bo.setOrder(null);
    }

    // ===== GETTERS & SETTERS =====
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSoDienThoaiUser() {
        return soDienThoaiUser;
    }

    public void setSoDienThoaiUser(String soDienThoaiUser) {
        this.soDienThoaiUser = soDienThoaiUser;
    }

    public LocalDate getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDate ngayDat) {
        this.ngayDat = ngayDat;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }

    public Double getTongGiaTien() {
        return tongGiaTien;
    }

    public void setTongGiaTien(Double tongGiaTien) {
        this.tongGiaTien = tongGiaTien;
    }

    public List<BookOrder> getBookOrders() {
        return bookOrders;
    }

    public void setBookOrders(List<BookOrder> bookOrders) {
        this.bookOrders = bookOrders;
    }
}