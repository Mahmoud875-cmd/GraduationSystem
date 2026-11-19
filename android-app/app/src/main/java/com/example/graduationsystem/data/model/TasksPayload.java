package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Envelope returned by GET /api/projects/{id}/tasks (TaskController@index). */
public class TasksPayload {
    @SerializedName("project") public ProjectInfo project;
    @SerializedName("team_members") public List<DashboardData.Member> teamMembers;
    @SerializedName("tasks") public List<TaskItem> tasks;

    public static class ProjectInfo {
        @SerializedName("id") public int id;
        @SerializedName("title") public String title;
        @SerializedName("status") public String status;
        @SerializedName("progress_percent") public int progressPercent;
        @SerializedName("is_professor") public boolean isProfessor;
    }

    public static class TaskItem {
        @SerializedName("id") public int id;
        @SerializedName("project_id") public int projectId;
        @SerializedName("title") public String title;
        @SerializedName("description") public String description;
        @SerializedName("deadline") public String deadline;
        @SerializedName("status") public String status; // todo | in_progress | done | late
        @SerializedName("assigned_to") public int assignedTo;
        @SerializedName("assignee_name") public String assigneeName;
    }
}
