# 🍔 Food Delivery Backend — Production-Grade Spring Boot API
> Modelled after Swiggy / Zomato backends

---

## Tech Stack

| Layer          | Technology                              |
|----------------|-----------------------------------------|
| Language       | Java 17                                 |
| Framework      | Spring Boot 3.2                         |
| Security       | Spring Security + JWT (JJWT 0.11)       |
| Persistence    | Spring Data JPA + Hibernate             |
| Database       | MySQL 8+                                |
| Real-time      | WebSocket (STOMP + SockJS)              |
| Build          | Maven                                   |
| Utilities      | Lombok, MapStruct                       |

---

## Project Structure

```
src/main/java/com/fooddelivery/
├── FoodDeliveryApplication.java
├── config/
│   ├── SecurityConfig.java          # JWT + RBAC setup
│   └── WebSocketConfig.java         # STOMP broker + JWT interceptor
├── controller/
│   ├── AuthController.java
│   ├── RestaurantController.java
│   ├── MenuItemController.java
│   ├── CartController.java
│   ├── OrderController.java
│   └── DeliveryController.java
├── dto/
│   ├── request/                     # Validated inbound payloads
│   └── response/                    # Clean outbound DTOs
├── entity/                          # JPA entities
│   ├── BaseEntity.java              # Auditing (createdAt, updatedAt)
│   ├── User.java
│   ├── Restaurant.java
│   ├── MenuItem.java
│   ├── Cart.java  /  CartItem.java
│   ├── Order.java / OrderItem.java
│   ├── DeliveryPartner.java
│   └── Delivery.java
├── enums/
│   ├── Role.java                    # CUSTOMER | ADMIN | DELIVERY_PARTNER
│   ├── OrderStatus.java             # PLACED → CONFIRMED → PREPARING → ... → DELIVERED
│   ├── DeliveryStatus.java          # ASSIGNED → ACCEPTED → PICKED_UP → DELIVERED
│   ├── FoodCategory.java            # VEG | NON_VEG | VEGAN | EGG
│   └── PaymentStatus.java           # PENDING | SUCCESS | FAILED | REFUNDED
├── exception/
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── UnauthorizedException.java
├── repository/                      # Spring Data JPA interfaces
├── security/
│   ├── JwtUtils.java                # Token generation / validation
│   ├── JwtAuthenticationFilter.java # Per-request filter
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── AuthService.java
│   ├── RestaurantService.java
│   ├── MenuItemService.java
│   ├── CartService.java
│   ├── OrderService.java
│   ├── DeliveryService.java
│   └── PaymentService.java          # Dummy → extendable to Razorpay
└── websocket/
    ├── LocationWebSocketController.java
    └── WebSocketAuthChannelInterceptor.java
```

---

## Database Setup

```sql
CREATE DATABASE food_delivery_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'foodapp'@'localhost' IDENTIFIED BY 'strongpassword';
GRANT ALL PRIVILEGES ON food_delivery_db.* TO 'foodapp'@'localhost';
FLUSH PRIVILEGES;
```

Update `application.properties`:
```properties
spring.datasource.username=foodapp
spring.datasource.password=strongpassword
```

---

## Running the Application

```bash
# Clone & build
mvn clean install -DskipTests

# Run
mvn spring-boot:run

# Or with JAR
java -jar target/food-delivery-1.0.0.jar
```

App starts at: `http://localhost:8080/api`

---

## API Reference & Sample Requests

### 🔐 Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "secret123",
  "phone": "9876543210",
  "address": "221B Baker Street, New Delhi",
  "role": "CUSTOMER"
}
```
**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "name": "Rahul Sharma",
      "email": "rahul@example.com",
      "role": "CUSTOMER"
    }
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{ "email": "rahul@example.com", "password": "secret123" }
```

---

### 🍽️ Restaurants

