package com.example.zen_study.daos;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.models.QuizAttemptAnswer;
import com.example.zen_study.models.QuizAttemptAnswerWithQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Dao
public abstract class QuizAttemptAnswerDao {
    protected final String tableName = "quiz_attempt_answer";


    @Transaction
    @Query("SELECT * FROM quiz_attempt_answer WHERE attempt_id = :attemptId")
    public abstract List<QuizAttemptAnswerWithQuestion> getAnswersWithQuestions(long attemptId);

    @Insert
    protected abstract void _insert(QuizAttemptAnswer answer);

    @Transaction
    public QuizAttemptAnswer insert(QuizAttemptAnswer answer) {
        if (answer.getAttemptId() == null || answer.getAttemptId() == 0
                || answer.getQuestionId() == null || answer.getQuestionId() == 0) {
            return null;
        }
        try {
            _insert(answer);
            return answer;
        } catch (Exception e) {
            Log.e("DAO", String.format(
                    "Failed to insert entity of type %s into the database: %s",
                    answer.getClass().getSimpleName(),
                    Objects.toString(answer)
            ));
            return null;
        }
    }

    @Insert
    protected abstract void _insertAll(List<QuizAttemptAnswer> answers);

    public List<QuizAttemptAnswer> insertAll(Iterable<? extends QuizAttemptAnswer> answers) {
        try {
            var answerList = new ArrayList<QuizAttemptAnswer>();
            answers.forEach(a -> {
                answerList.add(insert(a));
            });
            return answerList;
        } catch (Exception e) {
            Log.e("DAO", String.format(
                    "Failed to insert entity of type %s into the database: %s",
                    answers.getClass().getSimpleName(),
                    Objects.toString(answers)
            ));
            return null;
        }
    }

    @RawQuery
    protected abstract int _deleteAllByAttemptIdsInBatch(SupportSQLiteQuery query);

    @Transaction
    public void deleteAllByAttemptIdsInBatch(Iterable<? extends Long> ids) {
        if (!ids.iterator().hasNext()) {
            return;
        }

        var queryBuilder = new StringBuilder("DELETE FROM ")
                .append(tableName)
                .append(" WHERE attempt_id IN (");

        List<Long> idList = new ArrayList<>();
        final int[] index = {0};
        ids.forEach(id -> {
            if (index[0] > 0) {
                queryBuilder.append(",");
            }
            queryBuilder.append("?");
            idList.add(id);
            index[0]++;
        });
        queryBuilder.append(")");

        var query = new SimpleSQLiteQuery(
                queryBuilder.toString(),
                idList.toArray()
        );
        _deleteAllByAttemptIdsInBatch(query);
    }

    @RawQuery
    protected abstract int _deleteAllByQuestionIdsInBatch(SupportSQLiteQuery query);

    @Transaction
    public void deleteAllByQuestionIdsInBatch(Iterable<? extends Long> ids) {
        if (!ids.iterator().hasNext()) {
            return;
        }

        var queryBuilder = new StringBuilder("DELETE FROM ")
                .append(tableName)
                .append(" WHERE question_id IN (");

        List<Long> idList = new ArrayList<>();
        final int[] index = {0};
        ids.forEach(id -> {
            if (index[0] > 0) {
                queryBuilder.append(",");
            }
            queryBuilder.append("?");
            idList.add(id);
            index[0]++;
        });
        queryBuilder.append(")");

        var query = new SimpleSQLiteQuery(
                queryBuilder.toString(),
                idList.toArray()
        );
        _deleteAllByQuestionIdsInBatch(query);
    }
}
