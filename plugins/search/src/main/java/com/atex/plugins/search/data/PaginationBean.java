package com.atex.plugins.search.data;

import com.google.common.collect.Lists;
import com.polopoly.model.ModelTypeDescription;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Handle the pagination.
 */
public class PaginationBean implements ModelTypeDescription {

    private int currentPage = 0;
    private int pageSize = 0;
    private List<Integer> pages = Lists.newArrayList();
    private int pagesAvailable = 0;

    /**
     * Previous page index.
     *
     * @return an int based on the currentPage.
     */
    public int getPrevPageIndex() {
        return currentPage - 1;
    }

    /**
     * Next page index.
     *
     * @return an int based on the currentPage.
     */
    public int getNextPageIndex() {
        return currentPage + 1;
    }

    /**
     * Current page.
     *
     * @return an int representing the current page.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Set the current page.
     *
     * @param currentPage a positive int..
     */
    public void setCurrentPage(final int currentPage) {
        checkArgument(currentPage > 0);
        this.currentPage = currentPage;
    }

    /**
     * Return the current page size (i.e. number of results per page).
     *
     * @return an int.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Set the page size (i.e. number of results per page).
     *
     * @param pageSize a positive int.
     */
    public void setPageSize(final int pageSize) {
        checkArgument(pageSize > 0);
        this.pageSize = pageSize;
    }

    /**
     * Return an array of indexes.
     *
     * @return a not null array.
     */
    public List<Integer> getPages() {
        return pages;
    }

    /**
     * Generate the indexes array based on the currentPage and pagesAvailable.
     *
     * @param pageNavigatorRadius the number of indexes to generate on either
     *        side of the current page. If one of the sides cannot display its
     *        normal number of pages (because we are at an edge in the result)
     *        the other side will be expanded to attempt to make the navigator
     *        1 + (pageNavigatorRadius * 2) in size.
     */
    public void generateNavigatorPagesIndex(final int pageNavigatorRadius) {
        pages.clear();

        int minPageNaive = currentPage - pageNavigatorRadius;
        int maxPageNaive = currentPage + pageNavigatorRadius;
        int maxPage = maxPageNaive + Math.max(1 - minPageNaive, 0);
        int minPage = minPageNaive - Math.max(maxPageNaive - pagesAvailable, 0);

        for (int i = Math.max(minPage, 1); i <= Math.min(maxPage, pagesAvailable); i++) {
            pages.add(Integer.valueOf(i));
        }
    }

    /**
     * Return the number of available pages.
     *
     * @return an int.
     */
    public int getPagesAvailable() {
        return pagesAvailable;
    }

    /**
     * Set the number of available pages.
     *
     * @param pagesAvailable a positive int (it can be zero too).
     */
    public void setPagesAvailable(final int pagesAvailable) {
        checkArgument(pagesAvailable >= 0);
        this.pagesAvailable = pagesAvailable;
    }

    /**
     * Tell you if you should show the next button. Based on currentPage and pagesAvailable.
     *
     * @return a boolean.
     */
    public boolean isShowNext() {
        return (currentPage < pagesAvailable);
    }

    /**
     * Tell you if you should show the previous button. Based on currentPage.
     *
     * @return a boolean.
     */
    public boolean isShowPrevious() {
        return (currentPage > 1);
    }

}
