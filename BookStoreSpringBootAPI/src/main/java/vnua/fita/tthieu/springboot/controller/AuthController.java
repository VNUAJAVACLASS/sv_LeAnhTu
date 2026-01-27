package vnua.fita.tthieu.springboot.controller;

import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vnua.fita.tthieu.springboot.dto.AuthRequest;
import vnua.fita.tthieu.springboot.dto.RegisterRequest;
import vnua.fita.tthieu.springboot.entity.Role;
import vnua.fita.tthieu.springboot.entity.User;
import vnua.fita.tthieu.springboot.repository.RoleRepository;
import vnua.fita.tthieu.springboot.repository.UserRepository;
import vnua.fita.tthieu.springboot.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authManager; // Lớp tiện ích xác thực của Spring Security

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// UsernamePasswordAuthenticationToken là class thực thi interface Authentication trong Spring Security
	// Có thể chứa thông tin user (tài khoản, mật khẩu), trạng thái xác thực, danh sách quyền hạn
	// được tạo ra để làm đầu vào cho method authenticate()
	// Phương thức authenticate được gọi sẽ:
	// - Kiểm tra username/pass qua UserDetailsService và PasswordEncoder
	// - Trả về một đối tượng Authentication chứa các thông tin user cập nhật về trạng thái xác thực, ds quyền hạn
	@PostMapping("/login")
	public Map<String, Object> login(@RequestBody AuthRequest request) {
		Authentication authentication = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		// Lưu thông tin xác thực vào SecurityContextHolder (bộ nhớ bảo mật tạm thời của Spring cho request hiện tại)
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Sinh jwtToken dựa trên username
		String token = jwtService.generateToken(request.getUsername());

		// Lấy thông tin user và quyền từ Authentication
		UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // trả về Entity đã custom
		// getAuthorities() trả về danh sách quyền (ROLE_ADMIN, ROLE_USER) là các đối tượng GrantedAuthority
		// Chuyển thành List<String> để dễ xử lý
		List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		// Trả về response dạng HashMap, Spring sẽ tự chuyển thành dạng chuỗi JSON trả về client
		Map<String, Object> resp = new HashMap<>();
		resp.put("accessToken", token);
		resp.put("username", userDetails.getUsername());
		resp.put("roles", roles); // kiểu List<String>
		return resp;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
	    try {
	        // ===== VALIDATION =====
	        
	        // 1. Kiểm tra username
	        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
	            return ResponseEntity.badRequest().body("Tên đăng nhập không được để trống");
	        }
	        
	        if (req.getUsername().length() < 3 || req.getUsername().length() > 20) {
	            return ResponseEntity.badRequest().body("Tên đăng nhập phải từ 3-20 ký tự");
	        }

	        if (!req.getUsername().matches("^[a-zA-Z0-9_]+$")) {
	            return ResponseEntity.badRequest().body("Tên đăng nhập chỉ chứa chữ, số và dấu gạch dưới");
	        }

	        // Kiểm tra username đã tồn tại
	        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
	            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại");
	        }

	        // 2. Kiểm tra password
	        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
	            return ResponseEntity.badRequest().body("Mật khẩu không được để trống");
	        }

	        if (req.getPassword().length() < 5 || req.getPassword().length() > 20) {
	            return ResponseEntity.badRequest().body("Mật khẩu phải từ 5-20 ký tự");
	        }

	        // 3. Kiểm tra email
	        if (req.getGmail() == null || req.getGmail().trim().isEmpty()) {
	            return ResponseEntity.badRequest().body("Email không được để trống");
	        }

	        if (!isValidEmail(req.getGmail())) {
	            return ResponseEntity.badRequest().body("Email không hợp lệ");
	        }

	        // 4. Kiểm tra số điện thoại
	        if (req.getSoDienThoai() == null || req.getSoDienThoai().trim().isEmpty()) {
	            return ResponseEntity.badRequest().body("Số điện thoại không được để trống");
	        }

	        if (!isValidPhone(req.getSoDienThoai())) {
	            return ResponseEntity.badRequest().body("Số điện thoại phải có 10-11 chữ số");
	        }

	        // ===== TẠO USER =====
	        User user = new User();
	        user.setUsername(req.getUsername());
	        user.setPassword(passwordEncoder.encode(req.getPassword()));
	        
	        // ✅ LƯU EMAIL VÀ SỐ ĐIỆN THOẠI
	        user.setGmail(req.getGmail());
	        user.setSoDienThoai(req.getSoDienThoai());

	        // Gán role mặc định (ROLE_USER)
	        Role roleUser = roleRepo.findByName("ROLE_USER")
	                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
	        user.setRoles(Set.of(roleUser));

	        User savedUser = userRepo.save(user);

	        // Tạo response
	        Map<String, Object> response = new HashMap<>();
	        response.put("id", savedUser.getId());
	        response.put("username", savedUser.getUsername());
	        response.put("gmail", savedUser.getGmail());
	        response.put("soDienThoai", savedUser.getSoDienThoai());
	        response.put("message", "Đăng ký thành công");

	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
	    }
	}
	
	/**
	 * Helper: Validate email
	 */
	private boolean isValidEmail(String email) {
	    if (email == null || email.trim().isEmpty()) {
	        return false;
	    }
	    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
	    return email.matches(emailRegex);
	}

	/**
	 * Helper: Validate số điện thoại
	 */
	private boolean isValidPhone(String phone) {
	    if (phone == null || phone.trim().isEmpty()) {
	        return false;
	    }
	    return phone.matches("^[0-9]{10,11}$");
	}
}
