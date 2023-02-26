package searchengine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SiteMap {

    private static final Logger logger = LogManager.getLogger(SiteMap.class);

    public SiteMap(String url, boolean isInterrupted) {
        this.url = url;
        this.isInterrupted = isInterrupted;
    }
    private final String url;
    private final boolean isInterrupted;


    public List<String> getSiteMap() {
        return siteMap;
    }

    private List<String> siteMap;

    public void createSiteMap(){
        try {
            ForkJoinPool pool = new ForkJoinPool();
            String text = pool.invoke(new UrlParser(url, isInterrupted));
            siteMap = textToArray(text);
        } catch (Exception ex) {
            logger.error("An error occurred while creating site map", ex);
        }
    }
    private List<String> textToArray(String text){
        return Arrays.stream(text.split("\n")).collect(Collectors.toList());
    }


}
