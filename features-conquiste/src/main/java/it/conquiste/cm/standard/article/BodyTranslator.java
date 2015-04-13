package it.conquiste.cm.standard.article;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.atex.plugins.file.FilePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.servlet.HtmlPathUtil;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.path.ContentPathCreator;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.render.RenderRequest;
/*
 * Translator for Polopoly content links and images.
 */
public class BodyTranslator {

	private static final Logger LOG = Logger.getLogger(BodyTranslator.class.getName());

	public String translateBody(final RenderRequest request,
			final ContentId contentId,
			final String body,
			final boolean inPreviewMode) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		ContentPathCreator pathCreator = RequestPreparator.getPathCreator(httpServletRequest);

		URLBuilder builder = RequestPreparator.getURLBuilder(httpServletRequest);

		PolicyCMServer cmServer = RequestPreparator.getCMServer(httpServletRequest);

		// Use latest version when previewing
		VersionedContentId latestVersionContentId = null;

		if (inPreviewMode) {
			latestVersionContentId = contentId.getLatestVersionId();
		}

		try {

			String bodyTranslated = translateFileElements(body, cmServer);
			// If we are in preview mode we keep Polopoly attributes on links
			if (inPreviewMode) {
				return HtmlPathUtil.pathify(bodyTranslated,
						pathCreator,
						builder,
						httpServletRequest,
						latestVersionContentId,
						true);
			} else {
				return HtmlPathUtil.pathify(bodyTranslated,
						pathCreator,
						builder,
						httpServletRequest,
						latestVersionContentId);
			}
		} catch (CMException e) {
			LOG.log(Level.WARNING, "Could not parse body.", e);
		}
		return body;
	}

	private String translateFileElements(String body, PolicyCMServer cmServer) throws CMException {
		String patternString = "<a.*?polopoly:contentid=\\\"(.*?)\\\".*?>";
		Pattern      pattern      = Pattern.compile(patternString, Pattern.DOTALL);
		Matcher      matcher      = pattern.matcher(body);
		StringBuffer stringBuffer = new StringBuffer();

		while(matcher.find()){
			String replacement = "";
			ContentId contentId = ContentIdFactory.createContentId(matcher.group(1));
			Policy policy = cmServer.getPolicy(contentId);	
			int version = policy.getContentId().getVersion();
			if(policy instanceof FilePolicy){
				String filePath = ((FilePolicy) policy).getContentData().getFilePath();
				replacement = "<a href=\"/filedelivery/policy:" + matcher.group(1) + ':' + version + '/' + filePath + "\">";
			} else replacement = matcher.group(0);
			matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(stringBuffer);
		return stringBuffer.toString();
	}
}
