package hr.algebra.ecommerceplatform.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAuditDTO {
    private String username;
    private String ipAddress;
    private LocalDateTime loggedInAt;
}
