package searchengine;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.dao.DataIntegrityViolationException;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public class SiteIndexing extends Thread{

    private static final Logger logger = LogManager.getLogger(SiteIndexing.class);

    private final SiteEntity siteEntity;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final boolean allSites;


    @Override
    public void run() {
        if(allSites) {
            runAllSitesIndexing();
        } else {
            runOneSiteIndexing(siteEntity.getUrl());
        }
    }

    private void runAllSitesIndexing(){
        siteEntity.setStatus(StatusType.INDEXING);
        siteEntity.setStatusTime(new Date());
        siteEntity.setLastError("Ошибок не обнаружено");
        siteRepository.save(siteEntity);
        SiteMap siteMap = new SiteMap(siteEntity.getUrl(), this.isInterrupted());
        siteMap.createSiteMap();
        List<String> urlList = siteMap.getSiteMap();
        for (String url : urlList){
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            runOneSiteIndexing(url);
        }
    }

    public void runOneSiteIndexing(String url){
        siteEntity.setStatus(StatusType.INDEXING);
        siteEntity.setStatusTime(new Date());
        siteRepository.save(siteEntity);
        try {
            PageEntity pageEntity = getPage(url, siteEntity.getUrl(), siteEntity.getId());
            pageRepository.save(pageEntity);
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            Map<String, Integer> lemmas = lemmaFinder.collectLemmas(url);
            float lemmasSum = saveLemmasToDB(lemmas, siteEntity.getId());
            saveIndexToDB(lemmas, pageEntity.getId(), lemmasSum);
            } catch (IOException e) {
                siteEntity.setLastError("Ошибка чтения страницы: " + url);
                siteEntity.setStatus(StatusType.FAILED);
                logger.error("Error reading page: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (DataIntegrityViolationException e){
                siteEntity.setLastError("Данные страницы " + url + " отсутствуют");
                logger.error("Page's data is empty: " + e.getMessage());
                Thread.currentThread().interrupt();
            } finally {
            siteRepository.save(siteEntity);
        } synchronized (this) {
            siteEntity.setStatusTime(new Date());
            siteEntity.setStatus(StatusType.INDEXED);
            siteRepository.save(siteEntity);
        }
    }

    private PageEntity getPage(String url, String rootUrl, long siteId) throws IOException{
        PageEntity pageEntity = new PageEntity();
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .maxBodySize(0)
                    .execute();
            String content = response.body();
            if (url.contains("www.")){
                url = url.replaceAll("www.", "");
            }
            if (rootUrl.contains("www.")){
                rootUrl = rootUrl.replaceAll("www.", "");
            }
            String path = url.replaceAll(rootUrl, "");
            int code = response.statusCode();
            pageEntity.setContent(content);
            pageEntity.setPath(path);
            pageEntity.setCode(code);
            pageEntity.setSiteId(siteId);
            PageEntity check = pageRepository.findByPath(path);
            if(check != null){
                pageRepository.delete(check);
            }
        } catch (Exception ex){
            logger.error("Could not connect to " + url + " - " + ex.getMessage());
        }
        return pageEntity;
    }
    private float saveLemmasToDB(Map<String, Integer> lemmas, long siteId){
        float sum = 0;
        for(Map.Entry<String, Integer> lemma : lemmas.entrySet()){
            sum = sum + lemma.getValue();
            LemmaEntity lemmaEntity = lemmaRepository.findByLemma(lemma.getKey());
            int frequency = 0;
            if(lemmaEntity == null){
                lemmaEntity = new LemmaEntity();
            } else {
                frequency = lemmaEntity.getFrequency();
            }
            lemmaEntity.setLemma(lemma.getKey());
            lemmaEntity.setFrequency(frequency + 1);
            lemmaEntity.setSiteId(siteId);
            lemmaRepository.save(lemmaEntity);
        }
        return sum;
    }
    private void saveIndexToDB(Map<String, Integer> lemmas, long pageId, float lemmasSum){
        for(Map.Entry<String, Integer> lemma : lemmas.entrySet()){
            LemmaEntity lemmaEntity = lemmaRepository.findByLemma(lemma.getKey());
            IndexEntity indexEntity = indexRepository.findIndexByLemmaId(lemmaEntity.getId());
            if(indexEntity == null){
                indexEntity = new IndexEntity();
            }
            indexEntity.setPageId(pageId);
            indexEntity.setRank((lemma.getValue() / lemmasSum) * 100);
            indexEntity.setLemmaId(lemmaEntity.getId());
            indexRepository.save(indexEntity);
        }
    }
}
