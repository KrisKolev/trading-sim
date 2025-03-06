# Trading simulator

## Prerequisites

### Backend

- Java 21 (or a compatible JDK)
- Maven
- Internet access (to connect to Kraken WebSocket API)

### Frontend

- Node.js (v14+ recommended)
- npm (or yarn)

## Setup & Running the Project

### 1. Backend (Spring Boot)

1. **Clone the Repository:**  
   Navigate to the `backend` folder:
   ```bash
   cd crypto-trading-sim/backend
2. **Build the project**
    Use Maven
    ```bash
    mvn clean install
3. **Run the Application:**
    Start the Spring Boot application:
    ```bash
    Copy
    mvn spring-boot:run

### 2. Frontend (React)

1. **Clone the Repository:**
Navigate to the frontend folder:
    ```bash
    cd crypto-trading-sim/frontend

2. **Install Dependencies:**
    Install the required npm packages:
    ```bash
    npm install

If you need to install WebSocket libraries for STOMP:
    ```bash
    npm install @stomp/stompjs sockjs-client
    
3. **Run the React App:**
Start the React development server:
    ```bash
    npm start