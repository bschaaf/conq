package it.conquiste.plugins.sitemap.policies.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.policy.Policy;

/**
 * Utility class for accessing child policies with common error handling and fall backs.
 * @author sarasprang
 */
public class PolicyUtils {

    private static Logger LOG = Logger.getLogger(PolicyUtils.class.getName());

    /**
     * Utility method to get a single value
     *
     * @param policy the policy to get the value from
     * @param name name of the component
     * @return String or null if none found or Exception caught
     */
    public static String getSingleValue(Policy policy, String name) {
        String ret = null;
        try {
            Policy childPolicy = policy.getChildPolicy(name);
            if (childPolicy != null) {
                ret = ((SingleValued) childPolicy).getValue();
            }
        } catch (Exception e) {
            LOG.log(Level.FINEST, "Error getting single value.", e);
        }
        return ret;
    }

    /**
     * Utility method to get a single value or default value if missing
     *
     * @param policy the policy to get the value from
     * @param name name of the component
     * @param defaultValue
     * @return String or defaultValue if none found or Exception caught
     */
    public static String getSingleValue(Policy policy, String name, String defaultValue) {
        String value = getSingleValue(policy, name);
        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Utility method to check if check box is checked or default value if missing
     *
     * @param defaultValue value of the boolean if not found.
     * @param policy the policy to get the value from
     * @param name name of the component
     * @return true if checked or false if not checked, none found or Exception caught
     */
    public static boolean isChecked(boolean defaultValue, Policy policy, String name) {
        boolean ret = defaultValue;
        try {
            Policy childPolicy = policy.getChildPolicy(name);
            ret = ((CheckboxPolicy) childPolicy).getChecked();
        } catch (Exception e) {
            LOG.log(Level.FINEST, "Error getting isChecked.", e);
        }
        return ret;
    }
}