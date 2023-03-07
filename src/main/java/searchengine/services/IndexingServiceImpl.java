package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import searchengine.SiteIndexing;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private static final Logger logger = LogManager.getLogger(IndexingServiceImpl.class);
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;


    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    @Override
    public IndexingResponse startIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        boolean isIndexing;
        List<SiteEntity> siteEntityList = siteRepository.findAll();
        for (SiteEntity siteEntity : siteEntityList){
            isIndexing = startSiteIndexing(siteEntity);
            if(!isIndexing){
                indexingResponse.setResult(false);
                indexingResponse.setError("Не удалось запустить индексацию");
                return indexingResponse;
            }
        }
        indexingResponse.setResult(true);
        indexingResponse.setError("");
        return indexingResponse;
    }

    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        if(threadPoolExecutor.getActiveCount() == 0){
            indexingResponse.setResult(true);
            indexingResponse.setError("Индексация не была запущена");
            return indexingResponse;
        }
        threadPoolExecutor.shutdownNow();
        try{
            threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logger.error("ThreadPoolExecutor was not terminated: " + ex.getMessage());
        }
        if(threadPoolExecutor.isShutdown()){
            List<SiteEntity> siteEntityList = siteRepository.findAll();
            for (SiteEntity siteEntity : siteEntityList) {
                if (siteEntity.getStatus().equals(StatusType.INDEXING)) {
                    siteEntity.setStatus(StatusType.FAILED);
                    siteEntity.setStatusTime(new Date());
                    siteEntity.setLastError("Индексация была прервана");
                    siteRepository.save(siteEntity);
                }
            }
            indexingResponse.setResult(true);
            indexingResponse.setError("Индексация прервана");
        } else {
            indexingResponse.setResult(false);
            indexingResponse.setError("Не удалось остановить индексацию");
        }
        return indexingResponse;

    }

    @Override
    public IndexingResponse indexPage(String url){
        IndexingResponse indexingResponse = new IndexingResponse();
        List<SiteEntity> siteEntityList = siteRepository.findAll();
        String site = "";
        StatusType status = null;
        for (SiteEntity siteEntity : siteEntityList){
            if(url.contains(siteEntity.getUrl())){
                site = siteEntity.getUrl();
                status = siteEntity.getStatus();
            }
        }
        if(site.isBlank() || Objects.equals(status, StatusType.INDEXING)){
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена или ее никогда не запускали ранее");
        } else {
            SiteEntity siteEntity = siteRepository.findByUrl(site);
            siteEntity.setUrl(url);
            SiteIndexing siteIndexing = new SiteIndexing(siteEntity, siteRepository,
                    pageRepository, lemmaRepository, indexRepository, false);
            threadPoolExecutor.execute(siteIndexing);
            siteEntity.setUrl(site);
            siteEntity.setStatus(StatusType.INDEXED);
            siteRepository.save(siteEntity);
            indexingResponse.setError("");
            indexingResponse.setResult(true);
        }
        return indexingResponse;
    }

    private boolean startSiteIndexing(SiteEntity siteEntity){
        SiteEntity checkSiteEntity = siteRepository.findByUrl(siteEntity.getUrl());
        if (checkSiteEntity == null){
            siteRepository.save(siteEntity);
        }
        if (!siteEntity.getStatus().equals(StatusType.INDEXING)){
            SiteIndexing siteIndexing =
                    new SiteIndexing(siteEntity, siteRepository,
                            pageRepository, lemmaRepository, indexRepository, true);

            threadPoolExecutor.execute(siteIndexing);

            return true;
        } else {
            return false;
        }
    }
}