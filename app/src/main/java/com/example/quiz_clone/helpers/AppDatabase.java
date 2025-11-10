package com.example.quiz_clone.helpers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.quiz_clone.daos.EventDao;
import com.example.quiz_clone.daos.FlashcardDeckDao;
import com.example.quiz_clone.daos.FlashcardTermDao;
import com.example.quiz_clone.daos.NoteDao;
import com.example.quiz_clone.daos.PomodoroCycleDao;
import com.example.quiz_clone.daos.QuizAttemptAnswerDao;
import com.example.quiz_clone.daos.QuizAttemptDao;
import com.example.quiz_clone.daos.QuizDao;
import com.example.quiz_clone.daos.QuizQuestionDao;
import com.example.quiz_clone.daos.ResourceDao;
import com.example.quiz_clone.daos.StudySessionDao;
import com.example.quiz_clone.daos.SubjectDao;
import com.example.quiz_clone.daos.TaskDao;
import com.example.quiz_clone.models.Event;
import com.example.quiz_clone.models.FlashcardDeck;
import com.example.quiz_clone.models.FlashcardTerm;
import com.example.quiz_clone.models.Note;
import com.example.quiz_clone.models.PomodoroCycle;
import com.example.quiz_clone.models.Quiz;
import com.example.quiz_clone.models.QuizAttempt;
import com.example.quiz_clone.models.QuizAttemptAnswer;
import com.example.quiz_clone.models.QuizQuestion;
import com.example.quiz_clone.models.Resource;
import com.example.quiz_clone.models.StudySession;
import com.example.quiz_clone.models.Subject;
import com.example.quiz_clone.models.Task;
import com.example.quiz_clone.utils.ModernConverters;

@Database(entities = {
        FlashcardDeck.class,
        FlashcardTerm.class,
        Quiz.class,
        QuizAttempt.class,
        QuizAttemptAnswer.class,
        QuizQuestion.class,
        Resource.class,
        Subject.class,
        Event.class,
        StudySession.class,
        PomodoroCycle.class,
        Task.class,
        Note.class,
}, version = 4, exportSchema = false)
@TypeConverters({ModernConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "zen_study";
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                System.out.println("Initializing database");
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract FlashcardDeckDao flashcardDeckDao();

    public abstract FlashcardTermDao flashcardTermDao();

    public abstract QuizDao quizDao();

    public abstract QuizAttemptDao quizAttemptDao();

    public abstract QuizAttemptAnswerDao quizAttemptAnswerDao();

    public abstract QuizQuestionDao quizQuestionDao();

    public abstract ResourceDao resourceDao();

    public abstract SubjectDao subjectDao();

    public abstract EventDao eventDao();

    public abstract StudySessionDao studySessionDao();

    public abstract PomodoroCycleDao pomodoroCycleDao();

    public abstract TaskDao taskDao();

    public abstract NoteDao noteDao();
}
