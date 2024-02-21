import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

class YourClassTest {

    @Test
    void testCleanLinesWithAsteriskAndSpace() {
        String content = "* Line with asterisk and space";
        assertEquals(List.of("Line with asterisk and space"), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithAsteriskAndTab() {
        String content = "*\tLine with asterisk and tab";
        assertEquals(List.of("Line with asterisk and tab"), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithoutAsterisk() {
        String content = "Line without asterisk";
        assertEquals(List.of("Line without asterisk"), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithEmptyLines() {
        String content = "\n\n* Line with asterisk\n\n";
        assertEquals(List.of("Line with asterisk"), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithLeadingSpace() {
        String content = "   * Line with leading space";
        assertEquals(List.of("Line with leading space"), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithEmptyContent() {
        String content = "";
        assertEquals(Collections.emptyList(), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithOnlyWhitespaceLines() {
        String content = "  \n\n  ";
        assertEquals(Collections.emptyList(), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithEmptyFirstLine() {
        String content = "\nLine without asterisk";
        assertEquals(List.of("Line without asterisk"), JavadocParser.cleanLines(content));
    }

    @Test
    void testCleanLinesWithEmptyLastLine() {
        String content = "Line without asterisk\n";
        assertEquals(List.of("Line without asterisk"), JavadocParser.cleanLines(content));
    }
}