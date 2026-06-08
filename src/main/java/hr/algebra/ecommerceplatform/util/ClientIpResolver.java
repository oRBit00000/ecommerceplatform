package hr.algebra.ecommerceplatform.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientIpResolver {

    public String resolve(HttpServletRequest request) {
        String forwardedFor = extractFirstIp(request.getHeader("X-Forwarded-For"));
        if (isUsable(forwardedFor)) {
            return normalize(forwardedFor);
        }

        String realIp = request.getHeader("X-Real-IP");
        if (isUsable(realIp)) {
            return normalize(realIp);
        }

        String cloudflareIp = request.getHeader("CF-Connecting-IP");
        if (isUsable(cloudflareIp)) {
            return normalize(cloudflareIp);
        }

        return normalize(request.getRemoteAddr());
    }

    private String extractFirstIp(String headerValue) {
        if (!isUsable(headerValue)) {
            return null;
        }

        return headerValue.split(",")[0].trim();
    }

    private boolean isUsable(String value) {
        return value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value);
    }

    private String normalize(String ipAddress) {
        if (ipAddress == null) {
            return "unknown";
        }

        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        if (ipAddress.startsWith("::ffff:")) {
            return ipAddress.substring(7);
        }

        return ipAddress;
    }
}
