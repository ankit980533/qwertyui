# Render Deployment Guide

## Environment Variables for Render

Copy these values into Render's Environment Variables section:

### 1. MONGODB_URI
**Required** - Your MongoDB Atlas connection string

**How to get it:**
1. Go to [MongoDB Atlas](https://cloud.mongodb.com)
2. Create free M0 cluster
3. Click "Connect" → "Connect your application"
4. Copy the connection string
5. Replace `<password>` with your database user password
6. Replace `<dbname>` with `xpensetrack`

**Example:**
```
mongodb+srv://myuser:MySecurePassword123@cluster0.abcde.mongodb.net/xpensetrack?retryWrites=true&w=majority
```

**Value to paste in Render:**
```
mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@YOUR_CLUSTER.mongodb.net/xpensetrack?retryWrites=true&w=majority
```

---

### 2. JWT_SECRET
**Required** - Secret key for JWT token generation (minimum 32 characters)

**How to generate:**
Run this command in your terminal:
```bash
openssl rand -base64 32
```

**Example output:**
```
K8vN2pQ9mR5tY7wZ3xC6vB8nM4kL1jH9gF2dS5aP0oI=
```

**Value to paste in Render:**
```
K8vN2pQ9mR5tY7wZ3xC6vB8nM4kL1jH9gF2dS5aP0oI=
```

---

### 3. JWT_EXPIRATION
**Optional** - Token expiration time in milliseconds

**Default:** 604800000 (7 days)

**Value to paste in Render:**
```
604800000
```

---

## Complete Environment Variables Summary

| Variable | Value | Required |
|----------|-------|----------|
| MONGODB_URI | `mongodb+srv://user:pass@cluster.mongodb.net/xpensetrack` | ✅ Yes |
| JWT_SECRET | Generate with `openssl rand -base64 32` | ✅ Yes |
| JWT_EXPIRATION | `604800000` | ❌ No (has default) |

---

## Deployment Steps

1. **Setup MongoDB Atlas:**
   - Create account at [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas)
   - Create M0 free cluster
   - Create database user
   - Whitelist all IPs (0.0.0.0/0)
   - Get connection string

2. **Deploy to Render:**
   - Go to [render.com](https://render.com)
   - New → Web Service
   - Connect GitHub repo
   - Root Directory: `backend`
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -Dserver.port=$PORT -jar target/xpensetrack-backend-1.0.0.jar`
   - Add environment variables above

3. **Test Deployment:**
   ```bash
   # Health check
   curl https://your-app.onrender.com/api/health
   
   # Ping
   curl https://your-app.onrender.com/api/health/ping
   
   # Signup test
   curl -X POST https://your-app.onrender.com/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{"username":"test","email":"test@test.com","password":"test123"}'
   ```

---

## Health Check Endpoints

Your backend now has health check endpoints:

- **GET /api/health** - Full health status with database check
  ```json
  {
    "status": "UP",
    "timestamp": "2026-04-02T10:30:00",
    "service": "xpensetrack-backend",
    "database": "connected"
  }
  ```

- **GET /api/health/ping** - Simple ping/pong
  ```
  pong
  ```

These endpoints are public (no authentication required) and can be used by:
- Render's health checks
- Uptime monitoring services
- Keeping your service awake (prevent cold starts)

---

## Preventing Cold Starts

Free tier services sleep after 15 minutes. To keep it awake:

1. **Use Cron Job Service:**
   - Go to [cron-job.org](https://cron-job.org)
   - Create free account
   - Add job: `https://your-app.onrender.com/api/health/ping`
   - Schedule: Every 14 minutes

2. **Or upgrade to paid plan** ($7/month for always-on)

---

## Troubleshooting

**Build fails:**
- Check Java version is 17
- Ensure Maven can download dependencies
- Check logs in Render dashboard

**Database connection fails:**
- Verify MongoDB Atlas allows connections from 0.0.0.0/0
- Check username/password in connection string
- Ensure database name is `xpensetrack`

**JWT errors:**
- Ensure JWT_SECRET is at least 32 characters
- Check it's properly set in environment variables

**Port issues:**
- Render automatically sets PORT variable
- Your app now reads PORT first, then SERVER_PORT
