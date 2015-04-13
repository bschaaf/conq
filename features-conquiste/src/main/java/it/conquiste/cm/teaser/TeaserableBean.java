package it.conquiste.cm.teaser;

import java.util.List;

import com.polopoly.cm.ContentId;

public class TeaserableBean implements Teaserable {

  private ContentId contentId = null;
  private String overtitle = "";
  private String name = "";
  private String text = "";
  private ContentId imageContentId = null;
  private ContentId[] linkPath = null;
  private List<Teaserable.Attribute> attributes = null;

  public void setContentId(final ContentId contentId) {
      this.contentId = contentId;
  }
  
  public void setOvertitle(final String overtitle) {
      this.overtitle = overtitle;
  }

  public void setName(final String name) {
      this.name = name;
  }

  public void setText(final String text) {
      this.text = text;
  }

  public void setImageContentId(final ContentId imageContentId) {
      this.imageContentId = imageContentId;
  }

  public void setLinkPath(final ContentId[] linkPath) {
      this.linkPath = linkPath;
  }

  public void setAttributes(final List<Teaserable.Attribute> attributes) {
      this.attributes = attributes;
  }

  @Override
  public ContentId getContentId() {
      return contentId;
  }
  
  @Override
  public String getOvertitle() {
      return overtitle;
  }

  @Override
  public String getName() {
      return name;
  }

  @Override
  public String getText() {
      return text;
  }

  @Override
  public ContentId getImageContentId() {
      return imageContentId;
  }

  @Override
  public ContentId[] getLinkPath() {
      return linkPath;
  }

  @Override
  public List<Teaserable.Attribute> getAttributes() {
      return attributes;
  }
  
  private List<ContentId> relatedId = null;

  public void setListIdRelated(final List<ContentId> relatedId) {
    this.relatedId = relatedId;
  }

  public List<ContentId> getListIdRelated() {
    return relatedId;
  }

}
