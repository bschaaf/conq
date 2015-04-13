package it.conquiste.cm.standard.image;

import com.polopoly.application.Application;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.unified.content.ImageAware_v1;
import com.polopoly.unified.content.Image_v1;
import com.polopoly.unified.content.RepresentationApplicationFailedException;
import com.polopoly.unified.content.RepresentationFactory;

/**
 * ImagePolicy.
 *
 * @author mnova
 */
public class ImagePolicy extends com.atex.standard.image.ImagePolicy implements ImageAware_v1 {

    private final CmClient cmClient;

    public ImagePolicy(final CmClient cmClient, final Application application) throws IllegalApplicationStateException {
        super(cmClient, application);
        this.cmClient = cmClient;
    }

    @Override
    public void applyRepresentation(final Image_v1 image)
            throws RepresentationApplicationFailedException, RuntimeException {

        createImageAwareAdapter().applyRepresentation(image);
    }

    @Override
    public Image_v1 getRepresentation(final Image_v1 image, final RepresentationFactory factory)
            throws RepresentationApplicationFailedException, RuntimeException {

        return createImageAwareAdapter().getRepresentation(image, factory);
    }

    private ImageAwareAdapter createImageAwareAdapter()
    {
        return new ImageAwareAdapter(this, cmClient);
    }
}
