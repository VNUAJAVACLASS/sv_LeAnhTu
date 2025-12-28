package vnua.fita.tthieu.springboot.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ko được trùng, ko được để trống trường này
    @Column(unique = true, nullable = false) 
    private String username;  
    
    private String gmail;
    
    // Dùng String để tránh mất số 0 đầu
    private String soDienThoai;
    
    @Column(nullable = false)
    private String password;
    
    private String diaChi;
    
    // Quan hệ nhiều nhiều giữa 2 entity User - Role, bảng user_roles cũng được tự động sinh
    // Một User có thể có nhiều Role, và một Role có thể thuộc về nhiều User.
    // EAGER nghĩa là khi bạn load User, JPA sẽ tự động load toàn bộ danh sách Role đi kèm.
    // (Ngược lại, nếu dùng LAZY, danh sách Role chỉ được load khi ta thực sự truy cập thuộc tính roles.)
    // @JoinTable(...) là annotation mô tả bảng trung gian (user_roles) trong cơ sở dữ liệu dùng để lưu 
    // mối quan hệ nhiều-nhiều.
    // joinColumns: cột khóa ngoại trỏ về entity hiện tại (ở đây là User)
    // inverseJoinColumns: cột khóa ngoại trỏ về entity bên kia của mối quan hệ (ở đây là Role)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles; // danh sách các quyền của User, dùng Set để tránh trùng lặp
	
    // getter / setter
    public Long getId() {
		return id;
	}
    
    // Không cung cấp setId() vì id được sinh tự động ko cho sửa
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
	
	public String getSoDienThoai() {
		return soDienThoai;
	}
	
	public void setSoDienThoai(String soDienThoai) {
		this.soDienThoai = soDienThoai;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getDiaChi() {
		return diaChi;
	}
	
	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}
	
	// ===== getter / setter cho roles =====
	public Set<Role> getRoles() {
		return roles;
	}
	
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	//Supper admin
	@Column(nullable = false)
	private Boolean superAdmin = false;

	public Boolean getSuperAdmin() {
	    return superAdmin;
	}

	public void setSuperAdmin(Boolean superAdmin) {
	    this.superAdmin = superAdmin;
	}

}
