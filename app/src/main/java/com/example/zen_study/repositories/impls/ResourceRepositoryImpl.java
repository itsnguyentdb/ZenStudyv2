package com.example.zen_study.repositories.impls;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.zen_study.daos.FileMetadataDao;
import com.example.zen_study.daos.ResourceDao;
import com.example.zen_study.daos.SubjectDao;
import com.example.zen_study.daos.TaskDao;
import com.example.zen_study.helpers.AppDatabase;
import com.example.zen_study.models.FileMetadata;
import com.example.zen_study.models.Resource;
import com.example.zen_study.models.Subject;
import com.example.zen_study.models.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ResourceRepositoryImpl {
    private static final String TAG = "ResourceRepository";
    private final Context context;
    private final ResourceDao resourceDao;
    private final FileMetadataDao fileMetadataDao;
    private final SubjectDao subjectDao;
    private final TaskDao taskDao;

    public ResourceRepositoryImpl(Context context) {
        this.context = context;
        var instance = AppDatabase.getInstance(context);
        this.resourceDao = instance.resourceDao();
        this.fileMetadataDao = instance.fileMetadataDao();
        this.taskDao = instance.taskDao();
        this.subjectDao = instance.subjectDao();
    }

    public Resource createResource(Uri fileUri, Long taskId, Long subjectId, String title, String type) {
        FileMetadata fileMetadata = uploadFile(fileUri);
        if (fileMetadata == null) {
            throw new RuntimeException("Failed to upload file");
        }

        Resource resource = Resource.builder()
                .fileMetadataId(fileMetadata.getId())
                .taskId(taskId)
                .subjectId(subjectId)
                .title(title)
                .type(type)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        resourceDao.save(resource);
        Log.i(TAG, "Created resource with ID: " + resource.getId());
        return resource;
    }

    public void deleteResource(Long resourceId) {
        try {
            Optional<Resource> resourceOpt = resourceDao.findById(resourceId);
            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                resourceDao.delete(resource);
                if (resource.getFileMetadataId() != null) {
                    deleteFileMetadataAndFile(resource.getFileMetadataId());
                }
                Log.i(TAG, "Deleted resource with ID: " + resourceId);
            } else {
                Log.w(TAG, "No resource found for ID: " + resourceId);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Resource deletion failed", ex);
            throw new RuntimeException("Resource deletion failed");
        }
    }

    private FileMetadata uploadFile(Uri uri) {
        File tempFile = null;
        InputStream inputStream = null;
        try {
            String originalFilename = getFileName(uri);
            String uniqueId = UUID.randomUUID().toString();
            String localFilename = uniqueId + "_" + originalFilename;
            String tempFilename = "temp_" + localFilename;

            File cacheDir = context.getCacheDir();
            File filesDir = new File(context.getFilesDir(), "storage");
            if (!filesDir.exists()) {
                filesDir.mkdirs(); // Create the directory
            }

            tempFile = new File(cacheDir, tempFilename);
            File finalFile = new File(filesDir, localFilename);

            // Copy file to temp location and calculate MD5
            inputStream = context.getContentResolver().openInputStream(uri);
            long size = getFileSize(uri);

            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(inputStream, md);

            FileOutputStream tempOut = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = dis.read(buffer)) != -1) {
                tempOut.write(buffer, 0, bytesRead);
            }
            tempOut.close();
            dis.close();

            byte[] digest = md.digest();
            String md5Checksum = bytesToHex(digest);

            // Check for duplicate
            Optional<FileMetadata> duplicate = fileMetadataDao.findByMd5Checksum(md5Checksum);
            if (duplicate.isPresent()) {
                tempFile.delete(); // Clean up temp file
                return duplicate.get();
            }

            String contentType = context.getContentResolver().getType(uri);
            Long duration = null;
            if (contentType != null && contentType.startsWith("video")) {
                duration = getVideoDuration(uri);
            }

            // Move to final location by renaming the temp file
            if (tempFile.renameTo(finalFile)) {
                FileMetadata fileMetadata = FileMetadata.builder()
                        .contentType(contentType)
                        .size(size)
                        .duration(duration)
                        .md5Checksum(md5Checksum)
                        .path(finalFile.getAbsolutePath())
                        .uploadedAt(new Date())
                        .build();

                fileMetadataDao.save(fileMetadata);
                return fileMetadata;
            } else {
                throw new IOException("Failed to move file to final location");
            }

        } catch (IOException | NoSuchAlgorithmException | NullPointerException ex) {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete(); // Clean up on failure
            }
            Log.e(TAG, "File upload failed", ex);
            throw new RuntimeException("File upload failed");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void deleteFileMetadataAndFile(Long fileMetadataId) {
        try {
            Optional<FileMetadata> metadataOpt = fileMetadataDao.findById(fileMetadataId);
            if (metadataOpt.isPresent()) {
                FileMetadata metadata = metadataOpt.get();
                File physicalFile = new File(metadata.getPath());
                if (physicalFile.exists()) {
                    if (physicalFile.delete()) {
                        Log.i(TAG, "Deleted physical file: " + physicalFile.getName());
                    } else {
                        Log.w(TAG, "Failed to delete physical file: " + physicalFile.getName());
                    }
                }
                fileMetadataDao.delete(metadata);
                Log.i(TAG, "Deleted file metadata for ID: " + fileMetadataId);
            }
        } catch (Exception ex) {
            Log.e(TAG, "File deletion failed", ex);
            throw new RuntimeException("File deletion failed");
        }
    }

    private Long getVideoDuration(Uri uri) {
        try {
            android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
            retriever.setDataSource(context, uri);
            String durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();

            if (durationStr != null) {
                return Long.parseLong(durationStr);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get video duration", e);
        }
        return null;
    }

    /**
     * Gets file name from URI
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to get file name from content URI", e);
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    /**
     * Gets file size from URI
     */
    private long getFileSize(Uri uri) {
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        return cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to get file size", e);
            }
        }
        return 0;
    }

    private String getFilenameExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public LiveData<List<Resource>> getAllResources() {
        return resourceDao.findAllLiveData();
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.findAllLiveData();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return subjectDao.findAllLiveData();
    }


    public Resource updateResource(Long resourceId, String title, String type, Long taskId, Long subjectId) {
        return resourceDao.save(Resource.builder()
                .id(resourceId)
                .title(title)
                .type(type)
                .taskId(taskId)
                .subjectId(subjectId)
                .build());
    }

    public LiveData<Optional<Resource>> getResourceById(Long resourceId) {
        return resourceDao.findByIdLiveData(resourceId);
    }

    public Optional<FileMetadata> getFileMetadataById(Long fileMetadataId) {
        return fileMetadataDao.findById(fileMetadataId);
    }
}