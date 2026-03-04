package vnua.fita.tthieu.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vnua.fita.tthieu.springboot.entity.DanhMuc;
import vnua.fita.tthieu.springboot.repository.DanhMucRepository;
import java.util.List;

@RestController
@RequestMapping("/api/danh-muc")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DanhMucController {

    @Autowired
    private DanhMucRepository danhMucRepository;

    // GET tất cả danh mục (public)
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<DanhMuc> list = danhMucRepository.findAllByOrderByThuTuAsc();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // POST tạo danh mục mới (admin)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> create(@RequestBody DanhMuc danhMuc) {
        try {
            if (danhMucRepository.existsByTenDanhMuc(danhMuc.getTenDanhMuc())) {
                return ResponseEntity.badRequest().body("Danh mục đã tồn tại");
            }
            return ResponseEntity.ok(danhMucRepository.save(danhMuc));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // PATCH cập nhật danh mục (admin)
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DanhMuc updated) {
        try {
            DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            dm.setTenDanhMuc(updated.getTenDanhMuc());
            dm.setMoTa(updated.getMoTa());
            dm.setIcon(updated.getIcon());
            dm.setMauSac(updated.getMauSac());
            dm.setThuTu(updated.getThuTu());
            return ResponseEntity.ok(danhMucRepository.save(dm));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // DELETE danh mục (admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            danhMucRepository.deleteById(id);
            return ResponseEntity.ok("Đã xóa danh mục");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
}