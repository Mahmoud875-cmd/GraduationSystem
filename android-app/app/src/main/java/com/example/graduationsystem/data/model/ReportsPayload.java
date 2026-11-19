package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Envelope returned by GET /api/projects/{id}/reports (ReportController@index). */
public class ReportsPayload {
    @SerializedName("project") public ProjectInfo project;
    @SerializedName("tasks") public List<TaskRef> tasks;
    @SerializedName("reports") public List<ReportItem> reports;

    public static class ProjectInfo {
        @SerializedName("id") public int id;
        @SerializedName("title") public String title;
        @SerializedName("is_professor") public boolean isProfessor;
    }

    public static class TaskRef {
        @SerializedName("id") public int id;
        @SerializedName("title") public String title;
    }

    public static class ReportItem {
        @SerializedName("id") public int id;
        @SerializedName("project_id") public int projectId;
        @SerializedName("task_id") public Integer taskId;
        @SerializedName("task_title") public String taskTitle;
        @SerializedName("file_name") public String fileName;
        @SerializedName("feedback") public String feedback;
        @SerializedName("submitted_at") public String submittedAt;
        @SerializedName("download_url") public String downloadUrl;
    }
}
