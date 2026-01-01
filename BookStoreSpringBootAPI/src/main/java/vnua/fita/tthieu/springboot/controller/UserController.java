package vnua.fita.tthieu.springboot.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import vnua.fita.tthieu.springboot.entity.Role;
import vnua.fita.tthieu.springboot.entity.User;
import vnua.fita.tthieu.springboot.repository.RoleRepository;
import vnua.fita.tthieu.springboot.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    // Lấy tất cả user (chỉ admin)
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
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
    // Request body: ["ROLE_USER", "ROLE_ADMIN"]
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
}
