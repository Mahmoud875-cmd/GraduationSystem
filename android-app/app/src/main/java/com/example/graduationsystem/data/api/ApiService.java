package com.example.graduationsystem.data.api;

import com.example.graduationsystem.data.model.AdminProject;
import com.example.graduationsystem.data.model.AdminUser;
import com.example.graduationsystem.data.model.ApiResponse;
import com.example.graduationsystem.data.model.AuthData;
import com.example.graduationsystem.data.model.DashboardData;
import com.example.graduationsystem.data.model.EvaluationPayload;
import com.example.graduationsystem.data.model.ProfessorProject;
import com.example.graduationsystem.data.model.ReportsPayload;
import com.example.graduationsystem.data.model.TasksPayload;
import com.example.graduationsystem.data.model.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/** Single Retrofit interface mirroring routes/api.php. */
public interface ApiService {

    // ---- Auth ----
    @FormUrlEncoded
    @POST("register")
    Call<ApiResponse<AuthData>> register(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("login")
    Call<ApiResponse<AuthData>> login(@Field("email") String email, @Field("password") String password);

    @POST("logout")
    Call<ApiResponse<Object>> logout();

    @GET("me")
    Call<ApiResponse<User>> me();

    @GET("dashboard")
    Call<ApiResponse<DashboardData>> dashboard();

    // ---- Student: group + project ----
    @FormUrlEncoded
    @POST("groups")
    Call<ApiResponse<DashboardData.Group>> createGroup(@Field("name") String name,
                                                       @Field("member_emails") String emails);

    @GET("professors")
    Call<ApiResponse<List<User>>> professors();

    @FormUrlEncoded
    @POST("projects")
    Call<ApiResponse<DashboardData.Project>> createProject(@Field("title") String title,
                                                           @Field("description") String description,
                                                           @Field("professor_id") int professorId);

    // ---- Professor: review topics ----
    @GET("professor/projects")
    Call<ApiResponse<List<ProfessorProject>>> professorProjects();

    @FormUrlEncoded
    @POST("projects/{id}/approve")
    Call<ApiResponse<Object>> approveProject(@Path("id") int id,
                                             @Field("start_date") String startDate,
                                             @Field("end_date") String endDate);

    @POST("projects/{id}/reject")
    Call<ApiResponse<Object>> rejectProject(@Path("id") int id);

    // ---- Tasks ----
    @GET("projects/{id}/tasks")
    Call<ApiResponse<TasksPayload>> tasks(@Path("id") int projectId);

    @FormUrlEncoded
    @POST("projects/{id}/tasks")
    Call<ApiResponse<TasksPayload.TaskItem>> createTask(@Path("id") int projectId,
                                                        @Field("title") String title,
                                                        @Field("description") String description,
                                                        @Field("deadline") String deadline,
                                                        @Field("assigned_to") int assignedTo);

    @FormUrlEncoded
    @PATCH("tasks/{id}/status")
    Call<ApiResponse<Map<String, Object>>> updateTaskStatus(@Path("id") int taskId,
                                                            @Field("status") String status);

    // ---- Reports ----
    @GET("projects/{id}/reports")
    Call<ApiResponse<ReportsPayload>> reports(@Path("id") int projectId);

    @Multipart
    @POST("projects/{id}/reports")
    Call<ApiResponse<ReportsPayload.ReportItem>> uploadReport(@Path("id") int projectId,
                                                              @Part MultipartBody.Part file,
                                                              @Part("task_id") RequestBody taskId);

    @Streaming
    @GET
    Call<okhttp3.ResponseBody> downloadFile(@Url String url);

    @FormUrlEncoded
    @PATCH("reports/{id}/feedback")
    Call<ApiResponse<Object>> reportFeedback(@Path("id") int reportId,
                                             @Field("feedback") String feedback);

    // ---- Evaluation ----
    @GET("projects/{id}/evaluate")
    Call<ApiResponse<EvaluationPayload>> evaluationInfo(@Path("id") int projectId);

    @FormUrlEncoded
    @POST("projects/{id}/evaluate")
    Call<ApiResponse<Object>> submitEvaluation(@Path("id") int projectId,
                                               @Field("score") double score,
                                               @Field("comments") String comments);

    // ---- Admin ----
    @GET("admin/users")
    Call<ApiResponse<List<AdminUser>>> adminUsers();

    @FormUrlEncoded
    @POST("admin/users")
    Call<ApiResponse<AdminUser>> createAdminUser(@Field("name") String name,
                                                  @Field("email") String email,
                                                  @Field("password") String password,
                                                  @Field("role") String role);

    @FormUrlEncoded
    @PATCH("admin/users/{id}")
    Call<ApiResponse<AdminUser>> updateAdminUser(@Path("id") int id,
                                                  @Field("name") String name,
                                                  @Field("email") String email,
                                                  @Field("role") String role,
                                                  @Field("password") String password);

    @DELETE("admin/users/{id}")
    Call<ApiResponse<Object>> deleteAdminUser(@Path("id") int id);

    @GET("admin/projects")
    Call<ApiResponse<List<AdminProject>>> adminProjects();

    @FormUrlEncoded
    @POST("admin/projects")
    Call<ApiResponse<AdminProject>> createAdminProject(@Field("title") String title,
                                                        @Field("description") String description,
                                                        @Field("professor_id") Integer professorId,
                                                        @Field("start_date") String startDate,
                                                        @Field("end_date") String endDate,
                                                        @Field("status") String status);

    @FormUrlEncoded
    @PATCH("admin/projects/{id}")
    Call<ApiResponse<AdminProject>> updateAdminProject(@Path("id") int id,
                                                        @Field("title") String title,
                                                        @Field("description") String description,
                                                        @Field("professor_id") Integer professorId,
                                                        @Field("start_date") String startDate,
                                                        @Field("end_date") String endDate,
                                                        @Field("status") String status,
                                                        @Field("progress_percent") Integer progressPercent);

    @DELETE("admin/projects/{id}")
    Call<ApiResponse<Object>> deleteAdminProject(@Path("id") int id);

    @FormUrlEncoded
    @PATCH("admin/projects/{id}/extend")
    Call<ApiResponse<AdminProject>> extendDeadline(@Path("id") int id,
                                                    @Field("end_date") String endDate);
}
