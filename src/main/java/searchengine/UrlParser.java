package searchengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;


public class UrlParser extends RecursiveTask<String> {

    private static final Logger logger = LogManager.getLogger(UrlParser.class);

    @Value("${userAgent}")
    private String userAgent;

    public UrlParser(String url, boolean isInterrupted) {
        this.url = url;
        this.isInterrupted = isInterrupted;
    }
    private final String url;
    private final boolean isInterrupted;
    private final static List<String> urlList = new Vector<>();

    @Override
    protected synchronized String compute() {
        if (isInterrupted){
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(url);
        try {
            Thread.sleep(200);
            Document document = Jsoup.connect(url).userAgent(userAgent)
                    .referrer("http://www.google.com").maxBodySize(0).get();
            Elements elements = document.select("a");
            List<UrlParser> executedLinks = new ArrayList<>();
            for (Element element : elements){
                String link = element.attr("abs:href");
                if(link.startsWith(element.baseUri()) &&
                !link.equals(element.baseUri()) &&
                !link.contains("#") &&
                !link.contains(".pdf") &&
                !urlList.contains(link)){
                    urlList.add(link);
                    UrlParser urlParser = new UrlParser(link, false);
                    urlParser.fork();
                    executedLinks.add(urlParser);
                }
            }
            for(UrlParser parser : executedLinks){
                String text = parser.join();
                if(!text.equals("")){
                    result.append("\n");
                    result.append(text);
                }
            }
        } catch (RuntimeException | IOException | InterruptedException ex){
            logger.error("Error while processing URL: " + url + ex.getMessage());
        }
        return result.toString();
    }
}
