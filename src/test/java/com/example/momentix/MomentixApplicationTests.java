package com.example.momentix;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@Disabled("DB 연결 없이 컨텍스트 로드 불가 - 통합 테스트 환경 미구성")
@SpringBootTest
class MomentixApplicationTests {
    @Test
    void contextLoads() {
    }
}