#### Get All Open Restaurants (Public)
```http
GET /api/restaurants
```
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Biryani Blues",
      "city": "Delhi",
      "cuisineType": "Mughlai",
      "rating": 4.5,
      "avgDeliveryTime": 35,
      "minOrderAmount": 200.0,
      "open": true
    }
  ]
}
```

#### Search Restaurants
```http
GET /api/restaurants/search?q=pizza
```

#### Add Restaurant (Admin only)
```http
POST /api/restaurants
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Pizza Palace",
  "address": "42 MG Road",
  "city": "Bangalore",
  "cuisineType": "Italian",
  "phone": "9112345678",
  "avgDeliveryTime": 30,
  "minOrderAmount": 150.0
}
```

---

### 🍕 Menu

#### Get Restaurant Menu (Public)
```http
GET /api/menu/restaurant/1
```
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "description": "Classic tomato and mozzarella",
      "price": 299.0,
      "category": "VEG",
      "menuCategory": "Pizzas",
      "available": true
    }
  ]
}
```

#### Filter by category
```http
GET /api/menu/restaurant/1/category/VEG
```

#### Add Menu Item (Admin only)
```http
POST /api/menu/restaurant/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "Chicken Biryani",
  "description": "Hyderabadi dum biryani",
  "price": 349.0,
  "category": "NON_VEG",
  "menuCategory": "Biryani"
}
```

---

### 🛒 Cart

All cart endpoints require `Authorization: Bearer <customer_token>`

#### Add to Cart
```http
POST /api/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{ "menuItemId": 1, "quantity": 2 }
```
```json
{
  "success": true,
  "message": "Item added to cart",
  "data": {
    "restaurantName": "Pizza Palace",
    "items": [
      {
        "menuItemName": "Margherita Pizza",
        "unitPrice": 299.0,
        "quantity": 2,
        "subtotal": 598.0
      }
    ],
    "totalPrice": 598.0
  }
}
```

#### Update Quantity
```http
PUT /api/cart/items/1?quantity=3
Authorization: Bearer <token>
```

#### Remove Item
```http
DELETE /api/cart/items/1
Authorization: Bearer <token>
```

#### Clear Cart
```http
DELETE /api/cart
Authorization: Bearer <token>
```

---

### 📦 Orders

#### Place Order
```http
POST /api/orders
Authorization: Bearer <customer_token>
Content-Type: application/json

{
  "deliveryAddress": "Flat 4B, Sunshine Apartments, Koramangala, Bangalore",
  "paymentMethod": "UPI",
  "paymentToken": "upi_success_token_xyz",
  "specialInstructions": "Extra chutney please"
}
```
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 101,
    "status": "PLACED",
    "totalAmount": 598.0,
    "paymentStatus": "SUCCESS",
    "paymentMethod": "UPI",
    "restaurantName": "Pizza Palace",
    "orderItems": [
      {
        "menuItemName": "Margherita Pizza",
        "quantity": 2,
        "priceAtOrderTime": 299.0,
        "subtotal": 598.0
      }
    ]
  }
}
```

#### Get Order History
```http
GET /api/orders/my
Authorization: Bearer <customer_token>
```

#### Update Order Status (Admin)
```http
PATCH /api/orders/101/status?status=PREPARING
Authorization: Bearer <admin_token>
```

Valid status flow:
```
PLACED → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
```

#### Cancel Order
```http
DELETE /api/orders/101/cancel
Authorization: Bearer <customer_token>
```

---

### 🚴 Delivery

#### Assign Delivery Partner (Admin)
```http
POST /api/delivery/assign/101/partner/5
Authorization: Bearer <admin_token>
```

#### Accept/Reject Delivery (Partner)
```http
POST /api/delivery/partner/order/101/action
Authorization: Bearer <partner_token>
Content-Type: application/json

{ "action": "ACCEPT" }
```
or
```json
{ "action": "REJECT", "rejectionReason": "Too far from pickup" }
```

#### Mark Picked Up (Partner)
```http
POST /api/delivery/partner/order/101/pickup
Authorization: Bearer <partner_token>
```

#### Mark Delivered (Partner)
```http
POST /api/delivery/partner/order/101/deliver
Authorization: Bearer <partner_token>
```

#### Update Location via REST (Partner, fallback)
```http
POST /api/delivery/partner/order/101/location
Authorization: Bearer <partner_token>
Content-Type: application/json

