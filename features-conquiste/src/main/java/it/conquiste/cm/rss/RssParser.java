package it.conquiste.cm.rss;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RssParser {

    public RssFeed parse(InputStream inputStream) throws RssParserException {
        
        RssFeed rssFeed = null;
        try {
            DocumentBuilder documentBuilder = createDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            
            Element rootElement = document.getDocumentElement();
            
            NodeList channels = rootElement.getElementsByTagName("channel");
            
            if (!"rss".equals(rootElement.getTagName()) || channels.getLength() == 0) {
                throw new RssParserException("Invalid RSS XML Document");
            }
            
            rssFeed = new RssFeed();
            
            NodeList items = rootElement.getElementsByTagName("item");
            int length = items.getLength();
            for (int i = 0; i < length; i++) {
                
                Node item = items.item(i);
                
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) item;
                    
                    RssItem rssItem = new RssItem();
                    Element title = getElementByName(itemElement, "title");
                    if (title != null) {
                        rssItem.setTitle(title.getTextContent());
                    }
                    
                    Element description = getElementByName(itemElement, "description");
                    if (description != null) {
                        rssItem.setDescription(description.getTextContent());
                    }
                    
                    Element link = getElementByName(itemElement, "link");
                    if (link != null) {
                        rssItem.setLink(link.getTextContent());
                    }
                    
                    Element guid = getElementByName(itemElement, "guid");
                    if (guid != null) {
                        rssItem.setGuid(guid.getTextContent());
                    }
                    
                    Element pubDate = getElementByName(itemElement, "pubDate");
                    if (pubDate != null) {
                        rssItem.setPubDate(pubDate.getTextContent());
                    }
                    
                    Element author = getElementByName(itemElement, "author");
                    if (author != null) {
                        rssItem.setAuthor(author.getTextContent());
                    }
                    
                    rssFeed.addItem(rssItem);
                }
            }
            
        } catch (Exception e) {
            throw new RssParserException("Failed to parse RSS input stream", e);
        }
        return rssFeed;
    }

    private Element getElementByName(Element element, String tagName) {
        
        Element theElement = null;
        
        NodeList nodeList = element.getElementsByTagName(tagName);
        
        if (nodeList.getLength() > 0) {
            theElement = (Element) nodeList.item(0);
        }
        return theElement;
    }

    private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        return builderFactory.newDocumentBuilder();
    }
}
