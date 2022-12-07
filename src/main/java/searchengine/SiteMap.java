package searchengine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class SiteMap {
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
        String text = new ForkJoinPool().invoke(new UrlParser(url, isInterrupted));
        siteMap = textToArray(text);
    }
    private List<String> textToArray(String text){
        return Arrays.stream(text.split("\n")).collect(Collectors.toList());
    }


}
