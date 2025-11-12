package com.example.zen_study.helpers;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.zen_study.daos.EventDao;
import com.example.zen_study.daos.FileMetadataDao;
import com.example.zen_study.daos.FlashcardDeckDao;
import com.example.zen_study.daos.FlashcardTermDao;
import com.example.zen_study.daos.NoteDao;
import com.example.zen_study.daos.PomodoroCycleDao;
import com.example.zen_study.daos.QuizAttemptAnswerDao;
import com.example.zen_study.daos.QuizAttemptDao;
import com.example.zen_study.daos.QuizDao;
import com.example.zen_study.daos.QuizQuestionDao;
import com.example.zen_study.daos.ResourceDao;
import com.example.zen_study.daos.StudySessionDao;
import com.example.zen_study.daos.SubjectDao;
import com.example.zen_study.daos.TaskDao;
import com.example.zen_study.models.Event;
import com.example.zen_study.models.FileMetadata;
import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.FlashcardTerm;
import com.example.zen_study.models.Note;
import com.example.zen_study.models.PomodoroCycle;
import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizAttempt;
import com.example.zen_study.models.QuizAttemptAnswer;
import com.example.zen_study.models.QuizQuestion;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.StudySession;
import com.example.zen_study.models.Subject;
import com.example.zen_study.models.Task;
import com.example.zen_study.utils.ModernConverters;

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
        FileMetadata.class,
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

    public abstract FileMetadataDao fileMetadataDao();
}
