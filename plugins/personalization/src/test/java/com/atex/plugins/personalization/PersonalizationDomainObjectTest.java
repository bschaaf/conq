package com.atex.plugins.personalization;

import com.google.gson.Gson;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PersonalizationDomainObjectTest {

    private final Personalization personalization = new Personalization();
    private final PersonalizationDomainObject personalizationDomainObject =
            new PersonalizationDomainObject(personalization);

    /**
     * Test method getEntitiesSortedByCount given list of views is empty.
     */
    @Test
    public void testGetEntitiesSortedByCountGivenEmptyListOfViews() {
        // Given
        personalization.setViews(new ArrayList<Personalization.View>());

        // Then
        final Collection<Dimension> entities = personalizationDomainObject.getEntitiesSortedByCount(10);
        assertTrue(entities.isEmpty());
    }

    /**
     * Test method getEntitiesSortedByCount given multiple views with no entities.
     */
    @Test
    public void testGetEntitiesSortedByCountGivenMultipleViewAndNoEntities() {
        // Given
        List<Personalization.View> views = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            views.add(new Personalization.View());
        }
        personalization.setViews(views);

        // Then
        Collection<Dimension> entities = personalizationDomainObject.getEntitiesSortedByCount(10);
        assertTrue(entities.isEmpty());
    }


    /**
     * Test method getEntitiesSortedByCount given list of views is of size 1 and list of entities is 1.
     */
    @Test
    public void testGetEntitiesSortedByCountGivenOneViewWithOneEntity() {
        // Given
        final Dimension entity = new Dimension("Dimension1", "Dimension1", false, new Entity("My entity"));
        final List<Dimension> expectedEntities = new ArrayList<>();
        expectedEntities.add(entity);
        final Personalization.View view = new Personalization.View();
        view.setDimensions(expectedEntities);
        final List<Personalization.View> views = new ArrayList<>();
        views.add(view);
        personalization.setViews(views);

        // Then
        final Collection<Dimension> actualEntities = personalizationDomainObject.getEntitiesSortedByCount(10);
        assertEquals(1, actualEntities.size());
        assertArrayEquals(expectedEntities.toArray(), actualEntities.toArray());
    }

    /**
     * Test method getEntitiesSortedByCount given multiple views and multiple entities, where
     * entities have different count.
     */
    @Test
    public void testGetEntitiesSortedByCountGivenMultipleViewsAndMultipleEntities() {
        // Given
        Dimension entityCount3 = new Dimension("Dimension1", "Dimension1", false, new Entity("Entity with count 3"));
        Dimension entityCount2 = new Dimension("Dimension2", "Dimension2", false, new Entity("Entity with count 2"));
        Dimension entityCount1 = new Dimension("Dimension3", "Dimension3", false, new Entity("Entity with count 1"));

        final Personalization.View view1 = new Personalization.View();
        final Personalization.View view2 = new Personalization.View();
        final Personalization.View view3 = new Personalization.View();

        view1.getDimensions().add(entityCount3);
        view1.getDimensions().add(entityCount2);
        view1.getDimensions().add(entityCount1);

        view2.getDimensions().add(entityCount3);
        view2.getDimensions().add(entityCount2);

        view3.getDimensions().add(entityCount3);

        final List<Personalization.View> views = new ArrayList<>();
        views.add(view3);
        views.add(view1);
        views.add(view2);

        personalization.setViews(views);

        // Then
        final Dimension[] expectedEntities = {entityCount3, entityCount2, entityCount1};
        final Dimension[] actualEntities =
            personalizationDomainObject.getEntitiesSortedByCount(10).toArray(new Dimension[3]);
        assertArrayEquals(expectedEntities, actualEntities);
    }

    /**
     * Test method getEntitiesSortedByCount given entities with same count.
     */
    @Test
    public void testGetEntitiesSortedByCountGivenEntitiesWithEqualCount() {
        // Given
        final Dimension[] expectedEntityArray = {
            new Dimension("Dimension1", "Dimension1", false, new Entity("My entity")),
            new Dimension("Dimension2", "Dimension2", false, new Entity("My other entity")),
            new Dimension("Dimension3", "Dimension3", false, new Entity("My third entity"))};
        final List<Dimension> expectedEntities = Arrays.asList(expectedEntityArray);
        final List<Personalization.View> views = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final Personalization.View view = new Personalization.View();
            view.setDimensions(expectedEntities);
            views.add(view);
        }
        personalization.setViews(views);

        // Then
        final Collection<Dimension> actualEntities = personalizationDomainObject.getEntitiesSortedByCount(10);
        assertEquals(3, actualEntities.size());
        assertTrue(actualEntities.containsAll(expectedEntities));
    }

    @Test
    public void testGetMultipleEntitiesInSameDimension() {
        // Given
        final Dimension[] expectedEntityArray = {
            new Dimension("Dimension1", "Dimension1", false,
                          new Entity("My entity in first dimension"),
                          new Entity("My other entity in first dimension")),
            new Dimension("Dimension2", "Dimension2", false, new Entity("My entity in second dimension"))};
        final List<Dimension> expectedEntities = Arrays.asList(expectedEntityArray);
        final List<Personalization.View> views = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final Personalization.View view = new Personalization.View();
            view.setDimensions(Arrays.asList(expectedEntityArray));
            views.add(view);
        }
        personalization.setViews(views);

        // Then
        final Collection<Dimension> actualEntities = personalizationDomainObject.getEntitiesSortedByCount(10);
        assertEquals(2, actualEntities.size());
        if (!actualEntities.containsAll(expectedEntities)) {
            assertEquals(new Gson().toJson(expectedEntities), new Gson().toJson(actualEntities));
        }
    }

    /**
     * Test method getEntitiesSortedByCount with different values of argument maxSize and with same preconditions and
     * verify that size of returned list differs.
     */
    @Test
    public void testParamMaxSizeToMethodGetEntitiesSortedByCount() {
        // Given
        final List<Personalization.View> views = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            final List<Dimension> expectedEntities = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                expectedEntities.add(new Dimension("Dimension" + i + "-" + j, "Dimension" + i + "-" + j, false,
                                                   new Entity("Entity " + i + "-" + j)));
            }
            final Personalization.View view = new Personalization.View();
            view.setDimensions(expectedEntities);
            views.add(view);
        }
        personalization.setViews(views);

        // Then
        assertEquals(0, personalizationDomainObject.getEntitiesSortedByCount(0).size());
        assertEquals(1, personalizationDomainObject.getEntitiesSortedByCount(1).size());
        assertEquals(4, personalizationDomainObject.getEntitiesSortedByCount(4).size());
        assertEquals(4, personalizationDomainObject.getEntitiesSortedByCount(5).size());
    }

}
