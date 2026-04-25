package com.pharmacy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SwaggerErrorTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSwagger() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api-docs", String.class);
        System.out.println("SWAGGER RESPONSE STATUS: " + response.getStatusCode());
        System.out.println("SWAGGER RESPONSE: " + response.getBody());
    }
}
