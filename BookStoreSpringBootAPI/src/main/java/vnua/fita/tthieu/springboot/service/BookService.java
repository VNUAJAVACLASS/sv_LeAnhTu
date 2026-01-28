package vnua.fita.tthieu.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vnua.fita.tthieu.springboot.entity.Book;
import vnua.fita.tthieu.springboot.repository.BookRepository;
import java.time.LocalDate;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    /**
     * Lấy danh sách sách có phân trang
     * @param pageable - Thông tin phân trang (page, size, sort)
     * @return Page<Book> - Kết quả phân trang
     */
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    /**
     * Lấy sách theo ID
     */
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sách không tồn tại"));
    }

    /**
     * Tạo sách mới
     */
    public Book save(Book book) {
        if (book.getNgayThem() == null) {
            book.setNgayThem(LocalDate.now());
        }
        return bookRepository.save(book);
    }

    /**
     * Cập nhật sách
     */
    public Book update(Long id, Book updatedBook, String updatedBy) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sách không tồn tại"));
        
        existing.setTenSach(updatedBook.getTenSach());
        existing.setMoTa(updatedBook.getMoTa());
        existing.setTacGia(updatedBook.getTacGia());
        existing.setGia(updatedBook.getGia());
        existing.setImagePath(updatedBook.getImagePath());
        existing.setSoLuong(updatedBook.getSoLuong());
        existing.setUpdatedBy(updatedBy);
        
        return bookRepository.save(existing);
    }

    /**
     * Xóa sách
     */
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Sách không tồn tại");
        }
        bookRepository.deleteById(id);
    }
}