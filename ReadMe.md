# Complete Java Web Application Creation and Tomcat Deployment Guide

## Table of Contents
1. [Project Setup](#project-setup)
2. [Create Application Structure](#create-application-structure)
3. [Write the Java Code](#write-the-java-code)
4. [Create Configuration Files](#create-configuration-files)
5. [Compile the Application](#compile-the-application)
6. [Package into WAR](#package-into-war)
7. [Deploy to Tomcat](#deploy-to-tomcat)
8. [Test the Application](#test-the-application)
9. [Troubleshooting](#troubleshooting)

## Project Setup

### 1. Create Project Directory Structure

**Create the main project directory:**
```cmd
cd C:\
mkdir JavaWebApps
cd JavaWebApps
mkdir ATM-WebApp
cd ATM-WebApp
```

**Create the standard web application structure:**
```cmd
mkdir src
mkdir src\main
mkdir src\main\java
mkdir src\main\java\com
mkdir src\main\java\com\atm
mkdir src\main\webapp
mkdir src\main\webapp\WEB-INF
mkdir src\main\webapp\WEB-INF\lib
mkdir lib
mkdir build
mkdir build\classes
mkdir build\webapp
```

**Your directory structure should look like this:**
```
C:\JavaWebApps\ATM-WebApp\
├── src\
│   └── main\
│       ├── java\
│       │   └── com\
│       │       └── atm\
│       └── webapp\
│           └── WEB-INF\
│               └── lib\
├── lib\
└── build\
    ├── classes\
    └── webapp\
```

### 2. Download Required JAR Files

**Create download script (download-deps.bat):**
```batch
@echo off
echo Downloading required dependencies...

cd lib

echo Downloading Gson for JSON handling...
curl -L -o gson-2.10.1.jar "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"

echo Downloading MySQL Connector...
curl -L -o mysql-connector-java-8.0.33.jar "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar"

echo Dependencies downloaded successfully!
cd ..
pause
```

**Run the download script:**
```cmd
cd C:\JavaWebApps\ATM-WebApp
download-deps.bat
```

## Create Application Structure

### 1. Create Model Classes

**Create `src\main\java\com\atm\Account.java`:**
```java
package com.atm;

import java.math.BigDecimal;

public class Account {
    private int accountId;
    private String accountNumber;
    private String customerName;
    private BigDecimal balance;
    private String accountType;
    
    // Default constructor
    public Account() {}
    
    // Constructor with parameters
    public Account(int accountId, String accountNumber, String customerName, BigDecimal balance, String accountType) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.balance = balance;
        this.accountType = accountType;
    }
    
    // Getters and Setters
    public int getAccountId() {
        return accountId;
    }
    
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", balance=" + balance +
                ", accountType='" + accountType + '\'' +
                '}';
    }
}
```

### 2. Create Utility Classes

**Create `src\main\java\com\atm\JsonUtil.java`:**
```java
package com.atm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    public static String toJson(Object object) {
        return gson.toJson(object);
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
    
    public static void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        out.print(toJson(data));
        out.flush();
    }
    
    public static void sendJsonError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        ErrorResponse error = new ErrorResponse(false, message);
        PrintWriter out = response.getWriter();
        out.print(toJson(error));
        out.flush();
    }
    
    // Inner class for error responses
    public static class ErrorResponse {
        private boolean success;
        private String message;
        
        public ErrorResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    // Inner class for success responses
    public static class SuccessResponse<T> {
        private boolean success;
        private String message;
        private T data;
        
        public SuccessResponse(String message, T data) {
            this.success = true;
            this.message = message;
            this.data = data;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}
```

### 3. Create Database Access Object

**Create `src\main\java\com\atm\AccountDAO.java`:**
```java
package com.atm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simple in-memory DAO for demonstration
// In real application, this would connect to database
public class AccountDAO {
    private static Map<String, Account> accounts = new HashMap<>();
    private static int nextId = 1;
    
    // Initialize with sample data
    static {
        Account account1 = new Account(nextId++, "1001234567", "John Doe", new BigDecimal("50000.00"), "SAVINGS");
        Account account2 = new Account(nextId++, "1001234568", "Jane Smith", new BigDecimal("25000.00"), "CHECKING");
        Account account3 = new Account(nextId++, "1001234569", "Bob Johnson", new BigDecimal("75000.00"), "SAVINGS");
        
        accounts.put(account1.getAccountNumber(), account1);
        accounts.put(account2.getAccountNumber(), account2);
        accounts.put(account3.getAccountNumber(), account3);
    }
    
    public Account findByAccountNumber(String accountNumber) {
        return accounts.get(accountNumber);
    }
    
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }
    
    public Account save(Account account) {
        if (account.getAccountId() == 0) {
            account.setAccountId(nextId++);
        }
        accounts.put(account.getAccountNumber(), account);
        return account;
    }
    
    public boolean updateBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accounts.get(accountNumber);
        if (account != null) {
            account.setBalance(newBalance);
            return true;
        }
        return false;
    }
    
    public boolean delete(String accountNumber) {
        return accounts.remove(accountNumber) != null;
    }
    
    public int getTotalAccounts() {
        return accounts.size();
    }
}
```

## Write the Java Code

### 1. Create Servlet Classes

**Create `src\main\java\com\atm\AccountServlet.java`:**
```java
package com.atm;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/api/accounts/*")
public class AccountServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all accounts
                List<Account> accounts = accountDAO.findAll();
                JsonUtil.SuccessResponse<List<Account>> successResponse = 
                    new JsonUtil.SuccessResponse<>("Accounts retrieved successfully", accounts);
                JsonUtil.sendJsonResponse(response, successResponse);
                
            } else if (pathInfo.startsWith("/")) {
                // Get specific account by account number
                String accountNumber = pathInfo.substring(1);
                Account account = accountDAO.findByAccountNumber(accountNumber);
                
                if (account != null) {
                    JsonUtil.SuccessResponse<Account> successResponse = 
                        new JsonUtil.SuccessResponse<>("Account found", account);
                    JsonUtil.sendJsonResponse(response, successResponse);
                } else {
                    JsonUtil.sendJsonError(response, "Account not found", 404);
                }
            }
        } catch (Exception e) {
            JsonUtil.sendJsonError(response, "Internal server error: " + e.getMessage(), 500);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            // Parse JSON to Account object
            Account newAccount = JsonUtil.fromJson(sb.toString(), Account.class);
            
            // Validate required fields
            if (newAccount.getAccountNumber() == null || newAccount.getAccountNumber().trim().isEmpty()) {
                JsonUtil.sendJsonError(response, "Account number is required", 400);
                return;
            }
            
            if (newAccount.getCustomerName() == null || newAccount.getCustomerName().trim().isEmpty()) {
                JsonUtil.sendJsonError(response, "Customer name is required", 400);
                return;
            }
            
            // Check if account already exists
            if (accountDAO.findByAccountNumber(newAccount.getAccountNumber()) != null) {
                JsonUtil.sendJsonError(response, "Account already exists", 409);
                return;
            }
            
            // Set default values if not provided
            if (newAccount.getBalance() == null) {
                newAccount.setBalance(new BigDecimal("0.00"));
            }
            if (newAccount.getAccountType() == null) {
                newAccount.setAccountType("SAVINGS");
            }
            
            // Save account
            Account savedAccount = accountDAO.save(newAccount);
            
            JsonUtil.SuccessResponse<Account> successResponse = 
                new JsonUtil.SuccessResponse<>("Account created successfully", savedAccount);
            response.setStatus(201); // Created
            JsonUtil.sendJsonResponse(response, successResponse);
            
        } catch (Exception e) {
            JsonUtil.sendJsonError(response, "Failed to create account: " + e.getMessage(), 500);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendJsonError(response, "Account number is required in URL", 400);
            return;
        }
        
        try {
            String accountNumber = pathInfo.substring(1);
            
            // Check if account exists
            Account existingAccount = accountDAO.findByAccountNumber(accountNumber);
            if (existingAccount == null) {
                JsonUtil.sendJsonError(response, "Account not found", 404);
                return;
            }
            
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            // Parse JSON to Account object
            Account updatedAccount = JsonUtil.fromJson(sb.toString(), Account.class);
            
            // Update fields
            if (updatedAccount.getCustomerName() != null) {
                existingAccount.setCustomerName(updatedAccount.getCustomerName());
            }
            if (updatedAccount.getBalance() != null) {
                existingAccount.setBalance(updatedAccount.getBalance());
            }
            if (updatedAccount.getAccountType() != null) {
                existingAccount.setAccountType(updatedAccount.getAccountType());
            }
            
            // Save updated account
            Account savedAccount = accountDAO.save(existingAccount);
            
            JsonUtil.SuccessResponse<Account> successResponse = 
                new JsonUtil.SuccessResponse<>("Account updated successfully", savedAccount);
            JsonUtil.sendJsonResponse(response, successResponse);
            
        } catch (Exception e) {
            JsonUtil.sendJsonError(response, "Failed to update account: " + e.getMessage(), 500);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            JsonUtil.sendJsonError(response, "Account number is required in URL", 400);
            return;
        }
        
        try {
            String accountNumber = pathInfo.substring(1);
            
            // Check if account exists
            Account existingAccount = accountDAO.findByAccountNumber(accountNumber);
            if (existingAccount == null) {
                JsonUtil.sendJsonError(response, "Account not found", 404);
                return;
            }
            
            // Delete account
            boolean deleted = accountDAO.delete(accountNumber);
            
            if (deleted) {
                JsonUtil.SuccessResponse<String> successResponse = 
                    new JsonUtil.SuccessResponse<>("Account deleted successfully", accountNumber);
                JsonUtil.sendJsonResponse(response, successResponse);
            } else {
                JsonUtil.sendJsonError(response, "Failed to delete account", 500);
            }
            
        } catch (Exception e) {
            JsonUtil.sendJsonError(response, "Failed to delete account: " + e.getMessage(), 500);
        }
    }
}
```

**Create `src\main\java\com\atm\DashboardServlet.java`:**
```java
package com.atm;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private AccountDAO accountDAO = new AccountDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            List<Account> allAccounts = accountDAO.findAll();
            
            // Calculate statistics
            BigDecimal totalBalance = BigDecimal.ZERO;
            int savingsCount = 0;
            int checkingCount = 0;
            
            for (Account account : allAccounts) {
                totalBalance = totalBalance.add(account.getBalance());
                if ("SAVINGS".equalsIgnoreCase(account.getAccountType())) {
                    savingsCount++;
                } else if ("CHECKING".equalsIgnoreCase(account.getAccountType())) {
                    checkingCount++;
                }
            }
            
            // Create dashboard data
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("totalAccounts", allAccounts.size());
            dashboardData.put("totalBalance", totalBalance);
            dashboardData.put("savingsAccounts", savingsCount);
            dashboardData.put("checkingAccounts", checkingCount);
            dashboardData.put("accounts", allAccounts);
            
            JsonUtil.SuccessResponse<Map<String, Object>> successResponse = 
                new JsonUtil.SuccessResponse<>("Dashboard data retrieved successfully", dashboardData);
            JsonUtil.sendJsonResponse(response, successResponse);
            
        } catch (Exception e) {
            JsonUtil.sendJsonError(response, "Failed to retrieve dashboard data: " + e.getMessage(), 500);
        }
    }
}
```

**Create `src\main\java\com\atm\HealthCheckServlet.java`:**
```java
package com.atm;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/health")
public class HealthCheckServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Map<String, Object> healthData = new HashMap<>();
            healthData.put("status", "UP");
            healthData.put("timestamp", System.currentTimeMillis());
            healthData.put("application", "ATM Web Application");
            healthData.put("version", "1.0.0");
            
            JsonUtil.SuccessResponse<Map<String, Object>> successResponse = 
                new JsonUtil.SuccessResponse<>("Application is healthy", healthData);
            JsonUtil.sendJsonResponse(response, successResponse);
            
        } catch (Exception e) {
            JsonUtil.sendJsonError(response, "Health check failed: " + e.getMessage(), 500);
        }
    }
}
```

## Create Configuration Files

### 1. Create web.xml

**Create `src\main\webapp\WEB-INF\web.xml`:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee 
         http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
    
    <display-name>ATM Web Application</display-name>
    <description>Simple ATM Web Application with REST APIs</description>
    
    <!-- Welcome files -->
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>
    
    <!-- Session configuration -->
    <session-config>
        <session-timeout>30</session-timeout>
        <cookie-config>
            <http-only>true</http-only>
            <secure>false</secure>
        </cookie-config>
    </session-config>
    
    <!-- Error pages -->
    <error-page>
        <error-code>404</error-code>
        <location>/error.html</location>
    </error-page>
    
    <error-page>
        <error-code>500</error-code>
        <location>/error.html</location>
    </error-page>
    
</web-app>
```

### 2. Create Welcome Page

**Create `src\main\webapp\index.html`:**
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ATM Web Application</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .header {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
        }
        .api-endpoint {
            background: #f8f9fa;
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 15px;
            margin: 10px 0;
        }
        .method {
            font-weight: bold;
            color: #007bff;
        }
        .endpoint {
            font-family: monospace;
            background: #e9ecef;
            padding: 5px;
            border-radius: 3px;
        }
        .test-button {
            background: #28a745;
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 4px;
            cursor: pointer;
        }
        .test-button:hover {
            background: #218838;
        }
        #result {
            margin-top: 20px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 5px;
            white-space: pre-wrap;
            font-family: monospace;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🏦 ATM Web Application</h1>
            <p>Welcome to the ATM Web Application API</p>
        </div>
        
        <h2>Available API Endpoints</h2>
        
        <div class="api-endpoint">
            <div><span class="method">GET</span> <span class="endpoint">/api/health</span></div>
            <p>Check application health status</p>
            <button class="test-button" onclick="testApi('/api/health')">Test</button>
        </div>
        
        <div class="api-endpoint">
            <div><span class="method">GET</span> <span class="endpoint">/api/dashboard</span></div>
            <p>Get dashboard statistics and all accounts</p>
            <button class="test-button" onclick="testApi('/api/dashboard')">Test</button>
        </div>
        
        <div class="api-endpoint">
            <div><span class="method">GET</span> <span class="endpoint">/api/accounts</span></div>
            <p>Get all accounts</p>
            <button class="test-button" onclick="testApi('/api/accounts')">Test</button>
        </div>
        
        <div class="api-endpoint">
            <div><span class="method">GET</span> <span class="endpoint">/api/accounts/{accountNumber}</span></div>
            <p>Get specific account by number</p>
            <button class="test-button" onclick="testApi('/api/accounts/1001234567')">Test</button>
        </div>
        
        <div class="api-endpoint">
            <div><span class="method">POST</span> <span class="endpoint">/api/accounts</span></div>
            <p>Create new account (JSON body required)</p>
            <button class="test-button" onclick="testCreateAccount()">Test</button>
        </div>
        
        <div id="result"></div>
    </div>
    
    <script>
        function testApi(endpoint) {
            const resultDiv = document.getElementById('result');
            resultDiv.innerHTML = 'Loading...';
            
            fetch(endpoint)
                .then(response => response.json())
                .then(data => {
                    resultDiv.innerHTML = JSON.stringify(data, null, 2);
                })
                .catch(error => {
                    resultDiv.innerHTML = 'Error: ' + error.message;
                });
        }
        
        function testCreateAccount() {
            const resultDiv = document.getElementById('result');
            resultDiv.innerHTML = 'Creating account...';
            
            const newAccount = {
                accountNumber: '1001234570',
                customerName: 'Test User',
                balance: 10000.00,
                accountType: 'SAVINGS'
            };
            
            fetch('/api/accounts', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(newAccount)
            })
            .then(response => response.json())
            .then(data => {
                resultDiv.innerHTML = JSON.stringify(data, null, 2);
            })
            .catch(error => {
                resultDiv.innerHTML = 'Error: ' + error.message;
            });
        }
    </script>
</body>
</html>
```

**Create `src\main\webapp\error.html`:**
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - ATM Application</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            padding: 50px;
            background-color: #f5f5f5;
        }
        .error-container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            max-width: 500px;
            margin: 0 auto;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>🚫 Error</h1>
        <p>Sorry, an error occurred while processing your request.</p>
        <p><a href="/">Return to Home</a></p>
    </div>
</body>
</html>
```

## Compile the Application

### 1. Create Compilation Script

**Create `compile.bat`:**
```batch
@echo off
echo Compiling ATM Web Application...

set JAVA_SRC=src\main\java
set BUILD_DIR=build\classes
set LIB_DIR=lib
set TOMCAT_LIB=%CATALINA_HOME%\lib

echo Cleaning build directory...
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
mkdir %BUILD_DIR%

echo Setting up classpath...
set CLASSPATH=%LIB_DIR%\*;%TOMCAT_LIB%\servlet-api.jar;%TOMCAT_LIB%\jsp-api.jar

echo Compiling Java files...
javac -d %BUILD_DIR% -cp "%CLASSPATH%" -sourcepath %JAVA_SRC% %JAVA_SRC%\com\atm\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
pause
```

**Run compilation:**
```cmd
cd C:\JavaWebApps\ATM-WebApp
compile.bat
```

### 2. Verify Compilation

**Check compiled classes:**
```cmd
dir build\classes\com\atm
```

You should see:
```
Account.class
AccountDAO.class
AccountServlet.class
DashboardServlet.class
HealthCheckServlet.class
JsonUtil.class
JsonUtil$ErrorResponse.class
JsonUtil$SuccessResponse.class
```

## Package into WAR

### 1. Create Packaging Script

**Create `package.bat`:**
```batch
@echo off
echo Packaging ATM Web Application into WAR...

set BUILD_DIR=build
set WEB_DIR=src\main\webapp
set WAR_DIR=%BUILD_DIR%\war
set LIB_DIR=lib

echo Creating WAR structure...
if exist %WAR_DIR% rmdir /s /q %WAR_DIR%
mkdir %WAR_DIR%
mkdir %WAR_DIR%\WEB-INF
mkdir %WAR_DIR%\WEB-INF\classes
mkdir %WAR_DIR%\WEB-INF\lib

echo Copying web resources...
xcopy /E /Y %WEB_DIR%\* %WAR_DIR%\

echo Copying compiled classes...
xcopy /E /Y %BUILD_DIR%\classes\* %WAR_DIR%\WEB-INF\classes\

echo Copying JAR dependencies...
copy %LIB_DIR%\*.jar %WAR_DIR%\WEB-INF\lib\

echo Creating WAR file...
cd %WAR_DIR%
jar -cvf ..\ATM-WebApp.war *
cd ..\..\..

echo WAR file created successfully: %BUILD_DIR%\ATM-WebApp.war
dir %BUILD_DIR%\ATM-WebApp.war

pause
```

**Run packaging:**
```cmd
cd C:\JavaWebApps\ATM-WebApp
package.bat
```

### 2. Verify WAR File

**Check WAR contents:**
```cmd
cd build
jar -tvf ATM-WebApp.war
```

## Deploy to Tomcat

### 1. Deploy WAR File

**Copy WAR to Tomcat:**
```cmd
copy C:\JavaWebApps\ATM-WebApp\build\ATM-WebApp.war %CATALINA_HOME%\webapps\
```

### 2. Start Tomcat

**Start Tomcat server:**
```cmd
%CATALINA_HOME%\bin\startup.bat
```

**Wait for deployment (about 10-30 seconds), then check:**
```cmd
dir %CATALINA_HOME%\webapps\
```

You should see both:
- `ATM-WebApp.war` (the WAR file)
- `ATM-WebApp\` (the extracted directory)

### 3. Create Complete Deployment Script

**Create `deploy.bat`:**
```batch
@echo off
echo Deploying ATM Web Application to Tomcat...

echo Step 1: Compiling application...
call compile.bat

echo Step 2: Packaging into WAR...
call package.bat

echo Step 3: Stopping Tomcat (if running)...
%CATALINA_HOME%\bin\shutdown.bat
timeout 10

echo Step 4: Removing old deployment...
if exist %CATALINA_HOME%\webapps\ATM-WebApp rmdir /s /q %CATALINA_HOME%\webapps\ATM-WebApp
if exist %CATALINA_HOME%\webapps\ATM-WebApp.war del %CATALINA_HOME%\webapps\ATM-WebApp.war

echo Step 5: Deploying new WAR...
copy build\ATM-WebApp.war %CATALINA_HOME%\webapps\

echo Step 6: Starting Tomcat...
start %CATALINA_HOME%\bin\startup.bat

echo Step 7: Waiting for deployment...
timeout 15

echo Deployment completed!
echo Application should be available at: http://localhost:8080/ATM-WebApp/

echo Opening browser...
start http://localhost:8080/ATM-WebApp/

pause
```

**Run complete deployment:**
```cmd
cd C:\JavaWebApps\ATM-WebApp
deploy.bat
```

## Test the Application

### 1. Test Web Interface

**Open browser and navigate to:**
```
http://localhost:8080/ATM-WebApp/
```

You should see the welcome page with API documentation.

### 2. Test API Endpoints

**Test health endpoint:**
```cmd
curl http://localhost:8080/ATM-WebApp/api/health
```

**Expected response:**
```json
{
  "success": true,
  "message": "Application is healthy",
  "data": {
    "status": "UP",
    "timestamp": 1694602200000,
    "application": "ATM Web Application",
    "version": "1.0.0"
  }
}
```

**Test dashboard endpoint:**
```cmd
curl http://localhost:8080/ATM-WebApp/api/dashboard
```

**Test accounts endpoint:**
```cmd
curl http://localhost:8080/ATM-WebApp/api/accounts
```

**Test specific account:**
```cmd
curl http://localhost:8080/ATM-WebApp/api/accounts/1001234567
```

**Test create account:**
```cmd
curl -X POST http://localhost:8080/ATM-WebApp/api/accounts ^
  -H "Content-Type: application/json" ^
  -d "{\"accountNumber\":\"1001234571\",\"customerName\":\"New Customer\",\"balance\":5000.00,\"accountType\":\"CHECKING\"}"
```

### 3. Create Test Script

**Create `test-app.bat`:**
```batch
@echo off
echo Testing ATM Web Application...

set BASE_URL=http://localhost:8080/ATM-WebApp/api

echo Testing Health Check...
curl %BASE_URL%/health
echo.

echo Testing Dashboard...
curl %BASE_URL%/dashboard
echo.

echo Testing All Accounts...
curl %BASE_URL%/accounts
echo.

echo Testing Specific Account...
curl %BASE_URL%/accounts/1001234567
echo.

echo Testing Account Creation...
curl -X POST %BASE_URL%/accounts ^
  -H "Content-Type: application/json" ^
  -d "{\"accountNumber\":\"1001234572\",\"customerName\":\"Test Customer\",\"balance\":15000.00,\"accountType\":\"SAVINGS\"}"
echo.

echo All tests completed!
pause
```

**Run tests:**
```cmd
cd C:\JavaWebApps\ATM-WebApp
test-app.bat
```

## Troubleshooting

### 1. Common Issues

**Compilation Errors:**
```cmd
# Check Java version
java -version
javac -version

# Check JAVA_HOME
echo %JAVA_HOME%

# Check Tomcat installation
echo %CATALINA_HOME%
dir %CATALINA_HOME%\lib\servlet-api.jar
```

**Deployment Issues:**
```cmd
# Check Tomcat logs
type %CATALINA_HOME%\logs\catalina.out
type %CATALINA_HOME%\logs\localhost.log

# Check if application is deployed
dir %CATALINA_HOME%\webapps\ATM-WebApp\

# Check if Tomcat is running
netstat -ano | findstr :8080
```

**API Not Working:**
```cmd
# Test if Tomcat is running
curl http://localhost:8080

# Test if application is deployed
curl http://localhost:8080/ATM-WebApp/

# Check application logs
type %CATALINA_HOME%\logs\ATM-WebApp.log
```

### 2. Debug Script

**Create `debug.bat`:**
```batch
@echo off
echo Debugging ATM Web Application...

echo Java Version:
java -version
echo.

echo JAVA_HOME:
echo %JAVA_HOME%
echo.

echo CATALINA_HOME:
echo %CATALINA_HOME%
echo.

echo Checking Tomcat process:
tasklist | findstr java
echo.

echo Checking port 8080:
netstat -ano | findstr :8080
echo.

echo Checking deployment:
dir %CATALINA_HOME%\webapps\ATM-WebApp\
echo.

echo Checking logs:
if exist %CATALINA_HOME%\logs\catalina.out (
    echo Last 20 lines of catalina.out:
    powershell "Get-Content '%CATALINA_HOME%\logs\catalina.out' -Tail 20"
) else (
    echo catalina.out not found
)

pause
```

**Run debug script:**
```cmd
debug.bat
```

This complete guide creates a functional Java web application with REST APIs and deploys it to Tomcat. The application includes account management functionality with full CRUD operations, proper JSON responses, and a web interface for testing.