{ "latitude": 12.9716, "longitude": 77.5946 }
```

---

### 📡 Real-time Tracking (WebSocket)

#### Connect
```javascript
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: 'Bearer <jwt_token>' },
  () => {
    // Customer: subscribe to order location updates
    stompClient.subscribe('/topic/location/101', (message) => {
      const location = JSON.parse(message.body);
      console.log(`Lat: ${location.latitude}, Lng: ${location.longitude}`);
    });

    // Delivery partner: publish location
    stompClient.send('/app/location/101', {}, JSON.stringify({
      latitude: 12.9716,
      longitude: 77.5946
    }));
  }
);
```

#### LocationMessage (broadcast payload)
```json
{
  "orderId": 101,
  "deliveryPartnerId": 5,
  "latitude": 12.9716,
  "longitude": 77.5946,
  "status": "PICKED_UP",
  "timestamp": "2024-03-15T14:30:00"
}
```

---

## Error Responses

All errors follow the same `ApiResponse` shape:

```json
{
  "success": false,
  "message": "Cart already has items from another restaurant. Clear cart first.",
  "timestamp": "2024-03-15T14:30:00"
}
```

### Validation Error (400)
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Invalid email format",
    "phone": "Invalid Indian phone number"
  }
}
```

### HTTP Status Codes
| Status | Meaning                              |
|--------|--------------------------------------|
| 200    | Success                              |
| 201    | Created                              |
| 400    | Bad Request / Validation error       |
| 401    | Unauthorized (missing/invalid token) |
| 403    | Forbidden (insufficient role)        |
| 404    | Resource not found                   |
| 500    | Internal server error                |

---

## Role-Based Access Control

| Endpoint Group         | CUSTOMER | ADMIN | DELIVERY_PARTNER |
|------------------------|----------|-------|------------------|
| GET /restaurants       | ✅        | ✅     | ✅                |
| POST/PUT/DELETE /restaurants | ❌  | ✅     | ❌                |
| GET /menu              | ✅        | ✅     | ✅                |
| POST/PUT/DELETE /menu  | ❌        | ✅     | ❌                |
| /cart/**               | ✅        | ❌     | ❌                |
| POST /orders           | ✅        | ❌     | ❌                |
| GET /orders (all)      | ❌        | ✅     | ❌                |
| PATCH /orders/*/status | ❌        | ✅     | ❌                |
| /delivery/assign/**    | ❌        | ✅     | ❌                |
| /delivery/partner/**   | ❌        | ❌     | ✅                |

---

## Extending Payment to Razorpay

In `PaymentService.java`, replace the dummy block with:

```java
// Add dependency: com.razorpay:razorpay-java:1.4.5
RazorpayClient client = new RazorpayClient(KEY_ID, KEY_SECRET);
JSONObject options = new JSONObject();
options.put("amount", (int)(amount * 100));  // paise
options.put("currency", "INR");
options.put("receipt", "order_" + orderId);
com.razorpay.Order rzpOrder = client.orders.create(options);
// Return rzpOrder.get("id") to frontend → frontend completes payment
// Then verify signature in a /payment/verify endpoint
```

---

## Key Design Decisions

- **Soft deletes** — Restaurants and menu items are never hard-deleted; `active=false` hides them
- **Price snapshot** — `OrderItem.priceAtOrderTime` preserves price history independent of menu changes
- **Single-restaurant cart** — Validated at service layer before any item addition
- **Forward-only order state machine** — `validateStatusTransition()` prevents illegal status jumps
- **WebSocket + REST** — Location updates work via WebSocket; REST endpoint `/location` is a fallback
- **Layered architecture** — Controller → Service → Repository; no JPA leaking into controllers
