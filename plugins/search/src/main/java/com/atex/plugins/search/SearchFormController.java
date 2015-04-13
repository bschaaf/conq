package com.atex.plugins.search;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.common.collect.Lists;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;
import com.polopoly.util.StringUtil;

/**
 * Search form controller.
 */
public class SearchFormController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(SearchFormController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request, final TopModel m,
                                            final ControllerContext context) {

        super.populateModelBeforeCacheKey(request, m, context);

        final Model model = m.getLocal();

        // Get query string
        final String q = getQueryParam("q", request);

        if (!StringUtil.isEmpty(q)) {
            ModelPathUtil.set(model, "q", q);
        }

    }

    protected String getQueryParam(final String param, final RenderRequest request) {

        String value = request.getParameter(param);

        if (!StringUtil.isEmpty(value)) {
            // clean up all html from the query itself.
            return cleanup(value);
        } else {
            return "";
        }

    }

    protected List<String> getQueryParams(final String param, final RenderRequest request) {

        final List<String> paramValues = Lists.newArrayList();

        if (request instanceof HttpServletRequest) {
            final String[] values = ((HttpServletRequest) request).getParameterValues(param);

            if (values != null) {
                for (final String value : values) {
                    if (!StringUtil.isEmpty(value)) {
                        // clean up all html from the query itself.
                        paramValues.add(cleanup(value));
                    }
                }
            }
        }

        return paramValues;

    }

    protected Integer getIntQueryParam(final String param, final RenderRequest request) {
        final String value = getQueryParam(param, request);
        if (!StringUtil.isEmpty(value)) {
            try {
                int intValue = Integer.parseInt(value);
                return Integer.valueOf(intValue);
            } catch (NumberFormatException e) {
                LOGGER.warning(value + " is not a number for param "  + param);
            }
        }
        return null;
    }

    private String cleanup(final String value) {

        return StringEscapeUtils.unescapeHtml(Jsoup.clean(value, Whitelist.none()));

    }
}
