package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Role-specific dashboard payload from GET /api/dashboard.
 * Only the fields relevant to the current role are populated by the server.
 */
public class DashboardData {
    @SerializedName("role") public String role;
    @SerializedName("name") public String name;

    // ---- student ----
    @SerializedName("has_group") public boolean hasGroup;
    @SerializedName("can_create_group") public boolean canCreateGroup;
    @SerializedName("can_register_project") public boolean canRegisterProject;
    @SerializedName("group") public Group group;
    @SerializedName("project") public Project project;

    // ---- professor ----
    @SerializedName("pending_count") public int pendingCount;
    @SerializedName("active_projects") public List<Project> activeProjects;

    // ---- admin ----
    @SerializedName("users_count") public int usersCount;
    @SerializedName("projects_count") public int projectsCount;
    @SerializedName("pending_projects_count") public int pendingProjectsCount;

    public static class Group {
        @SerializedName("id") public int id;
        @SerializedName("name") public String name;
        @SerializedName("members") public List<Member> members;
    }

    public static class Member {
        @SerializedName("id") public int id;
        @SerializedName("name") public String name;
        @SerializedName("email") public String email;
    }

    public static class Project {
        @SerializedName("id") public int id;
        @SerializedName("title") public String title;
        @SerializedName("description") public String description;
        @SerializedName("status") public String status;
        @SerializedName("progress_percent") public int progressPercent;
        @SerializedName("start_date") public String startDate;
        @SerializedName("end_date") public String endDate;
        @SerializedName("professor_name") public String professorName;
        @SerializedName("group_name") public String groupName;
        @SerializedName("has_evaluation") public boolean hasEvaluation;
        @SerializedName("team_members") public List<Member> teamMembers;
        @SerializedName("evaluation") public Evaluation evaluation;
    }

    public static class Evaluation {
        @SerializedName("score") public double score;
        @SerializedName("comments") public String comments;
        @SerializedName("evaluated_at") public String evaluatedAt;
    }
}
