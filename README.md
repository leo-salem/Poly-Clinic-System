<img width="2254" height="1722" alt="Poly-Clinic-System drawio" src="https://github.com/user-attachments/assets/24fe19a2-781f-49ff-95bf-b3b2f7c0b46b" />

# Poly Clinic System  
A production-grade **distributed microservices platform** for medical clinic management — built using enterprise patterns, secure identity management, event-driven workflows, and financial-grade payment logic.

---

##  System Overview
| Aspect | Description |
|--------|-------------|
| **Type** | Production-ready distributed microservices system |
| **Domain** | Medical clinic management |
| **Architecture** | Microservices (sync + async) |
| **Scale** | Enterprise-level, not a demo |

---

##  Architecture & Technologies
| Component | Technology | Purpose |
|-----------|------------|---------|
| Framework | Spring Boot 3.2+, Spring Cloud | Microservices foundation |
| Event Streaming | Kafka + Outbox Pattern | Reliable async communication |
| Database | PostgreSQL (isolated schemas) | Persistent storage |
| ORM | Hibernate (advanced inheritance) | Complex domain modeling |
| Caching / Locks | Redis | Distributed locking & concurrency control |
| IAM | Keycloak | Authentication & RBAC |
| Payments | Stripe | Financial operations |
| Containerization | Docker + Docker Compose | Full environment orchestration |

---

##  Core Technical Features
| Category | Implementation | Benefit |
|----------|----------------|---------|
| Distributed Communication | REST + Feign (auto JWT) + Kafka (manual ack) | Hybrid communication with reliability |
| Security | API Gateway Token Converter + Keycloak Admin API | Enterprise IAM & RBAC |
| Data Modeling | Hibernate inheritance & mapping | Rich domain modeling |
| Financial Safety | Stripe manual capture + Redis locks | Prevents race conditions |
| Event Processing | Kafka + Outbox + Notification Service | Exactly-once delivery |
| System Maintenance | Cleanup Scheduler | Long-term stability |
| Mapping | MapStruct + Builder mappers | High-performance mapping |
| Error Handling | Global Exception Handler + Custom Exceptions | User-friendly & maintainable |

---

##  Microservices Breakdown
### **API Gateway**
- Routing + centralized security  
- Custom token converter  
- Role extraction before routing  

### **User Management Service**
- User lifecycle  
- Keycloak Admin API integration  
- Auto-admin creation  

### **Appointment Service**
- Booking & availability  
- Redis distributed locks  
- Payment integration  

### **Payment Service**
- Stripe PaymentIntent lifecycle  
- Manual capture flow  
- Idempotent transactions  

### **Notification Service**
- Kafka consumers  
- Email delivery  
- Event logging & tracing  

### **Prescription Service**
- Medical record management  
- Role-based access  

---

##  Security Architecture
- Centralized IAM via **Keycloak**
- Custom gateway **Token Converter**
- Secure inter-service calls via **Feign JWT Interceptor**
- Centralized **Token Service** for role/user extraction  
- RBAC for: Admin, Doctor, Nurse, Patient

---

##  Payment Flow (Stripe Manual Capture)
1. Patient reserves appointment  
2. Backend creates PaymentIntent  
3. Payment tested via Stripe CLI  
4. Server-to-server confirmation  
5. Admin manually captures payment  
6. Kafka → Notification → Email  

---

##  Development Environment
| Component | Access |
|----------|---------|
| API Gateway | http://localhost:8080 |
| Keycloak Admin | http://localhost:8443 |
| Eureka Dashboard | http://localhost:8761 |
| Kafka UI | http://localhost:8085 |

---

This project includes visual and technical documentation stored in the docs/ directory which is 
- API endpoints (Swagger)
- Project & Service ERDs
- Stripe payment flows
- Keycloak configurations

and these some examples of them 

![WhatsApp Image 2025-12-02 at 07 56 51_5672261f](https://github.com/user-attachments/assets/c158288e-718b-4f9b-915b-110b2e5cb667)

![WhatsApp Image 2025-12-02 at 08 14 20_44d453c4](https://github.com/user-attachments/assets/7fbac95b-effc-4a04-ad7b-59bb5b76ba29)

![WhatsApp Image 2025-12-02 at 07 40 55_e19e9461](https://github.com/user-attachments/assets/633e5843-4657-4c41-8e3c-7f3af3422906)

Start entire system:
```bash
docker-compose up -d
