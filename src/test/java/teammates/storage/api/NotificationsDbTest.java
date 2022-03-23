package teammates.storage.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.NotificationTargetUser;
import teammates.common.datatransfer.attributes.NotificationAttributes;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.test.BaseTestCaseWithLocalDatabaseAccess;

/**
 * SUT: {@link NotificationsDb}.
 */
public class NotificationsDbTest extends BaseTestCaseWithLocalDatabaseAccess {

    private final NotificationsDb notificationsDb = NotificationsDb.inst();
    private final Map<String, NotificationAttributes> typicalNotifications = getTypicalDataBundle().notifications;

    // Notification attributes will be assigned with value at setup()
    private NotificationAttributes n;

    @BeforeMethod
    public void setup() throws Exception {
        for (NotificationAttributes n : typicalNotifications.values()) {
            notificationsDb.createEntity(n);
        }
        n = typicalNotifications.get("notification1");
    }

    /**
     * Removes all notifications created by each test.
     */
    @AfterMethod
    public void cleanUp() {
        List<NotificationAttributes> retrieved = notificationsDb.getAllNotifications();
        assertNotNull(retrieved);

        retrieved.forEach(n -> notificationsDb.deleteNotification(n.getNotificationId()));
    }

    @Test
    public void testGetNotification() throws Exception {
        ______TS("typical success case");
        NotificationAttributes retrieved = notificationsDb.getNotification(n.getNotificationId());
        assertNotNull(retrieved);

        ______TS("expect null for non-existent account");
        retrieved = notificationsDb.getNotification("invalid_notification_id");
        assertNull(retrieved);

        ______TS("failure: null parameter");
        assertThrows(AssertionError.class, () -> notificationsDb.getNotification(null));
    }

    @Test
    public void testGetAllNotifications() throws Exception {
        ______TS("typical success case");
        List<NotificationAttributes> retrieved = notificationsDb.getAllNotifications();

        // Verifies only the number of returned notifications is correct.
        // No duplications or wrong data checks are made.
        assertNotNull(retrieved);
        assertEquals(typicalNotifications.size(), retrieved.size());
    }

    @Test
    public void testGetActiveNotificationsByTargetUser() throws Exception {
        // Conditions for this API: endTime > now, startTime < now, targetUser == specified target user
        ______TS("typical success case");

        // This size has to be changed if any changes are made to typicalDataBundle
        final int expectedReturnSize = 4;

        List<NotificationAttributes> retrieved =
                notificationsDb.getActiveNotificationsByTargetUser(NotificationTargetUser.STUDENT);

        // Verifies only the number of returned notifications is correct.
        // No duplications or wrong data checks are made.
        assertNotNull(retrieved);
        assertEquals(expectedReturnSize, retrieved.size());
    }

    @Test
    public void testCreateNotification() throws Exception {
        ______TS("typical success case");
        NotificationAttributes n1 = createNewNotification();

        ______TS("failure: duplicate notification with the same ID");
        assertThrows(EntityAlreadyExistsException.class, () -> notificationsDb.createEntity(n1));

        ______TS("failure: invalid non-null parameters");
        NotificationAttributes n2 = getNewNotificationAttributes();
        n2.setTitle("");
        assertThrows(InvalidParametersException.class, () -> notificationsDb.createEntity(n2));

        ______TS("failure: null parameters");
        assertThrows(AssertionError.class, () -> notificationsDb.createEntity(null));
    }

    // TODO: for extension, some fields are not allowed to be updated after shown is true
    @Test
    public void testUpdateNotification() throws Exception {
        ______TS("typical success case");
        // Try to update to another set of values, currently n's attributes are from notification1
        NotificationAttributes original = typicalNotifications.get("notification1");
        NotificationAttributes differentNotification = typicalNotifications.get("notification2");

        assertEquals(original.getTitle(), n.getTitle());
        assertEquals(original.getMessage(), n.getMessage());
        assertEquals(original.getType(), n.getType());
        assertEquals(original.getTargetUser(), n.getTargetUser());
        assertEquals(original.getStartTime(), n.getStartTime());
        assertEquals(original.getEndTime(), n.getEndTime());
        assertFalse(n.isShown());

        notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withTitle(differentNotification.getTitle())
                        .withMessage(differentNotification.getMessage())
                        .withType(differentNotification.getType())
                        .withTargetUser(differentNotification.getTargetUser())
                        .withStartTime(differentNotification.getStartTime())
                        .withEndTime(differentNotification.getEndTime())
                        .withShown()
                        .build());

        n = notificationsDb.getNotification(n.getNotificationId());
        assertEquals(differentNotification.getTitle(), n.getTitle());
        assertEquals(differentNotification.getMessage(), n.getMessage());
        assertEquals(differentNotification.getType(), n.getType());
        assertEquals(differentNotification.getTargetUser(), n.getTargetUser());
        assertEquals(differentNotification.getStartTime(), n.getStartTime());
        assertEquals(differentNotification.getEndTime(), n.getEndTime());
        assertTrue(n.isShown());

        ______TS("failure: update non-existent notification");
        assertThrows(EntityDoesNotExistException.class, () ->
                notificationsDb.updateNotification(NotificationAttributes.updateOptionsBuilder("invalid_notification_id")
                        .withTitle("title")
                        .build()));

