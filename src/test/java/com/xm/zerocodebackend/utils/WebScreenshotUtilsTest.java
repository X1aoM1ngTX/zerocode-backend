package com.xm.zerocodebackend.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String webUrl = "http://localhost:8080/V7jkGy/#/";
        String screenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        Assertions.assertNotNull(screenshotPath);
    }
}