package vnua.fita.tthieu.springboot.repository;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vnua.fita.tthieu.springboot.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Tìm sách và sắp xếp theo ngày thêm (mới nhất)
    Page<Book> findAllByOrderByNgayThemDesc(Pageable pageable);
    
    // Tìm sách và sắp xếp theo giá tăng dần
    Page<Book> findAllByOrderByGiaAsc(Pageable pageable);
    
    // Tìm sách và sắp xếp theo giá giảm dần
    Page<Book> findAllByOrderByGiaDesc(Pageable pageable);
    
    // Tìm sách bán chạy (dựa vào số lượng đã bán trong OrderHistory)
    @Query("SELECT b FROM Book b " +
           "LEFT JOIN OrderHistory oh ON b.id = oh.bookId " +
           "GROUP BY b.id " +
           "ORDER BY COALESCE(SUM(oh.soLuong), 0) DESC")
    Page<Book> findAllOrderByBestSelling(Pageable pageable);
    
    //==============================
    //Danh mục
    Page<Book> findByDanhMucId(Long danhMucId, Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN OrderHistory oh ON b.id = oh.bookId " +
           "WHERE b.danhMuc.id = :danhMucId " +
           "GROUP BY b.id ORDER BY COALESCE(SUM(oh.soLuong), 0) DESC")
    Page<Book> findByDanhMucIdOrderByBestSelling(@Param("danhMucId") Long danhMucId, Pageable pageable);

    Page<Book> findByDanhMucIdOrderByGiaAsc(Long danhMucId, Pageable pageable);
    Page<Book> findByDanhMucIdOrderByGiaDesc(Long danhMucId, Pageable pageable);
    Page<Book> findByDanhMucIdOrderByNgayThemDesc(Long danhMucId, Pageable pageable);
    
}