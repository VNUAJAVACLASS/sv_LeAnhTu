package vnua.fita.tthieu.springboot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenSach;

    @Column(length = 1000)
    private String moTa;

    private String tacGia;

    @Column(nullable = false)
    private Double gia;

    private String imagePath;

    private LocalDate ngayThem;

    @Column(nullable = false)
    private Integer soLuong;

    // Người tạo
    private String createdBy;

    // Người sửa gần nhất
    private String updatedBy;

    // Constructors ko tham số
    public Book() {}

    // Constructor có tham số chỉ cần nếu Dev cần dùng riêng, JPA ko cần
    // Tuy nhiên JPA cần constructor ko tham số để gọi tạo đối tượng
    // Nếu đã định nghĩa constructor có tham số thì Java ko cấp constructor ko tham
    // số mặc định nữa do đó phải định nghĩa constructor không tham số đi kèm (như ở trên),
    // nếu ko JPA sẽ ko hoạt động tạo đối tượng đc
    // Nếu Dev ko cần dùng riêng, ko cần định nghĩa constructor nào hết, dùng mặc định của Java
    public Book(String tenSach, String moTa, String tacGia, Double gia, Integer soLuong) {
        this.tenSach = tenSach;
        this.moTa = moTa;
        this.tacGia = tacGia;
        this.gia = gia;
        this.soLuong = soLuong;
        this.ngayThem = LocalDate.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getTenSach() {
        return tenSach;
    }

    public void setTenSach(String tenSach) {
        this.tenSach = tenSach;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTacGia() {
        return tacGia;
    }

    public void setTacGia(String tacGia) {
        this.tacGia = tacGia;
    }

    public Double getGia() {
        return gia;
    }

    public void setGia(Double gia) {
        this.gia = gia;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDate getNgayThem() {
        return ngayThem;
    }

    public void setNgayThem(LocalDate ngayThem) {
        this.ngayThem = ngayThem;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}