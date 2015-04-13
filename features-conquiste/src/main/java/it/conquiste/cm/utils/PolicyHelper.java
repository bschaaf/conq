package it.conquiste.cm.utils;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.CheckboxPolicy;
import com.polopoly.cm.app.policy.DateTimePolicy;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;

/**
 * Helper class for policies.
 *
 * @author mnova
 */
public class PolicyHelper {

    private static final Logger LOGGER = Logger.getLogger(PolicyHelper.class.getName());

    private final ContentPolicy policy;

    public PolicyHelper(final ContentPolicy policy) {
        this.policy = policy;
    }

    /**
     * Convenience method to access value of <code>SingleValued</code> child policies. The method is null-safe and will
     * return "" if the child policy or component doesn't exist.
     *
     * @param name
     *          name of the child policy
     *
     * @return the value of the child policy
     * @exception com.polopoly.cm.client.CMException
     *              if an error occurs
     */
    public final String getChildValue(final String name) {
        return getChildValue(name, "");
    }

    /**
     * Convenience method to access value of <code>SingleValued</code> child policies. The method is null-safe and will
     * return the given default value if the child policy or component doesn't exist.
     *
     * @param name
     *          the name of the child policy
     * @param defaultValue
     *          the desired default value
     *
     * @return the value of the child policy
     * @exception com.polopoly.cm.client.CMException
     *              if an error occurs
     */
    public final String getChildValue(final String name, final String defaultValue) {
        try {
            final SingleValued child = (SingleValued) getChildPolicy(name);

            if (child == null) {
                return defaultValue;
            }

            return child.getValue() != null ? child.getValue() : defaultValue;
        } catch (final ClassCastException cce) {
            LOGGER.warning(name + " in " + getContentId() + " has unsupported policy.");
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting child value", e);
        }

        return defaultValue;
    }

    public void setChildValue(final String singleValuedPolicyName, final String value) throws CMException {
        final Policy singleValued = getChildPolicy(singleValuedPolicyName);
        if (singleValued instanceof SingleValued) {
            ((SingleValued) singleValued).setValue(value);
        } else {
            LOGGER.warning("Failed to set value=" + value + " for " + singleValuedPolicyName + " in " + getContentId()
                    + "(policy was " + (singleValued != null ? singleValued.getClass().getName() : "null") + ")");
        }
    }

    public ContentId getChildReference(final String name) {
        try {
            final SingleReference ref = (SingleReference) getChildPolicy(name);
            if (ref != null) {
                return ref.getReference();
            } else {
                LOGGER.warning("Child policy " + name + " is null for content " + getContentId().getContentIdString());
            }
        } catch (final CMException e) {
            LOGGER.warning("Cannot get child policy " + name + " for content " + getContentId().getContentIdString());
        }
        return null;
    }

    public void setChildReference(final String name, final ContentId contentId) throws CMException {
        final SingleReference ref = (SingleReference) getChildPolicy(name);
        if (ref != null) {
            if (contentId != null) {
                ref.setReference(contentId.getContentId());
            } else {
                ref.setReference(null);
            }
        } else {
            LOGGER.warning("Child policy " + name + " is null for content " + getContentId().getContentIdString());
        }
    }

    public void setCheckboxValue(final String name, final boolean value) throws CMException {
        final CheckboxPolicy check = (CheckboxPolicy) getChildPolicy(name);
        if (check != null) {
            check.setChecked(value);
        }
    }

    public boolean getCheckboxValue(final String name, final boolean defaultValue) {
        try {
            final CheckboxPolicy check = (CheckboxPolicy) getChildPolicy(name);
            if (check != null) {
                return check.getChecked();
            }
        } catch (final CMException e) {
            LOGGER.log(Level.SEVERE, "Cannot get " + name + " value: " + e.getMessage(), e);
        }
        return defaultValue;
    }

    public boolean getCheckboxValue(final String name) {
        return getCheckboxValue(name, false);
    }

    public <E extends Enum<E> & EnumWithId> E getChildEnumValue(final String name, final E defaultValue)
    {
        final String enumValue = getChildValue(name, null);
        if (enumValue != null) {
            try {
                final int id = Integer.parseInt(enumValue);
                for (final E e : java.util.EnumSet.allOf(defaultValue.getDeclaringClass())) {
                    if (e.getId() == id) {
                        return e;
                    }
                }
                LOGGER.severe(name + " " + id + " not found for " + getContentId().getContentIdString());
            } catch (final NumberFormatException e) {
                LOGGER.log(Level.SEVERE, "Cannot parse " + name + " " + enumValue + " for "
                        + getContentId().getContentIdString(), e);
            }
        }
        return defaultValue;
    }

    public <E extends Enum<E> & EnumWithId> void setChildEnumValue(final String name, final E e) throws CMException
    {
        setChildValue(name, Integer.toString(e.getId()));
    }

    /**
     * Get the given date value.
     *
     * @param name the child policy name.
     * @return null if the value is not set.
     */
    public Date getChildDateValue(final String name) {
        return getChildDateValue(name, null);
    }

    /**
     * Get the given date value.
     *
     * @param name the child policy name.
     * @param defaultValue the default value if the child policy is not set.
     * @return the value from the child policy if it has been set or the default value.
     */
    public Date getChildDateValue(final String name, final Date defaultValue) {
        try {
            DateTimePolicy date = (DateTimePolicy) policy.getChildPolicy(name);
            if (date != null && date.hasValueSet()) {
                return date.getDate();
            }
        } catch (CMException e) {
            LOGGER.warning("Cannot get child policy " + name + " for content " + getContentId().getContentIdString());
        }
        return defaultValue;
    }

    /**
     * Set the date value.
     *
     * @param name the child policy name.
     * @param value the value (set it to null if you want to remove/unset it).
     * @throws CMException
     */
    public void setChildDateValue(final String name, final Date value) throws CMException {
        final Policy childPolicy = policy.getChildPolicy(name);
        if (childPolicy instanceof DateTimePolicy) {
            final DateTimePolicy dateTimePolicy = (DateTimePolicy) childPolicy;
            if (value != null) {
                dateTimePolicy.setTime(value);
            } else {
                dateTimePolicy.setComponent(DateTimePolicy.DATE_COMPONENT, null);
            }
        } else {
            LOGGER.warning("Failed to set value=" + value + " for " + name + " in " + getContentId()
                    + "(policy was " + (childPolicy != null ? childPolicy.getClass().getName() : "null") + ")");
        }
    }

    private Policy getChildPolicy(final String policyName) throws CMException {
        return policy.getChildPolicy(policyName);
    }

    private ContentId getContentId() {
        return policy.getContentId();
    }

    protected Date getModified() {
        Date commitDate = policy.getVersionInfo().getVersionCommitDate();
        if (commitDate == null) {
            return new Date();
        }
        return commitDate;
    }

}
