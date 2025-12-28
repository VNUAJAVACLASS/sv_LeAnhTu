package vnua.fita.tthieu.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vnua.fita.tthieu.springboot.entity.Book;
import vnua.fita.tthieu.springboot.repository.BookRepository;
import java.time.LocalDate;
import java.util.List;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    
    // Lấy tất cả book (public)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }
    
    // Lấy theo ID (nếu cần)
    // Trong SpringBoot có cơ chế xử lý ngoại lệ toàn cục, cá exception đẩy ra ở RestController
    // sẽ được bắt xử lý chuyển đổi thành Http response trả lại Client mà không đẩy ra JVM như 
    // chương trình Java thuần làm ngắt chương trình.
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book không tồn tại"));
    }
    
    // Tạo mới book
    public Book save(Book book) {
        // Tự động set ngày thêm nếu chưa có
        if (book.getNgayThem() == null) {
            book.setNgayThem(LocalDate.now());
        }
        return bookRepository.save(book);
    }
    
    // Cập nhật (chỉ admin)
    public Book update(Long id, Book updatedBook, String updatedBy) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book không tồn tại"));
        existing.setTenSach(updatedBook.getTenSach());
        existing.setMoTa(updatedBook.getMoTa());
        existing.setTacGia(updatedBook.getTacGia());
        existing.setGia(updatedBook.getGia());
        existing.setImagePath(updatedBook.getImagePath());
        existing.setSoLuong(updatedBook.getSoLuong());
        existing.setUpdatedBy(updatedBy); // account admin nào thực hiện update
        return bookRepository.save(existing);
    }
    
    // Xóa (chỉ admin)
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book không tồn tại");
        }
        bookRepository.deleteById(id);
    }
}