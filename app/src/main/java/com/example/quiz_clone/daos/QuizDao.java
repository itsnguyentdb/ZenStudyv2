package com.example.quiz_clone.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.quiz_clone.daos.common.AbstractGenericDao;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.Quiz;

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
    @RawQuery(observedEntities = {Quiz.class})
    protected abstract LiveData<Quiz> _findByIdLiveData(SupportSQLiteQuery query);
    @RawQuery(observedEntities = {Quiz.class})
    protected abstract LiveData<List<Quiz>> _findAllLiveData(SupportSQLiteQuery query);
}
