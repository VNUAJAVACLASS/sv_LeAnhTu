package vnua.fita.tthieu.springboot.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
/*
 * @Configuration: Đánh dấu class này là cấu hình Spring, nó sẽ được Spring Boot tự động phát hiện 
 * và xử lý tạo các bean bên trong.
 * @EnableMethodSecurity: Kích hoạt bảo mật trên phương thức, cho phép bạn dùng các annotation 
 * như @PreAuthorize, @Secured trên các method trong controller hoặc service.
 */
@Configuration
@EnableMethodSecurity  
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    // Đăng ký tạo bean này ở ApplicationContext để tiêm vào sử dụng ở AuthController
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    // Đăng ký tạo bean này ở ApplicationContext để tiêm vào sử dụng ở AuthController
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // CSRF: Dạng tấn công lợi dụng User đã đăng nhập một trang web A (có session), cài vào đoạn code truy cập
    // trang web A đó ở một trang X khác, nếu user truy cập trang X này cùng lúc, code trên trang X truy cập trang A
    // sẽ được chạy hợp lệ do các request đều đến từ cùng 1 trình duyệt trên 1 máy, webserver của trang A ko nghi ngờ
    
    // CORS: Nếu dùng SpringSecurity phải cấu hình cors ở đây
    // method corsConfigurationSource() sẽ gọi bean có sẵn đã được tạo theo cơ chế quản lý bean của SpringBoot
    
    // UsernamePasswordAuthenticationFilter là filter có sẵn của SpringSecurity, nếu submit form login truyền thống
    // sẽ sử dụng đến, trong ứng dụng dạng rest, ko cần đến nó nhưng nó vẫn tồn tại và hoạt động
    // cần thêm filter này vào trước nó: addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    // vì nếu nó được hoạt động trước thấy dữ liệu đúng định dạng sẽ hủy request
    // nếu jwtAuthFilter hoạt động trước đến lượt nó kiểm tra trạng thái user đã xác thực nên bỏ qua cho request đi tiếp
    
    // Mở rộng cấu hình theo quyền nếu cần, ví dụ: .requestMatchers(..).hasAuthority("ROLE_ADMIN")
    
    // Bean SecurityFilterChain gồm một chuỗi cấu hình lọc bảo mật sẽ được đăng ký vào FilterChainProxy
    // Khi 1 request đến, nó gặp FilterChainProxy đầu tiên, các cấu hình phù hợp sẽ được tìm và lọc
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	
    	http
    	.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable()) // ko dùng session nên ko cần chống csrf
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll() // CN login, register ko cần xác thực
            .requestMatchers(HttpMethod.GET, "/api/projects/**").permitAll()  // CN xem không cần xác thực
            .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll() // CN xem k cần xác thực sách
            .anyRequest().authenticated() // Kiểm tra trạng thái đã xác thực trong SecurityContextHolder
           
        )
        // Dùng jwtToken nên ko dùng session
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
    
    // CORS: 1 ứng dụng như localhost:8088 sẽ không cho phép truy cập dịch vụ của nó từ cùng domain khác cổng
    // hoặc khác domain, và có thể thêm giới hạn các phương thức, header được phép...
    // tạo bean cấu hình chi tiết cho cors sử dụng ở bên trên
    @Bean 
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // cho phép ứng dụng VueJS truy cập API
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        //configuration.setAllowCredentials(true); // nếu cần

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
