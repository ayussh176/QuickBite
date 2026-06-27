# QuickBite — Enterprise Food Delivery Ecosystem

QuickBite is a highly-scalable, enterprise-ready food delivery network linking food lovers (Customers), Merchants (Restaurants), Couriers (Delivery Partners), and Platform Administrators.

The workspace is structured into two main packages:
1. **`backend`**: Decoupled multi-service backend powered by Java 17, Spring Boot 3.x, Hibernate/JPA, RabbitMQ, and Spring WebSocket.
2. **`Frontend`**: Reactive web portal built using React, Redux Toolkit, React Router, and Tailwind CSS.

---

## 📸 Application Interface Preview

Here are screenshots of the QuickBite user experience:

### 1. Welcome Portal
The entrance page of the QuickBite application allows users to access their accounts or register a new one.
![QuickBite Welcome Portal](file:///C:/Users/Ayush%20Malik/.gemini/antigravity-ide/brain/a359b914-ba0a-4f05-a05e-7f62993bef7d/quickbite_homepage_1782548889897.png)

### 2. Multi-Role Authentication
QuickBite features unified authentication, allowing Customers, Restaurants, Delivery Partners, and Admins to select their portal role and authenticate securely.
![Login Page](file:///C:/Users/Ayush%20Malik/.gemini/antigravity-ide/brain/a359b914-ba0a-4f05-a05e-7f62993bef7d/quickbite_login_1782548901344.png)

*Admin interface selection:*
![Admin Login](file:///C:/Users/Ayush%20Malik/.gemini/antigravity-ide/brain/a359b914-ba0a-4f05-a05e-7f62993bef7d/quickbite_login_admin_1782548908927.png)

### 3. User Onboarding & Registration
New members can easily join the platform by providing their profile info and delivery addresses.
![Registration Top](file:///C:/Users/Ayush%20Malik/.gemini/antigravity-ide/brain/a359b914-ba0a-4f05-a05e-7f62993bef7d/quickbite_register_top_1782548916643.png)
![Registration Bottom](file:///C:/Users/Ayush%20Malik/.gemini/antigravity-ide/brain/a359b914-ba0a-4f05-a05e-7f62993bef7d/quickbite_register_bottom_1782548928127.png)

---

## 🛠️ Tech Stack & Architecture

### Backend (Spring Boot)
*   **Core**: Java 17, Spring Boot 3.5.x, Spring Security.
*   **Database**: MySQL with Hibernate/JPA ORM, 26 normalized entities, cascade constraints, and custom indexes.
*   **Cache**: Redis-ready session and API configs.
*   **Event Queue**: RabbitMQ for decoupled notification handling (email, push, SMS).
*   **WebSocket**: STOMP broadcast routing for live GPS coordinate updates.
*   **Documentation**: Swagger/OpenAPI 3 interface mapping 92 REST endpoints.

### Frontend (React & Vite)
*   **Core**: React 19, TypeScript, Vite.
*   **State Management**: Redux Toolkit & TanStack React Query.
*   **Styling**: Tailwind CSS & Lucide Icons.

---

## 🚀 Running the Workspace

### 1. Database & Brokers Setup
Ensure MySQL and RabbitMQ are running locally on their default ports.

### 2. Running the Backend
Navigate to the backend folder and compile/run the application:
```bash
cd backend
./mvnw spring-boot:run
```

### 3. Running the Frontend
Navigate to the Frontend folder, install package dependencies, and run Vite:
```bash
cd Frontend
npm install
npm run dev
```
Open your browser and navigate to `http://localhost:5173/`.
