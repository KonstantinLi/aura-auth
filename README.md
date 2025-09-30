<h1 align="center">AURA Auth</h1>

<p align="center">

<img alt="GitHub issues" src="https://img.shields.io/github/issues-raw/KonstantinLi/aura-auth">
<img alt="GitHub watchers" src="https://img.shields.io/github/watchers/KonstantinLi/aura-auth">
<img alt="GitHub contributors" src="https://img.shields.io/github/contributors/KonstantinLi/aura-auth">
<img alt="GitHub labels" src="https://img.shields.io/github/labels/KonstantinLi/aura-auth/help%20wanted">
<img alt="GitHub labels" src="https://img.shields.io/github/labels/KonstantinLi/aura-auth/invalid">
<img alt="GitHub labels" src="https://img.shields.io/github/labels/KonstantinLi/aura-auth/bug">

</p>

**AURA Auth** is the authentication microservice responsible for user registration, login, and adaptive authentication. It integrates with `aura-risk` for step-up verification and provides secure session management with JWT and OAuth2 support. This service is a core component of a high-availability, microservices-based real-time system.

---

## Features

* **User Registration & Login**: Supports email/password registration, login, and password management.
* **Two-Factor Authentication (TOTP)**: Optional MFA for enhanced account security.
* **OAuth2 Integration**: Supports external identity providers.
* **Adaptive Security Settings**: Users can manage connected devices and enforce two-factor authentication.
* **Session Management**: Stateless JWT tokens combined with step-up risk context from `aura-risk`.
* **Risk-Aware Authentication**: Integrates with `aura-risk` to enforce step-up verification based on user/device risk profile.

---

## Architecture

```
User <───> REST API / Web → AuthController
          │
          ├─▶ AuthService
          │   ├─ Password validation
          │   ├─ MFA / TOTP handling
          │   ├─ JWT token generation
          │   └─ Step-up integration with aura-risk
          │
          ├─▶ Repositories (User, Token, Device)
          │
          └─▶ External integrations: OAuth2, Vault
```

* **Config**: Application, security, JWT, OAuth2 settings
* **Controller**: REST endpoints for registration, login, device management, and MFA
* **DTO**: Data transfer objects for requests/responses
* **Converter/Mapper**: Mapping between entities and DTOs
* **Repository**: CRUD operations on users, devices, sessions
* **Service**: Core authentication, MFA, and risk-aware logic
* **Exception/Handler**: Unified error handling

---

## Configuration

Configurable parameters in `application.yml` include:

* Database and Redis connection settings
* JWT expiration and signing keys
* OAuth2 client credentials
* MFA/TOTP policies
* Vault secret paths

---

## Security

* Passwords are stored securely using hashing (BCrypt)
* JWT tokens are stateless and can be validated without database hits
* Step-up authentication integrates with `aura-risk` for real-time risk evaluation
* Device trust and session revocation support

---

## Observability

* Metrics collection via Micrometer/Prometheus
* Logging for authentication attempts, errors, and step-up events
* Health checks for database, Redis, and Vault integration