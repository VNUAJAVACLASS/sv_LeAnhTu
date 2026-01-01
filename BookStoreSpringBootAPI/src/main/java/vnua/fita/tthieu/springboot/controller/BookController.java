package vnua.fita.tthieu.springboot.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import vnua.fita.tthieu.springboot.entity.Book;
import vnua.fita.tthieu.springboot.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {
    // Không sử dụng trực tiếp bean BookRepository nếu ngoài việc truy suất CSDL thuần túy
	// còn cần thêm một số xử lý logic -> nên dùng bean Service để code phân tách rõ ràng hơn
	@Autowired
    private BookService bookService;
    
    // Thông tin ds book được public
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }
    
    // Thông tin chi tiết book được public
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.findById(id);
    }
    
    // Kiểu dữ liệu trả về (Book) không sử dụng ResponseEntity có nghĩa chỉ trả lại phần response body
    // Các phần response header, status để SpringBoot tự xử lý dựa vào ngữ cảnh
    // role user thường chỉ được quyền tạo, không được quyền sửa, xóa
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public Book createBook(@RequestBody Book book) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        book.setCreatedBy(username);
        return bookService.save(book);
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Book updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        // Ghi vào CSDL: account admin nào thực hiện update
    	String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return bookService.update(id, updatedBook, username);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteBook(@PathVariable Long id) {
        bookService.delete(id);
    }
}