package searchengine;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.dto.search.DetailedSearchData;
import searchengine.dto.search.SearchData;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
@RequiredArgsConstructor
public class Searching {

    private static final Logger logger = LogManager.getLogger(Searching.class);

    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    SearchData searchData = new SearchData();
    List<DetailedSearchData> detailedSearchDataList = new ArrayList<>();

    public SearchData getSearchData(String query, String url, int offset, int limit) throws IOException {
        query = query.toLowerCase();
        List<SiteEntity> siteList;
        if(url == null){
            siteList = siteRepository.findAll();
        } else {
            url = url.replace("www.", "");
            SiteEntity siteEntity = siteRepository.findByUrl(url);
            siteList = new ArrayList<>(Collections.singletonList(siteEntity));
        }
        for (SiteEntity siteEntity : siteList){
            fillInDetailedSearchDataList(query, siteEntity.getId());
        }

        detailedSearchDataList.sort(Comparator.comparing(DetailedSearchData::getRelevance));

        int count = Math.min(limit, detailedSearchDataList.size() - offset);
        List<DetailedSearchData> result = detailedSearchDataList.subList(offset, offset + count);
        searchData.setDetailedSearchData(result);
        searchData.setCount(detailedSearchDataList.size());
        return searchData;
    }

    private Map<PageEntity, Float> getRelevantData(String query, long siteId){
        HashMap<PageEntity, Float> relevantPages = new HashMap<>();
        List<LemmaEntity> requiredLemmas = getSortedLemmas(query, siteId);
        if(requiredLemmas.isEmpty()){
            return relevantPages;
        }
        List<IndexEntity> indexEntityList =
                    indexRepository.getAllIndexesByLemmaId(requiredLemmas.get(0).getId());
        Set<Long> pageIds = new HashSet<>();
        for (IndexEntity indexEntity : indexEntityList) {
            pageIds.add(indexEntity.getPageId());
        }
        for(int i = 1; i < requiredLemmas.size(); i++){
            List<IndexEntity> indexes = indexRepository.getAllIndexesByLemmaId(requiredLemmas.get(i).getId());
            Set<Long> newPageIds = new HashSet<>();
            for(IndexEntity indexEntity : indexes){
                if(pageIds.contains(indexEntity.getPageId())){
                    newPageIds.add(indexEntity.getPageId());
                }
            }
            pageIds = newPageIds;
        }

        for(Long pageId : pageIds){
            PageEntity pageEntity = pageRepository.findById(pageId).orElse(null);
            float absoluteRelevance = 0;
            for (LemmaEntity lemmaEntity : requiredLemmas) {
                absoluteRelevance += indexRepository.countAbsoluteRelevance(pageEntity.getId(), lemmaEntity.getId());
            }
            relevantPages.put(pageEntity, absoluteRelevance);
        }
        return relevantPages;
    }
    private List<LemmaEntity> getSortedLemmas(String query, long siteId){
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        List<String> requiredLemmas = getRequiredLemma(query);
        for(String lemma : requiredLemmas){
            List<LemmaEntity> lemmas = lemmaRepository.getLemmasList(lemma);
            for(LemmaEntity lemmaEntity : lemmas){
                if(lemmaEntity.getSiteId() == siteId && lemmaEntity.getFrequency() <= 100) {
                    lemmaEntityList.add(lemmaEntity);
                }
            }
        }
        lemmaEntityList.sort(Comparator.comparingInt(LemmaEntity::getFrequency));
        return lemmaEntityList;
    }
    private List<String> getRequiredLemma(String query){
        List<String> requestLemmas = new ArrayList<>();
        try {
            LuceneMorphology russianLuceneMorphology = new RussianLuceneMorphology();
            String[] queryWords = query.split("\\s+");
            for (String word : queryWords) {
                word = word.toLowerCase();
                Pattern pattern = Pattern.compile("а-яё");
                Matcher matcher = pattern.matcher(word);
                List<String> normalWord;
                if(matcher.matches()) {
                    normalWord = russianLuceneMorphology.getNormalForms(word);
                } else {
                    normalWord = new ArrayList<>();
                    normalWord.add(word);
                }
                requestLemmas.add(normalWord.get(0));
            }
        } catch (Exception ex){
            logger.info("Could not find request lemmas for: " + query + " - " + ex.getMessage());
        }
        return requestLemmas;
    }

