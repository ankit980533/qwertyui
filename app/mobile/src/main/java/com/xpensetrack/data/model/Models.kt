package com.xpensetrack.data.model

data class SignupRequest(val fullName: String, val phoneNumber: String, val email: String,
    val password: String, val confirmPassword: String, val termsAccepted: Boolean)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val userId: String, val fullName: String, val email: String)

data class UserProfile(val id: String, val displayId: String, val fullName: String, val email: String,
    val phoneNumber: String, val address: String?, val hostel: String?, val avatarUrl: String?,
    val coins: Int, val currentBalance: Double, val monthlyBudget: Double,
    val totalSaved: Double, val totalSpent: Double, val monthsActive: Int,
    val activeLabel: String?, val friendCount: Int, val joinedMonth: String)
data class UpdateProfileRequest(val fullName: String? = null, val phoneNumber: String? = null,
    val address: String? = null, val hostel: String? = null, val monthlyBudget: Double? = null)

data class AddExpenseRequest(val amount: Double, val description: String?, val category: String,
    val note: String? = null, val date: String? = null, val splitWithFriendIds: List<String> = emptyList())
data class ExpenseItem(val id: String, val amount: Double, val description: String?,
    val category: String, val note: String?, val date: String)

data class DashboardData(val fullName: String, val currentBalance: Double, val monthlyBudget: Double,
    val monthlySpent: Double, val remaining: Double, val budgetUsedPercent: Double,
    val budgetLeftPercent: Double, val coins: Int, val recentExpenses: List<ExpenseItem>,
    val dragonLevel: Int, val dragonHappiness: Int, val dragonHungry: Boolean,
    val monthlyBreakdown: Map<String, Double>, val unreadNotificationCount: Int)

data class TrendPoint(val label: String, val spent: Double, val budget: Double?, val saved: Double?)
data class ReportData(val period: String, val totalSpent: Double, val totalSaved: Double,
    val savedChangePercent: Double, val monthlySpendingTrend: List<TrendPoint>,
    val weeklySpending: List<TrendPoint>, val savingsTrend: List<TrendPoint>)

data class CalendarDayInfo(val date: String, val totalSpent: Double, val status: String)
data class UpcomingEventItem(val id: String, val title: String, val amount: Double, val dueDate: String, val paid: Boolean)
data class CalendarData(val year: Int, val month: Int, val dailyBudget: Double = 0.0,
    val weeklySpendLimit: Double = 0.0, val todayTotalSpent: Double = 0.0,
    val budgetOverrun: Boolean = false, val days: List<CalendarDayInfo>,
    val upcomingEvents: List<UpcomingEventItem>, val todayExpenses: List<ExpenseItem>)

data class FriendsOverview(val youOwe: Double, val toReceive: Double, val friendBalances: List<FriendBalanceItem>)
data class FriendBalanceItem(val userId: String, val fullName: String, val avatarUrl: String?,
    val amount: Double, val label: String, val transactions: List<TransactionItem>)
data class TransactionItem(val description: String, val amount: Double, val status: String)
data class FriendItem(val id: String, val fullName: String, val displayId: String,
    val email: String, val hostel: String?, val avatarUrl: String?)
data class FriendRequestItem(val id: String, val userId: String, val fullName: String,
    val displayId: String, val hostel: String?, val avatarUrl: String?, val mutualFriends: Int)

data class DragonData(val name: String, val level: Int, val happiness: Int, val experience: Int,
    val coinsToNextLevel: Int, val levelUpMessage: String, val accessories: List<String>,
    val activeSkin: String?, val userCoins: Int, val feedCost: Int, val shopMinCost: Int)

data class ShopItemData(val id: String, val name: String, val description: String?,
    val category: String, val price: Int, val happinessBoost: Int, val experienceBoost: Int,
    val imageUrl: String?, val owned: Boolean)
data class CoinPackData(val id: String, val coins: Int, val totalCoins: Int, val priceInr: Double, val label: String?)
data class ShopData(val userCoins: Int, val food: List<ShopItemData>, val addins: List<ShopItemData>,
    val skins: List<ShopItemData>, val coinPacks: List<CoinPackData>)

data class PiggyBankGoal(val id: String, val goalName: String, val targetAmount: Double,
    val savedAmount: Double, val deadline: String, val progressPercent: Double,
    val dailySavingNeeded: Double, val imageUrl: String?)
data class PiggyBankOverview(val monthlySavings: Double, val savingsTarget: Double,
    val savingsProgressPercent: Double, val recentGoals: List<PiggyBankGoal>)

data class ChatResponseData(val reply: String, val quickActions: List<String>)
data class ChatMessageItem(val id: String, val role: String, val content: String, val createdAt: String)

data class NotificationItem(val id: String, val type: String, val title: String,
    val message: String, val avatarUrl: String?, val read: Boolean, val createdAt: String)

data class PaymentResult(val id: String, val toUserName: String, val amount: Double,
    val method: String, val status: String, val cashbackPercent: Double,
    val cashbackAmount: Double, val secure: Boolean)

data class GroupItem(val id: String, val name: String, val memberIds: List<String>,
    val createdBy: String)

data class SplitExpenseItem(val id: String, val groupId: String?, val paidByUserId: String,
    val description: String, val totalAmount: Double)

data class CreateGroupRequest(val name: String, val memberIds: List<String>)

data class SplitAmongEntry(val userId: String, val amount: Double)

data class SplitExpenseRequest(val description: String, val totalAmount: Double,
    val groupId: String?, val splitAmong: List<SplitAmongEntry>)

data class CreatePiggyBankRequest(val goalName: String, val targetAmount: Double, val deadline: String)
