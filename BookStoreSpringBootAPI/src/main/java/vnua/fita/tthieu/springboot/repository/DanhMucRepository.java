package vnua.fita.tthieu.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnua.fita.tthieu.springboot.entity.DanhMuc;
import java.util.List;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    List<DanhMuc> findAllByOrderByThuTuAsc();
    boolean existsByTenDanhMuc(String tenDanhMuc);
}