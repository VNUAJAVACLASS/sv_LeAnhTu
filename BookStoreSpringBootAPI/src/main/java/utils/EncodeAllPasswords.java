package utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import vnua.fita.tthieu.springboot.entity.User;
import vnua.fita.tthieu.springboot.repository.UserRepository;

@Component
public class EncodeAllPasswords implements CommandLineRunner {

	 @Autowired
	    private UserRepository userRepository;

	    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	    @Override
	    public void run(String... args) throws Exception {
	        Long userId = 1L; // user admin
	        User user = userRepository.findById(userId)
	                .orElseThrow(() -> new RuntimeException("User id=1 không tồn tại"));

	        // Kiểm tra xem password đã mã hóa chưa (thường bắt đầu bằng $2a$)
	        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
	            String encodedPassword = passwordEncoder.encode(user.getPassword());
	            user.setPassword(encodedPassword);
	            userRepository.save(user);
	            System.out.println("Password của user id=1 đã được mã hóa bằng BCrypt.");
	        } else {
	            System.out.println("Password của user id=1 đã được mã hóa trước đó, không cần thay đổi.");
	        }
	    }
}
