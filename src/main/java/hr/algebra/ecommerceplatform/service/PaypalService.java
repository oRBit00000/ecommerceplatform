package hr.algebra.ecommerceplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.ecommerceplatform.dto.CartSummaryDTO;
import hr.algebra.ecommerceplatform.dto.PaypalOrderResponseDTO;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaypalService {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;
    private final CartService cartService;
    private final HttpClient httpClient;

    @Value("${paypal.base-url:https://api-m.sandbox.paypal.com}")
    private String baseUrl;

    @Value("${paypal.client-id:}")
    private String clientId;

    @Value("${paypal.client-secret:}")
    private String clientSecret;

    @Value("${paypal.currency:EUR}")
    private String currency;

    public PaypalService(ObjectMapper objectMapper, CartService cartService) {
        this.objectMapper = objectMapper;
        this.cartService = cartService;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getClientId() {
        return clientId;
    }

    public String getWebSdkUrl() {
        return baseUrl != null && baseUrl.contains("sandbox")
                ? "https://www.sandbox.paypal.com/web-sdk/v6/core"
                : "https://www.paypal.com/web-sdk/v6/core";
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }

    public String generateBrowserSafeClientToken() {
        ensureConfigured();
        String accessToken = fetchAccessToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/identity/generate-token"))
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken)
                .header("Accept-Language", "en_US")
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Unable to generate PayPal browser-safe client token.");
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            JsonNode clientToken = jsonNode.get("client_token");
            if (clientToken == null || clientToken.asText().isBlank()) {
                throw new IllegalStateException("PayPal did not return a browser-safe client token.");
            }
            return clientToken.asText();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to generate PayPal browser-safe client token.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate PayPal browser-safe client token.", ex);
        }
    }

    public PaypalOrderResponseDTO createOrder(String appBaseUrl) {
        ensureConfigured();
        CartSummaryDTO cartSummary = cartService.getCartSummary();
        if (cartSummary.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        String accessToken = fetchAccessToken();
        String payload = buildCreateOrderPayload(cartSummary.getTotalPrice(), appBaseUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Unable to create PayPal order.");
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            String orderId = jsonNode.get("id").asText();
            String approvalUrl = null;
            JsonNode links = jsonNode.get("links");
            if (links != null && links.isArray()) {
                for (JsonNode link : links) {
                    if ("payer-action".equalsIgnoreCase(link.path("rel").asText())
                            || "approve".equalsIgnoreCase(link.path("rel").asText())) {
                        approvalUrl = link.path("href").asText();
                        break;
                    }
                }
            }

            if (approvalUrl == null || approvalUrl.isBlank()) {
                throw new IllegalStateException("PayPal did not return an approval URL.");
            }

            return new PaypalOrderResponseDTO(orderId, approvalUrl);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to create PayPal order.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create PayPal order.", ex);
        }
    }

    public void captureOrder(String orderId) {
        ensureConfigured();
        String accessToken = fetchAccessToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/checkout/orders/" + orderId + "/capture"))
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("PayPal capture failed.");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to capture PayPal order.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to capture PayPal order.", ex);
        }
    }

    private String fetchAccessToken() {
        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/oauth2/token"))
                .header(HEADER_AUTHORIZATION, "Basic " + credentials)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED)
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = objectMapper.readTree(response.body());
            return jsonNode.get("access_token").asText();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to fetch PayPal access token.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to fetch PayPal access token.", ex);
        }
    }

    private String buildCreateOrderPayload(BigDecimal totalAmount, String appBaseUrl) {
        return """
                {
                  "intent": "CAPTURE",
                  "purchase_units": [
                    {
                      "amount": {
                        "currency_code": "%s",
                        "value": "%s"
                      }
                    }
                  ],
                  "payment_source": {
                    "paypal": {
                      "experience_context": {
                        "payment_method_preference": "IMMEDIATE_PAYMENT_REQUIRED",
                        "landing_page": "LOGIN",
                        "user_action": "PAY_NOW",
                        "return_url": "%s/mvc/orders/paypal/complete",
                        "cancel_url": "%s/mvc/orders/paypal/cancel"
                      }
                    }
                  }
                }
                """.formatted(
                currency,
                totalAmount.setScale(2, java.math.RoundingMode.HALF_UP),
                appBaseUrl,
                appBaseUrl
        );
    }

    private void ensureConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("PayPal sandbox credentials are not configured.");
        }
    }
}
