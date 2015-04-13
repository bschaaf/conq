package com.atex.plugins.personalization;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PersonalizationTest {

    @Test
    public void testGetViews() {
        Personalization personalization = new Personalization();
        assertNotNull(personalization.getViews());
    }

    @Test
    public void testGetEntities() {
        Personalization.View view = new Personalization.View();
        assertNotNull(view.getDimensions());
    }
}
