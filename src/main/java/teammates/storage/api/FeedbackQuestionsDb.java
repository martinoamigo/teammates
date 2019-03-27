package teammates.storage.api;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.LoadType;
import com.googlecode.objectify.cmd.Query;

import teammates.common.datatransfer.AttributesDeletionQuery;
import teammates.common.datatransfer.FeedbackParticipantType;
import teammates.common.datatransfer.attributes.FeedbackQuestionAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.storage.entity.FeedbackQuestion;

/**
 * Handles CRUD operations for feedback questions.
 *
 * @see FeedbackQuestion
 * @see FeedbackQuestionAttributes
 */
public class FeedbackQuestionsDb extends EntitiesDb<FeedbackQuestion, FeedbackQuestionAttributes> {
    public static final String ERROR_UPDATE_NON_EXISTENT = "Trying to update non-existent Feedback Question : ";

    /**
     * Preconditions: <br>
     * * All parameters are non-null.
     * @return Null if not found.
     */
    public FeedbackQuestionAttributes getFeedbackQuestion(String feedbackQuestionId) {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, feedbackQuestionId);

        return makeAttributesOrNull(getFeedbackQuestionEntity(feedbackQuestionId),
                "Trying to get non-existent Question: " + feedbackQuestionId);
    }

    /**
     * Preconditions: <br>
     * * All parameters are non-null.
     * @return Null if not found.
     */
    public FeedbackQuestionAttributes getFeedbackQuestion(
            String feedbackSessionName,
            String courseId,
            int questionNumber) {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, feedbackSessionName);
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, courseId);
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, questionNumber);

        return makeAttributesOrNull(getFeedbackQuestionEntity(feedbackSessionName, courseId, questionNumber),
                "Trying to get non-existent Question: " + questionNumber + "." + feedbackSessionName + "/" + courseId);
    }

    /**
     * Preconditions: <br>
     * * All parameters are non-null.
     * @return An empty list if no such questions are found.
     */
    public List<FeedbackQuestionAttributes> getFeedbackQuestionsForSession(
            String feedbackSessionName, String courseId) {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, feedbackSessionName);
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, courseId);

        return makeAttributes(getFeedbackQuestionEntitiesForSession(feedbackSessionName, courseId));
    }

    /**
     * Preconditions: <br>
     * * All parameters are non-null.
     * @return An empty list if no such questions are found.
     */
    public List<FeedbackQuestionAttributes> getFeedbackQuestionsForGiverType(
            String feedbackSessionName, String courseId, FeedbackParticipantType giverType) {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, feedbackSessionName);
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, courseId);
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, giverType);

        return makeAttributes(getFeedbackQuestionEntitiesForGiverType(feedbackSessionName, courseId, giverType));
    }

    /**
     * Updates a feedback question by {@code FeedbackQuestionAttributes.UpdateOptions}.
     *
     * @return updated feedback question
     * @throws InvalidParametersException if attributes to update are not valid
     * @throws EntityDoesNotExistException if the feedback question cannot be found
     */
    public FeedbackQuestionAttributes updateFeedbackQuestion(FeedbackQuestionAttributes.UpdateOptions updateOptions)
            throws InvalidParametersException, EntityDoesNotExistException {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, updateOptions);

        FeedbackQuestion feedbackQuestion = getFeedbackQuestionEntity(updateOptions.getFeedbackQuestionId());
        if (feedbackQuestion == null) {
            throw new EntityDoesNotExistException(ERROR_UPDATE_NON_EXISTENT + updateOptions);
        }

        FeedbackQuestionAttributes newAttributes = makeAttributes(feedbackQuestion);
        newAttributes.update(updateOptions);

        newAttributes.sanitizeForSaving();
        if (!newAttributes.isValid()) {
            throw new InvalidParametersException(newAttributes.getInvalidityInfo());
        }

        feedbackQuestion.setQuestionNumber(newAttributes.questionNumber);
        feedbackQuestion.setQuestionText(newAttributes.getSerializedQuestionDetails());
        feedbackQuestion.setQuestionDescription(newAttributes.questionDescription);
        feedbackQuestion.setGiverType(newAttributes.giverType);
        feedbackQuestion.setRecipientType(newAttributes.recipientType);
        feedbackQuestion.setShowResponsesTo(newAttributes.showResponsesTo);
        feedbackQuestion.setShowGiverNameTo(newAttributes.showGiverNameTo);
        feedbackQuestion.setShowRecipientNameTo(newAttributes.showRecipientNameTo);
        feedbackQuestion.setNumberOfEntitiesToGiveFeedbackTo(newAttributes.numberOfEntitiesToGiveFeedbackTo);

        saveEntity(feedbackQuestion, newAttributes);

        return makeAttributes(feedbackQuestion);
    }

    /**
     * Deletes a feedback question.
     */
    public void deleteFeedbackQuestion(String feedbackQuestionId) {
        Key<FeedbackQuestion> feedbackQuestionKey = makeKeyOrNullFromWebSafeString(feedbackQuestionId);
        if (feedbackQuestionKey != null) {
            deleteEntity(feedbackQuestionKey);
        }
    }

    /**
     * Deletes questions using {@link AttributesDeletionQuery}.
     */
    public void deleteFeedbackQuestions(AttributesDeletionQuery query) {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, query);

        Query<FeedbackQuestion> entitiesToDelete = load().project();
        if (query.isCourseIdPresent()) {
            entitiesToDelete = entitiesToDelete.filter("courseId =", query.getCourseId());
        }
        if (query.isFeedbackSessionNamePresent()) {
            entitiesToDelete = entitiesToDelete.filter("feedbackSessionName =", query.getFeedbackSessionName());
        }

        deleteEntity(entitiesToDelete.keys().list().toArray(new Key<?>[0]));
    }

    // Gets a question entity if its Key (feedbackQuestionId) is known.
    private FeedbackQuestion getFeedbackQuestionEntity(String feedbackQuestionId) {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, feedbackQuestionId);

        Key<FeedbackQuestion> key = makeKeyOrNullFromWebSafeString(feedbackQuestionId);
        if (key == null) {
            return null;
        }

        return ofy().load().key(key).now();
    }

    // Gets a feedbackQuestion based on feedbackSessionName and questionNumber.
    private FeedbackQuestion getFeedbackQuestionEntity(
            String feedbackSessionName, String courseId, int questionNumber) {
        return load()
                .filter("feedbackSessionName =", feedbackSessionName)
                .filter("courseId =", courseId)
                .filter("questionNumber =", questionNumber)
                .first().now();
    }

    private List<FeedbackQuestion> getFeedbackQuestionEntitiesForSession(
            String feedbackSessionName, String courseId) {
        return load()
                .filter("feedbackSessionName =", feedbackSessionName)
                .filter("courseId =", courseId)
                .list();
    }

    private List<FeedbackQuestion> getFeedbackQuestionEntitiesForGiverType(
            String feedbackSessionName, String courseId, FeedbackParticipantType giverType) {
        return load()
                .filter("feedbackSessionName =", feedbackSessionName)
                .filter("courseId =", courseId)
                .filter("giverType =", giverType)
                .list();
    }

    @Override
    protected LoadType<FeedbackQuestion> load() {
        return ofy().load().type(FeedbackQuestion.class);
    }

    @Override
    protected FeedbackQuestion getEntity(FeedbackQuestionAttributes attributes) {
        if (attributes.getId() != null) {
            return getFeedbackQuestionEntity(attributes.getId());
        }

        return getFeedbackQuestionEntity(attributes.feedbackSessionName, attributes.courseId, attributes.questionNumber);
    }

    @Override
    protected boolean hasExistingEntities(FeedbackQuestionAttributes entityToCreate) {
        return !load()
                .filter("feedbackSessionName =", entityToCreate.getFeedbackSessionName())
                .filter("courseId =", entityToCreate.getCourseId())
                .filter("questionNumber =", entityToCreate.getQuestionNumber())
                .keys()
                .list()
                .isEmpty();
    }

    @Override
    protected FeedbackQuestionAttributes makeAttributes(FeedbackQuestion entity) {
        Assumption.assertNotNull(Const.StatusCodes.DBLEVEL_NULL_INPUT, entity);

        return FeedbackQuestionAttributes.valueOf(entity);
    }
}
