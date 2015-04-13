package com.atex.gong.landingpage;

import com.atex.gong.AbstractWebTestBase;
import org.junit.Test;

public class TopicWebTestIT extends AbstractWebTestBase {

    @Test
    public void testTopic() throws Exception {
        webTest.loadPage("/cmlink/polopoly-post/topics/Person/Barack%20Obama");
        webTest.assertXPathExpression("//h1[@class='entity-title']/span[@class='entity-section']/text()", "Topic");
        webTest.assertXPathExpression("//h1[@class='entity-title']/span[@class='entity-name']/text()", "Barack Obama");
    }

    @Test
    public void testTopicWithoutContent() throws Exception {
        webTest.loadPage("/cmlink/polopoly-post/topics/Person/Iker%20Casillas");
        webTest.assertXPathExpression("//h1[@class='entity-title']/span[@class='entity-section']/text()", "Topic");
        webTest.assertXPathExpression("//h1[@class='entity-title']/span[@class='entity-name']/text()", "Iker Casillas");
    }

}
