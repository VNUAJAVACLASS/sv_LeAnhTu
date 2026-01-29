package vnua.fita.tthieu.springboot.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import vnua.fita.tthieu.springboot.entity.Role;
import vnua.fita.tthieu.springboot.entity.User;
import vnua.fita.tthieu.springboot.repository.RoleRepository;
import vnua.fita.tthieu.springboot.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<User> userPage = userRepository.findAll(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", userPage.getContent());
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("pageSize", userPage.getSize());
            response.put("hasNext", userPage.hasNext());
            response.put("hasPrevious", userPage.hasPrevious());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // Lấy user theo ID (admin hoặc chính user đó)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN') or #id == principal.id")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

    // Xóa user (chỉ admin, không thể xóa Super Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public String deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (Boolean.TRUE.equals(user.getSuperAdmin())) {
            throw new RuntimeException("Không thể xóa Super Admin");
        }

        userRepository.deleteById(id);
        return "Đã xóa user id " + id;
    }

    // Cập nhật roles của user (chỉ admin, không thể gỡ role Super Admin)
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public User updateRoles(@PathVariable Long id, @RequestBody Set<String> roleNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra Super Admin
        if (Boolean.TRUE.equals(user.getSuperAdmin()) &&
                roleNames.stream().noneMatch(r -> r.equals("ROLE_SUPER_ADMIN"))) {
            throw new RuntimeException("Không thể gỡ role Super Admin");
        }

        Set<Role> newRoles = roleRepository.findAll().stream()
                .filter(r -> roleNames.contains(r.getName()))
                .collect(java.util.stream.Collectors.toSet());

        user.setRoles(newRoles);
        return userRepository.save(user);
    }

    // Trang cá nhân - Lấy thông tin user hiện tại
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        // Lấy username từ SecurityContext
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Tạo response DTO (không trả password)
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("gmail", user.getGmail());
        response.put("soDienThoai", user.getSoDienThoai());
        response.put("diaChi", user.getDiaChi());

        // Chuyển roles thành List<String>
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        response.put("roles", roles);

        return ResponseEntity.ok(response);
    }

    /**
     * API ĐỔI MẬT KHẨU
     * POST /api/users/change-password
     * Body: {"currentPassword": "...", "newPassword": "..."}
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            // Validate input
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mật khẩu hiện tại không được để trống");
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mật khẩu mới không được để trống");
            }

            if (newPassword.length() < 5 || newPassword.length() > 20) {
                return ResponseEntity.badRequest().body("Mật khẩu mới phải từ 5-20 ký tự");
            }

            // Lấy username từ SecurityContext
            String username = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng");
            }

            // Kiểm tra mật khẩu mới không trùng mật khẩu cũ
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body("Mật khẩu mới không được trùng mật khẩu cũ");
            }

            // Cập nhật mật khẩu mới
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * API CẬP NHẬT THÔNG TIN USER
     * PATCH /api/users/update-info
     * Body: {"gmail": "...", "soDienThoai": "...", "diaChi": "..."}
     */
    @PatchMapping("/update-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserInfo(@RequestBody Map<String, String> userInfo) {
        try {
            // Lấy username từ SecurityContext
            String username = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Cập nhật thông tin (chỉ cập nhật nếu có trong request)
            if (userInfo.containsKey("gmail")) {
                String gmail = userInfo.get("gmail");
                // Validate email nếu không rỗng
                if (gmail != null && !gmail.trim().isEmpty()) {
                    if (!isValidEmail(gmail)) {
                        return ResponseEntity.badRequest().body("Email không hợp lệ");
                    }
                }
                user.setGmail(gmail);
            }

            if (userInfo.containsKey("soDienThoai")) {
                String phone = userInfo.get("soDienThoai");
                // Validate số điện thoại nếu không rỗng
                if (phone != null && !phone.trim().isEmpty()) {
                    if (!isValidPhone(phone)) {
                        return ResponseEntity.badRequest().body("Số điện thoại không hợp lệ (10-11 số)");
                    }
                }
                user.setSoDienThoai(phone);
            }

            if (userInfo.containsKey("diaChi")) {
                user.setDiaChi(userInfo.get("diaChi"));
            }

            userRepository.save(user);

            return ResponseEntity.ok("Cập nhật thông tin thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Helper: Validate email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Cho phép để trống
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    /**
     * Helper: Validate số điện thoại
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Cho phép để trống
        }
        return phone.matches("^[0-9]{10,11}$");
    }
}