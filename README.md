# 🍔 FoodOrder — Full-stack E-commerce Food Ordering Platform

A full-stack food e-commerce platform built with **Spring Boot, ReactJS, MySQL, and JWT Authentication**.

The project focuses on real-world backend development, including authentication and authorization, product and order management, online payment, real-time communication, email notifications, AI-powered search/recommendation, cloud image storage, and automated testing.

> **Project Type:** Full-stack E-commerce Platform  
> **Backend:** Spring Boot 3.5.7 + Java 21  
> **Frontend:** ReactJS + Vite  
> **Database:** MySQL 8.4

---

## 📌 Overview

**FoodOrder** is an online food ordering and e-commerce system that allows customers to browse products, manage their cart, place orders, make online payments, and communicate with administrators in real time.

The system also provides an **admin management interface** for managing products, categories, users, orders, and customer conversations.

The project was developed to practice and demonstrate practical skills in:

- RESTful API development
- Spring Boot architecture
- Spring Security & JWT
- Database design with JPA/Hibernate
- Payment integration
- WebSocket real-time communication
- Email notification
- Cloud image storage
- AI API integration
- Unit testing

---

## ✨ Key Features

### 👤 Authentication & Authorization

- User registration and login
- JWT-based authentication
- Access Token / Refresh Token
- Secure password handling
- Role-based authorization
- Protected REST APIs
- Token versioning for password changes and token invalidation

### 🛍️ Product & Category Management

- Create, update, delete products
- Product search and filtering
- Category management
- Product status management
- Product image management
- Product quantity and stock tracking
- Cloud image upload using **Cloudinary**

### 🛒 Shopping Cart

- Add products to cart
- Update product quantity
- Remove products from cart
- Clear cart after successful payment
- Support for guest and authenticated users

### 📦 Order Management

- Create and manage orders
- Order status tracking
- Order detail management
- Shipping address management
- Order cancellation
- Voucher/discount support
- Order history
- Admin order management

# 🧰 Tech Stack

## Backend

| Technology        | Purpose                        |
| ----------------- | ------------------------------ |
| Java 21           | Programming language           |
| Spring Boot 3.5.7 | Backend framework              |
| Spring Web        | RESTful API                    |
| Spring Data JPA   | Database access                |
| Hibernate         | ORM                            |
| Spring Security   | Authentication & authorization |
| JWT               | Token-based authentication     |
| MySQL 8.4         | Relational database            |
| Gradle            | Build tool                     |
| Lombok            | Reduce boilerplate             |
| Swagger / OpenAPI | API documentation              |
| JUnit 5           | Unit testing                   |
| Mockito           | Mocking dependencies           |

## External Services

| Service              | Purpose                             |
| -------------------- | ----------------------------------- |
| PayOS                | Online payment                      |
| Cloudinary           | Image/file upload and cloud storage |
| Groq API             | AI-powered search/recommendation    |
| SMTP / Email Service | Invoice email delivery              |

# ⚙️ Getting Started

## 1. Clone the repository

```bash
git clone https://github.com/thanhht05/food-order-backend.git
```

---

## 2. Backend Configuration

Create your environment configuration with values similar to:

```properties
DB_URL=jdbc:mysql://localhost:3306/food_order
DB_USERNAME=root
DB_PASSWORD=your_password

JWT_SECRET=your_jwt_secret

PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

GROQ_API_KEY=your_api_key

MAIL_USERNAME=your_email
MAIL_PASSWORD=your_email_password
```

> Never commit real credentials or secret keys to GitHub.

---

# 🚀 What I Learned

Through this project, I gained practical experience in:

- Designing RESTful APIs with Spring Boot
- Applying layered architecture
- Implementing authentication with Spring Security and JWT
- Designing database relationships with JPA/Hibernate
- Handling online payment and webhook callbacks
- Building real-time applications with WebSocket
- Integrating third-party APIs
- Implementing cloud-based image storage
- Writing unit tests with JUnit 5 and Mockito
- Managing frontend state with Redux Toolkit
- Debugging and integrating multiple services in a full-stack application

---

# 📈 Future Improvements

Planned improvements include:

- [ ] Add more integration tests
- [ ] Improve automated CI/CD pipeline
- [ ] Add product review and rating
- [ ] Improve recommendation accuracy
- [ ] Add more advanced analytics
- [ ] Improve application monitoring and logging
- [ ] Optimize database queries and API performance

---

# 👨‍💻 Author

**Thanh Huu**

Software Engineering Student | Java Backend Developer

# 📄 License

This project is created for **learning, portfolio, and demonstration purposes**.
