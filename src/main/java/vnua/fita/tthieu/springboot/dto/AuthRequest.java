package vnua.fita.tthieu.springboot.dto;

// Các lớp đóng vai trò DTO (data transfer object)
// Dùng để đóng gói dữ liệu từ request do client gửi sang
// Trong minh họa đơn giản có thể ko cần dùng lớp DTO này
// Mà sử dụng chung lớp Entity 
// Tuy nhiên ta nên tách biệt giữa DTO và Entity để rõ ràng hơn
// nhất là khi số thuộc tính trong DTO cần đến ít hơn so với trong Entity
public class AuthRequest {
	private String username;
    private String password;
    
    // getters / setters
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
    
    
}
