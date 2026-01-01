package vnua.fita.tthieu.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnua.fita.tthieu.springboot.entity.Book;

// Spring tự động scan các class kế thừa từ JpaRepository
// Đưa ra cơ chế tạo bean BookRepository trong ApplicationContext ở runtime (thời điểm chạy khi cần đến)
// để tiêm vào nơi cần sử dụng như trong BookService
public interface BookRepository extends JpaRepository<Book, Long> {
}