# QuickBite Platform Remaining Tasks (Launch Roadmap)

## Phase 1: REST API Client Integration
- [ ] Configure Axios instance in [apiClient.ts](file:///D:/QuickBite/Frontend/src/services/api/apiClient.ts) to target backend API URL context.
- [ ] Replace `apiService` mock storage methods with real async Axios request loops on `/api/v1` routes in:
  - [ ] [AuthPages.tsx](file:///D:/QuickBite/Frontend/src/pages/AuthPages.tsx) (Login & Register)
  - [ ] [CustomerPages.tsx](file:///D:/QuickBite/Frontend/src/pages/CustomerPages.tsx) (Catalog, Cart, Checkout, Tracking)
  - [ ] [MerchantPages.tsx](file:///D:/QuickBite/Frontend/src/pages/MerchantPages.tsx) (Menu, Orders Queue, Revenue stats)
  - [ ] [AdminPages.tsx](file:///D:/QuickBite/Frontend/src/pages/AdminPages.tsx) (Approvals & Tickets)
- [ ] Enable Authorization headers interceptor to inject `Bearer <jwt_token>` for authenticated endpoints.

## Phase 2: Missing Views & Portal Completion
- [ ] Implement **Merchant Registration UI** form in [AuthPages.tsx](file:///D:/QuickBite/Frontend/src/pages/AuthPages.tsx).
- [ ] Add the following pages inside the SuperAdmin console in [AdminPages.tsx](file:///D:/QuickBite/Frontend/src/pages/AdminPages.tsx) and map their routes:
  - [ ] **Manage Users** (View users, update role status, toggle accounts)
  - [ ] **Manage Restaurants** (Configure active outlet list, edit outlet profiles)
  - [ ] **Manage Orders** (Global dispatch queue monitoring)

## Phase 3: Real-time Communication & Cache Activation
- [ ] Develop `useWebSocket` connection hook using `@stomp/stompjs` in the React frontend.
- [ ] Bind active order tracking views to receive live status updates via `/user/queue/notifications`.
- [ ] Implement Spring `@Cacheable` annotation markers on high-read service methods (like restaurant lists and menus) to resolve targets from Redis.
- [ ] Wire up consumer listeners for the inactive RabbitMQ queues:
  - [ ] Email Queue (`quickbite.notification.email.queue`)
  - [ ] SMS Queue (`quickbite.notification.sms.queue`)
  - [ ] Order Queue (`quickbite.order.queue`)
  - [ ] Payment Queue (`quickbite.payment.queue`)

## Phase 4: Database Seeding & Security Hardening
- [ ] Create a `data.sql` script to bootstrap metadata, user roles, a default admin, and a baseline menu catalog in MySQL.
- [ ] Implement SMTP settings using `spring-boot-starter-mail` to enable email dispatch from the RabbitMQ listener.
- [ ] Restrict allowed origin patterns from wildcard `*` to specific production domains in [WebSocketConfig.java](file:///D:/QuickBite/Backend/src/main/java/com/quickbite/backend/config/WebSocketConfig.java).
