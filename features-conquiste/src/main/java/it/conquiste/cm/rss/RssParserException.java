package it.conquiste.cm.rss;

@SuppressWarnings("serial")
public class RssParserException extends Exception {

    public RssParserException(String message, Exception e) {
        super(message, e);
    }

    public RssParserException(String message) {
        super(message);
    }
}
