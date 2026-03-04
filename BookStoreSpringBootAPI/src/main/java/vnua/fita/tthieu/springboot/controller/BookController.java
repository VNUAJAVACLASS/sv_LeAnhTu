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

    @Autowired
    private vnua.fita.tthieu.springboot.repository.DanhMucRepository danhMucRepository;

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
            @RequestParam(defaultValue = "all") String sort,
            @RequestParam(required = false) Long danhMucId
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<Book> bookPage = bookService.findAll(pageable, sort, danhMucId);

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
            response.put("sortType", sort);
            response.put("danhMucId", danhMucId);

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
    public ResponseEntity<?> createBook(@RequestBody Map<String, Object> bookData) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            Book book = new Book();
            book.setTenSach((String) bookData.get("tenSach"));
            book.setTacGia((String) bookData.get("tacGia"));
            book.setMoTa((String) bookData.get("moTa"));
            book.setImagePath((String) bookData.get("imagePath"));
            book.setCreatedBy(username);

            if (bookData.get("gia") != null)
                book.setGia(Double.parseDouble(bookData.get("gia").toString()));
            if (bookData.get("soLuong") != null)
                book.setSoLuong(Integer.parseInt(bookData.get("soLuong").toString()));

            // Xử lý danh mục
            if (bookData.get("danhMucId") != null) {
                Long danhMucId = Long.parseLong(bookData.get("danhMucId").toString());
                danhMucRepository.findById(danhMucId).ifPresent(book::setDanhMuc);
            } else if (bookData.get("tenDanhMucMoi") != null && !bookData.get("tenDanhMucMoi").toString().isBlank()) {
                String tenMoi = bookData.get("tenDanhMucMoi").toString().trim();
                vnua.fita.tthieu.springboot.entity.DanhMuc dm = danhMucRepository.findAll()
                    .stream().filter(d -> d.getTenDanhMuc().equalsIgnoreCase(tenMoi)).findFirst()
                    .orElseGet(() -> {
                        vnua.fita.tthieu.springboot.entity.DanhMuc newDm = new vnua.fita.tthieu.springboot.entity.DanhMuc();
                        newDm.setTenDanhMuc(tenMoi);
                        newDm.setIcon("📚");
                        newDm.setMauSac("#95a5a6");
                        newDm.setThuTu(99);
                        return danhMucRepository.save(newDm);
                    });
                book.setDanhMuc(dm);
            }

            Book saved = bookService.save(book);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Map<String, Object> bookData) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Book existing = bookService.findById(id);

            if (bookData.get("tenSach") != null) existing.setTenSach((String) bookData.get("tenSach"));
            if (bookData.get("tacGia") != null) existing.setTacGia((String) bookData.get("tacGia"));
            if (bookData.get("moTa") != null) existing.setMoTa((String) bookData.get("moTa"));
            if (bookData.get("imagePath") != null) existing.setImagePath((String) bookData.get("imagePath"));
            if (bookData.get("gia") != null) existing.setGia(Double.parseDouble(bookData.get("gia").toString()));
            if (bookData.get("soLuong") != null) existing.setSoLuong(Integer.parseInt(bookData.get("soLuong").toString()));
            existing.setUpdatedBy(username);

            if (bookData.get("danhMucId") != null) {
                Long danhMucId = Long.parseLong(bookData.get("danhMucId").toString());
                danhMucRepository.findById(danhMucId).ifPresent(existing::setDanhMuc);
            } else if (bookData.get("tenDanhMucMoi") != null && !bookData.get("tenDanhMucMoi").toString().isBlank()) {
                String tenMoi = bookData.get("tenDanhMucMoi").toString().trim();
                vnua.fita.tthieu.springboot.entity.DanhMuc dm = danhMucRepository.findAll()
                    .stream().filter(d -> d.getTenDanhMuc().equalsIgnoreCase(tenMoi)).findFirst()
                    .orElseGet(() -> {
                        vnua.fita.tthieu.springboot.entity.DanhMuc newDm = new vnua.fita.tthieu.springboot.entity.DanhMuc();
                        newDm.setTenDanhMuc(tenMoi);
                        newDm.setIcon("📚");
                        newDm.setMauSac("#95a5a6");
                        newDm.setThuTu(99);
                        return danhMucRepository.save(newDm);
                    });
                existing.setDanhMuc(dm);
            }

            return ResponseEntity.ok(bookService.save(existing));
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