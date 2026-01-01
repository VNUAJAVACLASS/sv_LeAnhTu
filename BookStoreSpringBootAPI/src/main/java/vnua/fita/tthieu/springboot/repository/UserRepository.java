package vnua.fita.tthieu.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnua.fita.tthieu.springboot.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	// khuôn mẫu JPA: findBy + <tên trường username trong lớp entity User>
	// đúng khuôn JPA sẽ hỗ trợ tự sinh code
	Optional<User> findByUsername(String username); 
}
