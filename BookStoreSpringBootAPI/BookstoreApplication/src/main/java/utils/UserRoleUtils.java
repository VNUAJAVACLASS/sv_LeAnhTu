package utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vnua.fita.tthieu.springboot.entity.Role;
import vnua.fita.tthieu.springboot.entity.User;
import vnua.fita.tthieu.springboot.repository.RoleRepository;
import vnua.fita.tthieu.springboot.repository.UserRepository;

/*
Utility class để gán role admin cho user id 7
Chỉ cần gọi một lần khi cần
*/
@Component
public class UserRoleUtils {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public void assignAdminToUser7() {
        Long userId = 7L;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User 7 không tồn tại"));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Role admin không tồn tại"));

        Set<Role> roles = user.getRoles();
        if (roles == null) {
            roles = new HashSet<>();
        }

        roles.add(adminRole);
        user.setRoles(roles);

        userRepository.save(user);

        System.out.println("User 7 đã được gán role admin!");
    }
}
