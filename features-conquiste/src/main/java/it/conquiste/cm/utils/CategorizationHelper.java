package it.conquiste.cm.utils;

import static com.polopoly.cm.app.search.categorization.Categorization.categorization;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.app.search.categorization.CategorizationBuilder;
import com.polopoly.cm.app.search.categorization.CategorizationProvider;
import com.polopoly.cm.app.search.categorization.Category;
import com.polopoly.cm.app.search.categorization.CategoryBuilder;
import com.polopoly.cm.app.search.categorization.CategoryType;
import com.polopoly.cm.app.search.categorization.Dimension;
import com.polopoly.cm.app.search.categorization.DimensionBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;

//@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class CategorizationHelper {
    private CategorizationHelper() {
    }

    /**
     * Returns a categorization that consists of only TAG dimensions.
     */
    public static Categorization tagCategories(final CategorizationProvider provider) throws CMException {
        CategorizationBuilder builder = Categorization.categorization();
        for (Dimension dim : provider.getCategorization().getDimensions()) {
            if (CategoryType.TAG.equals(dim.getType())) {
                builder.withDimensions(dim);
            }
        }
        return builder.build();
    }

    /**
     * Returns a categorization that consists of only unenumerable dimensions.
     */
    public static Categorization tagCategories(final MetadataAware provider) throws CMException {
        CategorizationBuilder builder = Categorization.categorization();
        for (com.polopoly.metadata.Dimension dim : provider.getMetadata().getDimensions()) {
            if (!dim.isEnumerable()) {
                builder = builder.withDimensions(convert(dim));
            }
        }
        return builder.build();
    }

    /**
     * Replace TAG categories with new ones. Keep other category types as is.
     */
    public static Categorization updateCategories(final Categorization newCategorization,
                                                  final Categorization current) {

        CategorizationBuilder mod = categorization();
        for (Dimension dim : current.getDimensions()) {
            if (!dim.getType().equals(CategoryType.TAG)) {
                mod.withDimensions(dim);
            }
        }
        for (Dimension dim : newCategorization.getDimensions()) {
            mod.withDimensions(dim);
        }
        return mod.build();
    }

    /**
     * Replace unenumerable dimensions with new ones. Keep other dimensions as is.
     */
    public static Metadata updateMetadata(final Metadata newMetadata, final Metadata current) {
    	List<com.polopoly.metadata.Dimension> dimensions = new ArrayList<com.polopoly.metadata.Dimension>();
    	for (com.polopoly.metadata.Dimension dim : current.getDimensions()) {
    		if (dim.isEnumerable()) {
    			dimensions.add(dim);
    		}
    	}
    	for (com.polopoly.metadata.Dimension dim : newMetadata.getDimensions()) {
    		dimensions.add(dim);
    	}
    	return new Metadata(dimensions);
    }

    public static Metadata convert(final Categorization categorization) {
        List<com.polopoly.metadata.Dimension> dimensions = new ArrayList<com.polopoly.metadata.Dimension>();
        for (Dimension dimension : categorization.getDimensions()) {
            dimensions.add(convert(dimension));
        }
        Metadata metadata = new Metadata();
        metadata.setDimensions(dimensions);
        return metadata;
    }

    public static com.polopoly.metadata.Dimension convert(final Dimension dimension) {
        com.polopoly.metadata.Dimension result = new com.polopoly.metadata.Dimension();
        result.setEnumerable(dimension.getType() == CategoryType.TREE);
        result.setId(dimension.getId());
        result.setName(dimension.getName());
        List<Entity> entities = new ArrayList<Entity>();

        for (Category category : dimension.getCategories()) {
            entities.add(new Entity(category.getId(), category.getName()));
        }

        result.setEntities(entities);
        return result;
    }

    private static Dimension convert(final com.polopoly.metadata.Dimension dimension) {
    	List<Category> categories = new ArrayList<Category>();
    	for (Entity entity : dimension.getEntities()) {
    		Category category = new CategoryBuilder().id(entity.getId()).withName(entity.getName()).build();
    		categories.add(category);
    	}
    	DimensionBuilder builder = new DimensionBuilder();
    	builder = builder.id(dimension.getId()).withName(dimension.getName());
    	builder = builder.withType(dimension.isEnumerable() ? CategoryType.TREE : CategoryType.TAG);
    	builder = builder.withCategories(categories);
    	return builder.build();
    }
}
