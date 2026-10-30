# Calculator REST API — CI/CD Pipeline

A small Spring Boot calculator REST API wrapped in a complete CI/CD pipeline:
**Git/GitHub → Jenkins → Docker → Docker Compose**.

Submitted for CSE 816 — Software Production Engineering (Mini-Project).

- **Docker Hub image:** [`mockingjay123/calculator-api`](https://hub.docker.com/r/mockingjay123/calculator-api)

---

## Tech stack

| Component | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 (Web MVC, Actuator, Validation) |
| Build | Maven (wrapper included) |
| Tests | JUnit 5 + MockMvc |
| CI/CD | Jenkins (declarative pipeline) |
| Containers | Docker (multi-stage) + Docker Compose + Nginx |

---

## API

All endpoints take two numeric query parameters, `a` and `b`, and return JSON.

| Operation | Endpoint | Behaviour |
|---|---|---|
| Add | `GET /api/v1/add?a=&b=` | `a + b` |
| Subtract | `GET /api/v1/subtract?a=&b=` | `a - b` |
| Multiply | `GET /api/v1/multiply?a=&b=` | `a * b` |
| Divide | `GET /api/v1/divide?a=&b=` | `a / b`; `b=0` → HTTP 400 |
| Modulus | `GET /api/v1/mod?a=&b=` | `a % b`; `b=0` → HTTP 400 |
| Health | `GET /actuator/health` | Actuator probe (`UP`/`DOWN`) |

### Success response

```bash
curl "http://localhost:8081/api/v1/add?a=2&b=3"
```

```json
{ "operation": "add", "a": 2.0, "b": 3.0, "result": 5.0 }
```

### Error response

Division or modulus by zero, and missing or non-numeric parameters, return
**HTTP 400** with a structured error body:

```bash
curl -i "http://localhost:8081/api/v1/divide?a=10&b=0"
```

```json
{ "error": "Division by zero is not allowed" }
```

---

## Running locally

### 1. With Maven

```bash
./mvnw spring-boot:run
```

The app listens on **port 5000**, so endpoints are at `http://localhost:5000/...`.

### 2. With Docker

```bash
docker build -t calculator-api .
docker run --rm -p 5000:5000 calculator-api
```

### 3. With Docker Compose (API + Nginx reverse proxy)

```bash
docker compose up -d
```

The stack is reached **through the proxy on port 8081**:

```bash
curl "http://localhost:8081/api/v1/multiply?a=6&b=7"
```

Tear it down with:

```bash
docker compose down
```

> The API container's own port is not published — all external traffic goes
> through Nginx, which is the point of the reverse proxy.

---

## Build and test

Compile and package an executable JAR (`target/calculator-0.0.1-SNAPSHOT.jar`):

```bash
./mvnw clean package
```

Run the test suite (16 tests):

```bash
./mvnw test
```

JUnit XML reports are written to `target/surefire-reports/`, which Jenkins
publishes as the build's test results.

**Test layout**

| File | Kind |
|---|---|
| `CalculatorControllerTest` | Unit tests — one per operation, plus divide- and modulus-by-zero |
| `CalculatorControllerIntegrationTest` | Spring MVC integration tests via `MockMvc` over the real endpoints |
| `CalculatorApplicationTests` | Context load smoke test |

> The POM targets Java 21. If your default JDK is newer, set
> `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` before running Maven.

---

## Containerization

`Dockerfile` is a **multi-stage** build:

1. **Build stage** (`maven:3.9-eclipse-temurin-21`) — copies `pom.xml` alone and
   resolves dependencies first, so that layer stays cached when only `src/`
   changes, then packages the JAR.
2. **Runtime stage** (`eclipse-temurin:21-jre-alpine`) — a slim JRE image that
   copies *only* the JAR. No JDK or Maven in the final image.

Best practices applied: minimal base image, **non-root user** (`spring`),
explicit `EXPOSE 5000`, dependency layer caching, and a `.dockerignore`.

## Deployment

`docker-compose.yml` runs two services on a dedicated bridge network:

| Service | Role |
|---|---|
| `calculator` | The Spring Boot API, health-checked on `/actuator/health` |
| `nginx` | Reverse proxy, published on `${PROXY_PORT:-8081}` |

Nginx waits for the API to report healthy (`depends_on: condition:
service_healthy`) before it starts proxying, and both services use
`restart: unless-stopped`.

The image reference is `${IMAGE_NAME:-calculator}:${IMAGE_TAG:-latest}`, so the
pipeline can deploy a specific build:

```bash
IMAGE_NAME=mockingjay123/calculator-api IMAGE_TAG=12 docker compose up -d
```

---

## CI/CD pipeline

`Jenkinsfile` defines a declarative pipeline, triggered automatically by a
GitHub webhook (with `pollSCM` every 5 minutes as a fallback).

| Stage | What it does |
|---|---|
| **Checkout** | Clones the latest commit from GitHub |
| **Build** | `./mvnw clean package -DskipTests` |
| **Test** | `./mvnw test`, publishes JUnit results, **fails the build on any test failure** |
| **Docker Build** | Builds the image tagged with `${BUILD_NUMBER}` and `latest` |
| **Push** | Pushes both tags to Docker Hub using Jenkins-managed credentials |
| **Deploy** | `docker compose up -d`, then polls `/actuator/health` through the proxy and fails if it never reports `UP` |

Build status is emailed on both success and failure (failures attach the log).

**Re-runnable and idempotent:** `docker compose up -d` leaves unchanged services
running, and `disableConcurrentBuilds()` prevents overlapping deployments.

### Jenkins prerequisites

- The `jenkins` user must be in the `docker` group:
  `sudo usermod -aG docker jenkins && sudo systemctl restart jenkins`
- A *Username with password* credential with ID **`dockerhub-credentials`**
  holding a Docker Hub access token
- SMTP configured under *Extended E-mail Notification* for build emails
- Jenkins occupies port **8080**, which is why the deployed proxy uses **8081**

---

## Repository layout

```
calculator-ci-cd/
├── src/main/java/...        # Spring Boot application
├── src/test/java/...        # JUnit 5 + MockMvc tests
├── pom.xml                  # Maven build
├── Dockerfile               # multi-stage image build
├── .dockerignore
├── docker-compose.yml       # API + Nginx reverse proxy
├── nginx/default.conf       # reverse proxy configuration
├── Jenkinsfile              # declarative CI/CD pipeline
└── README.md
```
