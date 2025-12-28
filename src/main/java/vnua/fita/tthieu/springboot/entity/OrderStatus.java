package vnua.fita.tthieu.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người đặt
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // THÊM QUAN HỆ TRỰC TIẾP VỚI BOOK (để match với DB schema)
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private String soDienThoaiUser;

    @Column(nullable = false)
    private LocalDate ngayDat;

    // Trạng thái đơn
    @Column(nullable = false)
    private Integer trangThai;

    // CÁC TRƯỜNG BỊ THIẾU
    @Column(nullable = false)
    private String tenSach;

    @Column(nullable = false)
    private Integer soLuong;

    @Column(nullable = false)
    private Double tongGiaTien;

    // Quan hệ với BookOrder (một đơn có nhiều sách)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<BookOrder> bookOrders = new HashSet<>();

    // Constructors
    public OrderStatus() {
        this.ngayDat = LocalDate.now();
    }

    public OrderStatus(User user, Book book, String soDienThoaiUser, Integer trangThai) {
        this.user = user;
        this.book = book;
        this.soDienThoaiUser = soDienThoaiUser;
        this.trangThai = trangThai;
        this.ngayDat = LocalDate.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
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

    public Double getTongGiaTien() {
        return tongGiaTien;
    }

    public void setTongGiaTien(Double tongGiaTien) {
        this.tongGiaTien = tongGiaTien;
    }

    public Set<BookOrder> getBookOrders() {
        return bookOrders;
    }

    public void setBookOrders(Set<BookOrder> bookOrders) {
        this.bookOrders = bookOrders;
    }

    // Thêm tiện ích để thêm sách vào đơn
    public void addBookOrder(BookOrder bookOrder) {
        bookOrders.add(bookOrder);
        bookOrder.setOrder(this);
    }
}