package vnua.fita.tthieu.springboot.controller;

import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
	public User register(@RequestBody RegisterRequest req) {
		// kiểm tra user tồn tại
		if (userRepo.findByUsername(req.getUsername()).isPresent()) {
			throw new RuntimeException("Username đã tồn tại");
		}
		User user = new User();
		user.setUsername(req.getUsername());
		user.setPassword(passwordEncoder.encode(req.getPassword()));

		// gán role mặc định (ROLE_USER)
		Role roleUser = roleRepo.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Role không tồn tại"));
		user.setRoles(Set.of(roleUser));

		return userRepo.save(user);
	}
}
