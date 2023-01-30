package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LemmaFinder {

    private static final Logger logger = LogManager.getLogger(LemmaFinder.class);

    private final LuceneMorphology luceneMorphology;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    public LemmaFinder(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }
    public static LemmaFinder getInstance() throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        return new LemmaFinder(morphology);
    }

    public Map<String, Integer> collectLemmas(String url){
        String text = htmlToText(url);
        String[] words = arrayContainsRussianWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for(String word : words){
            if(word.isBlank()){
                continue;
            }
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if(anyWordBaseBelongToParticle(wordBaseForms)){
                continue;
            }
            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if(normalForms.isEmpty()){
                continue;
            }
            String normalWord = normalForms.get(0);
            if(lemmas.containsKey(normalWord)){
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }
    private String[] arrayContainsRussianWords(String text){
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms){
        return wordBaseForms.stream().anyMatch(this :: hasParticularProperty);
    }

    private boolean hasParticularProperty(String wordBase){
        for(String property : particlesNames){
            if(wordBase.toUpperCase().contains(property)){
                return true;
            }
        }
        return false;
    }
    public String htmlToText(String url) {
        Connection.Response body = null;
        try {
            body = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .maxBodySize(0)
                    .execute();
        } catch (Exception ex) {
            logger.error("Could not connect to: " + url + " - " + ex.getMessage());
        }
        String html = "";
        if (body != null) {
            html = body.body();
        }
        StringBuilder text = new StringBuilder();
        Document document = Jsoup.parse(html);
        Elements elements = document.children();
        for (Element element : elements) {
            text.append(element.text()).append(" ");
        }
        return text.toString();
    }
}
