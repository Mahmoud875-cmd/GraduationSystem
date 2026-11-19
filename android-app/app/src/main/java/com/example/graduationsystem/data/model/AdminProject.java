package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;

/** Item of GET /api/admin/projects (AdminController@projects). */
public class AdminProject {
    @SerializedName("id") public int id;
    @SerializedName("title") public String title;
    @SerializedName("description") public String description;
    @SerializedName("status") public String status;
    @SerializedName("progress_percent") public int progressPercent;
    @SerializedName("group_id") public Integer groupId;
    @SerializedName("professor_id") public Integer professorId;
    @SerializedName("group_name") public String groupName;
    @SerializedName("professor_name") public String professorName;
    @SerializedName("score") public Double score;
    @SerializedName("start_date") public String startDate;
    @SerializedName("end_date") public String endDate;
}
