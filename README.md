## **📌 32Bit SAU2025-BE Project**

# SAU2025-BE: Real-Time Forex Data Processing System

## 📖 Overview

**32Bit SAU2025-BE** is a backend application designed to fetch, process, and distribute real-time foreign exchange (
forex)
rates. The system integrates with multiple financial data platforms, cleans incoming data, performs calculations, and
provides structured forex data. It also supports real-time monitoring and logging.

### **🎯 Key Functionalities**

- ✅ **Multi-Platform Data Fetching:** Supports TCP and REST API-based data providers.
- ✅ **Real-Time Data Processing:** Aggregates BID & ASK rates and calculates derived currency values.
- ✅ **Data Cleaning & Validation:** Applies a **1% tolerance rule** to remove anomalies.
- ✅ **Caching:** Uses **Redis** for storing raw and processed raw rate data.
- ✅ **Logging & Monitoring:** Uses **Log4j2** for logging, with filebeat & logstash inserting into OpenSearch.
- ✅ **Dynamic Calculation of Rates:** Supports dynamic computation using JavaScript, Python.

---

## ⚙️ System Architecture

### **📌 Data Flow**

1. **Platform 1 (PF1 - TCP Stream):**
    - Provides raw rates via **TCP socket streaming**.
2. **Platform 2 (PF2 - REST API):**
    - Provides raw rates via **HTTP REST API**.
3. **Main Application:**
    - Fetches, cleans, and processes raw data via dynamically loaded methods from **Javascript & Python**.
    - Stores raw rates in **cache (Redis)** for quick access.
    - Publishes raw rate & calculated rate to **Kafka** for storage and further analysis.
4. **Kafka Postgres Consumer:**
    - Consumes raw rate data and inserts it into **PostgreSQL**.
5. **Kafka Opensearch Consumer:**
    - Consumes raw rate & calculated rate data and inserts them into **OpenSearch**.

> **Note:** Calculation formulas are **dynamically loaded** and can be changed via **coordinator configuration** (
> JavaScript, Python).

---

## 🛠️ Technologies Used

| Component                 | Technology            |
|---------------------------|-----------------------|
| **Backend Framework**     | Java (Spring Boot)    |
| **Streaming & Messaging** | Kafka                 |
| **Database**              | PostgreSQL            |
| **Caching**               | Redis                 |
| **Logging**               | Log4j2, OpenSearch    |
| **Log Management**        | Filebeat & Logstash   |
| **Monitoring**            | OpenSearch Dashboards |

---

## 📜 Logging & Alerts

The application logs various events:

## 📡 Platform Simulation

The project includes **two platforms** for raw rate.
They both simulate **live raw rate streaming**.

### **1️⃣ [TCP-Based Platform](https://github.com/Luieitalian/TCPPlatform)**

- Provides raw data via TCP connection.
- Can be tested via **Telnet**

```
name=USDTRY|bid=34.44|ask=35.43|timestamp=2024-12-14T21:18:21
```

### **2️⃣ [REST API-Based Platform](https://github.com/Luieitalian/RestPlatform)**

- Provides raw data via REST API.

```json
{
  "name": "USDTRY",
  "bid": 34.44,
  "ask": 35.43,
  "timestamp": "2024-12-14T21:18:21"
}
```

---

## 📦 Kafka Producer

The Kafka Producer:

- Writes raw rate and calculated rate data to **Kafka topics**.

---