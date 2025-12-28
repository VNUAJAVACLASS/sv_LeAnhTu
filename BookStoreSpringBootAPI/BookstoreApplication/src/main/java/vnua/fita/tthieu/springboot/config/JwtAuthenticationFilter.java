package vnua.fita.tthieu.springboot.config;

import java.io.IOException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vnua.fita.tthieu.springboot.service.CustomUserDetailsService;
import vnua.fita.tthieu.springboot.service.JwtService;

// @Component: đánh dấu đây là một bean sẽ được quét (scanning) bởi SpringBoot, 
// tạo đối tượng và đăng ký vào AppplicationContext
// OncePerRequestFilter là lớp trừu tượng của SpringSecurity đảm bảo filter chỉ chạy đúng một lần cho 1 request
// Bean JwtAuthenticationFilter sẽ được cấu hình chạy trong lớp SecurityConfig bởi phương thức addFilterBefore
// Mục đích để với mỗi request gọi kiểm tra tính hợp lệ của jwtToken nếu cần
@Component  
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    
    // jwtToken gửi trong http request tới server thường có dạng
    // Authorization: Bearer <token>
    // Authorization là header theo chuẩn http dùng để chứa thông tin xác thực
    // Bearer (ý nghĩa người mang) Theo chuẩn OAuth 2.0 (RFC 6750)
    // Nhiều framework tuân theo chuẩn này vì thế ta nên thuận theo
    // Khi dựa theo chuẩn này, server cần kiểm tra cú pháp theo đúng chuẩn đó
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        
        // Kiểm tra đúng loại bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // lấy ra chuỗi jwtToken
            try {
                username = jwtService.extractUsername(jwt); // trích xuất username từ token
            } catch (Exception e) {
                // token invalid, bạn có thể log hoặc xử lý
            }
        }
        
        // SecurityContextHolder: là vùng nhớ tạm cho mỗi request
        // Kiểm tra dk: SecurityContextHolder.getContext().getAuthentication() == null  -> Để tránh xác thực 
        // lại nhiều lần với mỗi request vì request có thể qua nhiều filter hoặc là request nội bộ đã xác thực
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // lấy thông tin user từ CSDL
            // nếu tồn tại user trong CSDL và token chưa hết hạn
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {  
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                 // principal
                                null,                        // credentials
                                userDetails.getAuthorities() // authorities
                        );
                // Lưu vào SecurityContextHolder, đánh dấu request đã xác thực
                SecurityContextHolder.getContext().setAuthentication(authToken); 
            }
        }
        
        // doFilter: cho request chạy tiếp đến các filter khác, 
        // filter này chỉ có nhiệm vụ kiểm tra jwtToken ghi vào vùng nhớ tạm SecurityContextHolder
        // filter khác như FilterSecurityInterceptor sẽ chặn request nếu 
        // yêu cầu xác thực (@PreAuthorize, authenticated(), v.v.) được thiết lập
        filterChain.doFilter(request, response);
    }
}
