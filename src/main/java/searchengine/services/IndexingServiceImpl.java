package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import searchengine.SiteIndexing;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;

    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    @Override
    public boolean startIndexing() {
        boolean isIndexing;
        List<SiteEntity> siteEntityList = getListFromConfig();
        for (SiteEntity siteEntity : siteEntityList){
            isIndexing = startSiteIndexing(siteEntity);
            if(!isIndexing){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean stopIndexing() {
        threadPoolExecutor.shutdownNow();
        List<SiteEntity> siteEntityList = siteRepository.findAll();
        for (SiteEntity siteEntity : siteEntityList) {
            if(siteEntity.getStatus().equals(StatusType.INDEXING)) {
                siteEntity.setStatus(StatusType.FAILED);
                siteEntity.setStatusTime(new Date());
                siteEntity.setLastError("Индексация была прервана");
                siteRepository.save(siteEntity);
            }
        }
        return threadPoolExecutor.getActiveCount() == 0;

    }

    @Override
    public boolean indexPage(String url){
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
            return false;
        } else {
            SiteEntity siteEntity = siteRepository.findByUrl(site);
            siteEntity.setUrl(url);
            SiteIndexing siteIndexing = new SiteIndexing(siteEntity, siteRepository,
                    pageRepository, lemmaRepository, indexRepository, false);
            threadPoolExecutor.execute(siteIndexing);
            siteEntity.setUrl(site);
            siteEntity.setStatus(StatusType.INDEXED);
            siteRepository.save(siteEntity);
            return true;
        }
    }
    private List<SiteEntity> getListFromConfig(){
        List<SiteEntity> siteEntityList = new ArrayList<>();
        List<Site> siteList = sitesList.getSites();
        for (Site site : siteList){
            if (site.getUrl().contains("www.")){
                site.setUrl(site.getUrl().replaceAll("www.", ""));
            }
            SiteEntity siteEntity;
            if(siteRepository.findByUrl(site.getUrl()) != null) {
                siteEntity = siteRepository.findByUrl(site.getUrl());
            } else {
                siteEntity = new SiteEntity();
                siteEntity.setUrl(site.getUrl());
                siteEntity.setName(site.getName());
                siteEntity.setStatus(StatusType.FAILED);
                siteEntity.setStatusTime(new Date());
            }

            siteEntityList.add(siteEntity);
        }
        return siteEntityList;
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
