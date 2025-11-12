package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.QuizAttempt;
import com.example.zen_study.models.QuizAttemptWithAnswers;

import java.util.List;

@Dao
public abstract class QuizAttemptDao extends AbstractGenericDao<QuizAttempt> {
    protected QuizAttemptDao() {
        super("quiz_attempt");
    }

    @Transaction
    @Query("SELECT * FROM quiz_attempt WHERE id = :attemptId")
    public abstract QuizAttemptWithAnswers getAttemptWithAnswers(long attemptId);


    @Query("SELECT * FROM quiz_attempt WHERE quiz_id = :quizId")
    public abstract List<QuizAttempt> findByQuizId(long quizId);

    @Query("DELETE FROM QUIZ_ATTEMPT WHERE quiz_id = :quizId")
    public abstract void deleteByQuizId(long quizId);

    @RawQuery(observedEntities = {QuizAttempt.class})
    protected abstract LiveData<QuizAttempt> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {QuizAttempt.class})
    protected abstract LiveData<List<QuizAttempt>> _findAllLiveData(SupportSQLiteQuery query);
}