        ______TS("failure: invalid non-null parameters");
        // Empty title is used here, which triggers InvalidParametersException
        assertThrows(InvalidParametersException.class, () ->
                notificationsDb.updateNotification(NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withTitle("")
                        .build()));

        ______TS("failure: null update options");
        assertThrows(AssertionError.class, () -> notificationsDb.updateNotification(null));
    }

    @Test
    public void testUpdateNotification_singleFieldUpdate_shouldUpdateSuccessfully() throws Exception {
        ______TS("success: single field - title");
        // Try to update to another set of values, currently n's attributes are from notification1
        NotificationAttributes original = typicalNotifications.get("notification1");
        NotificationAttributes differentNotification = typicalNotifications.get("notification2");

        assertEquals(original.getTitle(), notificationsDb.getNotification(n.getNotificationId()).getTitle());
        assertEquals(differentNotification.getTitle(), notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withTitle(differentNotification.getTitle())
                        .build()).getTitle());
        assertEquals(differentNotification.getTitle(), notificationsDb.getNotification(n.getNotificationId()).getTitle());

        ______TS("success: single field - message");
        assertEquals(original.getMessage(), notificationsDb.getNotification(n.getNotificationId()).getMessage());
        assertEquals(differentNotification.getMessage(), notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withMessage(differentNotification.getMessage())
                        .build()).getMessage());
        assertEquals(differentNotification.getMessage(),
                notificationsDb.getNotification(n.getNotificationId()).getMessage());

        ______TS("success: single field - type");
        assertEquals(original.getType(), notificationsDb.getNotification(n.getNotificationId()).getType());
        assertEquals(differentNotification.getType(), notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withType(differentNotification.getType())
                        .build()).getType());
        assertEquals(differentNotification.getType(), notificationsDb.getNotification(n.getNotificationId()).getType());

        ______TS("success: single field - targetUser");
        assertEquals(original.getTargetUser(), notificationsDb.getNotification(n.getNotificationId()).getTargetUser());
        assertEquals(differentNotification.getTargetUser(), notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withTargetUser(differentNotification.getTargetUser())
                        .build()).getTargetUser());
        assertEquals(differentNotification.getTargetUser(),
                notificationsDb.getNotification(n.getNotificationId()).getTargetUser());

        ______TS("success: single field - startTime");
        assertEquals(original.getStartTime(), notificationsDb.getNotification(n.getNotificationId()).getStartTime());
        assertEquals(differentNotification.getStartTime(), notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withStartTime(differentNotification.getStartTime())
                        .build()).getStartTime());
        assertEquals(differentNotification.getStartTime(),
                notificationsDb.getNotification(n.getNotificationId()).getStartTime());

        ______TS("success: single field - endTime");
        assertEquals(original.getEndTime(), notificationsDb.getNotification(n.getNotificationId()).getEndTime());
        assertEquals(differentNotification.getEndTime(), notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withEndTime(differentNotification.getEndTime())
                        .build()).getEndTime());
        assertEquals(differentNotification.getEndTime(),
                notificationsDb.getNotification(n.getNotificationId()).getEndTime());

        ______TS("success: single field - shown");
        assertFalse(notificationsDb.getNotification(n.getNotificationId()).isShown());
        assertTrue(notificationsDb.updateNotification(
                NotificationAttributes.updateOptionsBuilder(n.getNotificationId())
                        .withShown()
                        .build()).isShown());
        assertTrue(notificationsDb.getNotification(n.getNotificationId()).isShown());
    }

    @Test
    public void testDeleteNotification() throws Exception {
        ______TS("silent deletion of non-existant notification");
        notificationsDb.deleteNotification("invalid_notification_id");

        ______TS("typical success case");
        assertNotNull(notificationsDb.getNotification(n.getNotificationId()));
        notificationsDb.deleteNotification(n.getNotificationId());
        assertNull(notificationsDb.getNotification(n.getNotificationId()));

        ______TS("silent deletion of the same notification twice");
        notificationsDb.deleteNotification(n.getNotificationId());

        ______TS("failure: null parameter");
        assertThrows(AssertionError.class, () -> notificationsDb.deleteNotification(null));
    }

    @Test
    public void testHasExistingEntities() throws Exception {
        NotificationAttributes n = getNewNotificationAttributes();

        ______TS("false before entity creation");
        assertEquals(false, notificationsDb.hasExistingEntities(n));

        ______TS("true after entity creation");
        notificationsDb.createEntity(n);
        assertEquals(true, notificationsDb.hasExistingEntities(n));
    }

    private NotificationAttributes createNewNotification()
            throws EntityAlreadyExistsException, InvalidParametersException {
        return notificationsDb.createEntity(getNewNotificationAttributes());
    }

    private NotificationAttributes getNewNotificationAttributes() {
        NotificationAttributes typical = typicalNotifications.get("notification1");
        return NotificationAttributes.builder(UUID.randomUUID().toString())
                .withTitle(typical.getTitle())
                .withMessage(typical.getMessage())
                .withType(typical.getType())
                .withTargetUser(typical.getTargetUser())
                .withStartTime(typical.getStartTime())
                .withEndTime(typical.getEndTime())
                .build();
    }
}
