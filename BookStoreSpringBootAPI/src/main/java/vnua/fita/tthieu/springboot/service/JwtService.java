package vnua.fita.tthieu.springboot.service;

import java.security.Key;


import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${jwt.secret}") // lấy chuỗi bí mật từ application.properties
	private String jwtSecret;

	@Value("${jwt.expiration-ms}")  // lấy thời gian hết hạn của jwtToken
	private long jwtExpirationMs;

	// Trả về một đối tượng Key (khóa mật - thuộc kiểu java.security.Key) từ chuỗi bí mật, 
	// thích hợp cho thuật toán HMAC-SHA, có thể dùng để:
	// Ký JWT: khi tạo token (mã hóa)
	// Xác minh JWT: khi kiểm tra token nhận được (giải mã)
	// Key bao gồm thông tin (thuật toán, định dạng (thường ở dạng nhị phân), 
	// chuỗi nhị phân (khóa mật - dùng mã hóa, giải mã) tương ứng chuỗi bí mật)
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    // jwtToken gồm 3 phần header.payload.signature (nối với nhau bởi dấu chấm)
    // header chứa metadata về token gồm thuật toán băm + loại token, 
    // payload gồm nhiều loại thông tin (claim) như chủ đề token (subject), thời điểm tạo, thời điểm hết hạn...
    // có thể tùy biến chuỗi đưa vào subject hoặc tự định nghĩa thêm thông tin đưa vào payload
    // signature (chữ ký) - là mã băm của chuỗi: base64UrlEncode(header) + "." + base64UrlEncode(payload)
    // sử dụng thuật toán chỉ định và khóa mật để tạo chuỗi băm, bất kỳ thay đổi header, payload sẽ làm đổi mã băm
    // Sở dĩ có thể chấm gọi một loạt phương thức setX(..) vì chúng đều trả về kiểu JwtBuilder có hỗ trợ hàm setX(..) đó
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // đưa username vào làm subject của payload
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) 
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) 
                .compact(); // Nối kết các phần thông tin tạo chuỗi jwtToken
    }
    
    // Lấy ra username từ token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Function<Claims, T> claimsResolver: dạng function interface, nhận đầu vào là Claims, trả về kiểu T (tự suy kiểu)
    // là cách thức truyền 1 hàm có sẵn vào để xử lý dưới dạng tham số cho phép rút gọn code
    // function interface có 1 method apply sẽ được gọi để áp dụng function này cho dữ liệu, trả về kết quả kiểu T
    // Ví dụ gọi: Claims::getSubject là hàm mũi tên tương đương (claims) -> claims.getSubject()
    // với tham số truyền vào là Claims, hàm gọi đến là getSubject (của Claim), kiểu trả về là String (tự suy)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // đưa khóa mật vào
                .build()
                .parseClaimsJws(token) // đưa chuỗi jwtToken vào
                .getBody(); // giải mã lấy ra phần payload dưới dạng Claims
        return claimsResolver.apply(claims); // áp dụng hàm truyền vào cho claims ở trên
    }

    public boolean isTokenValid(String token, String username) {
        String user = extractUsername(token);
        return (user.equals(username) && !isTokenExpired(token));
    }
    
    // Kiểm tra token hết hạn chưa
    private boolean isTokenExpired(String token) {
        final Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date()); // thời điểm hết hạn đã qua
    }
}
