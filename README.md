# ApoloBackend

Backend application for Apolo project with AI integration using Groq API.

## Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MongoDB running on localhost:27017

### Configuration

1. Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

2. Edit `.env` and add your Groq API key:
```
GROQ_API_KEY=your-actual-api-key-here
```

3. Set the environment variable before running:
```bash
export GROQ_API_KEY=your-actual-api-key-here
```

Or add it to your IDE run configuration.

### Running the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`