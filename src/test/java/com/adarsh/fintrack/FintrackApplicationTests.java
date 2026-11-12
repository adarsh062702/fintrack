package com.adarsh.fintrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.jwt.secret=VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdUZXN0U2VjcmV0S2V5Rm9ySldU"
})
class FintrackApplicationTests {

    @Test
    void contextLoads() {
    }
}