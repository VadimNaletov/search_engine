package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.Indexing;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final Indexing indexing;
    @Override
    public ResponseService startIndexing() {
        ResponseService responseService;
        boolean isIndexing;
        try {
            isIndexing = indexing.allSiteIndexing();
            if(isIndexing) {
                responseService = new TrueResponseService();
            } else {
                responseService = new FalseResponseService();
            }
        } catch (InterruptedException e) {
            responseService = new FalseResponseService();
            e.printStackTrace();
        }
        return responseService;
    }

    @Override
    public ResponseService stopIndexing() {
        boolean isIndexingStopped;
        isIndexingStopped = indexing.stopIndexingAllSites();
        if(isIndexingStopped){
            return new TrueResponseService();
        } else {
            return new FalseResponseService();
        }
    }

    @Override
    public ResponseService indexPage(String url){
        boolean response = indexing.startOneSiteIndexing(url);
        if(response){
            return new TrueResponseService();
        } else {
            return new FalseResponseService();
        }
    }
}
