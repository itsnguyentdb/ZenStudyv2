package com.example.zen_study.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zen_study.dto.DailyStudyTime;
import com.example.zen_study.dto.FlashcardStats;
import com.example.zen_study.dto.NotesStats;
import com.example.zen_study.dto.QuizStats;
import com.example.zen_study.dto.StudyOverview;
import com.example.zen_study.dto.SubjectStats;
import com.example.zen_study.dto.TaskProgress;
import com.example.zen_study.dto.WeeklyAverage;
import com.example.zen_study.enums.TimePeriod;
import com.example.zen_study.models.FlashcardDeck;
import com.example.zen_study.models.FlashcardTerm;
import com.example.zen_study.models.Quiz;
import com.example.zen_study.models.QuizAttempt;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.StudySession;
import com.example.zen_study.models.Subject;
import com.example.zen_study.models.Task;
import com.example.zen_study.repositories.impls.FlashcardRepositoryImpl;
import com.example.zen_study.repositories.impls.QuizRepositoryImpl;
import com.example.zen_study.repositories.impls.ResourceRepositoryImpl;
import com.example.zen_study.repositories.impls.StudySessionRepositoryImpl;
import com.example.zen_study.repositories.impls.SubjectRepositoryImpl;
import com.example.zen_study.repositories.impls.TaskRepositoryImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsViewModel extends AndroidViewModel {

    // Repositories
    private StudySessionRepositoryImpl studySessionRepository;
    private TaskRepositoryImpl taskRepository;
    private FlashcardRepositoryImpl flashcardRepository;
    private QuizRepositoryImpl quizRepository;
    private ResourceRepositoryImpl resourceRepository;
    private SubjectRepositoryImpl subjectRepository;

    // LiveData for each fragment
    private MutableLiveData<List<DailyStudyTime>> dailyStudyTime = new MutableLiveData<>();
    private MutableLiveData<List<WeeklyAverage>> weeklyTrend = new MutableLiveData<>();
    private MutableLiveData<List<SubjectStats>> subjectBreakdown = new MutableLiveData<>();
    private MutableLiveData<TaskProgress> taskProgress = new MutableLiveData<>();
    private MutableLiveData<List<Task>> upcomingTasks = new MutableLiveData<>();
    private MutableLiveData<FlashcardStats> flashcardStats = new MutableLiveData<>();
    private MutableLiveData<QuizStats> quizStats = new MutableLiveData<>();
    private MutableLiveData<NotesStats> notesStats = new MutableLiveData<>();
    private MutableLiveData<StudyOverview> studyOverview = new MutableLiveData<>();

    private TimePeriod currentTimePeriod = TimePeriod.WEEK;

    public StatsViewModel(@NonNull Application application) {
        super(application);
        initializeRepositories(application);
        loadAllData();
    }

    private void initializeRepositories(Application application) {
        studySessionRepository = new StudySessionRepositoryImpl(application);
        taskRepository = new TaskRepositoryImpl(application);
        flashcardRepository = new FlashcardRepositoryImpl(application);
        quizRepository = new QuizRepositoryImpl(application);
        resourceRepository = new ResourceRepositoryImpl(application);
        subjectRepository = new SubjectRepositoryImpl(application);
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.currentTimePeriod = timePeriod;
        loadTimeAnalyticsData();
    }

    public void loadAllData() {
        loadStudyOverview();
        loadTimeAnalyticsData();
        loadSubjectBreakdown();
        loadTaskProgress();
        loadFlashcardStats();
        loadQuizStats();
        loadNotesStats();
    }

    private void loadStudyOverview() {
        // Load study overview data from multiple repositories
        new Thread(() -> {
            try {
                // Get total study time from sessions
                long totalStudyTimeMs = getTotalStudyTime();
                int sessionsCompleted = getSessionsCount();
                int currentStreak = calculateCurrentStreak();
                int averageFocus = calculateAverageFocus();

                StudyOverview overview = new StudyOverview(
                        formatStudyTime(totalStudyTimeMs),
                        sessionsCompleted,
                        currentStreak,
                        averageFocus
                );

                studyOverview.postValue(overview);
            } catch (Exception e) {
                Log.e("StatsViewModel", "Error loading study overview", e);
            }
        }).start();
    }

    private void loadTimeAnalyticsData() {
        new Thread(() -> {
            try {
                List<StudySession> sessions = getSessionsForTimePeriod(currentTimePeriod);
                List<DailyStudyTime> dailyData = convertSessionsToDailyData(sessions);
                List<WeeklyAverage> weeklyData = convertSessionsToWeeklyData(sessions);

                dailyStudyTime.postValue(dailyData);
                weeklyTrend.postValue(weeklyData);
            } catch (Exception e) {
                Log.e("StatsViewModel", "Error loading time analytics", e);
            }
        }).start();
    }

    private void loadSubjectBreakdown() {
        new Thread(() -> {
            try {
                List<Subject> subjects = subjectRepository.getAllSubjects().getValue();
                Map<Long, Long> subjectStudyTime = getStudyTimeBySubject();

                if (subjects != null) {
                    List<SubjectStats> subjectStats = createSubjectStats(subjects, subjectStudyTime);
                    subjectBreakdown.postValue(subjectStats);
                }
            } catch (Exception e) {
                Log.e("StatsViewModel", "Error loading subject breakdown", e);
            }
        }).start();
    }

    private void loadTaskProgress() {
        taskRepository.getAllTasks().observeForever(tasks -> {
            if (tasks != null) {
                TaskProgress progress = calculateTaskProgress(tasks);
                taskProgress.postValue(progress);

                List<Task> upcoming = getUpcomingTasks(tasks);
                upcomingTasks.postValue(upcoming);
            }
        });
    }

    private void loadFlashcardStats() {
        flashcardRepository.getAllDecks().observeForever(decks -> {
            if (decks != null) {
                List<FlashcardTerm> allTerms = getAllFlashcardTerms(decks);
                FlashcardStats stats = createFlashcardStats(decks, allTerms);
                flashcardStats.postValue(stats);
            }
        });
    }

    private void loadQuizStats() {
        new Thread(() -> {
            try {
                List<Quiz> quizzes = quizRepository.getAllQuizzes();
                List<QuizAttempt> allAttempts = getAllQuizAttempts(quizzes);
                QuizStats stats = createQuizStats(quizzes, allAttempts);
                quizStats.postValue(stats);
            } catch (Exception e) {
                Log.e("StatsViewModel", "Error loading quiz stats", e);
            }
        }).start();
    }

    private void loadNotesStats() {
        new Thread(() -> {
            try {
                List<Resource> resources = resourceRepository.getAllResources().getValue();
                // Note: You'll need to create a NoteRepository similar to your other repositories
                // For now, we'll use placeholder data
                NotesStats stats = createNotesStats(resources);
                notesStats.postValue(stats);
            } catch (Exception e) {
                Log.e("StatsViewModel", "Error loading notes stats", e);
            }
        }).start();
    }

    // Helper methods for data processing

    private long getTotalStudyTime() {
        // This would require a new method in StudySessionRepository
        // For now, return placeholder
        return 18 * 60 * 60 * 1000L; // 18 hours in milliseconds
    }

    private int getSessionsCount() {
        // This would require a new method in StudySessionRepository
        return 42; // placeholder
    }

    private int calculateCurrentStreak() {
        // Implement streak calculation based on study sessions
        return 6; // placeholder
    }

    private int calculateAverageFocus() {
        // Calculate from Pomodoro cycles
        return 84; // placeholder
    }

    private List<StudySession> getSessionsForTimePeriod(TimePeriod period) {
        // This would require a new method in StudySessionRepository
        // Return placeholder data for now
        return createMockSessions();
    }

    private Map<Long, Long> getStudyTimeBySubject() {
        // This would require a new method in StudySessionRepository
        Map<Long, Long> result = new HashMap<>();
        result.put(1L, 8 * 60 * 60 * 1000L); // 8 hours for subject 1
        result.put(2L, 5 * 60 * 60 * 1000L); // 5 hours for subject 2
        result.put(3L, 3 * 60 * 60 * 1000L); // 3 hours for subject 3
        return result;
    }

    private List<FlashcardTerm> getAllFlashcardTerms(List<FlashcardDeck> decks) {
        List<FlashcardTerm> allTerms = new ArrayList<>();
        for (FlashcardDeck deck : decks) {
            List<FlashcardTerm> terms = flashcardRepository.getTermsForDeck(deck.getId());
            allTerms.addAll(terms);
        }
        return allTerms;
    }

    private List<QuizAttempt> getAllQuizAttempts(List<Quiz> quizzes) {
        List<QuizAttempt> allAttempts = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            List<QuizAttempt> attempts = quizRepository.getQuizAttempts(quiz.getId());
            allAttempts.addAll(attempts);
        }
        return allAttempts;
    }

    // Data conversion methods (same as before but adapted for your repositories)

    private List<DailyStudyTime> convertSessionsToDailyData(List<StudySession> sessions) {
        Map<String, Float> dailyMinutes = new HashMap<>();

        for (StudySession session : sessions) {
            String dayKey = getDayKey(session.getStartTime());
            float minutes = session.getDuration() / (1000 * 60); // Convert ms to minutes
            dailyMinutes.put(dayKey, dailyMinutes.getOrDefault(dayKey, 0f) + minutes);
        }

        List<DailyStudyTime> result = new ArrayList<>();
        for (Map.Entry<String, Float> entry : dailyMinutes.entrySet()) {
            result.add(new DailyStudyTime(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    private List<WeeklyAverage> convertSessionsToWeeklyData(List<StudySession> sessions) {
        Map<String, List<Float>> weeklyData = new HashMap<>();

        for (StudySession session : sessions) {
            String weekKey = getWeekKey(session.getStartTime());
            float hours = session.getDuration() / (1000 * 60 * 60); // Convert ms to hours

            if (!weeklyData.containsKey(weekKey)) {
                weeklyData.put(weekKey, new ArrayList<>());
            }
            weeklyData.get(weekKey).add(hours);
        }

        List<WeeklyAverage> result = new ArrayList<>();
        for (Map.Entry<String, List<Float>> entry : weeklyData.entrySet()) {
            float average = (float) entry.getValue().stream().mapToDouble(Float::doubleValue).average().orElse(0);
            result.add(new WeeklyAverage(entry.getKey(), average));
        }

        return result;
    }

    private List<SubjectStats> createSubjectStats(List<Subject> subjects, Map<Long, Long> subjectTimes) {
        List<SubjectStats> result = new ArrayList<>();
        long totalTime = subjectTimes.values().stream().mapToLong(Long::longValue).sum();

        for (Subject subject : subjects) {
            Long subjectId = subject.getId();
            long studyTimeMs = subjectTimes.getOrDefault(subjectId, 0L);
            float minutes = studyTimeMs / (1000 * 60);
            float percentage = totalTime > 0 ? (studyTimeMs * 100f) / totalTime : 0;

            String timeFormatted = formatStudyTime(minutes);
            result.add(new SubjectStats(subject.getName(), timeFormatted, percentage));
        }

        result.sort((s1, s2) -> Float.compare(s2.getPercentage(), s1.getPercentage()));
        return result;
    }

    private TaskProgress calculateTaskProgress(List<Task> tasks) {
        int completed = 0;
        int total = 0;
        int upcoming = 0;

        Date tomorrow = getTomorrow();

        for (Task task : tasks) {
            if (task.getStatus() == Task.TaskType.COMPLETED) {
                completed++;
            }
            total++;

            if (task.getDeadline() != null &&
                    (isSameDay(task.getDeadline(), new Date()) ||
                            isSameDay(task.getDeadline(), tomorrow))) {
                upcoming++;
            }
        }

        int completionRate = total > 0 ? (completed * 100) / total : 0;
        return new TaskProgress(completionRate, completed, total, upcoming);
    }

    private List<Task> getUpcomingTasks(List<Task> tasks) {
        List<Task> upcoming = new ArrayList<>();
        Date tomorrow = getTomorrow();
        Date dayAfterTomorrow = getDayAfterTomorrow();

        for (Task task : tasks) {
            if (task.getDeadline() != null &&
                    task.getStatus() != Task.TaskType.COMPLETED &&
                    (isSameDay(task.getDeadline(), tomorrow) ||
                            isSameDay(task.getDeadline(), dayAfterTomorrow))) {
                upcoming.add(task);
            }
        }

        upcoming.sort(Comparator.comparing(Task::getDeadline));
        return upcoming;
    }

    private FlashcardStats createFlashcardStats(List<FlashcardDeck> decks, List<FlashcardTerm> terms) {
        int totalCards = terms.size();
        int totalDecks = decks.size();

        // Calculate average rating
        double avgRating = terms.stream()
                .mapToInt(FlashcardTerm::getRating)
                .average()
                .orElse(0);

        // Find most studied deck
        String mostStudiedDeck = decks.stream()
                .max(Comparator.comparingInt(FlashcardDeck::getCardCount))
                .map(FlashcardDeck::getTitle)
                .orElse("None");

        int accuracy = (int) ((avgRating / 5.0) * 100);

        return new FlashcardStats(totalDecks, totalCards, accuracy, 0, mostStudiedDeck);
    }

    private QuizStats createQuizStats(List<Quiz> quizzes, List<QuizAttempt> attempts) {
        if (attempts.isEmpty()) {
            return new QuizStats(0, "0m", "None", 0, 0);
        }

        double avgScore = attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0);

        long fastestDuration = attempts.stream()
                .mapToLong(QuizAttempt::getDuration)
                .min()
                .orElse(0);

        Map<Long, Double> quizScores = new HashMap<>();
        for (QuizAttempt attempt : attempts) {
            Long quizId = attempt.getQuizId();
            double currentAvg = quizScores.getOrDefault(quizId, 0.0);
            quizScores.put(quizId, (currentAvg + attempt.getScore()) / 2);
        }

        String highestScoreDeck = "None";
        int highestScore = 0;
        for (Map.Entry<Long, Double> entry : quizScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue().intValue();
                highestScoreDeck = findQuizTitle(quizzes, entry.getKey());
            }
        }

        String fastestFormatted = formatDuration(fastestDuration);
        return new QuizStats((int) avgScore, fastestFormatted, highestScoreDeck, highestScore, attempts.size());
    }

    private NotesStats createNotesStats(List<Resource> resources) {
        // Placeholder - you'll need to implement NoteRepository
        int notesCount = 32; // Get from NoteRepository
        int resourcesCount = resources != null ? resources.size() : 0;

        return new NotesStats(notesCount, resourcesCount, "Mathematics", "Derivatives Summary", 5);
    }

    // Utility methods (same as before)

    private String formatStudyTime(float minutes) {
        if (minutes < 60) {
            return String.format(Locale.getDefault(), "%.0fm", minutes);
        } else {
            float hours = minutes / 60;
            return String.format(Locale.getDefault(), "%.1fh", hours);
        }
    }

    private String formatStudyTime(long milliseconds) {
        float minutes = milliseconds / (1000 * 60);
        return formatStudyTime(minutes);
    }

    private String getDayKey(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        return sdf.format(date);
    }

    private String getWeekKey(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        return "Week " + week;
    }

    private Date getTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }

    private Date getDayAfterTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        return cal.getTime();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String findQuizTitle(List<Quiz> quizzes, Long quizId) {
        return quizzes.stream()
                .filter(quiz -> quiz.getId().equals(quizId))
                .findFirst()
                .map(Quiz::getTitle)
                .orElse("Unknown Quiz");
    }

    private String formatDuration(long durationMs) {
        long minutes = durationMs / (1000 * 60);
        long seconds = (durationMs % (1000 * 60)) / 1000;
        return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds);
    }

    private List<StudySession> createMockSessions() {
        List<StudySession> sessions = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        // Create mock data for the last 7 days
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);

            StudySession session = new StudySession();
            session.setStartTime(cal.getTime());
            session.setDuration((long) (Math.random() * 120 + 30) * 60 * 1000); // 30-150 minutes
            sessions.add(session);
        }

        return sessions;
    }

    // Getters for LiveData
    public LiveData<List<DailyStudyTime>> getDailyStudyTime() {
        return dailyStudyTime;
    }

    public LiveData<List<WeeklyAverage>> getWeeklyTrend() {
        return weeklyTrend;
    }

    public LiveData<List<SubjectStats>> getSubjectBreakdown() {
        return subjectBreakdown;
    }

    public LiveData<TaskProgress> getTaskProgress() {
        return taskProgress;
    }

    public LiveData<List<Task>> getUpcomingTasks() {
        return upcomingTasks;
    }

    public LiveData<FlashcardStats> getFlashcardStats() {
        return flashcardStats;
    }

    public LiveData<QuizStats> getQuizStats() {
        return quizStats;
    }

    public LiveData<NotesStats> getNotesStats() {
        return notesStats;
    }

    public LiveData<StudyOverview> getStudyOverview() {
        return studyOverview;
    }
}