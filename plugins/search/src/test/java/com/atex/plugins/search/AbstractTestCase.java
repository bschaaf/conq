package com.atex.plugins.search;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.common.lang.StringUtil;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Base abstract test class.
 */
@Ignore
public abstract class AbstractTestCase extends TestCase {

    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Random rnd = new Random(new Date().getTime());

    /**
     * Look in the provided argument captures for an argument named "name".
     *
     * @param ac1  names argument capture
     * @param ac2  values argument capture
     * @param name name of the argument to be search for.
     * @param <T>  expected type.
     * @return the value for the argument or will fail the test.
     */
    protected <T> T getArg(final ArgumentCaptor<String> ac1, final ArgumentCaptor<T> ac2, final String name) {
        final List<String> names = ac1.getAllValues();
        final List<T> objects = ac2.getAllValues();

        assertEquals(names.size(), objects.size());

        // we will return the last one in case it has been overwritten.

        T value = null;
        for (int idx = 0; idx < names.size(); idx++) {
            final String n = names.get(idx);
            if (StringUtil.equals(n, name)) {
                value = objects.get(idx);
            }
        }

        if (value == null) {
            fail("Argument " + name + " has not been captured");
        }

        return value;
    }

    /**
     * Create a random {@link com.polopoly.cm.ContentId}.
     *
     * @param major
     * @return a not null {@link com.polopoly.cm.ContentId}
     */
    protected ContentId getRandomContentId(final int major) {
        final int minor = Math.abs(rnd.nextInt());
        return new ContentId(major, minor);
    }

    /**
     * Create a random {@link com.polopoly.cm.VersionedContentId}.
     *
     * @param major
     * @return a not null {@link com.polopoly.cm.VersionedContentId}
     */
    protected VersionedContentId getRandomVersionedContentId(final int major) {
        final int minor = Math.abs(rnd.nextInt());
        final int version = Math.abs(rnd.nextInt());
        return new VersionedContentId(major, minor, version);
    }
}