    private DetailedSearchData getDetailedSearchData(Map.Entry<PageEntity, Float> data, String query){
        DetailedSearchData detailedSearchData = new DetailedSearchData();
        PageEntity pageEntity = data.getKey();
        Optional<SiteEntity> siteEntityOptional = siteRepository.findById(pageEntity.getSiteId());
        if(siteEntityOptional.isPresent()){
            SiteEntity siteEntity = siteEntityOptional.get();
            String url = siteEntity.getUrl() + pageEntity.getPath();
            Document document = null;
            try {
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                logger.error("Could not connect to: " + url + " - " + e.getMessage());
            }
            if (document != null) {
                detailedSearchData.setTitle(document.title());
            }
            detailedSearchData.setUri(pageEntity.getPath());
            detailedSearchData.setRelevance(data.getValue());
            detailedSearchData.setSiteName(siteEntity.getName());
            detailedSearchData.setSite(siteEntity.getUrl());
            try {
                detailedSearchData.setSnippet(getSnipped(pageEntity.getContent(), query));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return detailedSearchData;
    }
    private String getSnipped (String html, String request) throws IOException {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        String text = Jsoup.parse(html).text();
        String[] requestWords = request.split("\\s+");
        List<String> requestLemmas = new ArrayList<>();
        for(String req : requestWords){
            List<String> reqLemmas = luceneMorphology.getNormalForms(req);
            requestLemmas.add(reqLemmas.get(0));
        }
        String[] textWords = text.split("—|\\p{Punct}|\\s");
        StringBuilder builder = new StringBuilder();
        for (String textWord : textWords) {
            try {
                if (requestLemmas.contains(luceneMorphology.getNormalForms(textWord.toLowerCase()).get(0))) {
                    builder.append("<b>").append(textWord).append("</b> ");
                } else {
                    builder.append(textWord).append(" ");
                }
            } catch (Exception ex) {
                logger.info("Could not find request lemmas for: " + textWord + " - " + ex.getMessage());
            }
        }
        String preResult =
                builder.substring(Math.max(builder.indexOf("<b>") - 100, 0),
                        Math.min(builder.indexOf("</b>") + 100, builder.length()));
        String result = preResult.substring(preResult.indexOf(" "));
        while(!result.endsWith(" ")){
            result = result.substring(0, result.length() - 1);
        }
        result = result.substring(1, result.length() - 1);
        return result;
    }
    private void fillInDetailedSearchDataList(String query, long siteId){
        Map<PageEntity, Float> relevantData = getRelativeRelevance(query, siteId);
        for (Map.Entry<PageEntity, Float> data : relevantData.entrySet()){
            DetailedSearchData detailedSearchData = getDetailedSearchData(data, query);
            detailedSearchDataList.add(detailedSearchData);
        }
    }

    private Map<PageEntity, Float> getRelativeRelevance(String query, long siteId){
        Map<PageEntity, Float> relevantPages = getRelevantData(query, siteId);
        float maxAbsoluteRelevance = 0F;

        if (!relevantPages.isEmpty()) {
            maxAbsoluteRelevance = Collections.max(relevantPages.values());
        }
        Map<PageEntity, Float> relativeRelevance = new HashMap<>();
        for (Map.Entry<PageEntity, Float> entry : relevantPages.entrySet()) {
            float absoluteRelevance = entry.getValue();
            float relative = maxAbsoluteRelevance > 0 ? absoluteRelevance / maxAbsoluteRelevance : 0;
            relativeRelevance.put(entry.getKey(), relative);
        }
        return relativeRelevance;
    }
}


