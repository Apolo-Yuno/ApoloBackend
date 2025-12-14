# 🚀 ApoloBackend

**Backend for the Apolo project (Yuno Hackathon)** — A Spring Boot service that integrates Artificial Intelligence capabilities for risk profile analysis.

---

## 👥 Team

* **Juan Sebastian Puentes Julio**
* **Daniel Patiño Mejia**
* **Tulio Riaño Sánchez**
* **Isaac Burgos**

---

## 🎨 Design & Architecture

This section illustrates the system architecture. The images are stored in `docs/diagrams/`.

### 🧩 General Component Diagram

![General Component Diagram](docs/DiagramaDeArquitecturaGeneral.png)

This high-level architecture diagram outlines the core components of our solution. We leverage external APIs for AI capabilities and integrations with platforms such as Slack and email. A non-relational database is utilized to efficiently handle high data volumes and flexible data structures.

### ⚙️ Specific Component Diagram

![Specific Component Diagram](docs/DiagramaDeComponentesEspecifico.png)

This diagram details the specific services involved in the implementation. The central component is the Merchant Service, which orchestrates merchant creation, manages interactions, and coordinates calls to AI services.

### 🗄️ Database Model


![Database Diagram](docs/DiagramaBaseDeDatos.png)

Our data model is implemented using MongoDB to handle document-based storage. We utilize a combination of embedded and referenced documents to optimize performance and data integrity:

*   **Embedded:** Merchant Context
*   **Referenced:** Interactions

### 📦 Class Diagram

![Class Diagram](docs/DiagramaDeClases.png)

The class diagram illustrates the relationships between entities, enumerations, and the design patterns applied:

- **Builder Pattern:** Used for constructing complex objects like `Merchant`, `MerchantContext`, and `Interactions`, allowing for flexible object creation with varying attributes.

- **Facade Pattern:** Implemented to simplify file format conversions, providing a unified interface to delegate tasks to specific implementation classes.

---

## ℹ️ General Overview

This repository contains a Spring Boot backend (Java 17) that exposes REST endpoints and integrates with external AI services. It uses MongoDB for persistence and exposes API documentation via Swagger/OpenAPI.

### 🛠️ Technical Details
* **Java:** 17 ☕
* **Framework:** Spring Boot 🍃
* **Build Tool:** Maven 🐘
* **Database:** MongoDB 🍃
* **API Documentation:** Swagger UI (springdoc) 📜

---

## 📋 Prerequisites

To run this project you need:

1.  **Java 17** or newer.
2.  **Maven 3.6+**.
3.  A **MongoDB** instance (local or cloud).

---

## ⚙️ Configuration

The application reads configuration from `src/main/resources/application.properties` by default.

> **⚠️ IMPORTANT:** Sensitive values (API Keys, DB credentials) should **never** be committed to the repository. It is recommended to use environment variables.

**Key Properties:**
* `spring.data.mongodb.uri`: MongoDB connection URI.
* `openai.api.key`: OpenAI API Key.
* `gladia.api.key`: Gladia API Key.

### Running with Environment Variables (Recommended)

#### 1️⃣ Using Maven (Dev)

```bash
export OPENAI_API_KEY="your-openai-key"
export GLADIA_API_KEY="your-gladia-key"
export MONGODB_URI="your-mongodb-uri"

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dopenai.api.key=$OPENAI_API_KEY -Dgladia.api.key=$GLADIA_API_KEY -Dspring.data.mongodb.uri=$MONGODB_URI"

```

#### 2️⃣ Build and Run JAR (Prod)

```bash
mvn clean package -DskipTests

java -Dopenai.api.key="$OPENAI_API_KEY" -Dgladia.api.key="$GLADIA_API_KEY" -Dspring.data.mongodb.uri="$MONGODB_URI" -jar target/ApoloBackend-0.0.1-SNAPSHOT.jar

```

### 🚀 Common Commands

- Build (with tests)

```bash
mvn clean package
```

- Build (skip tests)

```bash
mvn clean package -DskipTests
```

- Run Tests

```bash
mvn test
```