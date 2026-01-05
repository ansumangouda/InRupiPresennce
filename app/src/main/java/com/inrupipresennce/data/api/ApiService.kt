
package com.inrupipresennce.data.api



import com.inrupipresennce.data.model.AttendanceRequest
import com.inrupipresennce.data.model.AttendanceResponse
import com.inrupipresennce.data.model.AttendanceTodayResponse
import com.inrupipresennce.data.model.BirthdayResponse
import com.inrupipresennce.data.model.EarlyBirdResponse
import com.inrupipresennce.data.model.LeaveResponse
import com.inrupipresennce.data.model.LoginResult
import com.inrupipresennce.data.model.LunchResponse
import com.inrupipresennce.data.model.OffTodayResponse
import com.inrupipresennce.data.model.PayslipResponse
import com.inrupipresennce.data.model.PresenceResponse
import com.inrupipresennce.data.model.TeamMatesResponse
import com.inrupipresennce.data.model.request.ApplyLeaveRequest
import com.inrupipresennce.data.model.request.LoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("attendance/login")
    @Headers("Accept: application/json", "Content-Type: application/json")
    suspend fun login(@Body body: LoginRequest): Response<LoginResult>
    @Multipart
    @POST("attendance/face-scan")
    suspend fun uploadAttendance(
        @Part("admin_id") adminId: RequestBody, // ✅ add this line
        @Part("face_descriptor") faceDescriptor: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?
    ): Response<AttendanceResponse>

    @GET
    @Streaming
    suspend fun downloadFile(
        @Url fileUrl: String
    ): Response<ResponseBody>

    @POST("attendance/history")
    suspend fun getPresenceHistory(
        @Body request: AttendanceRequest
    ): PresenceResponse


    @GET("attendance/today/{admin_id}")
    suspend fun getTodayAttendance(
        @Path("admin_id") adminId: Int
    ): Response<AttendanceTodayResponse>

    @POST("attendance/lunch/{admin_id}")
    suspend fun lunchBreak(
        @Path("admin_id") adminId: Int
    ): Response<LunchResponse>

    @GET("wish-them")
    suspend fun getBirthdays(): Response<BirthdayResponse>

    @GET("off-today")
    suspend fun getOffToday(): Response<OffTodayResponse>

    @GET("early-bird")
    suspend fun getEarlyBirdReport(): Response<EarlyBirdResponse>

    @GET("team-mates/{admin_id}")
    suspend fun getTeamMates(@Path("admin_id") adminId: Int): Response<TeamMatesResponse>

    @GET("payslips/{admin_id}")
    suspend fun getPayslips(@Path("admin_id") adminId: Int): PayslipResponse

    @GET("leaves/{admin_id}")
    suspend fun getLeaves(
        @Path("admin_id") adminId: Int,
        @Query("date") year: String? = null
    ): LeaveResponse

    @POST("leaves")
    suspend fun applyLeave(
        @Body request: ApplyLeaveRequest
    ): LeaveResponse

    @PUT("leaves/{leave_id}")
    suspend fun updateLeave(
        @Path("leave_id") leaveId: Int,
        @Body request: ApplyLeaveRequest
    ): LeaveResponse


}