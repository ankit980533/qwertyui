# 🎮 XpenseTrack - Complete Project Documentation
## Explained Like You're 5 Years Old! 🧒

---

## 📚 Table of Contents
1. [What is XpenseTrack?](#what-is-xpensetrack)
2. [The Big Picture](#the-big-picture)
3. [Backend - The Brain](#backend---the-brain)
4. [Mobile App - The Face](#mobile-app---the-face)
5. [Database - The Memory](#database---the-memory)
6. [How Everything Works Together](#how-everything-works-together)
7. [Code Explanation](#code-explanation)

---

## 🎯 What is XpenseTrack?

Imagine you have a piggy bank, but it's SUPER SMART! 🐷✨

XpenseTrack is like a magical notebook that:
- Remembers every rupee you spend 💰
- Tells you if you're spending too much 🚨
- Helps you save money for things you want 🎁
- Gives you a pet dragon that grows when you're good with money! 🐉
- Lets you split bills with friends (like when you share pizza!) 🍕

It's made for hostel students who want to track their pocket money and learn to save!

---


## 🏗️ The Big Picture

Think of XpenseTrack like a restaurant:

### The Kitchen (Backend) 🍳
- This is where all the cooking happens
- Written in **Java** (a programming language)
- Uses **Spring Boot** (like a recipe book for building apps)
- Stores data in **MongoDB** (like a giant filing cabinet)
- Lives on the internet at: `https://xpensetrack-4fdf.onrender.com/`

### The Dining Room (Mobile App) 📱
- This is what you see and touch
- Written in **Kotlin** (another programming language for Android)
- Uses **Jetpack Compose** (like LEGO blocks for building screens)
- Pretty purple colors and fun animations!

### The Recipe Book (Database) 📖
- **MongoDB** remembers everything:
  - Who you are (your profile)
  - What you spent (your expenses)
  - Your pet dragon's mood
  - Your savings goals
  - Your friends

---


## 🧠 Backend - The Brain

The backend is like the brain of XpenseTrack. It thinks, calculates, and remembers everything!

### 🏢 Project Structure

```
backend/
├── src/main/java/com/xpensetrack/
│   ├── controller/     ← The Receptionists (handle requests)
│   ├── service/        ← The Workers (do the actual work)
│   ├── model/          ← The Forms (what data looks like)
│   ├── repository/     ← The File Clerks (save/find data)
│   ├── dto/            ← The Messengers (carry data between places)
│   └── config/         ← The Rules (security, settings)
└── resources/
    └── application.yml ← The Settings File
```

### 🎭 The Characters (Main Components)

#### 1. **Controllers** - The Receptionists 📞

Controllers are like receptionists at a hotel. When you ask for something, they listen and get the right person to help!

**AuthController** - The Security Guard 🛡️
```java
// When you want to create an account:
POST /api/auth/signup
// You give: name, email, password, phone, budget
// You get: a special key (JWT token) to enter the app
```

**ExpenseController** - The Accountant 💼
```java
// When you spend money:
POST /api/expenses
// You tell: how much, what for, which category
// It saves it and gives you 1 coin! 🪙
```

**DragonController** - The Pet Keeper 🐉
```java
// When you want to see your dragon:
GET /api/dragon
// You get: dragon's name, level, happiness, experience
```

**PiggyBankController** - The Savings Helper 🏦
```java
// When you create a savings goal:
POST /api/piggybank
// You say: what you're saving for, how much, by when
```

**FriendController** - The Social Manager 👥
```java
// When you want to add a friend:
POST /api/friends/request
// When you split a bill:
POST /api/friends/split
```


#### 2. **Services** - The Workers 👷

Services do the actual work! They're like the chefs in the kitchen.

**AuthService** - The Registration Office 📝

```java
public AuthResponse signup(SignupRequest req) {
    // Step 1: Check if passwords match
    if (!req.getPassword().equals(req.getConfirmPassword()))
        throw new IllegalArgumentException("Passwords do not match");
    
    // Step 2: Check if email already exists
    if (userRepo.existsByEmail(req.getEmail()))
        throw new IllegalArgumentException("Email already registered");
    
    // Step 3: Create a new user
    var user = new User();
    user.setFullName(req.getFullName());
    user.setEmail(req.getEmail());
    user.setPassword(passwordEncoder.encode(req.getPassword())); // Lock the password!
    user.setMonthlyBudget(req.getMonthlyBudget());
    user = userRepo.save(user);
    
    // Step 4: Give them a baby dragon! 🐣
    var dragon = new Dragon();
    dragon.setUserId(user.getId());
    dragonRepo.save(dragon);
    
    // Step 5: Give them a special key (JWT token)
    return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()));
}
```

**What's happening here?**
1. You fill a signup form
2. The service checks if everything is okay
3. It creates your account
4. It gives you a baby dragon as a welcome gift!
5. It gives you a special key (like a VIP pass) to use the app


**ExpenseService** - The Money Tracker 💰

```java
public ExpenseResponse addExpense(String userId, AddExpenseRequest req) {
    // Step 1: Create a new expense record
    var expense = new Expense();
    expense.setUserId(userId);
    expense.setAmount(req.getAmount());
    expense.setDescription(req.getDescription());
    expense.setCategory(req.getCategory()); // FOOD, TRAVEL, etc.
    expense.setDate(Instant.now()); // Right now!
    expense = expenseRepo.save(expense);
    
    // Step 2: Reward the user with 1 coin! 🪙
    var user = userRepo.findById(userId).orElseThrow();
    user.setCoins(user.getCoins() + 1);
    userRepo.save(user);
    
    // Step 3: Check if they spent too much today 🚨
    double dailyBudget = user.getMonthlyBudget() / 30; // Divide by days in month
    double todaySpent = calculateTodaySpending(userId);
    
    if (todaySpent > dailyBudget) {
        // Send a warning notification!
        sendNotification(userId, "You spent too much today! 😱");
    }
    
    // Step 4: Check if dragon is hungry 🐉
    var dragon = dragonRepo.findByUserId(userId).orElse(null);
    if (dragon != null && dragon.getHappiness() < 30) {
        sendNotification(userId, "Your dragon is hungry! Feed it! 🍖");
    }
    
    return toResponse(expense);
}
```

**What's happening here?**
1. You tell the app you spent ₹50 on food
2. It saves this information
3. It gives you 1 coin as a reward!
4. It checks: "Did you spend too much today?"
5. It checks: "Is your dragon sad and hungry?"
6. If yes to either, it sends you a reminder!


**DragonService** - The Pet Caretaker 🐲

```java
public DragonResponse feedDragon(String userId, int coins) {
    // Step 1: Check if user has enough coins
    var user = userRepo.findById(userId).orElseThrow();
    if (user.getCoins() < coins) {
        throw new IllegalArgumentException("Not enough coins! 😢");
    }
    
    // Step 2: Get the dragon
    var dragon = dragonRepo.findByUserId(userId).orElseThrow();
    
    // Step 3: Calculate rewards
    int expGain = coins * 10;           // Experience points
    int happinessGain = coins * 2;      // Happiness points
    
    // Step 4: Add experience and check for level up!
    int newExp = dragon.getExperience() + expGain;
    int newLevel = dragon.getLevel();
    
    // Level up formula: need (level × 3000) experience
    while (newExp >= newLevel * 3000) {
        newExp -= newLevel * 3000;
        newLevel++;  // LEVEL UP! 🎉
    }
    
    // Step 5: Update dragon
    dragon.setExperience(newExp);
    dragon.setLevel(newLevel);
    dragon.setHappiness(Math.min(dragon.getHappiness() + happinessGain, 100));
    dragonRepo.save(dragon);
    
    // Step 6: Take coins from user
    user.setCoins(user.getCoins() - coins);
    userRepo.save(user);
    
    return getDragon(userId);
}
```

**What's happening here?**
1. You decide to feed your dragon 100 coins
2. The app checks: "Do you have 100 coins?"
3. Your dragon gets:
   - 1000 experience points (100 × 10)
   - 200 happiness points (100 × 2, max 100)
4. If dragon has enough experience, it LEVELS UP! 🎊
5. Your coins go down by 100
6. Your dragon is now happier and stronger!

**Example:**
- Dragon is Level 3 with 8000 experience
- Needs 9000 experience to reach Level 4 (3 × 3000)
- You feed 100 coins → gains 1000 experience
- Now has 9000 experience → LEVEL UP to Level 4!
- Leftover experience: 0 (9000 - 9000)


**PiggyBankService** - The Savings Calculator 🏦

```java
public PiggyBankOverviewResponse getOverview(String userId) {
    var user = userRepo.findById(userId).orElseThrow();
    var goals = piggyBankRepo.findByUserId(userId);
    
    // Step 1: Calculate THIS month's savings
    var now = LocalDate.now();
    int daysInMonth = now.lengthOfMonth();        // 30 or 31 days
    int daysPassed = now.getDayOfMonth();         // Today is day 15
    
    double dailyBudget = user.getMonthlyBudget() / daysInMonth;
    // If budget is ₹3000/month and month has 30 days:
    // Daily budget = ₹100
    
    double budgetSoFar = dailyBudget * daysPassed;
    // If 15 days passed: ₹100 × 15 = ₹1500
    
    double actualSpent = calculateMonthSpending(userId);
    // Let's say you spent ₹1200
    
    double currentMonthSavings = budgetSoFar - actualSpent;
    // Savings = ₹1500 - ₹1200 = ₹300 saved! 🎉
    
    // Step 2: Calculate PREVIOUS months' savings
    double previousMonthsSavings = 0;
    // Loop through all completed months since account creation
    // Add up savings from each month
    
    // Step 3: Total savings
    double totalSavings = currentMonthSavings + previousMonthsSavings;
    
    // Step 4: Distribute savings to goals (earliest deadline first)
    for (var goal : goals) {
        if (!goal.isCompleted() && totalSavings > 0) {
            double toAdd = Math.min(totalSavings, goal.getTargetAmount() - goal.getSavedAmount());
            goal.setSavedAmount(goal.getSavedAmount() + toAdd);
            totalSavings -= toAdd;
            
            if (goal.getSavedAmount() >= goal.getTargetAmount()) {
                goal.setCompleted(true);
                user.setCoins(user.getCoins() + 50); // Bonus! 🎁
            }
        }
    }
    
    return buildOverview(currentMonthSavings, previousMonthsSavings, goals);
}
```

**What's happening here?**
1. Calculate how much you SHOULD have spent (daily budget × days passed)
2. Calculate how much you ACTUALLY spent
3. Difference = Your savings! 💰
4. Add savings from previous months
5. Automatically put savings into your goals (like "Buy a bike")
6. If a goal is complete, you get 50 bonus coins!

**Example:**
- Monthly budget: ₹3000
- Today is March 15 (15 days passed)
- Daily budget: ₹3000 ÷ 30 = ₹100
- Should have spent: ₹100 × 15 = ₹1500
- Actually spent: ₹1200
- **Savings: ₹300!** 🎊


**FriendService** - The Social Manager 👥

```java
public void splitExpense(String userId, String expenseId, List<String> friendIds) {
    // Step 1: Get the expense
    var expense = expenseRepo.findById(expenseId).orElseThrow();
    
    // Step 2: Calculate how much each person owes
    int totalPeople = friendIds.size() + 1; // Friends + You
    double amountPerPerson = expense.getAmount() / totalPeople;
    
    // Example: Pizza costs ₹600, split among 3 people
    // Each person owes: ₹600 ÷ 3 = ₹200
    
    // Step 3: Create split records
    for (String friendId : friendIds) {
        var split = new SplitExpense();
        split.setExpenseId(expenseId);
        split.setPaidBy(userId);           // You paid
        split.setOwedBy(friendId);         // Friend owes you
        split.setAmount(amountPerPerson);  // ₹200
        split.setSettled(false);           // Not paid yet
        splitExpenseRepo.save(split);
        
        // Send notification to friend
        sendNotification(friendId, "You owe ₹" + amountPerPerson + " for " + expense.getDescription());
    }
}

public void settlePayment(String userId, String friendId) {
    // Step 1: Find all unsettled splits between you and friend
    var splits = splitExpenseRepo.findUnsettledBetween(userId, friendId);
    
    // Step 2: Mark them all as settled
    for (var split : splits) {
        split.setSettled(true);
        split.setSettledAt(Instant.now());
        splitExpenseRepo.save(split);
    }
    
    // Step 3: Send confirmation
    sendNotification(friendId, "Payment received! Thanks! 💰");
}
```

**What's happening here?**
1. You buy pizza for ₹600
2. You split it with 2 friends
3. Each person owes: ₹600 ÷ 3 = ₹200
4. App creates records: "Friend A owes you ₹200", "Friend B owes you ₹200"
5. Friends get notifications
6. When friend pays, you mark it as "Settled"
7. Friend gets a "Thank you" notification!


#### 3. **Models** - The Forms 📋

Models are like forms that define what information looks like.

**User Model** - Your Profile 👤

```java
@Document(collection = "users")
public class User {
    @Id
    private String id;                    // Unique ID (like HL123456)
    private String fullName;              // "Rahul Kumar"
    private String email;                 // "rahul@example.com"
    private String password;              // Encrypted! 🔒
    private String phoneNumber;           // "9876543210"
    private int coins = 0;                // Coins you earned
    private double monthlyBudget = 5000;  // Your monthly budget
    private List<String> friendIds;       // List of friend IDs
    private Instant createdAt;            // When you joined
}
```

**Expense Model** - A Spending Record 💸

```java
@Document(collection = "expenses")
public class Expense {
    @Id
    private String id;
    private String userId;                // Who spent
    private double amount;                // How much (₹50)
    private String description;           // "Lunch at canteen"
    private ExpenseCategory category;     // FOOD, TRAVEL, RENT, etc.
    private String note;                  // Optional note
    private Instant date;                 // When you spent
    private List<String> splitWithFriendIds; // If split with friends
}
```

**Dragon Model** - Your Pet 🐉

```java
@Document(collection = "dragons")
public class Dragon {
    @Id
    private String id;
    private String userId;                // Whose dragon
    private String name = "Baby Dragon";  // Dragon's name
    private int level = 1;                // Current level
    private int happiness = 50;           // Happiness (0-100)
    private int experience = 0;           // Experience points
    private String activeSkinId;          // Current skin/costume
    private List<String> ownedItemIds;    // Items owned
    
    // Calculate coins needed for next level
    public int getCoinsToNextLevel() {
        return level * 3000;  // Level 1 needs 3000, Level 2 needs 6000, etc.
    }
}
```

**PiggyBank Model** - A Savings Goal 🎯

```java
@Document(collection = "piggy_banks")
public class PiggyBank {
    @Id
    private String id;
    private String userId;
    private String goalName;              // "Buy a bicycle"
    private double targetAmount;          // ₹5000
    private double savedAmount = 0.0;     // ₹1200 saved so far
    private LocalDate deadline;           // "2024-12-31"
    private String imageUrl;              // Picture of goal
    
    // Calculate daily saving needed
    public double getDailySavingNeeded() {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        return (targetAmount - savedAmount) / daysLeft;
        // If need ₹3800 more in 38 days = ₹100/day
    }
    
    // Calculate progress percentage
    public double getProgressPercent() {
        return (savedAmount / targetAmount) * 100;
        // ₹1200 / ₹5000 = 24%
    }
}
```


#### 4. **Security** - The Bodyguard 🛡️

**JWT (JSON Web Token)** - Your VIP Pass 🎫

```java
public class JwtUtil {
    private String secret = "super-secret-key-256-bits-long";
    
    // Create a token when you login
    public String generateToken(String userId, String email) {
        return Jwts.builder()
            .setSubject(userId)                    // Your ID
            .claim("email", email)                 // Your email
            .setIssuedAt(new Date())               // Created now
            .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
            // Expires in 7 days
            .signWith(SignatureAlgorithm.HS256, secret)  // Sign it!
            .compact();
    }
    
    // Check if token is valid
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;  // Valid! ✅
        } catch (Exception e) {
            return false; // Invalid! ❌
        }
    }
}
```

**What's happening here?**
1. When you login, you get a special token (like a VIP wristband)
2. This token has your ID and email encoded in it
3. It's valid for 7 days
4. Every time you make a request, you show this token
5. The app checks: "Is this token real and not expired?"
6. If yes, you can proceed! If no, you need to login again!

**Password Encryption** 🔐

```java
// When you signup:
String plainPassword = "myPassword123";
String encryptedPassword = passwordEncoder.encode(plainPassword);
// Becomes: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

// When you login:
boolean matches = passwordEncoder.matches("myPassword123", encryptedPassword);
// Returns: true ✅
```

**What's happening here?**
- Your password is never stored as plain text!
- It's scrambled using BCrypt (like a secret code)
- Even if someone steals the database, they can't read passwords!
- When you login, the app scrambles your input and compares


---

## 📱 Mobile App - The Face

The mobile app is what you see and touch! It's built with Kotlin and Jetpack Compose.

### 🎨 App Structure

```
mobile/
├── data/
│   ├── api/
│   │   ├── ApiClient.kt      ← Talks to backend
│   │   └── ApiService.kt     ← Defines all API calls
│   └── model/
│       └── Models.kt         ← Data structures
├── ui/
│   ├── screens/              ← All the screens you see
│   └── theme/                ← Colors and styles
└── navigation/
    └── NavGraph.kt           ← How screens connect
```

### 🌐 ApiClient - The Messenger 📬

```kotlin
object ApiClient {
    // Where the backend lives
    private const val BASE_URL = "https://xpensetrack-4fdf.onrender.com/"
    
    // Your VIP pass (JWT token)
    var token: String? = null
    
    // Add token to every request
    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
        token?.let { 
            req.addHeader("Authorization", "Bearer $it") 
        }
        chain.proceed(req.build())
    }
    
    // Build the messenger
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().addInterceptor(authInterceptor).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

**What's happening here?**
1. ApiClient knows where the backend lives (the URL)
2. It stores your JWT token
3. Every time you make a request, it automatically adds your token
4. It's like showing your VIP pass at every door!

**Example Request:**
```kotlin
// You want to add an expense
val api = ApiClient.create<ExpenseApi>()
val expense = api.addExpense(AddExpenseRequest(
    amount = 50.0,
    description = "Lunch",
    category = "FOOD"
))

// Behind the scenes:
// POST https://xpensetrack-4fdf.onrender.com/api/expenses
// Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
// Body: {"amount":50.0,"description":"Lunch","category":"FOOD"}
```


### 🎬 Navigation - The Map 🗺️

```kotlin
object Routes {
    const val SPLASH = "splash"           // First screen (logo)
    const val ONBOARDING = "onboarding"   // Welcome tour
    const val LOGIN = "login"             // Login screen
    const val SIGNUP = "signup"           // Create account
    const val MAIN = "main"               // Main dashboard
    const val ADD_EXPENSE = "add_expense" // Add expense form
    const val DRAGON = "dragon"           // Dragon screen
    const val PIGGY_BANK = "piggy_bank"   // Savings goals
    // ... and more!
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.MAIN) { MainScreen(navController) }
        // ... all screens
    }
}
```

**What's happening here?**
1. NavGraph is like a map of your app
2. Each screen has a name (route)
3. You can jump from one screen to another
4. Example: Splash → Onboarding → Login → Main → Dragon

**Navigation Example:**
```kotlin
// In LoginScreen, after successful login:
navController.navigate(Routes.MAIN) {
    popUpTo(Routes.SPLASH) { inclusive = true }
    // Clear all previous screens, can't go back to login
}

// In MainScreen, when you click "Add Expense":
navController.navigate(Routes.ADD_EXPENSE)
// Opens AddExpenseScreen

// When done adding expense:
navController.popBackStack()
// Go back to MainScreen
```


### 🖼️ Screens - What You See

#### DashboardTab - The Home Screen 🏠

```kotlin
@Composable
fun DashboardTab(navController: NavController) {
    var dashboard by remember { mutableStateOf<DashboardData?>(null) }
    
    // Load data when screen opens
    LaunchedEffect(Unit) {
        try {
            val api = ApiClient.create<ExpenseApi>()
            dashboard = api.getDashboard()
        } catch (e: Exception) {
            // Show error
        }
    }
    
    Column {
        // Header with greeting
        Box(background = Purple) {
            Text("Hello, ${dashboard?.userName}! 👋")
            Text("Current Balance: ₹${dashboard?.currentBalance}")
        }
        
        // Budget progress bar
        LinearProgressIndicator(
            progress = dashboard?.budgetUsedPercent ?: 0f,
            color = if (dashboard?.budgetUsedPercent > 0.8) Red else Green
        )
        
        // Quick actions
        Row {
            ActionCard("Add Expense") { navController.navigate(Routes.ADD_EXPENSE) }
            ActionCard("Friends") { navController.navigate(Routes.FRIENDS) }
        }
        
        // Spending overview (donut chart)
        DonutChart(dashboard?.categoryBreakdown)
        
        // Recent expenses list
        LazyColumn {
            items(dashboard?.recentExpenses) { expense ->
                ExpenseCard(expense)
            }
        }
    }
}
```

**What's happening here?**
1. When screen opens, it asks backend for dashboard data
2. Shows your name and balance at top
3. Shows a progress bar (how much budget used)
4. Shows quick action buttons
5. Shows a colorful donut chart of spending by category
6. Shows list of recent expenses

**Data Flow:**
```
User opens app
    ↓
DashboardTab loads
    ↓
Calls: GET /api/expenses/dashboard
    ↓
Backend calculates everything
    ↓
Returns: { userName, balance, expenses, categories, ... }
    ↓
Screen displays data
```


#### DragonScreen - Your Pet 🐉

```kotlin
@Composable
fun DragonScreen(navController: NavController) {
    var dragon by remember { mutableStateOf<DragonData?>(null) }
    var feedAmount by remember { mutableStateOf(100) }
    
    // Load dragon data
    LaunchedEffect(Unit) {
        val api = ApiClient.create<DragonApi>()
        dragon = api.getDragon()
    }
    
    Column {
        // Dragon emoji (gets bigger with level!)
        Text(
            text = "🐉",
            fontSize = (50 + dragon?.level * 5).sp
        )
        
        // Dragon info
        Text("${dragon?.name} - Level ${dragon?.level}")
        
        // Happiness bar
        LinearProgressIndicator(
            progress = (dragon?.happiness ?: 0) / 100f,
            color = when {
                dragon?.happiness > 70 -> Green
                dragon?.happiness > 30 -> Yellow
                else -> Red
            }
        )
        Text("Happiness: ${dragon?.happiness}/100")
        
        // Experience bar
        LinearProgressIndicator(
            progress = (dragon?.experience ?: 0) / (dragon?.coinsToNextLevel ?: 1).toFloat()
        )
        Text("${dragon?.experience}/${dragon?.coinsToNextLevel} XP to Level ${dragon?.level + 1}")
        
        // Feed controls
        Slider(
            value = feedAmount.toFloat(),
            onValueChange = { feedAmount = it.toInt() },
            valueRange = 10f..500f
        )
        Text("Feed ${feedAmount} coins")
        
        Button(onClick = {
            // Feed the dragon!
            val api = ApiClient.create<DragonApi>()
            dragon = api.feed(mapOf("coins" to feedAmount))
        }) {
            Text("Feed Dragon 🍖")
        }
        
        // Shop button
        Button(onClick = { navController.navigate(Routes.SHOP) }) {
            Text("Visit Shop 🛒")
        }
    }
}
```

**What's happening here?**
1. Shows your dragon with emoji (bigger = higher level!)
2. Shows happiness bar (green = happy, red = sad)
3. Shows experience bar (how close to next level)
4. Slider to choose how many coins to feed
5. Feed button sends coins to dragon
6. Dragon gets happier and gains experience!

**Feeding Flow:**
```
User moves slider to 100 coins
    ↓
Clicks "Feed Dragon"
    ↓
Calls: POST /api/dragon/feed { coins: 100 }
    ↓
Backend:
  - Checks if user has 100 coins
  - Adds 1000 XP to dragon
  - Adds 200 happiness
  - Checks for level up
  - Deducts 100 coins from user
    ↓
Returns updated dragon data
    ↓
Screen refreshes with new stats
    ↓
Dragon is happier! 😊
```


#### PiggyBankScreen - Savings Goals 🏦

```kotlin
@Composable
fun PiggyBankScreen(navController: NavController) {
    var overview by remember { mutableStateOf<PiggyBankOverview?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val api = ApiClient.create<PiggyBankApi>()
        overview = api.getOverview()
    }
    
    Column {
        // Savings summary
        Card {
            Text("💰 Monthly Savings")
            Text("₹${overview?.currentMonthSavings}", fontSize = 32.sp)
            LinearProgressIndicator(
                progress = (overview?.savingsProgress ?: 0f) / 100f
            )
        }
        
        // Create goal button
        Button(onClick = { showCreateDialog = true }) {
            Text("+ Create New Goal")
        }
        
        // Goals grid (2 columns)
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(overview?.goals ?: emptyList()) { goal ->
                GoalCard(goal) {
                    // Mark as complete
                    val api = ApiClient.create<PiggyBankApi>()
                    api.markComplete(goal.id)
                    // Refresh
                    overview = api.getOverview()
                }
            }
        }
    }
    
    // Create goal dialog
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            var goalName by remember { mutableStateOf("") }
            var targetAmount by remember { mutableStateOf("") }
            var deadline by remember { mutableStateOf(LocalDate.now()) }
            
            Column {
                TextField(value = goalName, onValueChange = { goalName = it },
                    label = { Text("Goal Name") })
                TextField(value = targetAmount, onValueChange = { targetAmount = it },
                    label = { Text("Target Amount") })
                DatePicker(date = deadline, onDateChange = { deadline = it })
                
                Button(onClick = {
                    // Create goal
                    val api = ApiClient.create<PiggyBankApi>()
                    api.create(CreatePiggyBankRequest(
                        goalName = goalName,
                        targetAmount = targetAmount.toDouble(),
                        deadline = deadline
                    ))
                    showCreateDialog = false
                    // Refresh
                    overview = api.getOverview()
                }) {
                    Text("Create Goal")
                }
            }
        }
    }
}

@Composable
fun GoalCard(goal: PiggyBankGoal, onComplete: () -> Unit) {
    Card {
        Image(url = goal.imageUrl)
        Text(goal.goalName)
        Text("₹${goal.savedAmount} / ₹${goal.targetAmount}")
        LinearProgressIndicator(progress = goal.progressPercent / 100f)
        Text("${goal.daysLeft} days left")
        Text("Save ₹${goal.dailySavingNeeded}/day")
        
        if (goal.savedAmount >= goal.targetAmount) {
            Button(onClick = onComplete) {
                Text("Mark Complete ✅")
            }
        }
    }
}
```

**What's happening here?**
1. Shows your total monthly savings at top
2. Shows all your savings goals in a grid
3. Each goal shows:
   - Picture of what you're saving for
   - Progress bar
   - How much saved / target
   - Days left
   - Daily saving needed
4. Click "+" to create new goal
5. When goal is reached, click "Mark Complete" to get 50 bonus coins!


#### AddExpenseScreen - Quick Entry 💸

```kotlin
@Composable
fun AddExpenseScreen(navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FOOD") }
    var note by remember { mutableStateOf("") }
    var selectedFriends by remember { mutableStateOf<List<String>>(emptyList()) }
    
    Column {
        // Amount input
        TextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (₹)") },
            keyboardType = KeyboardType.Number
        )
        
        // Description
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("What did you buy?") }
        )
        
        // Category selector
        Row {
            CategoryChip("🍔 Food", category == "FOOD") { category = "FOOD" }
            CategoryChip("🚗 Travel", category == "TRAVEL") { category = "TRAVEL" }
            CategoryChip("🏠 Rent", category == "RENT") { category = "RENT" }
            CategoryChip("💡 Utilities", category == "UTILITIES") { category = "UTILITIES" }
            CategoryChip("📦 Misc", category == "MISC") { category = "MISC" }
        }
        
        // Optional note
        TextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") }
        )
        
        // Split with friends
        Text("Split with friends?")
        FriendSelector(onFriendsSelected = { selectedFriends = it })
        
        // Add button
        Button(onClick = {
            val api = ApiClient.create<ExpenseApi>()
            api.addExpense(AddExpenseRequest(
                amount = amount.toDouble(),
                description = description,
                category = category,
                note = note,
                splitWithFriendIds = selectedFriends
            ))
            navController.popBackStack() // Go back
        }) {
            Text("Add Expense ✅")
        }
    }
}
```

**What's happening here?**
1. Enter amount (₹50)
2. Enter description ("Lunch at canteen")
3. Select category (Food, Travel, etc.)
4. Optionally add a note
5. Optionally split with friends
6. Click "Add Expense"
7. Backend saves it, gives you 1 coin, checks budget
8. Go back to dashboard


---

## 💾 Database - The Memory

MongoDB is like a giant filing cabinet that remembers everything!

### 📁 Collections (Folders)

#### Users Collection 👥
```json
{
  "_id": "507f1f77bcf86cd799439011",
  "fullName": "Rahul Kumar",
  "email": "rahul@example.com",
  "password": "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
  "phoneNumber": "9876543210",
  "coins": 150,
  "monthlyBudget": 5000,
  "currentBalance": 2500,
  "friendIds": ["507f1f77bcf86cd799439012", "507f1f77bcf86cd799439013"],
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### Expenses Collection 💸
```json
{
  "_id": "607f1f77bcf86cd799439021",
  "userId": "507f1f77bcf86cd799439011",
  "amount": 250,
  "description": "Lunch with friends",
  "category": "FOOD",
  "note": "Pizza at Dominos",
  "date": "2024-03-15T13:30:00Z",
  "splitWithFriendIds": ["507f1f77bcf86cd799439012"],
  "createdAt": "2024-03-15T13:30:00Z"
}
```

#### Dragons Collection 🐉
```json
{
  "_id": "707f1f77bcf86cd799439031",
  "userId": "507f1f77bcf86cd799439011",
  "name": "Sparky",
  "level": 5,
  "happiness": 85,
  "experience": 12000,
  "activeSkinId": "skin_fire",
  "ownedItemIds": ["item_food_1", "item_accessory_hat"],
  "ownedSkinIds": ["skin_fire", "skin_ice"]
}
```

#### PiggyBanks Collection 🏦
```json
{
  "_id": "807f1f77bcf86cd799439041",
  "userId": "507f1f77bcf86cd799439011",
  "goalName": "Buy a Bicycle",
  "targetAmount": 8000,
  "savedAmount": 3200,
  "deadline": "2024-12-31",
  "imageUrl": "https://example.com/bicycle.jpg",
  "createdAt": "2024-01-01"
}
```

### 🔗 Relationships

```
User (Rahul)
  ├── Has many Expenses
  │   ├── Expense 1: ₹50 on Food
  │   ├── Expense 2: ₹100 on Travel
  │   └── Expense 3: ₹250 on Food (split with friend)
  │
  ├── Has one Dragon
  │   └── Dragon: Level 5, Happiness 85
  │
  ├── Has many PiggyBanks
  │   ├── Goal 1: Buy Bicycle (₹3200/₹8000)
  │   └── Goal 2: New Phone (₹5000/₹15000)
  │
  └── Has many Friends
      ├── Friend 1: Amit (owes ₹125)
      └── Friend 2: Priya (you owe ₹50)
```


---

## 🔄 How Everything Works Together

Let's follow a complete journey through the app!

### 📖 Story: Rahul's Day with XpenseTrack

#### 1️⃣ Morning: Creating an Account

```
Rahul opens app
    ↓
Sees splash screen (logo)
    ↓
Sees onboarding (welcome tour)
    ↓
Clicks "Sign Up"
    ↓
Fills form:
  - Name: Rahul Kumar
  - Email: rahul@example.com
  - Password: ********
  - Phone: 9876543210
  - Monthly Budget: ₹5000
    ↓
Clicks "Create Account"
    ↓
Mobile App → POST /api/auth/signup
    ↓
Backend:
  1. Checks if email exists (no)
  2. Encrypts password
  3. Creates User in database
  4. Creates Baby Dragon for user
  5. Generates JWT token
    ↓
Returns: { token, userId, name, email }
    ↓
Mobile App:
  1. Saves token
  2. Navigates to Main Screen
    ↓
Rahul sees dashboard! 🎉
```

#### 2️⃣ Afternoon: Adding an Expense

```
Rahul buys lunch for ₹80
    ↓
Opens app → Dashboard
    ↓
Clicks "Add Expense"
    ↓
Fills form:
  - Amount: 80
  - Description: Lunch at canteen
  - Category: FOOD
    ↓
Clicks "Add Expense"
    ↓
Mobile App → POST /api/expenses
    ↓
Backend:
  1. Creates Expense record
  2. Adds 1 coin to Rahul's account (now has 1 coin!)
  3. Calculates today's spending:
     - Daily budget: ₹5000 ÷ 30 = ₹166
     - Today spent: ₹80
     - Still under budget ✅
  4. Checks dragon happiness: 50 (okay)
    ↓
Returns: Expense details
    ↓
Mobile App:
  - Shows success message
  - Goes back to dashboard
  - Dashboard refreshes
    ↓
Rahul sees:
  - Balance: ₹4920 (₹5000 - ₹80)
  - Coins: 1 🪙
  - Recent expense: "Lunch at canteen - ₹80"
```


#### 3️⃣ Evening: Splitting a Bill with Friends

```
Rahul and 2 friends order pizza for ₹600
Rahul pays the full amount
    ↓
Opens app → Add Expense
    ↓
Fills form:
  - Amount: 600
  - Description: Pizza with friends
  - Category: FOOD
  - Split with: Amit, Priya
    ↓
Clicks "Add Expense"
    ↓
Mobile App → POST /api/expenses
    ↓
Backend:
  1. Creates Expense (₹600)
  2. Adds 1 coin to Rahul
  3. Calculates split:
     - Total people: 3 (Rahul + 2 friends)
     - Per person: ₹600 ÷ 3 = ₹200
  4. Creates SplitExpense records:
     - Amit owes Rahul ₹200
     - Priya owes Rahul ₹200
  5. Sends notifications to Amit and Priya
    ↓
Returns: Expense details
    ↓
Amit and Priya get notification:
  "You owe ₹200 for Pizza with friends"
    ↓
Rahul opens Friends screen:
  - Amit owes: ₹200
  - Priya owes: ₹200
  - Total to receive: ₹400
```

#### 4️⃣ Next Day: Friend Pays Back

```
Amit pays Rahul ₹200 in cash
    ↓
Rahul opens Friends screen
    ↓
Clicks on Amit's card
    ↓
Clicks "Settle Up"
    ↓
Mobile App → POST /api/friends/settle
    ↓
Backend:
  1. Finds all unsettled splits between Rahul and Amit
  2. Marks them as settled
  3. Sends notification to Amit: "Payment received! Thanks!"
    ↓
Returns: Success
    ↓
Rahul's Friends screen updates:
  - Amit owes: ₹0 ✅
  - Priya owes: ₹200
  - Total to receive: ₹200
```


#### 5️⃣ Weekend: Feeding the Dragon

```
Rahul has earned 10 coins from 10 expenses
    ↓
Opens Dragon screen
    ↓
Sees:
  - Dragon: Baby Dragon
  - Level: 1
  - Happiness: 45 (getting sad!)
  - Experience: 0/3000
  - Coins available: 10
    ↓
Moves slider to 10 coins
    ↓
Clicks "Feed Dragon"
    ↓
Mobile App → POST /api/dragon/feed { coins: 10 }
    ↓
Backend:
  1. Checks: Does Rahul have 10 coins? Yes ✅
  2. Calculates rewards:
     - Experience gain: 10 × 10 = 100 XP
     - Happiness gain: 10 × 2 = 20 points
  3. Updates dragon:
     - Experience: 0 + 100 = 100 XP
     - Happiness: 45 + 20 = 65 (happy now!)
     - Level: Still 1 (needs 3000 XP)
  4. Deducts coins: 10 - 10 = 0 coins
    ↓
Returns: Updated dragon data
    ↓
Rahul sees:
  - Dragon: Baby Dragon 😊
  - Level: 1
  - Happiness: 65 (green bar!)
  - Experience: 100/3000
  - Coins: 0
```

#### 6️⃣ Month End: Creating a Savings Goal

```
It's March 31st
Rahul wants to save for a bicycle
    ↓
Opens Piggy Bank screen
    ↓
Sees:
  - Monthly Savings: ₹800
  (Budget was ₹5000, spent ₹4200, saved ₹800!)
    ↓
Clicks "+ Create New Goal"
    ↓
Fills form:
  - Goal Name: Buy a Bicycle
  - Target Amount: ₹8000
  - Deadline: December 31, 2024
  - Image: (uploads bicycle picture)
    ↓
Clicks "Create Goal"
    ↓
Mobile App → POST /api/piggybank
    ↓
Backend:
  1. Creates PiggyBank record
  2. Calculates:
     - Days until deadline: 275 days
     - Daily saving needed: ₹8000 ÷ 275 = ₹29/day
  3. Auto-distributes March savings:
     - Takes ₹800 from savings
     - Adds to goal: ₹800/₹8000
    ↓
Returns: Goal details
    ↓
Rahul sees new goal card:
  - 🚲 Buy a Bicycle
  - ₹800 / ₹8000 (10% complete)
  - 275 days left
  - Save ₹29/day
```


#### 7️⃣ Next Month: Auto-Savings Distribution

```
It's April 30th
Rahul spent ₹4500 in April
    ↓
Opens Piggy Bank screen
    ↓
Mobile App → GET /api/piggybank
    ↓
Backend calculates:
  1. April budget: ₹5000
  2. April spent: ₹4500
  3. April savings: ₹500
  4. Previous savings: ₹800 (from March)
  5. Total savings: ₹1300
  6. Auto-distribute to goals:
     - Bicycle goal: ₹800 + ₹500 = ₹1300
    ↓
Returns: Updated overview
    ↓
Rahul sees:
  - Monthly Savings: ₹500
  - Total Savings: ₹1300
  - Bicycle goal: ₹1300/₹8000 (16% complete!)
```

#### 8️⃣ Overspending Alert

```
It's May 15th
Rahul's daily budget: ₹166
Today he spent: ₹250 (₹80 lunch + ₹170 shopping)
    ↓
When he adds the ₹170 expense:
    ↓
Backend calculates:
  - Daily budget: ₹166
  - Today's total: ₹250
  - Overspent by: ₹84 🚨
    ↓
Backend sends notification:
  "Overspending Alert! You've spent ₹250 today, exceeding your daily budget of ₹166."
    ↓
Rahul gets notification
    ↓
Opens Notifications screen
    ↓
Sees alert with red icon
    ↓
Thinks: "I should be more careful tomorrow!"
```


---

## 💻 Code Explanation - Deep Dive

### 🔐 How Authentication Works (Step by Step)

#### Signup Process

```java
// 1. User fills signup form in mobile app
SignupRequest request = new SignupRequest(
    fullName: "Rahul Kumar",
    email: "rahul@example.com",
    password: "myPassword123",
    confirmPassword: "myPassword123",
    phoneNumber: "9876543210",
    monthlyBudget: 5000,
    termsAccepted: true
);

// 2. Mobile app sends to backend
POST /api/auth/signup
Body: { fullName, email, password, ... }

// 3. Backend receives in AuthController
@PostMapping("/signup")
public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest req) {
    return ResponseEntity.ok(authService.signup(req));
}

// 4. AuthService processes
public AuthResponse signup(SignupRequest req) {
    // Validation 1: Passwords match?
    if (!req.getPassword().equals(req.getConfirmPassword())) {
        throw new IllegalArgumentException("Passwords do not match");
    }
    
    // Validation 2: Email already exists?
    if (userRepo.existsByEmail(req.getEmail())) {
        throw new IllegalArgumentException("Email already registered");
    }
    
    // Validation 3: Terms accepted?
    if (!req.isTermsAccepted()) {
        throw new IllegalArgumentException("Must accept terms");
    }
    
    // Create user
    User user = new User();
    user.setFullName(req.getFullName());
    user.setEmail(req.getEmail());
    
    // Encrypt password (BCrypt)
    // "myPassword123" → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    
    user.setPhoneNumber(req.getPhoneNumber());
    user.setMonthlyBudget(req.getMonthlyBudget());
    user.setCoins(0);
    user.setCreatedAt(Instant.now());
    
    // Save to database
    user = userRepo.save(user);
    
    // Create baby dragon
    Dragon dragon = new Dragon();
    dragon.setUserId(user.getId());
    dragon.setName("Baby Dragon");
    dragon.setLevel(1);
    dragon.setHappiness(50);
    dragonRepo.save(dragon);
    
    // Generate JWT token
    String token = jwtUtil.generateToken(user.getId(), user.getEmail());
    
    // Return response
    return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail());
}

// 5. Mobile app receives response
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "507f1f77bcf86cd799439011",
    "fullName": "Rahul Kumar",
    "email": "rahul@example.com"
}

// 6. Mobile app saves token
ApiClient.token = response.token

// 7. Navigate to main screen
navController.navigate(Routes.MAIN)
```


#### JWT Token Explained

```java
// JWT has 3 parts: Header.Payload.Signature

// 1. HEADER (algorithm and type)
{
  "alg": "HS256",
  "typ": "JWT"
}

// 2. PAYLOAD (data)
{
  "sub": "507f1f77bcf86cd799439011",  // User ID
  "email": "rahul@example.com",
  "iat": 1710000000,                   // Issued at (timestamp)
  "exp": 1710604800                    // Expires at (7 days later)
}

// 3. SIGNATURE (security)
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)

// Final token:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDdmMWY3N2JjZjg2Y2Q3OTk0MzkwMTEiLCJlbWFpbCI6InJhaHVsQGV4YW1wbGUuY29tIiwiaWF0IjoxNzEwMDAwMDAwLCJleHAiOjE3MTA2MDQ4MDB9.signature_here

// How it's used:
// Every request includes: Authorization: Bearer <token>
// Backend validates:
//   1. Is signature valid? (not tampered)
//   2. Is token expired? (check exp)
//   3. Extract userId from payload
//   4. Allow request ✅
```

#### Login Process

```java
// 1. User enters email and password
LoginRequest request = new LoginRequest(
    email: "rahul@example.com",
    password: "myPassword123"
);

// 2. Backend receives
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
    return ResponseEntity.ok(authService.login(req));
}

// 3. AuthService validates
public AuthResponse login(LoginRequest req) {
    // Find user by email
    User user = userRepo.findByEmail(req.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    
    // Check password
    // Compares "myPassword123" with stored encrypted password
    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
        throw new IllegalArgumentException("Invalid credentials");
    }
    
    // Generate new token
    String token = jwtUtil.generateToken(user.getId(), user.getEmail());
    
    return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail());
}
```


### 💰 How Budget Calculation Works

```java
// Scenario: User has ₹5000 monthly budget
// Today is March 15th (15 days into the month)
// March has 30 days

public DashboardResponse getDashboard(String userId) {
    User user = userRepo.findById(userId).orElseThrow();
    
    // 1. Calculate daily budget
    LocalDate today = LocalDate.now();
    int daysInMonth = today.lengthOfMonth();  // 30 days
    double dailyBudget = user.getMonthlyBudget() / daysInMonth;
    // ₹5000 ÷ 30 = ₹166.67 per day
    
    // 2. Calculate how much SHOULD have been spent by today
    int daysPassed = today.getDayOfMonth();  // 15 days
    double budgetSoFar = dailyBudget * daysPassed;
    // ₹166.67 × 15 = ₹2500
    
    // 3. Calculate how much ACTUALLY spent this month
    Instant monthStart = today.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant now = Instant.now();
    List<Expense> monthExpenses = expenseRepo.findByUserIdAndDateRange(userId, monthStart, now);
    double actualSpent = monthExpenses.stream()
        .mapToDouble(Expense::getAmount)
        .sum();
    // Let's say: ₹2200
    
    // 4. Calculate savings
    double currentMonthSavings = budgetSoFar - actualSpent;
    // ₹2500 - ₹2200 = ₹300 saved!
    
    // 5. Calculate budget used percentage
    double budgetUsedPercent = (actualSpent / budgetSoFar) * 100;
    // (₹2200 / ₹2500) × 100 = 88%
    
    // 6. Calculate remaining budget for the month
    double remainingBudget = user.getMonthlyBudget() - actualSpent;
    // ₹5000 - ₹2200 = ₹2800 left
    
    // 7. Calculate days left in month
    int daysLeft = daysInMonth - daysPassed;
    // 30 - 15 = 15 days left
    
    // 8. Calculate daily budget for remaining days
    double dailyBudgetRemaining = remainingBudget / daysLeft;
    // ₹2800 ÷ 15 = ₹186.67 per day
    
    return DashboardResponse.builder()
        .monthlyBudget(5000)
        .actualSpent(2200)
        .budgetUsedPercent(88)
        .currentMonthSavings(300)
        .remainingBudget(2800)
        .dailyBudgetRemaining(186.67)
        .daysLeft(15)
        .build();
}
```

**Visual Representation:**

```
Month: March (30 days)
Budget: ₹5000
Today: Day 15

Timeline:
Day 1 ────────────── Day 15 ────────────── Day 30
      (15 days passed)    (15 days left)

Budget allocation:
Should spend by Day 15: ₹2500 (₹166.67/day × 15)
Actually spent: ₹2200
Savings: ₹300 ✅

Remaining:
Budget left: ₹2800
Days left: 15
New daily budget: ₹186.67/day
```


### 🐉 How Dragon Leveling Works

```java
// Dragon starts at Level 1 with 0 experience
// Each level requires: level × 3000 experience

public DragonResponse feedDragon(String userId, int coins) {
    User user = userRepo.findById(userId).orElseThrow();
    Dragon dragon = dragonRepo.findByUserId(userId).orElseThrow();
    
    // Check if user has enough coins
    if (user.getCoins() < coins) {
        throw new IllegalArgumentException("Not enough coins");
    }
    
    // Calculate gains
    int expGain = coins * 10;        // 1 coin = 10 XP
    int happinessGain = coins * 2;   // 1 coin = 2 happiness
    
    // Add experience
    int currentExp = dragon.getExperience();
    int newExp = currentExp + expGain;
    int currentLevel = dragon.getLevel();
    
    // Check for level ups
    while (newExp >= currentLevel * 3000) {
        // Level up!
        newExp -= currentLevel * 3000;  // Subtract XP needed
        currentLevel++;                  // Increase level
    }
    
    // Update dragon
    dragon.setExperience(newExp);
    dragon.setLevel(currentLevel);
    dragon.setHappiness(Math.min(dragon.getHappiness() + happinessGain, 100));
    dragonRepo.save(dragon);
    
    // Deduct coins
    user.setCoins(user.getCoins() - coins);
    userRepo.save(user);
    
    return getDragon(userId);
}
```

**Example Scenarios:**

**Scenario 1: Simple Feed**
```
Dragon: Level 3, 2000 XP
Feed: 100 coins

Calculation:
- XP gain: 100 × 10 = 1000 XP
- New XP: 2000 + 1000 = 3000 XP
- Level 3 needs: 3 × 3000 = 9000 XP to reach Level 4
- 3000 < 9000, so no level up
- Result: Level 3, 3000 XP
```

**Scenario 2: Level Up**
```
Dragon: Level 3, 8500 XP
Feed: 100 coins

Calculation:
- XP gain: 100 × 10 = 1000 XP
- New XP: 8500 + 1000 = 9500 XP
- Level 3 needs: 9000 XP to reach Level 4
- 9500 >= 9000, LEVEL UP! 🎉
- Leftover XP: 9500 - 9000 = 500 XP
- Result: Level 4, 500 XP
```

**Scenario 3: Multiple Level Ups**
```
Dragon: Level 1, 2500 XP
Feed: 1000 coins (rich user!)

Calculation:
- XP gain: 1000 × 10 = 10,000 XP
- New XP: 2500 + 10,000 = 12,500 XP

Level 1 → 2:
- Needs: 1 × 3000 = 3000 XP
- 12,500 >= 3000, LEVEL UP!
- Leftover: 12,500 - 3000 = 9500 XP

Level 2 → 3:
- Needs: 2 × 3000 = 6000 XP
- 9500 >= 6000, LEVEL UP!
- Leftover: 9500 - 6000 = 3500 XP

Level 3 → 4:
- Needs: 3 × 3000 = 9000 XP
- 3500 < 9000, stop
- Result: Level 3, 3500 XP
```

**Level Requirements Table:**
```
Level 1 → 2: 3,000 XP (300 coins)
Level 2 → 3: 6,000 XP (600 coins)
Level 3 → 4: 9,000 XP (900 coins)
Level 4 → 5: 12,000 XP (1,200 coins)
Level 5 → 6: 15,000 XP (1,500 coins)
...
Level 10 → 11: 30,000 XP (3,000 coins)
```


### 🏦 How Piggy Bank Auto-Distribution Works

```java
public PiggyBankOverviewResponse getOverview(String userId) {
    User user = userRepo.findById(userId).orElseThrow();
    List<PiggyBank> goals = piggyBankRepo.findByUserId(userId);
    
    // 1. Calculate current month savings
    LocalDate today = LocalDate.now();
    int daysInMonth = today.lengthOfMonth();
    int daysPassed = today.getDayOfMonth();
    
    double dailyBudget = user.getMonthlyBudget() / daysInMonth;
    double budgetSoFar = dailyBudget * daysPassed;
    
    Instant monthStart = today.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant now = Instant.now();
    double monthSpent = expenseRepo.findByUserIdAndDateRange(userId, monthStart, now)
        .stream().mapToDouble(Expense::getAmount).sum();
    
    double currentMonthSavings = Math.max(budgetSoFar - monthSpent, 0);
    
    // 2. Calculate previous months' savings
    double previousMonthsSavings = 0;
    LocalDate accountCreated = user.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
    LocalDate firstMonth = accountCreated.withDayOfMonth(1);
    LocalDate thisMonthStart = today.withDayOfMonth(1);
    
    // Loop through each completed month
    LocalDate month = firstMonth;
    while (month.isBefore(thisMonthStart)) {
        Instant mStart = month.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant mEnd = month.plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        
        double monthBudget = user.getMonthlyBudget();
        double monthActualSpent = expenseRepo.findByUserIdAndDateRange(userId, mStart, mEnd)
            .stream().mapToDouble(Expense::getAmount).sum();
        
        double monthSavings = Math.max(monthBudget - monthActualSpent, 0);
        previousMonthsSavings += monthSavings;
        
        month = month.plusMonths(1);
    }
    
    // 3. Total available savings
    double totalSavings = currentMonthSavings + previousMonthsSavings;
    
    // 4. Sort goals by deadline (earliest first)
    goals.sort(Comparator.comparing(PiggyBank::getDeadline));
    
    // 5. Distribute savings to goals
    for (PiggyBank goal : goals) {
        if (goal.getSavedAmount() >= goal.getTargetAmount()) {
            continue; // Already complete
        }
        
        if (totalSavings <= 0) {
            break; // No more savings to distribute
        }
        
        // How much does this goal need?
        double needed = goal.getTargetAmount() - goal.getSavedAmount();
        
        // How much can we give?
        double toAdd = Math.min(totalSavings, needed);
        
        // Add to goal
        goal.setSavedAmount(goal.getSavedAmount() + toAdd);
        totalSavings -= toAdd;
        
        // Check if goal is now complete
        if (goal.getSavedAmount() >= goal.getTargetAmount()) {
            // Bonus coins!
            user.setCoins(user.getCoins() + 50);
        }
        
        piggyBankRepo.save(goal);
    }
    
    userRepo.save(user);
    
    return buildOverview(currentMonthSavings, previousMonthsSavings, goals);
}
```

**Example:**

```
User: Rahul
Monthly Budget: ₹5000
Account created: January 1, 2024
Today: March 15, 2024

Goals:
1. Bicycle - Target: ₹8000, Deadline: Dec 31, 2024
2. Phone - Target: ₹15000, Deadline: Jun 30, 2025

Savings History:
- January: Spent ₹4500, Saved ₹500
- February: Spent ₹4800, Saved ₹200
- March (so far): Should spend ₹2500, Spent ₹2200, Saved ₹300

Total Savings: ₹500 + ₹200 + ₹300 = ₹1000

Distribution:
1. Bicycle (earliest deadline):
   - Needs: ₹8000
   - Has: ₹0
   - Give: ₹1000 (all available)
   - New: ₹1000/₹8000

2. Phone:
   - Needs: ₹15000
   - Has: ₹0
   - Give: ₹0 (no savings left)
   - New: ₹0/₹15000

Result:
- Bicycle: ₹1000/₹8000 (12.5%)
- Phone: ₹0/₹15000 (0%)
```


### 👥 How Friend Splitting Works

```java
// Scenario: Pizza costs ₹600, split among 3 people (you + 2 friends)

public void splitExpense(String userId, String expenseId, List<String> friendIds) {
    Expense expense = expenseRepo.findById(expenseId).orElseThrow();
    
    // Calculate per-person amount
    int totalPeople = friendIds.size() + 1;  // Friends + You
    double amountPerPerson = expense.getAmount() / totalPeople;
    // ₹600 ÷ 3 = ₹200 per person
    
    // Create split records for each friend
    for (String friendId : friendIds) {
        SplitExpense split = new SplitExpense();
        split.setExpenseId(expenseId);
        split.setPaidBy(userId);              // You paid
        split.setOwedBy(friendId);            // Friend owes you
        split.setAmount(amountPerPerson);     // ₹200
        split.setSettled(false);              // Not paid yet
        split.setCreatedAt(Instant.now());
        splitExpenseRepo.save(split);
        
        // Send notification
        User friend = userRepo.findById(friendId).orElseThrow();
        Notification notif = new Notification();
        notif.setUserId(friendId);
        notif.setType(NotificationType.SPLIT_EXPENSE);
        notif.setTitle("New Split Expense");
        notif.setMessage("You owe ₹" + amountPerPerson + " for " + expense.getDescription());
        notificationRepo.save(notif);
    }
}

// Calculate balances between friends
public FriendsOverviewResponse getOverview(String userId) {
    // Find all splits where you paid (friends owe you)
    List<SplitExpense> youPaid = splitExpenseRepo.findByPaidByAndSettled(userId, false);
    double totalToReceive = youPaid.stream()
        .mapToDouble(SplitExpense::getAmount)
        .sum();
    
    // Find all splits where friend paid (you owe them)
    List<SplitExpense> friendsPaid = splitExpenseRepo.findByOwedByAndSettled(userId, false);
    double totalYouOwe = friendsPaid.stream()
        .mapToDouble(SplitExpense::getAmount)
        .sum();
    
    // Calculate per-friend balances
    Map<String, Double> balances = new HashMap<>();
    
    for (SplitExpense split : youPaid) {
        String friendId = split.getOwedBy();
        balances.put(friendId, balances.getOrDefault(friendId, 0.0) + split.getAmount());
    }
    
    for (SplitExpense split : friendsPaid) {
        String friendId = split.getPaidBy();
        balances.put(friendId, balances.getOrDefault(friendId, 0.0) - split.getAmount());
    }
    
    return FriendsOverviewResponse.builder()
        .totalToReceive(totalToReceive)
        .totalYouOwe(totalYouOwe)
        .friendBalances(balances)
        .build();
}

// Settle payment
public void settlePayment(String userId, String friendId) {
    // Find all unsettled splits between you and friend
    List<SplitExpense> splits = splitExpenseRepo.findUnsettledBetween(userId, friendId);
    
    // Mark all as settled
    for (SplitExpense split : splits) {
        split.setSettled(true);
        split.setSettledAt(Instant.now());
        splitExpenseRepo.save(split);
    }
    
    // Send confirmation notification
    Notification notif = new Notification();
    notif.setUserId(friendId);
    notif.setType(NotificationType.PAYMENT_RECEIVED);
    notif.setTitle("Payment Received");
    notif.setMessage("Payment received! Thanks! 💰");
    notificationRepo.save(notif);
}
```

**Example Timeline:**

```
Day 1: Pizza Party
- Rahul pays ₹600 for pizza
- Splits with Amit and Priya
- Creates 2 SplitExpense records:
  * Amit owes Rahul ₹200
  * Priya owes Rahul ₹200

Database:
{
  expenseId: "exp123",
  paidBy: "rahul_id",
  owedBy: "amit_id",
  amount: 200,
  settled: false
}
{
  expenseId: "exp123",
  paidBy: "rahul_id",
  owedBy: "priya_id",
  amount: 200,
  settled: false
}

Rahul's view:
- Amit owes: ₹200
- Priya owes: ₹200
- Total to receive: ₹400

Day 3: Amit pays back
- Rahul clicks "Settle Up" for Amit
- Updates split record: settled = true
- Sends notification to Amit

Rahul's view:
- Amit owes: ₹0 ✅
- Priya owes: ₹200
- Total to receive: ₹200

Day 5: Movie night
- Priya pays ₹300 for movie tickets
- Splits with Rahul
- Creates SplitExpense:
  * Rahul owes Priya ₹150

Rahul's view:
- Amit owes: ₹0
- Priya: ₹200 (she owes) - ₹150 (you owe) = ₹50 (she owes)
- Total to receive: ₹50
```


### 📊 How Reports Work

```java
public ReportResponse getReport(String userId, String period, int year, int month) {
    User user = userRepo.findById(userId).orElseThrow();
    
    if (period.equals("WEEKLY")) {
        return getWeeklyReport(userId, year, month);
    } else {
        return getMonthlyReport(userId, year);
    }
}

private ReportResponse getMonthlyReport(String userId, int year) {
    List<MonthData> months = new ArrayList<>();
    
    // Get last 7 months of data
    LocalDate today = LocalDate.now();
    for (int i = 6; i >= 0; i--) {
        LocalDate month = today.minusMonths(i);
        
        // Calculate month range
        Instant start = month.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = month.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        
        // Get expenses for this month
        List<Expense> expenses = expenseRepo.findByUserIdAndDateRange(userId, start, end);
        
        // Calculate total spent
        double spent = expenses.stream()
            .mapToDouble(Expense::getAmount)
            .sum();
        
        // Calculate savings
        User user = userRepo.findById(userId).orElseThrow();
        double saved = Math.max(user.getMonthlyBudget() - spent, 0);
        
        // Add to list
        months.add(MonthData.builder()
            .month(month.getMonth().name())
            .year(month.getYear())
            .spent(spent)
            .saved(saved)
            .build());
    }
    
    // Calculate trends
    double currentMonthSpent = months.get(6).getSpent();
    double previousMonthSpent = months.get(5).getSpent();
    double spentChange = ((currentMonthSpent - previousMonthSpent) / previousMonthSpent) * 100;
    
    double currentMonthSaved = months.get(6).getSaved();
    double previousMonthSaved = months.get(5).getSaved();
    double savedChange = ((currentMonthSaved - previousMonthSaved) / previousMonthSaved) * 100;
    
    return ReportResponse.builder()
        .period("MONTHLY")
        .data(months)
        .spentTrend(spentChange)
        .savedTrend(savedChange)
        .build();
}
```

**Example Output:**

```json
{
  "period": "MONTHLY",
  "data": [
    { "month": "SEPTEMBER", "year": 2023, "spent": 4200, "saved": 800 },
    { "month": "OCTOBER", "year": 2023, "spent": 4500, "saved": 500 },
    { "month": "NOVEMBER", "year": 2023, "spent": 4800, "saved": 200 },
    { "month": "DECEMBER", "year": 2023, "spent": 5200, "saved": 0 },
    { "month": "JANUARY", "year": 2024, "spent": 4500, "saved": 500 },
    { "month": "FEBRUARY", "year": 2024, "spent": 4300, "saved": 700 },
    { "month": "MARCH", "year": 2024, "spent": 4100, "saved": 900 }
  ],
  "spentTrend": -4.65,    // Spent 4.65% less than last month ✅
  "savedTrend": 28.57     // Saved 28.57% more than last month 🎉
}
```

**Visual Chart:**

```
Spending Trend (Last 7 Months)
₹6000 |
      |                    ●
₹5000 |              ●
      |        ●
₹4000 |  ●                       ●     ●     ●
      |
₹3000 |
      |___________________________________________
       Sep  Oct  Nov  Dec  Jan  Feb  Mar

Savings Trend (Last 7 Months)
₹1000 |                                      ●
      |                                ●
₹800  |  ●
      |                          ●
₹500  |        ●
      |              ●
₹0    |                    ●
      |___________________________________________
       Sep  Oct  Nov  Dec  Jan  Feb  Mar
```


---

## 🎨 UI/UX Design Patterns

### Color Scheme 🌈

```kotlin
// Theme.kt
val Purple = Color(0xFF6A0DAD)      // Primary color
val LightPurple = Color(0xFFE1BEE7) // Secondary
val Gold = Color(0xFFFFB300)        // Accent (coins, rewards)
val Green = Color(0xFF4CAF50)       // Success, savings
val Red = Color(0xFFF44336)         // Danger, overspending
val Orange = Color(0xFFFF9800)      // Warning
val Gray = Color(0xFF9E9E9E)        // Neutral

// Category Colors
val CategoryColors = mapOf(
    "FOOD" to Purple,
    "TRAVEL" to LightPurple,
    "RENT" to Gray,
    "UTILITIES" to Gold,
    "MISC" to Orange
)
```

### Common UI Components

#### Budget Progress Bar
```kotlin
@Composable
fun BudgetProgressBar(spent: Double, budget: Double) {
    val progress = (spent / budget).toFloat()
    val color = when {
        progress < 0.5 -> Green      // Under 50%: Good!
        progress < 0.8 -> Orange     // 50-80%: Warning
        else -> Red                  // Over 80%: Danger!
    }
    
    Column {
        LinearProgressIndicator(
            progress = progress,
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
        Text("₹$spent / ₹$budget")
    }
}
```

#### Expense Card
```kotlin
@Composable
fun ExpenseCard(expense: ExpenseItem) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CategoryColors[expense.category], CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(getCategoryEmoji(expense.category), fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.Bold)
                Text(expense.category, color = Gray, fontSize = 12.sp)
                Text(formatDate(expense.date), color = Gray, fontSize = 12.sp)
            }
            
            // Amount
            Text(
                "₹${expense.amount}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Red
            )
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "FOOD" -> "🍔"
        "TRAVEL" -> "🚗"
        "RENT" -> "🏠"
        "UTILITIES" -> "💡"
        "MISC" -> "📦"
        else -> "💰"
    }
}
```


---

## 🚀 Deployment & Configuration

### Backend Deployment (Render.com)

```yaml
# render.yaml
services:
  - type: web
    name: xpensetrack-backend
    env: java
    buildCommand: mvn clean package -DskipTests
    startCommand: java -jar target/xpensetrack-backend-1.0.0.jar
    envVars:
      - key: MONGODB_URI
        value: mongodb+srv://username:password@cluster.mongodb.net/xpensetrack
      - key: JWT_SECRET
        value: your-super-secret-key-min-256-bits
      - key: JWT_EXPIRATION
        value: 604800000  # 7 days
      - key: PORT
        value: 8080
```

### Environment Variables

```bash
# .env file (for local development)
MONGODB_URI=mongodb://localhost:27017/xpensetrack
JWT_SECRET=xpensetrack-secret-key-change-in-production-min-256-bits-long-key
JWT_EXPIRATION=604800000
PORT=8080
```

### Database Setup (MongoDB)

```javascript
// MongoDB Collections are auto-created by Spring Data
// But here's what they look like:

use xpensetrack;

// Create indexes for performance
db.users.createIndex({ "email": 1 }, { unique: true });
db.expenses.createIndex({ "userId": 1, "date": -1 });
db.dragons.createIndex({ "userId": 1 }, { unique: true });
db.piggy_banks.createIndex({ "userId": 1, "deadline": 1 });
db.split_expenses.createIndex({ "paidBy": 1, "settled": 1 });
db.split_expenses.createIndex({ "owedBy": 1, "settled": 1 });
db.notifications.createIndex({ "userId": 1, "read": 1 });
```

### Mobile App Configuration

```kotlin
// ApiClient.kt
object ApiClient {
    // Production
    private const val BASE_URL = "https://xpensetrack-4fdf.onrender.com/"
    
    // Local development options:
    // Emulator: "http://10.0.2.2:8080/"
    // Physical device: "http://192.168.1.60:8080/" (your computer's IP)
    
    var token: String? = null
    
    // ... rest of code
}
```

### Running Locally

**Backend:**
```bash
# Navigate to backend folder
cd xpensetrack/backend

# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run

# Or use the batch file (Windows)
run.bat

# Backend starts at http://localhost:8080
```

**Mobile App:**
```bash
# Navigate to app folder
cd xpensetrack/app

# Build the app
./gradlew build

# Run on emulator
./gradlew mobile:installDebug

# Or open in Android Studio and click Run
```


---

## 🎓 Key Concepts Explained Simply

### What is REST API? 🌐

Think of it like ordering food at a restaurant:

1. **You (Mobile App)** tell the waiter what you want
2. **Waiter (API)** takes your order to the kitchen
3. **Kitchen (Backend)** prepares your food
4. **Waiter** brings it back to you

**Example:**
```
You: "I want to add an expense of ₹50 for lunch"
    ↓
Mobile App: POST /api/expenses { amount: 50, description: "Lunch" }
    ↓
Backend: Saves to database, gives you 1 coin
    ↓
Mobile App: "Success! You earned 1 coin!"
```

### What is JWT Token? 🎫

It's like a VIP wristband at an amusement park:

1. When you enter (login), you get a wristband (token)
2. Every ride (API call) checks your wristband
3. If valid, you can ride (access data)
4. If expired or fake, you're kicked out (need to login again)

**Token Structure:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9  ← Header (algorithm)
.
eyJzdWIiOiJ1c2VyMTIzIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIn0  ← Payload (your data)
.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature (security)
```

### What is MongoDB? 🗄️

It's like a filing cabinet with folders:

**Traditional Database (SQL):**
```
Users Table:
| ID | Name  | Email           |
|----|-------|-----------------|
| 1  | Rahul | rahul@email.com |

Expenses Table:
| ID | UserID | Amount | Description |
|----|--------|--------|-------------|
| 1  | 1      | 50     | Lunch       |
```

**MongoDB (NoSQL):**
```json
{
  "_id": "1",
  "name": "Rahul",
  "email": "rahul@email.com",
  "expenses": [
    { "amount": 50, "description": "Lunch" }
  ]
}
```

MongoDB is more flexible - you can add new fields anytime!

### What is Jetpack Compose? 🎨

It's like building with LEGO blocks:

**Old Way (XML):**
```xml
<LinearLayout>
  <TextView text="Hello" />
  <Button text="Click me" />
</LinearLayout>
```

**New Way (Compose):**
```kotlin
Column {
  Text("Hello")
  Button(onClick = { }) { Text("Click me") }
}
```

Compose is easier to read and write!

### What is Retrofit? 📡

It's like a phone that calls the backend:

```kotlin
// Define what you want to call
interface ExpenseApi {
    @POST("api/expenses")
    suspend fun addExpense(@Body req: AddExpenseRequest): ExpenseItem
}

// Make the call
val api = ApiClient.create<ExpenseApi>()
val expense = api.addExpense(AddExpenseRequest(amount = 50))

// Behind the scenes:
// Retrofit converts your Kotlin object to JSON
// Sends HTTP request to backend
// Receives JSON response
// Converts back to Kotlin object
```


---

## 🔧 Common Operations Explained

### Adding a New Feature

Let's say you want to add a "Categories" feature where users can create custom categories.

**Step 1: Create Model (Backend)**
```java
// backend/src/main/java/com/xpensetrack/model/Category.java
@Data
@Document(collection = "categories")
public class Category {
    @Id
    private String id;
    private String userId;
    private String name;
    private String emoji;
    private String color;
}
```

**Step 2: Create Repository**
```java
// backend/src/main/java/com/xpensetrack/repository/CategoryRepository.java
public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByUserId(String userId);
}
```

**Step 3: Create Service**
```java
// backend/src/main/java/com/xpensetrack/service/CategoryService.java
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepo;
    
    public Category create(String userId, String name, String emoji, String color) {
        Category category = new Category();
        category.setUserId(userId);
        category.setName(name);
        category.setEmoji(emoji);
        category.setColor(color);
        return categoryRepo.save(category);
    }
    
    public List<Category> getAll(String userId) {
        return categoryRepo.findByUserId(userId);
    }
}
```

**Step 4: Create Controller**
```java
// backend/src/main/java/com/xpensetrack/controller/CategoryController.java
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody CreateCategoryRequest req) {
        String userId = AuthUtil.getCurrentUserId();
        return ResponseEntity.ok(categoryService.create(userId, req.getName(), req.getEmoji(), req.getColor()));
    }
    
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        String userId = AuthUtil.getCurrentUserId();
        return ResponseEntity.ok(categoryService.getAll(userId));
    }
}
```

**Step 5: Create API Interface (Mobile)**
```kotlin
// mobile/src/main/java/com/xpensetrack/data/api/ApiService.kt
interface CategoryApi {
    @POST("api/categories")
    suspend fun create(@Body req: CreateCategoryRequest): Category
    
