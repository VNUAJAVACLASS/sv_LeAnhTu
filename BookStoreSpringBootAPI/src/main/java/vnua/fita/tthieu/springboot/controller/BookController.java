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
     * Lấy danh sách sách có phân trang và sắp xếp
     * GET /api/books?page=0&size=10&sort=priceAsc
     *
     * @param page - Số trang (bắt đầu từ 0)
     * @param size - Số sách mỗi trang (mặc định 10)
     * @param sort - Loại sắp xếp: all, priceAsc, priceDesc, bestSeller, newest
     * @return Page<Book> với thông tin phân trang
     */
    @GetMapping
    public ResponseEntity<?> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String sort
    ) {
        try {
            // Tạo Pageable với sắp xếp theo id giảm dần (mặc định)
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            // Lấy dữ liệu phân trang với loại sắp xếp
            Page<Book> bookPage = bookService.findAll(pageable, sort);

            // Tạo response với đầy đủ thông tin phân trang
            Map<String, Object> response = new HashMap<>();
            response.put("content", bookPage.getContent());
            response.put("currentPage", bookPage.getNumber());
            response.put("totalItems", bookPage.getTotalElements());
            response.put("totalPages", bookPage.getTotalPages());
            response.put("pageSize", bookPage.getSize());
            response.put("hasNext", bookPage.hasNext());
            response.put("hasPrevious", bookPage.hasPrevious());
            response.put("isFirst", bookPage.isFirst());
            response.put("isLast", bookPage.isLast());
            response.put("sortType", sort); // Trả về loại sắp xếp hiện tại

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        try {
            Book book = bookService.findById(id);
            return ResponseEntity.ok(book);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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