package teammates.storage.entity;

import java.security.SecureRandom;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;

/**
 * An association class that represents the association Account
 * --> [is an instructor for] --> Course.
 */
@Entity
@Index
public class Instructor extends BaseEntity {

    /**
     * The unique id of the entity.
     *
     * @see #generateId(String, String)
     */
    @Id
    private String id;

    /**
     * The Google id of the instructor, used as the foreign key to locate the Account object.
     */
    private String googleId;

    /** The foreign key to locate the Course object. */
    private String courseId;

    /** Whether the associated course is archived. */
    private boolean isArchived;

    /** The instructor's name used for this course. */
    private String name;

    /** The instructor's email used for this course. */
    private String email;

    /** The instructor's registration key used for joining. */
    private String registrationKey;

    @Unindex
    private String role;

    private Boolean isDisplayedToStudents;

    @Unindex
    private String displayedName;

    private Text instructorPrivilegesAsText;

    @SuppressWarnings("unused")
    private Instructor() {
        // required by Objectify
    }

    public Instructor(String instructorGoogleId, String courseId, boolean isArchived, String instructorName,
                      String instructorEmail, String role, boolean isDisplayedToStudents, String displayedName,
                      String instructorPrivilegesAsText) {
        this.setGoogleId(instructorGoogleId);
        this.setCourseId(courseId);
        this.setIsArchived(isArchived);
        this.setName(instructorName);
        this.setEmail(instructorEmail);
        this.setRole(role);
        this.setIsDisplayedToStudents(isDisplayedToStudents);
        this.setDisplayedName(displayedName);
        this.setInstructorPrivilegeAsText(instructorPrivilegesAsText);
        // setId should be called after setting email and courseId
        this.setUniqueId(generateId(this.getEmail(), this.getCourseId()));
        this.setRegistrationKey(generateRegistrationKey());
    }

    /**
     * Generates an unique ID for the instructor.
     */
    public static String generateId(String email, String courseId) {
        // Format: email%courseId e.g., adam@gmail.com%cs1101
        return email + '%' + courseId;
    }

    /**
     * Returns the unique ID of the entity (format: email%courseId).
     */
    public String getUniqueId() {
        return id;
    }

    /**
     * Sets the unique ID for the instructor entity.
     *
     * @param uniqueId
     *          The unique ID of the entity (format: email%courseId).
     */
    public void setUniqueId(String uniqueId) {
        this.id = uniqueId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String instructorGoogleId) {
        this.googleId = instructorGoogleId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public String getName() {
        return name;
    }

    public void setName(String instructorName) {
        this.name = instructorName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String instructorEmail) {
        this.email = instructorEmail;
    }

    public String getRegistrationKey() {
        return registrationKey;
    }

    public void setRegistrationKey(String key) {
        this.registrationKey = key;
    }

    /**
     * Generate unique registration key for the instructor.
     * The key contains random elements to avoid being guessed.
     */
    private String generateRegistrationKey() {
        String uniqueId = getUniqueId();
        SecureRandom prng = new SecureRandom();

        return uniqueId + prng.nextInt();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns whether the instructor is displayed to students.
     */
    public boolean isDisplayedToStudents() {
        if (this.isDisplayedToStudents == null) {
            return true;
        }
        return isDisplayedToStudents;
    }

    public void setIsDisplayedToStudents(boolean shouldDisplayToStudents) {
        this.isDisplayedToStudents = shouldDisplayToStudents;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    /**
     * Gets the instructor privileges stored in JSON format.
     */
    public String getInstructorPrivilegesAsText() {
        if (instructorPrivilegesAsText == null) {
            return null;
        }
        return instructorPrivilegesAsText.getValue();
    }

    public void setInstructorPrivilegeAsText(String instructorPrivilegesAsText) {
        this.instructorPrivilegesAsText = new Text(instructorPrivilegesAsText);
    }
}