    @GET("api/categories")
    suspend fun getAll(): List<Category>
}
```

**Step 6: Create Screen (Mobile)**
```kotlin
// mobile/src/main/java/com/xpensetrack/ui/screens/CategoriesScreen.kt
@Composable
fun CategoriesScreen(navController: NavController) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        val api = ApiClient.create<CategoryApi>()
        categories = api.getAll()
    }
    
    Column {
        Text("My Categories", fontSize = 24.sp)
        
        LazyColumn {
            items(categories) { category ->
                CategoryCard(category)
            }
        }
        
        Button(onClick = { /* Show create dialog */ }) {
            Text("+ Add Category")
        }
    }
}
```

**Step 7: Add to Navigation**
```kotlin
// mobile/src/main/java/com/xpensetrack/navigation/NavGraph.kt
object Routes {
    // ... existing routes
    const val CATEGORIES = "categories"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.SPLASH) {
        // ... existing routes
        composable(Routes.CATEGORIES) { CategoriesScreen(navController) }
    }
}
```

Done! Now users can create custom categories! 🎉


---

## 🐛 Debugging Tips

### Backend Debugging

**Check Logs:**
```bash
# In terminal where backend is running
# Look for errors like:

ERROR: MongoException: Connection refused
→ MongoDB is not running! Start MongoDB first.

