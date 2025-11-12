package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.BaseEntity;
import com.example.zen_study.models.QuizQuestion;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Dao
public abstract class QuizQuestionDao extends AbstractGenericDao<QuizQuestion> {
    protected QuizQuestionDao() {
        super("quiz_question");
    }

    @Query("SELECT * FROM quiz_question WHERE quiz_id = :quizId")
    public abstract List<QuizQuestion> findByQuizId(long quizId);

    @Query("SELECT * FROM quiz_question WHERE quiz_id = :quizId AND questionType = :questionType")
    public abstract List<QuizQuestion> findByQuizIdAndType(long quizId, String questionType);

    @Query("SELECT * FROM quiz_question WHERE quiz_id = :quizId ORDER BY position ASC")
    public abstract List<QuizQuestion> findByQuizIdOrdered(long quizId);

    @Query("SELECT * FROM quiz_question WHERE quiz_id = :quizId AND questionType = :questionType ORDER BY position ASC")
    public abstract List<QuizQuestion> findByQuizIdAndTypeOrdered(long quizId, String questionType);

    @Query("SELECT COUNT(id) from quiz_question WHERE quiz_id = :quizId")
    public abstract int getQuestionCount(long quizId);

    @Query("SELECT SUM(points) from quiz_question WHERE quiz_id = :quizId")
    public abstract int getTotalPoints(long quizId);

    @Query("SELECT MAX(position) from quiz_question WHERE quiz_id = :quizId")
    public abstract Integer getMaxPosition(long quizId);

    @Query("UPDATE quiz_question SET position = :newPosition WHERE id = :questionId")
    public abstract void updatePosition(long questionId, int newPosition);

    @Transaction
    public QuizQuestion insertWithAutoPosition(QuizQuestion quizQuestion) {
        var maxPosition = getMaxPosition(quizQuestion.getQuizId());
        var newPosition = (maxPosition != null ? maxPosition + 1 : 0);
        quizQuestion.setPosition(newPosition);
        return save(quizQuestion);
    }

    @Transaction
    public boolean reorderQuestions(long quizId) {
        try {
            var questionIds = findByQuizIdOrdered(quizId).stream().map(QuizQuestion::getId)
                    .collect(Collectors.toList());
            var idx = new AtomicInteger(1);
            questionIds.forEach(id -> {
                updatePosition(id, idx.get());
                idx.incrementAndGet();
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transaction
    public boolean reorderQuestions(long quizId, Iterable<? extends Long> questionIds) {
        try {
            var idSet = findByQuizId(quizId).stream()
                    .map(BaseEntity::getId)
                    .collect(Collectors.toCollection(HashSet::new));
            for (var id : questionIds) {
                if (!idSet.contains(id)) {
                    return false;
                }
            }
            var idx = new AtomicInteger(1);
            questionIds.forEach(id -> {
                updatePosition(id, idx.get());
                idx.incrementAndGet();
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Query("UPDATE quiz_question SET position = position - 1 WHERE quiz_id = :quizId AND position > :fromPosition")
    abstract void decrementPositions(long quizId, int fromPosition);

    @Transaction
    public boolean deleteQuestionThenReorder(long questionId) {
        try {
            var question = findById(questionId).orElseThrow();
            if (question == null) {
                return false;
            }

            var quizId = question.getQuizId();
            var deletedPosition = question.getPosition();

            deleteById(questionId);
            decrementPositions(quizId, deletedPosition);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void importQuestionsFrom_() {
    }

    @Query("DELETE FROM quiz_question WHERE quiz_id = :quizId")
    public abstract void deleteByQuizId(long quizId);

    @RawQuery(observedEntities = {QuizQuestion.class})
    protected abstract LiveData<QuizQuestion> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {QuizQuestion.class})
    protected abstract LiveData<List<QuizQuestion>> _findAllLiveData(SupportSQLiteQuery query);
}
