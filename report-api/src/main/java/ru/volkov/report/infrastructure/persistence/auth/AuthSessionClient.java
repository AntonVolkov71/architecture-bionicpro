package ru.volkov.report.infrastructure.persistence.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.volkov.report.dto.ValidateResponse;

@Service
public class AuthSessionClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.auth.validate-url}")
    private String validateUrl;

    public ValidateResponse validateOrThrow(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        // пробрасываем Cookie
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            headers.add(HttpHeaders.COOKIE, cookieHeader);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ValidateResponse> resp = restTemplate.exchange(
                validateUrl,
                HttpMethod.POST,
                entity,
                ValidateResponse.class
        );

        ValidateResponse body = resp.getBody();
        if (body == null || !body.valid() || body.userId() == null || body.userId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session");
        }
        return body;
    }
}