ERROR: JwtException: Invalid token
→ Token expired or invalid. User needs to login again.

ERROR: IllegalArgumentException: Email already registered
→ User trying to signup with existing email.
```

**Test API with Postman:**
```
1. Open Postman
2. Create new request:
   - Method: POST
   - URL: http://localhost:8080/api/auth/login
   - Body (JSON):
     {
       "email": "test@example.com",
       "password": "password123"
     }
3. Send
4. Check response:
   - 200 OK: Success! ✅
   - 401 Unauthorized: Wrong password ❌
   - 500 Error: Backend problem ❌
```

### Mobile App Debugging

**Check Logcat:**
```bash
# In Android Studio, open Logcat
# Filter by "xpensetrack"

# Common errors:

E/Retrofit: HTTP 401 Unauthorized
→ Token expired, need to login again

E/Retrofit: java.net.ConnectException: Failed to connect
→ Backend is not running or wrong URL

E/Compose: java.lang.NullPointerException
→ Trying to access null data, add null checks
```

**Add Debug Logs:**
```kotlin
// In your code
Log.d("XpenseTrack", "Adding expense: amount=$amount, description=$description")

try {
    val response = api.addExpense(request)
    Log.d("XpenseTrack", "Success: $response")
} catch (e: Exception) {
    Log.e("XpenseTrack", "Error: ${e.message}", e)
}
```

### Common Issues

**Issue 1: "Cannot connect to backend"**
```
Problem: Mobile app can't reach backend
Solutions:
1. Check backend is running (http://localhost:8080/api/health)
2. Use correct URL:
   - Emulator: http://10.0.2.2:8080/
   - Physical device: http://YOUR_COMPUTER_IP:8080/
3. Check network_security_config.xml allows cleartext traffic
```

**Issue 2: "Token expired"**
```
Problem: JWT token expired after 7 days
Solution:
1. Catch 401 Unauthorized error
2. Clear saved token
3. Navigate to login screen
4. User logs in again
5. Get new token
```

**Issue 3: "Dragon not leveling up"**
```
Problem: Fed dragon but level didn't increase
Debug:
1. Check current experience: 2500 XP
2. Check level: 3
3. Calculate needed: 3 × 3000 = 9000 XP
4. 2500 < 9000, so no level up (correct!)
5. Need 6500 more XP (650 more coins)
```


---

## 📈 Performance Optimization

### Backend Optimization

**1. Database Indexing**
```java
// Add indexes to frequently queried fields
@Indexed
private String email;  // For login lookups

@Indexed
private String userId;  // For user-specific queries

@CompoundIndex(def = "{'userId': 1, 'date': -1}")
// For date-range queries on expenses
```

**2. Caching**
```java
// Cache frequently accessed data
@Cacheable("users")
public User getUser(String userId) {
    return userRepo.findById(userId).orElseThrow();
}

// Clear cache when data changes
@CacheEvict(value = "users", key = "#userId")
public User updateUser(String userId, UpdateRequest req) {
    // ... update logic
}
```

**3. Pagination**
```java
// Don't load all expenses at once
@GetMapping("/expenses")
public Page<Expense> getExpenses(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
    return expenseRepo.findByUserId(userId, pageable);
}
```

### Mobile App Optimization

**1. Lazy Loading**
```kotlin
// Load data only when needed
LazyColumn {
    items(expenses) { expense ->
        ExpenseCard(expense)
    }
}
// Only renders visible items, not all 1000 expenses!
```

**2. Image Caching**
```kotlin
// Use Coil for image loading with caching
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build(),
    contentDescription = null
)
```

**3. State Management**
```kotlin
// Use remember to avoid recomposition
@Composable
fun ExpensiveCalculation() {
    val result = remember(key1 = expenses) {
        // Only recalculates when expenses change
        expenses.sumOf { it.amount }
    }
}
```

---

## 🔒 Security Best Practices

### Backend Security

**1. Password Encryption**
```java
// NEVER store plain passwords!
// ❌ BAD
user.setPassword(req.getPassword());

