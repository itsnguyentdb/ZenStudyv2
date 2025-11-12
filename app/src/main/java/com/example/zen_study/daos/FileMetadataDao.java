package com.example.zen_study.daos;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.zen_study.daos.common.AbstractGenericDao;
import com.example.zen_study.models.FileMetadata;
import com.example.zen_study.models.Resource;

import java.util.List;
import java.util.Optional;


@Dao
public abstract class FileMetadataDao extends AbstractGenericDao<FileMetadata> {
    protected FileMetadataDao() {
        super("file_metadata");
    }

    @RawQuery(observedEntities = {FileMetadata.class})
    protected abstract FileMetadata _findByMd5Checksum(SupportSQLiteQuery query);

    public Optional<FileMetadata> findByMd5Checksum(String md5Checksum) {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(
                "SELECT * FROM " + tableName + " WHERE md5Checksum = ?",
                new Object[]{md5Checksum}
        );
        var entity = _findByMd5Checksum(query);
        return entity != null ? Optional.of(entity)
                : Optional.empty();
    }

    @RawQuery(observedEntities = {FileMetadata.class})
    protected abstract LiveData<FileMetadata> _findByIdLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {FileMetadata.class})
    protected abstract LiveData<List<FileMetadata>> _findAllLiveData(SupportSQLiteQuery query);
}
