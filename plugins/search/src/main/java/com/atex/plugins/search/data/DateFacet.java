package com.atex.plugins.search.data;

import com.polopoly.model.ModelTypeDescription;

/**
 * Contains configuration for a date range facet.
 */
public class DateFacet implements ModelTypeDescription {

    private final String name;
    private final String start;
    private final String end;
    private final String gap;
    private final String formatValue;

    /**
     * Constructor.
     *
     * @param name        field name.
     * @param start       range start
     * @param end         range end
     * @param gap         range gap
     * @param formatValue format to be used for a {@link java.text.SimpleDateFormat#format(java.util.Date)}.
     */
    public DateFacet(final String name, final String start, final String end, final String gap,
                     final String formatValue) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.gap = gap;
        this.formatValue = formatValue;
    }

    /**
     * The field name.
     *
     * @return a not null String.
     */
    public String getName() {
        return name;
    }

    /**
     * The range start, it is parsed by {@link DateMathParser#parseMath(String)}.
     *
     * @return a not null String.
     */
    public String getStart() {
        return start;
    }

    /**
     * The range end, it is parsed by {@link DateMathParser#parseMath(String)}.
     *
     * @return a not null String.
     */
    public String getEnd() {
        return end;
    }

    /**
     * The range gap, it is parsed by {@link DateMathParser#parseMath(String)}.
     *
     * @return a not null String.
     */
    public String getGap() {
        return gap;
    }

    /**
     * The format that will be used with {@link java.text.SimpleDateFormat#format(java.util.Date)} to format the facet
     * value when it is displayed in the site.
     *
     * @return a not null String, it may be empty.
     */
    public String getFormatValue() {
        return formatValue;
    }

}
