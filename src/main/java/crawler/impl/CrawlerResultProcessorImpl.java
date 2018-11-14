package crawler.impl;

import crawler.CrawlerResultProcessor;

import java.util.List;
import java.util.Map;

public class CrawlerResultProcessorImpl implements CrawlerResultProcessor {

    private boolean processObject = false;
    private boolean processFragment = false;
    private boolean processList = false;

    public CrawlerResultProcessorImpl() {
    }

    public CrawlerResultProcessorImpl(boolean processObject, boolean processFragment, boolean processList) {
        this.processObject = processObject;
        this.processFragment = processFragment;
        this.processList = processList;
    }

    public void setProcessObject(boolean processObject) {
        this.processObject = processObject;
    }

    public void setProcessFragment(boolean processFragment) {
        this.processFragment = processFragment;
    }

    public void setProcessList(boolean processList) {
        this.processList = processList;
    }

    @Override
    public boolean isNeededToProcessObject() {
        return processObject;
    }

    @Override
    public boolean isNeededToProcessFragmentList() {
        return processFragment;
    }

    @Override
    public boolean isNeededToProcessList() {
        return processList;
    }

    @Override
    public void processResultObject(Map<String, String> object) {
        System.out.println(object);
    }

    @Override
    public void processResultFragmentList(List<Map<String, String>> list) {
        System.out.println("Fragment size: " + list.size());
        System.out.println();
    }

    @Override
    public void processResultList(List<Map<String, String>> list) {
        System.out.println("Total records: " + list.size());
    }
}
