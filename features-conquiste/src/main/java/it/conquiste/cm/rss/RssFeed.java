package it.conquiste.cm.rss;

import java.util.ArrayList;
import java.util.List;

public class RssFeed {
    
    List<RssItem> items = new ArrayList<RssItem>();

    public List<RssItem> getItems() {
        return items;
    }

    public void addItem(RssItem rssItem) {
        items.add(rssItem);
    }
}
