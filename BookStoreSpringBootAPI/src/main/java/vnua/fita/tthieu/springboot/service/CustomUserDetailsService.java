package vnua.fita.tthieu.springboot.service;

import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import vnua.fita.tthieu.springboot.entity.User;
import vnua.fita.tthieu.springboot.repository.UserRepository;

// @Service sẽ giúp tạo một bean đăng ký trong ApplicationContext có dạng
// UserDetailsService userDetailsService = new CustomUserDetailsService();
// Bean AuthenticationManager chịu trách nhiệm xác thực trong SpringSecurity
// sẽ tự động gọi phương thức loadUserByUsername của bean UserDetailsService để lấy thông tin user
// AuthenticationManager sẽ được dùng trong AuthController
// Ngoài ra phương thức loadUserByUsername sẽ được gọi thủ công trong JwtAuthenticationFilter
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                  .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // stream() gọi từ collection: để tạo một luồng stream dữ liệu, xử lý theo lô hàm hỗ trợ tiếp sau
        // Stream map(Function) hàm của Stream: biến đổi từng phần tử trong stream xử lý theo hàm truyền vào
        // collect(Collector) hàm của Stream: để gom các kết quả cuối cùng về một cấu trúc dữ liệu (List, Set, Map...)
        
        // authorities được dùng truyền vào constructor bên dưới yêu cầu 
        // có kiểu Collection <? extends GrantedAuthority> do đó cần chuyển
        // roles thành SimpleGrantedAuthority (là lớp extends của GrantedAuthority)
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // truyền hàm mũi tên vào map
                .collect(Collectors.toList()); // gom về dạng List
        
        // Lớp User này trong thư viện spring implements interface UserDetails nên có thể dùng return
        // org.springframework.security.core.userdetails.User.User (String username, String password, 
        //                                                     Collection <? extends GrantedAuthority> authorities)
        // Lớp này dùng để đóng gói thông tin căn bản về user được Spring hỗ trợ sẵn
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
