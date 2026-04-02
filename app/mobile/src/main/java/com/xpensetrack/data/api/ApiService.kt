package com.xpensetrack.data.api

import com.xpensetrack.data.model.*
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/signup")
    suspend fun signup(@Body req: SignupRequest): AuthResponse
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse
}

interface ProfileApi {
    @GET("api/profile")
    suspend fun getProfile(): UserProfile
    @PUT("api/profile")
    suspend fun updateProfile(@Body req: UpdateProfileRequest): UserProfile
}

interface ExpenseApi {
    @POST("api/expenses")
    suspend fun addExpense(@Body req: AddExpenseRequest): ExpenseItem
    @GET("api/expenses")
    suspend fun getExpenses(): List<ExpenseItem>
    @GET("api/expenses/dashboard")
    suspend fun getDashboard(): DashboardData
    @GET("api/expenses/report")
    suspend fun getReport(@Query("period") period: String, @Query("year") year: Int, @Query("month") month: Int): ReportData
    @GET("api/expenses/calendar")
    suspend fun getCalendar(@Query("year") year: Int, @Query("month") month: Int): CalendarData
}

interface FriendApi {
    @GET("api/friends/overview")
    suspend fun getOverview(): FriendsOverview
    @GET("api/friends")
    suspend fun getFriends(): List<FriendItem>
    @GET("api/friends/search")
    suspend fun search(@Query("query") query: String): List<FriendItem>
    @GET("api/friends/requests")
    suspend fun getPendingRequests(): List<FriendRequestItem>
    @GET("api/friends/requests/sent")
    suspend fun getSentRequests(): List<FriendRequestItem>
    @POST("api/friends/request")
    suspend fun sendRequest(@Body body: Map<String, String>): retrofit2.Response<Unit>
    @PUT("api/friends/requests/{id}")
    suspend fun respond(@Path("id") id: String, @Query("accept") accept: Boolean): retrofit2.Response<Unit>
    @DELETE("api/friends/requests/{id}")
    suspend fun revokeRequest(@Path("id") id: String): retrofit2.Response<Unit>
    @POST("api/friends/settle")
    suspend fun settle(@Body body: Map<String, String>): retrofit2.Response<Unit>
    @POST("api/friends/groups")
    suspend fun createGroup(@Body body: CreateGroupRequest): GroupItem
    @GET("api/friends/groups")
    suspend fun getGroups(): List<GroupItem>
    @POST("api/friends/split")
    suspend fun splitExpense(@Body body: SplitExpenseRequest): SplitExpenseItem
}

interface DragonApi {
    @GET("api/dragon")
    suspend fun getDragon(): DragonData
    @POST("api/dragon/feed")
    suspend fun feed(@Body body: Map<String, Int>): DragonData
    @GET("api/dragon/shop")
    suspend fun getShop(): ShopData
    @POST("api/dragon/shop/buy")
    suspend fun buyItem(@Body body: Map<String, String>): DragonData
    @POST("api/dragon/shop/coins")
    suspend fun buyCoinPack(@Body body: Map<String, String>): Map<String, Int>
}

interface PiggyBankApi {
    @GET("api/piggybank")
    suspend fun getOverview(): PiggyBankOverview
    @POST("api/piggybank")
    suspend fun create(@Body body: CreatePiggyBankRequest): PiggyBankGoal
    @POST("api/piggybank/{id}/save")
    suspend fun addSavings(@Path("id") id: String, @Body body: Map<String, Double>): PiggyBankGoal
    @POST("api/piggybank/{id}/complete")
    suspend fun markComplete(@Path("id") id: String): PiggyBankGoal
}

interface ChatApi {
    @POST("api/chat")
    suspend fun chat(@Body body: Map<String, String>): ChatResponseData
    @GET("api/chat/history")
    suspend fun getHistory(): List<ChatMessageItem>
}

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getAll(): List<NotificationItem>
    @GET("api/notifications/unread")
    suspend fun getUnread(): List<NotificationItem>
    @PUT("api/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): String
}

interface PaymentApi {
    @POST("api/payments")
    suspend fun pay(@Body body: Map<String, Any>): PaymentResult
    @GET("api/payments/history")
    suspend fun getHistory(): List<PaymentResult>
}
