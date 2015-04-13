package com.atex.plugins.social.sharing;

import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.mockito.ArgumentCaptor;

import com.polopoly.util.StringUtil;

/**
 * Base abstract test.
 */
@Ignore
public class AbstractTest {

    /**
     * Look in the provided argument captures for an argument named "name".
     *
     * @param ac1 names argument capture
     * @param ac2 values argument capture
     * @param name name of the argument to be search for.
     * @param <T> expected type.
     * @return the value for the argument or will fail the test.
     */
    protected  <T> T getArg(final ArgumentCaptor<String> ac1, final ArgumentCaptor<T> ac2, final String name) {

        final List<String> names = ac1.getAllValues();
        final List<T> objects = ac2.getAllValues();

        Assert.assertEquals(names.size(), objects.size());

        // we will return the last one in case it has been overwritten.

        T value = null;
        for (int idx = 0; idx < names.size(); idx++) {
            final String n = names.get(idx);
            if (StringUtil.equals(n, name)) {
                value = objects.get(idx);
            }
        }

        if (value == null) {
            Assert.fail("Argument " + name + " has not been captured");
        }

        return value;
    }

    /**
     * Look in the provided argument captures that the argument named "name" is missing.
     *
     * @param ac1 names argument capture
     * @param ac2 values argument capture
     * @param name name of the argument to be search for.
     * @param <T> expected type.
     */
    protected  <T> void missingArg(final ArgumentCaptor<String> ac1, final ArgumentCaptor<T> ac2, final String name) {

        final List<String> names = ac1.getAllValues();
        final List<T> objects = ac2.getAllValues();

        Assert.assertEquals(names.size(), objects.size());

        // we will return the last one in case it has been overwritten.

        T value = null;
        for (int idx = 0; idx < names.size(); idx++) {
            final String n = names.get(idx);
            if (StringUtil.equals(n, name)) {
                value = objects.get(idx);
            }
        }

        if (value != null) {
            Assert.fail("Argument " + name + " has been captured");
        }

    }

}
