package com.vulsite.secure;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.vulsite.entity.User;
import com.vulsite.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("secure")
class BSecurityPlaywrightTests {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("file.upload-dir", () -> tempDir.resolve("uploads").toString());
    }

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private String baseUrl;
    private static final Path VIDEO_DIR = Paths.get("build", "playwright-videos");

    @BeforeAll
    static void setUpBrowser() {
        int slowMo = Integer.parseInt(System.getProperty("playwright.slowMo", "0"));
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setSlowMo(slowMo));
    }

    @AfterAll
    static void tearDownBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(VIDEO_DIR)
                .setRecordVideoSize(720, 1280)
                .setViewportSize(720, 1280)
                .setDeviceScaleFactor(1.0));
        context.addInitScript(
                "(() => {" +
                "const style = document.createElement('style');" +
                "style.innerHTML = 'html,body{background:#0b0f1a !important;}';" +
                "(document.head || document.documentElement).appendChild(style);" +
                "})()"
        );
        page = context.newPage();

        String username = "bsecure_" + UUID.randomUUID();
        User user = new User(username, "password", username + "@test.com", "B담당");
        userRepository.save(user);

        login(username, "password");
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    private void login(String username, String password) {
        page.navigate(baseUrl + "/user/login");
        page.fill("input[name=username]", username);
        page.fill("input[name=password]", password);
        page.click("button[type=submit]");
        page.waitForURL(url -> !url.contains("/login"));
    }

    private void pauseForDemo() throws InterruptedException {
        long pauseMs = Long.parseLong(System.getProperty("playwright.pauseMs", "0"));
        if (pauseMs > 0) {
            Thread.sleep(pauseMs);
        }
    }

    @Test
    @DisplayName("[보안 기대] 위험한 확장자 업로드는 차단되어야 함 (Playwright)")
    void uploadDangerousFile_shouldBeBlocked() throws Exception {
        Path payload = Files.createTempFile(tempDir, "payload", ".jsp");
        Files.write(payload, "<% out.println('x'); %>".getBytes(StandardCharsets.UTF_8));

        page.navigate(baseUrl + "/file/upload");
        page.setInputFiles("input[type=file]", payload);
        page.click("button[type=submit]");

        // 보안 적용 시 업로드 페이지에 머물며 에러 표시 기대
        page.waitForLoadState();
        assertThat(page.url()).contains("/file/upload");
        assertThat(page.locator(".alert-error").count()).isGreaterThan(0);
        pauseForDemo();
    }

    @Test
    @DisplayName("[보안 기대] 관리자 페이지는 인증 없이 접근 불가해야 함 (Playwright)")
    void adminDashboard_requiresAuth() throws InterruptedException {
        // 새 컨텍스트로 비로그인 접근
        try (BrowserContext guestContext = browser.newContext()) {
            Page guestPage = guestContext.newPage();
            guestPage.navigate(baseUrl + "/admin/dashboard");
            guestPage.waitForLoadState();
            assertThat(guestPage.url()).contains("/user/login");
        }
        pauseForDemo();
    }
}
