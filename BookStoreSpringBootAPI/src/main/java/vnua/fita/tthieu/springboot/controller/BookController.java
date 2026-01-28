package vnua.fita.tthieu.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import vnua.fita.tthieu.springboot.entity.Book;
import vnua.fita.tthieu.springboot.service.BookService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookController {
    
    @Autowired
    private BookService bookService;

    /**
     *  Lấy danh sách sách có phân trang
     * GET /api/books?page=0&size=10
     * 
     * @param page - Số trang (bắt đầu từ 0)
     * @param size - Số sách mỗi trang (mặc định 10)
     * @return Page<Book> với thông tin phân trang
     */
    @GetMapping
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            // Tạo Pageable với sắp xếp theo id giảm dần (sách mới nhất trước)
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            
            // Lấy dữ liệu phân trang
            Page<Book> bookPage = bookService.findAll(pageable);
            
            // Tạo response với đầy đủ thông tin phân trang
            Map<String, Object> response = new HashMap<>();
            response.put("content", bookPage.getContent());           // Danh sách sách
            response.put("currentPage", bookPage.getNumber());        // Trang hiện tại (0-indexed)
            response.put("totalItems", bookPage.getTotalElements());  // Tổng số sách
            response.put("totalPages", bookPage.getTotalPages());     // Tổng số trang
            response.put("pageSize", bookPage.getSize());             // Số sách mỗi trang
            response.put("hasNext", bookPage.hasNext());              // Có trang tiếp theo?
            response.put("hasPrevious", bookPage.hasPrevious());      // Có trang trước?
            response.put("isFirst", bookPage.isFirst());              // Trang đầu tiên?
            response.put("isLast", bookPage.isLast());                // Trang cuối cùng?
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin chi tiết sách theo ID
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.findById(id);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Tạo sách mới (chỉ admin)
     * POST /api/books
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createBook(@RequestBody Book book) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            book.setCreatedBy(username);
            
            Book savedBook = bookService.save(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Cập nhật sách (chỉ admin)
     * PATCH /api/books/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Book book = bookService.update(id, updatedBook, username);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Xóa sách (chỉ admin)
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.delete(id);
            return ResponseEntity.ok("Đã xóa sách thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}