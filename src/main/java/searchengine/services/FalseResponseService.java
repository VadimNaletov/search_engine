package searchengine.services;

public class FalseResponseService implements ResponseService{

    @Override
    public boolean getResult() {
        return false;
    }
}
