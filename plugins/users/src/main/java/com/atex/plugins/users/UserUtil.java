package com.atex.plugins.users;


import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.repository.ContentModifiedException;

public class UserUtil {

    private static final Subject SYSTEM_SUBJECT = new Subject("98", null);

    private final ContentId userId;
    private final ContentManager contentManager;

    public UserUtil(final ContentId userId,
                    final ContentManager contentManager) {
        this.userId = userId;
        this.contentManager = contentManager;
    }

    public ContentId getUserId() {
        return userId;
    }

    public <T> T getEngagement(final Class<T> engagementClass,
                               final String engagementName) {
        ContentId engagementId = getEngagementId(engagementName);
        if (engagementId != null) {
            ContentVersionId engagementVersionedId = contentManager.resolve(engagementId, SYSTEM_SUBJECT);
            ContentResult<T> engagementRead =
                    contentManager.get(
                            engagementVersionedId, null, engagementClass, null, SYSTEM_SUBJECT);
            return engagementRead.getContent().getContentData();
        }
        return null;
    }

    private ContentId getEngagementId(final String engagementName) {
        return getUser().getEngagements().get(engagementName);
    }

    public User getUser() {
        ContentVersionId userVerId = contentManager.resolve(userId, SYSTEM_SUBJECT);
        ContentResult<User> userRead = contentManager.get(userVerId, null, User.class, null, SYSTEM_SUBJECT);
        return userRead.getContent().getContentData();
    }

    public <T> void setEngagement(final Class<T> engagementClass,
                                  final T engagement,
                                  final String engagementName)
            throws ContentModifiedException {
        ContentVersionId userVersionedId = contentManager.resolve(userId, SYSTEM_SUBJECT);
        ContentResult<User> userRead =
                contentManager.get(userVersionedId, null, User.class, null, SYSTEM_SUBJECT);
        User user = userRead.getContent().getContentData();
        ContentWrite<T> engagementContentWrite =
                new ContentWrite<>(engagementClass.getName(), engagement);
        ContentResult<T> createResult =
                contentManager.create(engagementContentWrite, SYSTEM_SUBJECT);
        user.getEngagements().put(engagementName, createResult.getContentId().getContentId());
        ContentWrite<User> userContentWrite = new ContentWrite<>(userRead.getContent());
        userContentWrite.setContentData(user);
        contentManager.update(userContentWrite.getId().getContentId(), userContentWrite, SYSTEM_SUBJECT);
    }

    public void setEngagementId(final String engagementName,
                                final com.atex.onecms.content.ContentId engagementContentId)
        throws ContentModifiedException {

        ContentVersionId id = contentManager.resolve(userId, SYSTEM_SUBJECT);
        ContentResult<User> userRead = contentManager.get(id, null, User.class, null, SYSTEM_SUBJECT);
        ContentWrite<User> userUpdate = new ContentWrite<>(userRead.getContent());
        User contentData = userRead.getContent().getContentData();
        contentData.getEngagements().put(engagementName, engagementContentId);
        userUpdate.setContentData(contentData);
        contentManager.update(userUpdate.getId().getContentId(), userUpdate, SYSTEM_SUBJECT);
    }

}
