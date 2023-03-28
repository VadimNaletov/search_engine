import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.LemmaFinder;

import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class LemmaFinderTest {
    private LemmaFinder lemmaFinder;

    @BeforeEach
    public void setUp() throws IOException {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        lemmaFinder = new LemmaFinder(luceneMorphology);
    }

    @Test
    public void testCollectLemmas() {
        String testUrl = "https://lenta.ru/news/2023/02/26/velo/";
        Map<String, Integer> result = lemmaFinder.collectLemmas(testUrl);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.containsKey("лет"));
        assertFalse(result.containsKey("срок"));
    }
}
