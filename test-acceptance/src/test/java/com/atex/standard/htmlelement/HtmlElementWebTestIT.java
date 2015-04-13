package com.atex.standard.htmlelement;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.atex.gong.AbstractWebTestBase;
import com.google.inject.Inject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.siteengine.structure.SitePolicy;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnj.TestNJRunner;
import com.polopoly.util.IdGenerator;

@RunWith(TestNJRunner.class)
public class HtmlElementWebTestIT
        extends AbstractWebTestBase {
    private static IdGenerator idGenerator = new IdGenerator(HtmlElementWebTestIT.class.getName(),
            "topsecret");
    @Inject
    private PolicyCMServer policyCMServer;
    @Inject
    private ChangeList changeList;

    @Test
    public void testHtmlElementOnSite() throws Exception {
        SitePolicy site = (SitePolicy) policyCMServer.createContent(
                policyCMServer.getMajorByName(DefaultMajorNames.DEPARTMENT),
                new ExternalContentId("p.siteengine.Sites.d"),
                new ExternalContentId("p.siteengine.Site"));
        site.setName("Kalle bolle");
        policyCMServer.commitContent(site);

        ContentId siteId = site.getContentId().getContentId();
        Policy element = policyCMServer.createContent(policyCMServer.getMajorByName(DefaultMajorNames.LAYOUTELEMENT),
                siteId,
                new ExternalContentId("standard.HtmlElement"));
        String h2Id = idGenerator.createId();
        element.getContent().setComponent("code",
                "value",
                String.format("<h2 id=\"%s\">Fine html element</h2>", h2Id));
        policyCMServer.commitContent(element);

        changeList.waitFor("preview");

        webTest.loadPage(String.format("/cmlink/%s/%s",
                siteId.getContentIdString(),
                element.getContentId().getContentId().getContentIdString()));

        webTest.assertContainsXPath(String.format("//h2[@id = '%s']", h2Id));
    }
}
