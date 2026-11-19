package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Item of GET /api/professor/projects (ProjectController@professorIndex). */
public class ProfessorProject {
    @SerializedName("id") public int id;
    @SerializedName("title") public String title;
    @SerializedName("description") public String description;
    @SerializedName("status") public String status;
    @SerializedName("progress_percent") public int progressPercent;
    @SerializedName("start_date") public String startDate;
    @SerializedName("end_date") public String endDate;
    @SerializedName("group_name") public String groupName;
    @SerializedName("team_members") public List<DashboardData.Member> teamMembers;
    @SerializedName("has_evaluation") public boolean hasEvaluation;
}
