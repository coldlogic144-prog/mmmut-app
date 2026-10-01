package com.mmmut.ero.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.mmmut.ero.core.BackendConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class HealthResponse(
    val ok: Boolean = false,
    val service: String? = null,
    val rosterCount: Int = 0,
    val branches: Map<String, Int>? = null
)

data class RosterRecordDto(
    val rollNumber: String = "",
    val enrollmentNo: String = "",
    val applicantName: String = "",
    val formalName: String = "",
    val branchName: String = "",
    val section: String = "",
    val batch: String = "",
    val block: Int = 0,
    val sourceFormNumber: String = ""
)

data class RosterResponse(
    val ok: Boolean = false,
    val found: Boolean = false,
    val record: RosterRecordDto? = null
)

data class TelegramCreateTokenReq(val uid: String)
data class TelegramCreateTokenResp(val ok: Boolean = false, val token: String? = null, val error: String? = null)

data class TelegramTokenStatusResp(val ok: Boolean = false, val status: String? = null, val uid: String? = null)

data class TelegramInviteResp(val ok: Boolean = false, val inviteLink: String? = null)

data class TelegramCheckMembershipReq(val uid: String, val telegramUserId: String? = null)
data class TelegramCheckMembershipResp(val ok: Boolean = false, val isMember: Boolean = false, val status: String? = null)

interface MmmutApiService {
    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/roster/{roll}")
    suspend fun getRoster(@Path("roll") roll: String): Response<RosterResponse>

    @POST("api/telegram/create-token")
    suspend fun createTelegramToken(@Body body: TelegramCreateTokenReq): Response<TelegramCreateTokenResp>

    @GET("api/telegram/token-status/{token}")
    suspend fun getTelegramTokenStatus(@Path("token") token: String): Response<TelegramTokenStatusResp>

    @GET("api/telegram/channel-invite")
    suspend fun getTelegramChannelInvite(): Response<TelegramInviteResp>

    @POST("api/telegram/check-membership")
    suspend fun checkTelegramMembership(@Body body: TelegramCheckMembershipReq): Response<TelegramCheckMembershipResp>
}

object MmmutApiClient {
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                // Synchronous fetch of token for interceptor or skip if unauthenticated
                val tokenTask = user.getIdToken(false)
                // Timeout wait or attempt token
                val idToken = com.google.android.gms.tasks.Tasks.await(tokenTask, 2, TimeUnit.SECONDS).token
                if (!idToken.isNullOrBlank()) {
                    builder.header("Authorization", "Bearer $idToken")
                }
            }
        } catch (_: Exception) { }
        chain.proceed(builder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    val service: MmmutApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BackendConfig.API_BASE_URL.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MmmutApiService::class.java)
    }
}
