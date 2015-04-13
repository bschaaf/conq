package it.conquiste.cm.rss;


@SuppressWarnings("serial")
public class RssFeedException extends Exception {

    public RssFeedException(String message, Exception e) {
        super(message, e);
    }
}
