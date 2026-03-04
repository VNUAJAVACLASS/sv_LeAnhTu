package vnua.fita.tthieu.springboot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "danh_muc")
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_danh_muc", nullable = false, unique = true)
    private String tenDanhMuc;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "icon", length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String icon;

    @Column(name = "mau_sac", length = 100)
    private String mauSac;

    @Column(name = "thu_tu")
    private Integer thuTu;

    public DanhMuc() {}

    public Long getId() { return id; }
    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getMauSac() { return mauSac; }
    public void setMauSac(String mauSac) { this.mauSac = mauSac; }
    public Integer getThuTu() { return thuTu; }
    public void setThuTu(Integer thuTu) { this.thuTu = thuTu; }
}