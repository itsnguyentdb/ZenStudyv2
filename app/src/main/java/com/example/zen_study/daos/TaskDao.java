package com.example.zen_study.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.Task;

import java.util.Date;
import java.util.List;

@Dao
public abstract class TaskDao extends AbstractGenericDao<Task> {
    public TaskDao() {
        super("task");
    }

    @Query("SELECT * FROM task WHERE subject_id = :subjectId")
    public abstract List<Task> findTasksBySubjectId(long subjectId);

    @Query("SELECT * FROM task WHERE subject_id = :subjectId ORDER BY priority DESC, deadline ASC")
    public abstract LiveData<List<Task>> getTasksBySubject(long subjectId);

    @Query("SELECT * FROM task WHERE parent_task_id = :parentTaskId")
    public abstract LiveData<List<Task>> getSubtasks(long parentTaskId);

    @Query("SELECT * FROM task WHERE parent_task_id = 0")
    public abstract LiveData<List<Task>> getRootTasks();

    @Query("SELECT * FROM task WHERE status = :status")
    public abstract LiveData<List<Task>> getTasksByStatus(Task.TaskType status);

    @Query("SELECT * FROM task WHERE deadline BETWEEN :start AND :end")
    public abstract LiveData<List<Task>> getTasksByDateRange(Date start, Date end);

    @Query("UPDATE task SET progress = :progress, progressDuration = :progressDuration WHERE id = :taskId")
    public abstract void updateProgress(long taskId, float progress, int progressDuration);

    @Query("UPDATE task SET status = :status WHERE id = :taskId")
    public abstract void updateStatus(long taskId, Task.TaskType status);

    @Query("DELETE FROM task WHERE parent_task_id = :taskId")
    public abstract void deleteTasksByParentId(long taskId);

    @RawQuery(observedEntities = {Task.class})
    protected abstract LiveData<Task> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {Task.class})
    protected abstract LiveData<List<Task>> _findAllLiveData(SupportSQLiteQuery query);
}
