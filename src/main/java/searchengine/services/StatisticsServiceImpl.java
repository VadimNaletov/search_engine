package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = getTotalStatistics();
        List<DetailedStatisticsItem> detailed = getDetailedStatisticItems();
        return getStatisticsResponse(total, detailed);
    }
    
    private TotalStatistics getTotalStatistics(){
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);
        total.setLemmas(lemmaRepository.countLemmas());
        total.setPages(pageRepository.countPages());
        return total;
    }

    private List<DetailedStatisticsItem> getDetailedStatisticItems(){
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            String url = site.getUrl();
            if (url.contains("www.")) url = url.replaceAll("www.", "");
            SiteEntity siteEntity = getSiteEntity(url, site);

            item.setName(site.getName());
            item.setUrl(url);
            int pages = pageRepository.countBySiteId(siteEntity.getId());
            int lemmas = lemmaRepository.countBySiteId(siteEntity.getId());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(siteEntity.getStatus());
            item.setError(siteEntity.getLastError());
            item.setStatusTime(siteEntity.getStatusTime());
            detailed.add(item);
        }
        return detailed;
    }

    private SiteEntity getSiteEntity(String url, Site site){
        SiteEntity siteEntity = siteRepository.findByUrl(url);
        if (siteEntity == null) {
            siteEntity = new SiteEntity();
            siteEntity.setUrl(url);
            siteEntity.setName(site.getName());
            siteEntity.setStatusTime(new Date());
            siteEntity.setStatus(StatusType.FAILED);
            siteEntity.setLastError("Индексация еще не запускалась");
            siteRepository.save(siteEntity);
        }
        return siteEntity;
    }

    private StatisticsResponse getStatisticsResponse(TotalStatistics total, List<DetailedStatisticsItem> detailed){
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