// ✅ GOOD
user.setPassword(passwordEncoder.encode(req.getPassword()));
```

**2. Input Validation**
```java
// Validate all inputs
@PostMapping("/expenses")
public ResponseEntity<Expense> addExpense(@Valid @RequestBody AddExpenseRequest req) {
    // @Valid triggers validation
}

// In request DTO
public class AddExpenseRequest {
    @NotNull
    @Positive
    private Double amount;
    
    @NotBlank
    @Size(max = 100)
    private String description;
}
```

**3. SQL Injection Prevention**
```java
// MongoDB is safe from SQL injection
// But still use parameterized queries
expenseRepo.findByUserIdAndDateRange(userId, start, end);
// NOT: expenseRepo.query("SELECT * FROM expenses WHERE userId = " + userId);
```

**4. Rate Limiting**
```java
// Prevent abuse
@RateLimiter(name = "api", fallbackMethod = "rateLimitFallback")
@PostMapping("/expenses")
public ResponseEntity<Expense> addExpense(@RequestBody AddExpenseRequest req) {
    // ...
}
```

### Mobile App Security

**1. Secure Token Storage**
```kotlin
// Use EncryptedSharedPreferences
val sharedPreferences = EncryptedSharedPreferences.create(
    "secure_prefs",
    masterKey,
    context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// Save token
sharedPreferences.edit().putString("token", token).apply()
```

**2. HTTPS Only**
```kotlin
// NEVER use HTTP in production!
// ❌ BAD
private const val BASE_URL = "http://api.example.com/"

// ✅ GOOD
private const val BASE_URL = "https://api.example.com/"
```

**3. Certificate Pinning**
```kotlin
// Prevent man-in-the-middle attacks
val certificatePinner = CertificatePinner.Builder()
    .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```


---

## 🎯 Future Enhancements

### Planned Features

**1. Recurring Expenses**
```
Problem: User pays ₹500 rent every month
Solution: Add recurring expense feature
- Set frequency (daily, weekly, monthly)
- Auto-create expense on schedule
- Send reminder before due date
```

**2. Budget Categories**
```
Problem: Want different budgets for different categories
Solution: Category-wise budgets
- Food: ₹2000/month
- Travel: ₹1000/month
- Entertainment: ₹500/month
- Track separately
```

**3. Expense Photos**
```
Problem: Want to attach receipt photos
Solution: Image upload
- Take photo of receipt
- Upload to cloud storage
- Attach to expense
- View later for reference
```

**4. Multi-Currency Support**
```
Problem: Traveling abroad, spending in USD
Solution: Currency conversion
- Select currency per expense
- Auto-convert to INR
- Show both amounts
- Use live exchange rates
```

**5. Expense Analytics**
```
Problem: Want to see spending patterns
Solution: Advanced analytics
- Spending by day of week
- Most expensive category
- Average daily spending
- Predictions for next month
```

**6. Group Expenses**
```
Problem: Hostel room sharing expenses
Solution: Group management
- Create groups (Roommates, Trip buddies)
- Add group expenses
- Auto-split among members
- Track group balances
```

**7. Bill Reminders**
```
Problem: Forget to pay electricity bill
Solution: Reminder system
- Add upcoming bills
- Set reminder date
- Get notification
- Mark as paid
```

**8. Export Data**
```
Problem: Want expense data in Excel
Solution: Export feature
- Export to CSV/Excel
- Filter by date range
- Email to self
- Use for tax filing
```

---

## 📚 Learning Resources

### For Backend (Java/Spring Boot)

**Books:**
- "Spring Boot in Action" by Craig Walls
- "Java: The Complete Reference" by Herbert Schildt

**Online Courses:**
- Spring Boot Tutorial (YouTube)
- Udemy: Spring Boot Masterclass
- Official Spring Guides: spring.io/guides

**Practice:**
- Build a simple REST API
- Add authentication with JWT
- Connect to MongoDB
- Deploy to Heroku/Render

### For Mobile (Kotlin/Compose)

**Books:**
- "Kotlin in Action" by Dmitry Jemerov
- "Jetpack Compose Essentials" by Neil Smyth

**Online Courses:**
- Android Basics with Compose (Google)
- Udacity: Kotlin Bootcamp
- YouTube: Philipp Lackner (Compose tutorials)

**Practice:**
- Build a simple app with Compose
- Make API calls with Retrofit
- Handle navigation
- Manage state

### For Database (MongoDB)

**Resources:**
- MongoDB University (free courses)
- MongoDB Documentation
- YouTube: MongoDB Crash Course

**Practice:**
- Create collections
- Write queries
- Create indexes
- Understand relationships

---

## 🎉 Conclusion

Congratulations! You now understand how XpenseTrack works from top to bottom! 🎊

**What we learned:**
1. ✅ Project structure (Backend + Mobile + Database)
2. ✅ How authentication works (JWT tokens)
3. ✅ How expenses are tracked and calculated
4. ✅ How the dragon leveling system works
5. ✅ How savings goals are managed
6. ✅ How friend splitting works
7. ✅ How all components communicate
8. ✅ How to add new features
9. ✅ How to debug issues
10. ✅ Security and performance best practices

**Remember:**
- Backend is the brain (thinks and calculates)
- Mobile app is the face (what you see)
- Database is the memory (remembers everything)
- API is the messenger (carries information)
- JWT is your VIP pass (proves who you are)

**Next Steps:**
1. Run the project locally
2. Try adding a new feature
3. Experiment with the code
4. Break things and fix them (best way to learn!)
5. Build your own features

Happy coding! 🚀💻✨

---

**Made with ❤️ for hostel students who want to manage their money better!**

