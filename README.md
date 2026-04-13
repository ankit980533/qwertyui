# 🎮 XpenseTrack - Setup Instructions

A gamified expense tracking app for hostel students with a pet dragon companion, savings goals, and friend bill splitting.

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Backend Setup](#backend-setup)
- [Mobile App Setup](#mobile-app-setup)
- [Running the Application](#running-the-application)
- [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisites

### Backend Requirements
- Java 17 or higher
- Maven 3.6+
- MongoDB (local or Atlas account)

### Mobile App Requirements
- Android Studio (latest version)
- JDK 17 or higher
- Android SDK (API level 24+)
- Physical Android device or emulator

---

## 🖥️ Backend Setup

### 1. Install MongoDB

**Option A: Local MongoDB**
```bash
# Ubuntu/Debian
sudo apt-get install mongodb

# macOS
brew install mongodb-community

# Start MongoDB
sudo systemctl start mongodb  # Linux
brew services start mongodb-community  # macOS
```

**Option B: MongoDB Atlas (Cloud)**
1. Create account at [https://cloud.mongodb.com](https://cloud.mongodb.com)
2. Create a free cluster
3. Get connection string (looks like: `mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/`)

### 2. Configure Environment Variables

Navigate to backend directory:
```bash
cd xpensetrack/backend
```

Copy the example environment file:
```bash
cp .env.example .env
```

Edit `.env` file with your settings:
```env
# For local MongoDB
MONGODB_URI=mongodb://localhost:27017/xpensetrack

# OR for MongoDB Atlas
# MONGODB_URI=mongodb+srv://your-username:your-password@cluster0.xxxxx.mongodb.net/xpensetrack?retryWrites=true&w=majority

# Generate secure JWT secret (run this command):
# openssl rand -base64 32
JWT_SECRET=your-generated-secret-key-here-must-be-at-least-256-bits
JWT_EXPIRATION=604800000

SERVER_PORT=8080
```

### 3. Build and Run Backend

**Using Maven:**
```bash
# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

**Using the run script (Windows):**
```bash
run.bat
```

The backend will start at `http://localhost:8080`

### 4. Verify Backend is Running

Open browser or use curl:
```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 📱 Mobile App Setup

### 1. Open Project in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to `xpensetrack/app`
4. Click "OK"
5. Wait for Gradle sync to complete

### 2. Configure API Endpoint

Open `app/mobile/src/main/java/com/xpensetrack/data/api/ApiClient.kt`

Update the BASE_URL:
```kotlin
// For local development (Android emulator)
private const val BASE_URL = "http://10.0.2.2:8080/"

// For physical device on same network
// private const val BASE_URL = "http://YOUR_COMPUTER_IP:8080/"

// For production
// private const val BASE_URL = "https://xpensetrack-4fdf.onrender.com/"
```

**Finding your computer's IP:**
```bash
# Linux/macOS
ifconfig | grep "inet "

# Windows
ipconfig
```

### 3. Configure Network Security (for HTTP)

The app is already configured to allow HTTP connections for local development in:
`app/mobile/src/main/res/xml/network_security_config.xml`

### 4. Generate App Icons (Optional)

If you want to regenerate app icons:
```bash
cd xpensetrack/app
python generate_icons.py
```

### 5. Build the App

In Android Studio:
1. Click "Build" → "Make Project"
2. Wait for build to complete

---

## 🚀 Running the Application

### Start Backend First

```bash
cd xpensetrack/backend
mvn spring-boot:run
```

Wait for message: `Started XpensetrackApplication in X seconds`

### Run Mobile App

**Option 1: Using Android Emulator**
1. In Android Studio, click "Device Manager"
2. Create/start an emulator (API 24+)
3. Click "Run" button (green play icon)
4. Select your emulator

**Option 2: Using Physical Device**
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click "Run" button
5. Select your device

### First Time Setup

1. App opens to Splash Screen
2. Swipe through Onboarding screens
3. Click "Sign Up"
4. Fill in:
   - Full Name
   - Email
   - Phone Number
   - Password
   - Monthly Budget (e.g., 5000)
5. Click "Create Account"
6. You'll get a baby dragon automatically! 🐉

---

## 🧪 Testing the Setup

### Test Backend Endpoints

**Signup:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "phoneNumber": "9876543210",
    "password": "password123",
    "confirmPassword": "password123",
    "monthlyBudget": 5000
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

Save the token from response and use it for authenticated requests:
```bash
curl -X GET http://localhost:8080/api/expenses/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🐛 Troubleshooting

### Backend Issues

**MongoDB Connection Failed**
```
Error: MongoTimeoutException
```
Solution:
- Check MongoDB is running: `sudo systemctl status mongodb`
- Verify MONGODB_URI in `.env` file
- For Atlas, check IP whitelist and credentials

**Port Already in Use**
```
Error: Port 8080 is already in use
```
Solution:
- Change SERVER_PORT in `.env` to different port (e.g., 8081)
- Or kill process using port: `lsof -ti:8080 | xargs kill -9` (Linux/macOS)

**JWT Secret Too Short**
```
Error: JWT secret must be at least 256 bits
```
Solution:
- Generate new secret: `openssl rand -base64 32`
- Update JWT_SECRET in `.env`

### Mobile App Issues

**Cannot Connect to Backend**
```
Error: Failed to connect to /10.0.2.2:8080
```
Solution:
- Verify backend is running
- Check BASE_URL in ApiClient.kt
- For physical device, use computer's IP instead of 10.0.2.2
- Ensure device and computer are on same network

**Gradle Build Failed**
```
Error: Could not resolve dependencies
```
Solution:
- Click "File" → "Invalidate Caches / Restart"
- Delete `.gradle` folder and sync again
- Check internet connection

**App Crashes on Startup**
```
Error: Unable to start activity
```
Solution:
- Check Logcat in Android Studio for error details
- Verify BASE_URL is correct
- Ensure backend is running and accessible

### Common Issues

**"Unauthorized" Error in App**
- Token expired (login again)
- Token not saved properly (clear app data and login again)

**Dragon Not Appearing**
- Check backend logs for errors
- Verify dragon was created during signup
- Try GET /api/dragon endpoint manually

**Expenses Not Saving**
- Check network connection
- Verify token is valid
- Check backend logs for validation errors

---

## 📚 Additional Resources

- [Project Documentation](PROJECT_DOCUMENTATION.md) - Detailed technical documentation
- [Backend Deployment Guide](backend/RENDER_DEPLOYMENT.md) - Deploy to Render
- [API Documentation](http://localhost:8080/swagger-ui.html) - Interactive API docs (when backend is running)

---

## 🎯 Quick Start Summary

```bash
# 1. Start MongoDB
sudo systemctl start mongodb

# 2. Setup backend
cd xpensetrack/backend
cp .env.example .env
# Edit .env with your settings
mvn spring-boot:run

# 3. Open Android Studio
# Open xpensetrack/app
# Update ApiClient.kt with correct BASE_URL
# Click Run

# 4. Create account in app and start tracking expenses!
```

---

## 💡 Tips

- Use monthly budget of 3000-5000 for realistic testing
- Feed your dragon regularly to keep it happy
- Create savings goals to stay motivated
- Add friends to split bills easily
- Check notifications for spending alerts

Happy expense tracking! 🎉
