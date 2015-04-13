package com.atex.gong.utils;

import java.util.logging.Logger;

import com.google.inject.Inject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.common.lang.ArrayUtil;

/**
 * Injectable test utility to provide access to test content.
 */
public class ContentTestUtil {

    private static final Logger LOGGER = Logger.getLogger(ContentTestUtil.class.getName());
    public static final String DEFAULT_SUFFIX = ".content";

    @Inject
    private PolicyCMServer cmServer;

    /**
     * Get the standard test content external id for the given test class, but with a custom suffix
     * (which will replace the default '.content').
     */
    public String getTestContentId(final Class<?> testClass, final String suffix) {
        return testClass.getSimpleName() + suffix;
    }

    /**
     * Get the standard test content external id for the caller, but with a custom suffix
     * (which will replace the default '.content'). Uses magic to determine the class
     * of the caller.
     */
    public String getTestContentId(final String suffix) {
        return getCallerClassName() + suffix;
    }

    /**
     * Get the standard test content external id for the given test class.
     */
    public String getTestContentId(final Class<?> testClass) {
        return getTestContentId(testClass, DEFAULT_SUFFIX);
    }

    /**
     * Get the standard test content external id for the caller.
     * Uses magic to determine the class of the caller.
     */
    public String getTestContentId() {
        return getTestContentId(DEFAULT_SUFFIX);
    }

    public Policy getTestPolicy(final Class<?> testClass, final String suffix) throws CMException {
        return getPolicyForTesting(getTestContentId(testClass, suffix));
    }

    public Policy getTestPolicy(final String suffix) throws CMException {
        return getPolicyForTesting(getTestContentId(suffix));
    }

    public Policy getTestPolicy(final Class<?> testClass) throws CMException {
        return getPolicyForTesting(getTestContentId(testClass));
    }

    public Policy getTestPolicy() throws CMException {
        return getPolicyForTesting(getTestContentId());
    }

    public String getTestInputTemplateId() throws CMException {
        return getTestPolicy().getInputTemplate().getExternalId().getExternalId();
    }

    public String getTestInputTemplateId(final Class<?> testClass) throws CMException {
        return getTestPolicy(testClass).getInputTemplate().getExternalId().getExternalId();
    }

    public Policy getPolicyForTesting(final String externalContentIdString) throws CMException {
        ExternalContentId externalContentId = new ExternalContentId(externalContentIdString);
        LOGGER.info("Trying to fetch: " + externalContentIdString);
        ContentId contentId = cmServer.findContentIdByExternalId(externalContentId);
        if (contentId == null) {
            throw new ContentOperationFailedException(
                    "Unable to find content using id " + externalContentId.getExternalId());
        }
        return cmServer.getPolicy(contentId);
    }

    public Policy getPolicyForTesting(final ContentId contentId) throws CMException {
        LOGGER.info("Trying to fetch: " + contentId.getContentIdString());
        return cmServer.getPolicy(contentId);
    }

    private String getCallerClassName() {
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        for (int i = 0; i < stackElements.length; i++) {
            if (!stackElements[i].getClassName().equals(ContentTestUtil.class.getName())) {
                try {
                    String guess = stackElements[i].getClassName();
                    return Class.forName(guess).getSimpleName();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("This can't happen: found class " + stackElements[i].getClassName()
                            + " in stack trace but was unable to load that class.", e);
                }
            }
        }
        throw new RuntimeException("Unable to detect calling test in stacktrace:" + ArrayUtil.toString(stackElements));
    }
}
