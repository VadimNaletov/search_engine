package searchengine.services;

public interface IndexingService {
    ResponseService startIndexing();
    ResponseService stopIndexing();
    ResponseService indexPage(String url);
}
