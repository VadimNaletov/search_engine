import org.junit.Test;
import searchengine.UrlParser;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class UrlParserTest {
    @Test
    public void testComputeWithValidUrl(){
        UrlParser parser = new UrlParser("https://www.test.com", false);
        String result = ReflectionTestUtils.invokeMethod(parser, "compute");
        assertNotNull(result);
    }

    @Test
    public void testComputeWithAnInvalidURL(){
        UrlParser parser = new UrlParser("https://www.incorrect_test.com", false);
        String result = ReflectionTestUtils.invokeMethod(parser, "compute");
        assertNotNull(result);
    }

    @Test
    public void testComputeWhenThreadIsInterrupted(){
        UrlParser parser = new UrlParser("https://www.test.com", true);
        String result = ReflectionTestUtils.invokeMethod(parser, "compute");
        assertEquals("", result);
    }

}
