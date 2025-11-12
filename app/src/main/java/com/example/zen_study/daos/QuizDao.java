package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizWithQuestions;

import java.util.List;

@Dao
public abstract class QuizDao extends AbstractGenericDao<Quiz> {
    protected QuizDao() {
        super("quiz");
    }

    @Query("SELECT * FROM quiz WHERE subject_id = :subjectId")
    public abstract List<Quiz> findBySubjectId(long subjectId);

    @Query("SELECT * FROM quiz WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    public abstract List<Quiz> searchByTitleOrDescription(String query);

    @Transaction
    @Query("SELECT * FROM quiz WHERE id = :quizId")
    public abstract QuizWithQuestions getQuizWithQuestions(Long quizId);

    @RawQuery(observedEntities = {Quiz.class})
    protected abstract LiveData<Quiz> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {Quiz.class})
    protected abstract LiveData<List<Quiz>> _findAllLiveData(SupportSQLiteQuery query);

}
