# Event Feedback Analyzer

A full-stack event feedback analyzer with a Java Spring Boot backend and Angular frontend. The backend supports event creation, feedback submission, and AI-powered sentiment analysis using Hugging Face. The frontend allows users to create events, submit feedback, and view sentiment summaries.

---

## Features
- Event creation and listing
- Feedback submission for events
- AI-powered sentiment analysis (negative, neutral, positive) using Hugging Face([nlptown/bert-base-multilingual-uncased-sentiment](https://huggingface.co/nlptown/bert-base-multilingual-uncased-sentiment))
- Sentiment summary visualization
- OpenAPI/Swagger API documentation
- Dockerized backend for easy deployment
- Environment-based configuration (via environment variables or properties)
- Automated backend integration tests
- H2 in-memory database for all environments

## Live Demo / Deployed API

The deployed backend API is available at:  
[https://event-feedback-analyzer-387154959779.europe-west1.run.app/events](https://event-feedback-analyzer-387154959779.europe-west1.run.app/events)

**Note:** Written data is not permanent and will reset whenever the service is updated, restarted, or goes idle (Cloud Run uses an in-memory H2 database).

---

## Prerequisites
- [Node.js & npm](https://nodejs.org/) (for frontend)
- [Java 21+](https://adoptium.net/) (for backend)
- [Maven](https://maven.apache.org/) (or use the included `mvnw`/`mvnw.cmd`)
- [Docker](https://www.docker.com/) (optional, for containerized backend, cloud deployment)

---

## Backend Setup (Spring Boot)

1. **Configure Hugging Face API**
   - Set the following environment variables (recommended for cloud/deployment):
     - `HUGGINGFACE_API_KEY=YOUR_HUGGINGFACE_API_KEY`
     - `HUGGINGFACE_API_SENTIMENT_URL=https://api-inference.huggingface.co/models/nlptown/bert-base-multilingual-uncased-sentiment`
   - Or, for local development, create `server/src/main/resources/application-dev.properties` and use dev profile with:
     ```properties
     huggingface.api.key=YOUR_HUGGINGFACE_API_KEY
     huggingface.api.sentiment.url=https://api-inference.huggingface.co/models/nlptown/bert-base-multilingual-uncased-sentiment
     ```
   - The backend uses H2 in-memory database for all environments by default.

2. **Run with Maven**
   ```sh
   cd server
   mvn spring-boot:run
   ```
   Or use the Maven wrapper:
   ```sh
   ./mvnw spring-boot:run   # On Linux/macOS
   mvnw.cmd spring-boot:run # On Windows
   ```

3. **API Documentation**
   - Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for interactive API docs.

4. **Run Tests**
   ```sh
   mvn test
   ```

5. **Docker (optional)**
   ```sh
   cd server
   docker build -t event-feedback-analyzer .
   docker run -p 8080:8080 \
     -e HUGGINGFACE_API_KEY=YOUR_HUGGINGFACE_API_KEY \
     -e HUGGINGFACE_API_SENTIMENT_URL=https://api-inference.huggingface.co/models/nlptown/bert-base-multilingual-uncased-sentiment \
     event-feedback-analyzer
   ```
   - The container will listen on the port specified by the `PORT` environment variable (default 8080).

6. **Cloud Deployment (Cloud Run, etc.)**
   - Set the required environment variables in your cloud provider's console.

---

## Frontend Setup (Angular)

1. **Install dependencies**
   ```sh
   cd frontend
   npm install
   ```

2. **Run the app**
   ```sh
   npm start
   # or
   ng serve
   ```
   - The app will be available at [http://localhost:4200](http://localhost:4200)
   - Ensure the API URL in `frontend/src/app/api.service.ts` points to your backend (local or deployed Cloud Run URL).

3. **Build for production**
   ```sh
   npm run build
   ```

---

## Usage
- Create events and submit feedback via the Angular UI.
- View sentiment summary for each event.
- All feedback is analyzed using Hugging Face and stored in H2 in-memory database.

---

## Project Structure
```
frontend/   # Angular app
server/     # Spring Boot backend
```

---

## Environment Configuration
- Backend config: via environment variables or `server/src/main/resources/application-dev.properties` (for local dev)
- Frontend config: (if needed, use Angular environment files)

---

## Troubleshooting
- **CORS errors:** Ensure your backend has `@CrossOrigin` or a CORS config to allow requests from your frontend.
- **Cloud Run startup issues:** Make sure that all required environment variables are set.
- **Hugging Face errors:** Check that your API key and URL are correct and not rate-limited.
- **Database:** H2 is in-memory and resets on restart. For persistence, switch to a production database.

---

