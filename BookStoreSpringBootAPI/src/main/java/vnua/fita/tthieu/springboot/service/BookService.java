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
     * Lấy danh sách sách có phân trang và sắp xếp
     * @param pageable - Thông tin phân trang
     * @param sortType - Loại sắp xếp: all, priceAsc, priceDesc, bestSeller, newest
     * @return Page<Book>
     */
    public Page<Book> findAll(Pageable pageable, String sortType) {
        if (sortType == null || sortType.equals("all")) {
            return bookRepository.findAll(pageable);
        }
        
        switch (sortType) {
            case "priceAsc":
                return bookRepository.findAllByOrderByGiaAsc(pageable);
            case "priceDesc":
                return bookRepository.findAllByOrderByGiaDesc(pageable);
            case "bestSeller":
                return bookRepository.findAllOrderByBestSelling(pageable);
            case "newest":
                return bookRepository.findAllByOrderByNgayThemDesc(pageable);
            default:
                return bookRepository.findAll(pageable);
        }
    }

    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sách không tồn tại"));
    }

    public Book save(Book book) {
        if (book.getNgayThem() == null) {
            book.setNgayThem(LocalDate.now());
        }
        return bookRepository.save(book);
    }

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

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Sách không tồn tại");
        }
        bookRepository.deleteById(id);
    }
}