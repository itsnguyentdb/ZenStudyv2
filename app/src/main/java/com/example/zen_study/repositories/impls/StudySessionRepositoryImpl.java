package com.example.zen_study.repositories.impls;

import android.content.Context;

import com.example.zen_study.daos.PomodoroCycleDao;
import com.example.zen_study.daos.StudySessionDao;
import com.example.zen_study.daos.TaskDao;
import com.example.zen_study.enums.TimePeriod;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.PomodoroCycle;
import com.example.zen_study.models.StudySession;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class StudySessionRepositoryImpl {
    private final StudySessionDao studySessionDao;
    private final PomodoroCycleDao pomodoroCycleDao;
    private final TaskDao taskDao;
    private final Executor executor;

    public StudySessionRepositoryImpl(Context context) {
        var instance = AppDatabase.getInstance(context);
        studySessionDao = instance.studySessionDao();
        pomodoroCycleDao = instance.pomodoroCycleDao();
        taskDao = instance.taskDao();
        executor = instance.getQueryExecutor();
    }


    public long getTotalStudyTime() {
        try {
            // If your DAO doesn't have getTotalStudyTime(), calculate it manually
            List<StudySession> allSessions = getAllSessionsSync();
            long totalTime = 0;
            for (StudySession session : allSessions) {
                totalTime += session.getDuration();
            }
            return totalTime;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public int getSessionsCount() {
        try {
            List<StudySession> allSessions = getAllSessionsSync();
            return allSessions.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<StudySession> getSessionsForTimePeriod(TimePeriod period) {
        try {
            Calendar cal = Calendar.getInstance();
            Date endDate = new Date();
            Date startDate;

            switch (period) {
                case WEEK:
                    cal.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case MONTH:
                    cal.add(Calendar.MONTH, -1);
                    break;
                case YEAR:
                    cal.add(Calendar.YEAR, -1);
                    break;
                default:
                    cal.add(Calendar.WEEK_OF_YEAR, -1);
            }
            startDate = cal.getTime();

            // If your DAO doesn't have getSessionsBetweenDates, filter manually
            List<StudySession> allSessions = getAllSessionsSync();
            List<StudySession> filteredSessions = new java.util.ArrayList<>();
            for (StudySession session : allSessions) {
                if (!session.getStartTime().before(startDate) && !session.getStartTime().after(endDate)) {
                    filteredSessions.add(session);
                }
            }
            return filteredSessions;
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.List.of();
        }
    }

    public Map<Long, Long> getStudyTimeBySubject() {
        try {
            List<StudySession> allSessions = getAllSessionsSync();
            Map<Long, Long> subjectTimes = new HashMap<>();

            for (StudySession session : allSessions) {
                var taskId = session.getTaskId();
                var task = taskDao.findById(taskId).orElse(null);
                if (task == null) {
                    continue;
                }
                var subjectId = task.getSubjectId();
                long currentTime = subjectTimes.getOrDefault(subjectId, 0L);
                subjectTimes.put(subjectId, currentTime + session.getDuration());
            }

            return subjectTimes;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public List<StudySession> getAllSessionsSync() {
        try {
            // If your DAO has findAll(), use it. Otherwise implement manually
            return studySessionDao.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.List.of();
        }
    }

    // Rest of your existing methods...
    public void getSessionsByTaskId(long taskId, OnStudySessionOperationComplete callback) {
        executor.execute(() -> {
            try {
                List<StudySession> sessions = studySessionDao.getSessionsByTaskId(taskId);
                if (callback != null) {
                    callback.onSuccess(sessions);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void insertSession(StudySession session, OnStudySessionOperationComplete callback) {
        executor.execute(() -> {
            try {
                studySessionDao.save(session);
                List<StudySession> sessions = studySessionDao.getSessionsByTaskId(session.getTaskId());
                if (callback != null) {
                    callback.onSuccess(sessions);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void insertPomodoroCycle(PomodoroCycle currentPomodoroCycle) {
        executor.execute(() -> {
            pomodoroCycleDao.save(currentPomodoroCycle);
        });
    }

    public List<StudySession> getSessionsByTaskIdSync(Long taskId) {
        return studySessionDao.getSessionsByTaskId(taskId);
    }


    public interface OnStudySessionOperationComplete {
        void onSuccess(List<StudySession> sessions);

        void onError(Exception e);
    }
}
