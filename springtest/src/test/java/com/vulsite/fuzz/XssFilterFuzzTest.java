package com.vulsite.fuzz;

import com.code_intelligence.jazzer.junit.FuzzTest;
import com.vulsite.service.BoardService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

class XssFilterFuzzTest {

    @FuzzTest(maxDuration = "30s")
    void filterXss_shouldNotLeaveHtmlElements(String input) {
        String output = invokeFilterXss(input);
        if (output == null) {
            return;
        }

        Document doc = Jsoup.parseBodyFragment(output);
        if (!doc.body().children().isEmpty()) {
            String tags = doc.body().children().stream()
                    .map(Element::tagName)
                    .collect(Collectors.joining(","));
            String html = doc.body().html();
            Assertions.fail("HTML elements remained after filtering. tags=" + tags + " html=" + html);
        }
    }

    private static String invokeFilterXss(String input) {
        try {
            BoardService service = new BoardService(null);
            Method method = BoardService.class.getDeclaredMethod("filterXss", String.class);
            method.setAccessible(true);
            return (String) method.invoke(service, input);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to invoke filterXss via reflection.", e);
        }
    }
}
