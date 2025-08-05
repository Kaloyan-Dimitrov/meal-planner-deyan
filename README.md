# Meal Planner (STILL IN PROGRESS!)

**Author:** Deyan Papazov  
**GitHub:** [github.com/DeyanPPapazov](https://github.com/DeyanPPapazov)  
**LinkedIn:** [linkedin.com/in/deyan-papazov](https://www.linkedin.com/in/deyan-papazov)

Weekly meal planning, automatic shopping lists, weight tracking with achievement badges, and a modern dark-mode UI — all in one full-stack web application.

---

## Table of Contents

1. Project Overview
2. Features
3. Tech Stack
4. Getting Started
    - Local Setup
    - One-Click Docker Setup (Optional)
5. Environment Variables
6. Screenshots
8. Contact

---

## Project Overview

*Meal Planner* lets users:

- Set calorie & macro goals depending on their goals
- Generate 1-day or 7-day meal plans via the Spoonacular API
- View a nutrition summary (target vs. actual)
- Auto-create a consolidated shopping list
- Log daily weight and unlock streak-based achievements

The backend is a RESTful Spring Boot service; the frontend is a Vite + React SPA styled with Tailwind CSS.

---

## Features

| Category           | Highlights                                                                                   |
|--------------------|----------------------------------------------------------------------------------------------|
| *Meal Planning*  | API-generated meal plans, regenerate with one click, slot-based (Breakfast / Lunch / Dinner) |
| *Shopping List*  | Unified list of all ingredients needed for the meal plan                                     |
| *User Account*   | JWT authentication (access + refresh tokens), secure password hashing                        |
| **Weight Tracking**| Daily log, streak counter, achievement pop-ups                                               |
| *UX*             | Responsive design, dark-mode toggle, toast notifications for all actions                     |

---

## Tech Stack

| Layer            | Technology                                                                          |
|------------------|-------------------------------------------------------------------------------------|
| *Backend*      | Java 17, Spring Boot 3, PostgreSQL 15, JOOQ 3, Flyway, WebClient                   |
| *Frontend*     | React 18 (Vite), React Router, Tailwind CSS, Headless UI, React Toastify            |
| *Integrations* | Spoonacular API (meal data)                                                         |
| *Build & Dev*  | Gradle 8, npm 10, Docker & Docker Compose                                           |

---

## Getting Started

### Local Setup

1. *Clone the repo*

    ```bash
    git clone https://github.com/DeyanPPapazov/meal-planner.git
    cd meal-planner
    ```

2. *Create PostgreSQL database*
    - Database name: mealplanner  
    - Port: 5433  
    - User/password: postgres / postgres

    ```sql
    CREATE DATABASE mealplanner;
    ```

3. *Copy the env template*:

    ```bash
    cp backend/.env.example backend/.env
    ```

4. *Add your Spoonacular API key* to `backend/.env`:

    ```env
    SPOONACULAR_KEY=your_spoonacular_api_key_here
    ```

5. *Start the backend*

    ```bash
    cd backend
    ./gradlew bootRun
    ```

    The backend will run at: `http://localhost:8080`

6. *Start the frontend*

    ```bash
    cd ../frontend
    npm install
    npm run dev
    ```

    The frontend will run at: `http://localhost:5173`

---

### 🐳 One-Click Docker Setup (Optional) - NOT AVAILABLE YET!

Use this if you have Docker & Docker Compose installed and want everything (PostgreSQL, backend, frontend) running with a single command — no manual setup required.

#### 1. Add your Spoonacular API key

Create a `.env` file in the *project root* (same directory as `docker-compose.yml`):

```env
SPOONACULAR_KEY=your_spoonacular_api_key_here
```

This file is automatically read by Docker Compose to inject your key into the backend container.

#### 2. Start the entire stack

From the root of the project:

```bash
docker-compose up --build
```

This will:
- Start a PostgreSQL container (port 5433)
- Build and run the Spring Boot backend (port 8080)
- Build and serve the React frontend (port 5173)

#### 3. Open the app

| Service        | URL                   |
|----------------|-----------------------|
| Backend (API)  | http://localhost:8080 |
| Frontend (SPA) | http://localhost:5173 |
| PostgreSQL DB  | port 5433 (internal)  |

---

⚠️ **Important:** If you're using Docker, you do **not** need to:
- Manually install PostgreSQL
- Create the `mealplanner` database
- Run `./gradlew bootRun`
- Run `npm install` or `npm run dev`

Docker takes care of everything.

---

## Environment Variables

| Variable              | Scope   | Purpose                                   |
|----------------------|---------|-------------------------------------------|
| `SPOONACULAR_KEY`    | Backend | Authenticates requests to Spoonacular API |
| `JWT_SECRET` (opt.)  | Backend | Override default JWT secret               |

---

## Contact

Made by *Deyan P. Papazov*  
📫 Email: deyanpapazov@gmail.com  
🌐 LinkedIn: [https://www.linkedin.com/in/deyanpapazov](https://www.linkedin.com/in/deyan-papazov)
