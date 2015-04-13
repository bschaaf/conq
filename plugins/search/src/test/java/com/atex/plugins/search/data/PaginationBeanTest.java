package com.atex.plugins.search.data;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.atex.plugins.search.AbstractTestCase;

/**
 * Unit test for {@link com.atex.plugins.search.data.PaginationBean}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaginationBeanTest extends AbstractTestCase {

    private PaginationBean bean = new PaginationBean();

    @Test
    public void test_current_page() {
        assertEquals(0, bean.getCurrentPage());

        final int page = randomButAtLeast(1);

        bean.setCurrentPage(page);

        assertEquals(page, bean.getCurrentPage());
    }

    @Test
    public void test_pagesAvailable() {
        assertEquals(0, bean.getPagesAvailable());

        final int pagesAvailable = randomButAtLeast(1);

        bean.setPagesAvailable(pagesAvailable);

        assertEquals(pagesAvailable, bean.getPagesAvailable());
    }

    @Test
    public void test_prev() {
        assertEquals(-1, bean.getPrevPageIndex());
        assertEquals(false, bean.isShowPrevious());

        final int page = randomButAtLeast(2);

        bean.setCurrentPage(page);

        assertEquals(page - 1, bean.getPrevPageIndex());
        assertEquals(true, bean.isShowPrevious());
    }

    @Test
    public void test_next() {
        assertEquals(1, bean.getNextPageIndex());
        assertEquals(false, bean.isShowNext());

        final int pagesAvailable = 10;
        final int page = randomWithinInterval(1, pagesAvailable - 1);

        bean.setPagesAvailable(pagesAvailable);
        bean.setCurrentPage(page);

        assertEquals(page + 1, bean.getNextPageIndex());
        assertEquals(true, bean.isShowNext());
    }

    @Test
    public void test_pages_full_navigator() {
        assertNotNull(bean.getPages());
        assertEquals(0, bean.getPages().size());

        final int navigatorRadius = 3;
        final int navigatorWidth = navigatorRadius * 2 + 1;
        final int lastPage = navigatorWidth * 2;

        bean.setPagesAvailable(lastPage);

        bean.setCurrentPage(1);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, navigatorWidth);

        bean.setCurrentPage(navigatorRadius + 1);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, navigatorWidth);

        bean.setCurrentPage(navigatorRadius + 2);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(2, navigatorWidth + 1);

        int currentPage = lastPage - navigatorRadius;
        bean.setCurrentPage(currentPage);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(lastPage - navigatorWidth + 1, lastPage);

        currentPage = currentPage + 1;
        bean.setCurrentPage(currentPage);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(lastPage - navigatorWidth + 1, lastPage);

        currentPage = lastPage;
        bean.setCurrentPage(currentPage);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(lastPage - navigatorWidth + 1, lastPage);
    }

    @Test
    public void test_pages_exactly_at_navigator_width() {
        assertNotNull(bean.getPages());
        assertEquals(0, bean.getPages().size());

        final int navigatorRadius = 3;
        final int navigatorWidth = navigatorRadius * 2 + 1;
        final int lastPage = navigatorWidth;

        bean.setPagesAvailable(lastPage);

        bean.setCurrentPage(1);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, navigatorWidth);

        bean.setCurrentPage(lastPage / 2);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, navigatorWidth);

        bean.setCurrentPage(lastPage);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, navigatorWidth);
    }

    @Test
    public void test_pages_less_than_navigator_width() {
        assertNotNull(bean.getPages());
        assertEquals(0, bean.getPages().size());

        final int navigatorRadius = 3;
        final int navigatorWidth = navigatorRadius * 2 + 1;
        final int lastPage = navigatorWidth - 4;

        bean.setPagesAvailable(lastPage);

        bean.setCurrentPage(1);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, lastPage);

        bean.setCurrentPage(2);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, lastPage);

        bean.setCurrentPage(lastPage);
        bean.generateNavigatorPagesIndex(navigatorRadius);
        assertNavigatorPageRange(1, lastPage);
    }

    private void assertNavigatorPageRange(final int expectedFirst,
                                      final int expectedLast) {
        List<Integer> pages = this.bean.getPages();
        assertNotNull(pages);
        assertEquals(expectedLast - expectedFirst + 1, pages.size());
        assertEquals(expectedFirst, pages.get(0).intValue());
        assertEquals(expectedLast, pages.get(pages.size() - 1).intValue());
    }

    private int randomButAtLeast(final int min) {
        return randomWithinInterval(min, min + 100); // Some arbitrary larger number
    }

    private int randomWithinInterval(final int min, final int max) {
        return rnd.nextInt(max + 1 - min) + min;
    }

}
