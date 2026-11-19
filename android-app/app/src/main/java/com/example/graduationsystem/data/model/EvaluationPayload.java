package com.example.graduationsystem.data.model;

import com.google.gson.annotations.SerializedName;

/** Envelope returned by GET /api/projects/{id}/evaluate (EvaluationController@show). */
public class EvaluationPayload {
    @SerializedName("project") public ProjectInfo project;
    @SerializedName("already_evaluated") public boolean alreadyEvaluated;
    @SerializedName("evaluation") public DashboardData.Evaluation evaluation;

    public static class ProjectInfo {
        @SerializedName("id") public int id;
        @SerializedName("title") public String title;
        @SerializedName("group_name") public String groupName;
    }
}
