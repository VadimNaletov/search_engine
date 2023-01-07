package searchengine;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

@Component
@RequiredArgsConstructor
public class Searching {
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    SearchData searchData = new SearchData();

    List<DetailedSearchData> detailedSearchDataList = new ArrayList<>();

    public SearchData getSearchData(String query, String url, int offset, int limit) throws IOException {
        query = query.toLowerCase();
        List<SiteEntity> siteList = siteRepository.findAll();
        if(url == null){
            for(SiteEntity siteEntity : siteList){
                fillInDetailedSearchDataList(query, siteEntity.getId());
            }
        } else {
            if(url.contains("www.")){
                url = url.replace("www.", "");
            }
            SiteEntity siteEntity = siteRepository.findByUrl(url);
            fillInDetailedSearchDataList(query, siteEntity.getId());
        }
        detailedSearchDataList.sort(Comparator.comparing(DetailedSearchData::getRelevance));
        List<DetailedSearchData> result = new ArrayList<>();
        int count = 0;
        if (detailedSearchDataList.isEmpty()){
            searchData.setCount(count);
        }
        if (limit + offset < detailedSearchDataList.size()) {
            count = limit;
        } else {
            count = detailedSearchDataList.size() - offset;
        }
        if(offset == 0 && count == 0){
            result = detailedSearchDataList;
        } else {
            for(int i = offset; i < count; i++){
                result.add(i, detailedSearchDataList.get(i));
            }
        }
        searchData.setDetailedSearchData(result);
        searchData.setCount(detailedSearchDataList.size());
        return searchData;
    }
    private Map<PageEntity, Float> getRelevantData(String query, long siteId){
        HashMap<PageEntity, Float> relevantPages = new HashMap<>();
        List<LemmaEntity> requiredLemmas = getSortedLemmas(query, siteId);
        List<Long> pageIndexes = new ArrayList<>();
        if(!requiredLemmas.isEmpty()) {
            List<IndexEntity> indexEntityList =
                    indexRepository.getAllIndexesByLemmaId(requiredLemmas.get(0).getId());
            indexEntityList.forEach(indexEntity -> pageIndexes.add(indexEntity.getPageId()));
            for (LemmaEntity lemmaEntity : requiredLemmas) {
                if (!pageIndexes.isEmpty() && lemmaEntity.getId() != requiredLemmas.get(0).getId()) {
                    List<IndexEntity> leftIndexEntityList =
                            indexRepository.getAllIndexesByLemmaId(lemmaEntity.getId());
                    List<Long> requiredPageId = new ArrayList<>();
                    leftIndexEntityList.forEach(indexEntity -> requiredPageId.add(indexEntity.getId()));
                    pageIndexes.retainAll(requiredPageId);
                }
            }
            Map<PageEntity, Float> pagesRelevance = new HashMap<>();
            float maxRelevance = 0.0F;
            for(Long pageId : pageIndexes){
                Optional<PageEntity> pageEntityOptional = pageRepository.findById(pageId);
                if(pageEntityOptional.isPresent()){
                    PageEntity pageEntity = pageEntityOptional.get();
                    float relevance = getRelevance(pageEntity, requiredLemmas);
                    pagesRelevance.put(pageEntity, relevance);
                    if (relevance > maxRelevance) maxRelevance = relevance;
                }
            }
            for(Map.Entry<PageEntity, Float> pageRelevance : pagesRelevance.entrySet()){
                relevantPages.put(pageRelevance.getKey(), pageRelevance.getValue() / maxRelevance);
            }
        }
        return relevantPages;
    }
    private List<LemmaEntity> getSortedLemmas(String query, long siteId){
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        List<String> requiredLemmas = getRequiredLemma(query);
        for(String lemma : requiredLemmas){
            List<LemmaEntity> lemmas = lemmaRepository.getLemmasList(lemma);
            for(LemmaEntity lemmaEntity : lemmas){
                if(lemmaEntity.getSiteId() == siteId) lemmaEntityList.add(lemmaEntity);
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
            ex.printStackTrace();
        }
        return requestLemmas;
    }
    private float getRelevance(PageEntity pageEntity, List<LemmaEntity> lemmaEntityList){
        float relevance = 0.0F;
        for(LemmaEntity lemmaEntity : lemmaEntityList){
            Optional<IndexEntity> indexEntityOptional =
                    indexRepository.findIndexByLemmaIdAndPageId(pageEntity.getId(), lemmaEntity.getId());
            if(indexEntityOptional.isPresent()){
                IndexEntity indexEntity = indexEntityOptional.get();
                relevance += indexEntity.getRank();
            }
        }
        return relevance;
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
                e.printStackTrace();
            }
            detailedSearchData.setTitle(document.title());
            detailedSearchData.setUri(pageEntity.getPath());
            detailedSearchData.setRelevance(data.getValue());
            detailedSearchData.setSiteName(siteEntity.getName());
            detailedSearchData.setSite(siteEntity.getUrl());
            detailedSearchData.setSnippet(getSnipped(pageEntity.getContent(), query));
        }
        return detailedSearchData;
    }
    private String getSnipped(String content, String query){
        StringBuilder snipped = new StringBuilder();

        Document document = Jsoup.parse(content);
        Elements elements = document.children();
        List<String> contentWords = new ArrayList<>();
        List<String> lemmaContentWords = new ArrayList<>();
        for(Element element : elements){
            String[] words = element.text().split("\\s+");
            for(String word : words){
                contentWords.add(word);
                word = word.replaceAll("\\p{Punct}", "").toLowerCase();
                List<String> lemmaContentWord = getRequiredLemma(word);
                lemmaContentWords.add(lemmaContentWord.get(0));
            }
        }
        String[] queryArgs = query.replaceAll("\\p{Punct}", "").toLowerCase().split("\\s+");
        List<String> lemmaQueryWords = new ArrayList<>();
        for (String word : queryArgs){
            List<String> lemmaQueryWord = getRequiredLemma(word);
            lemmaQueryWords.add(lemmaQueryWord.get(0));
        }

        int start = 0;
        int finish = 0;
        for(String word : lemmaQueryWords){
            for(int i = 0; i < lemmaContentWords.size(); i++){
                if(lemmaContentWords.get(i).equals(word)){
                    word = word.replaceAll(word, "<b>" + contentWords.get(i) + "</b>");
                    contentWords.remove(i);
                    contentWords.add(i, word);
                    if (i > 10){
                        start = i - 10;
                        finish = i + 20;
                    }
                }
            }
        }
        if(finish == 0) return "";
        if(finish > contentWords.size()) finish = contentWords.size();

        for (int i = start; i < finish; i++){
            snipped.append(contentWords.get(i)).append(" ");
        }
        return snipped.toString();
    }
    private void fillInDetailedSearchDataList(String query, long siteId){
        Map<PageEntity, Float> relevantData = getRelevantData(query, siteId);
        for (Map.Entry<PageEntity, Float> data : relevantData.entrySet()){
            DetailedSearchData detailedSearchData = getDetailedSearchData(data, query);
            detailedSearchDataList.add(detailedSearchData);
        }
    }
}
