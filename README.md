# Repo Browser API

A Github proxy REST API built with Spring Boot that consumes the external GitHub API. It allows consumers to fetch a list of non-fork repositories for any given GitHub user, alongside their respective branches and latest commit hashes.

## Tech Stack

* **Java 25:** Leveraging the latest language features and toolchains.
* **Spring Boot 4.1.0:** Core framework for the REST API and dependency injection.
* **Gradle (Kotlin DSL):** Build automation and dependency management.
* **WireMock Standalone (3.13):** Used for robust, offline end-to-end integration testing.
---

## Quick Start

### Prerequisites
* **JDK 25** must be installed on your machine.
* (Optional) An IDE like IntelliJ IDEA or Eclipse.

### Build and Run Locally

1. Clone the repository and navigate to the project root:
```bash
git clone <repository-url>
cd repo-browser
```
2. Build the project (this will also run tests and generate a JaCoCo coverage report):
```bash
./gradlew build
```
3. Run the application:
```bash
./gradlew bootRun
```

The application will start locally on port `8080` (default) and proxy requests to `https://api.github.com`.

### API Documentation

#### 1. Fetch User Repositories

Retrieves all non-fork repositories for a specific GitHub user.

Endpoint:

`GET /api/repositories/{username}`

**Successful Response (200 OK):**
```json
[
  {
    "repositoryName": "valid-repo",
    "ownerLogin": "jankowalski",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "12345abcde"
      },
      {
        "name": "feature-branch",
        "lastCommitSha": "67890fghij"
      }
    ]
  }
]
```

### 2. Error handling

If the requested GitHub user does not exist, the API intercepts the upstream error and returns a customized, explicitly formatted JSON response.

**Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Github user does not exist with given login"
}
```

**Response:**
```json
{
 
    "status": "${responseCode}",
    "message": "Unexpected error while communicating with GitHuba API: ${whyItHasHappenend}"
}
```

### Testing Strategy
The application includes an End-to-End (E2E) integration test suite designed to run entirely offline without relying on actual internet connectivity to GitHub.

- WireMock is registered on a dynamic random port. The Spring Boot application dynamically injects this localised URL to intercept all RestClient outgoing traffic.
- Test coverage is reported via JaCoCo and GitHub Actions CI.

**Run Tests:**
```bash
./gradlew test
```

### Architectural Notes and Known Limitations

To strictly adhere to the project constraints (minimal overengineering, no WebFlux, treating the application strictly as a proxy without complex DDD layers), the following architectural choices were made:

1. The GitHub REST API v3 does not provide a bulk-fetch endpoint for repository branches. Consequently, fulfilling the acceptance criteria requires querying the repositories and subsequently querying the branches for each valid repository, creating an N+1 problem. To stay within the project's constraints, fetcing branches in parallel is not implemented.
2. The application deliberately avoids Hexagonal/DDD patterns, using a straightforward Layered proxy architecture, keeping all components cohesive within a single package structure.
3. No additional Javadoc documentation is provided for the application, and no additional tests are not included.