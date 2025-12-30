package com.inrupipresennce.data.api



import com.inrupipresennce.data.api.model.AttendanceResponse
import com.inrupipresennce.data.api.model.AttendanceTodayResponse
import com.inrupipresennce.data.api.model.BirthdayResponse
import com.inrupipresennce.data.api.model.EarlyBirdResponse
import com.inrupipresennce.data.api.model.LoginResult
import com.inrupipresennce.data.api.model.LunchResponse
import com.inrupipresennce.data.api.model.OffTodayResponse
import com.inrupipresennce.data.api.model.PresenceResponse
import com.inrupipresennce.data.api.model.request.LoginRequest
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

    @FormUrlEncoded
    @POST("attendance/history")
    suspend fun getPresenceHistory(
        @Field("admin_id") adminId: Int
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




}