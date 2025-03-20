# Cipher Dispatcher & Interceptor

This is a message processing system built with Spring Boot and Apache Kafka. It visualises a scalable design for processing messages with retry capabilities and switchable competing consumers pattern.

## Project Overview

It contains the following components:

- **Cipher Dispatcher**: A service that receives payload requests via a REST end point  and dispatches them to a Kafka topic for processing
- **Infinity Interceptor**: Two instances of this services consume these messages from Kafka, process them, and send callbacks to the dispatcher.
- **Cipher Model**: Shared data models used across services

- Multiple interceptor instances compete to process messages
- Failed messages are retried once before being marked as failed
- **Simulated Failures**: 40% failure rate is simulated for testing resilience

## Running the Application

1. Clone the repository

2. Start the services using Docker Compose:
   ```bash
   docker-compose up --build -d
   ```

3. The services will be available at:
   - Cipher Dispatcher: http://localhost:8080
   - Infinity Interceptor 1: http://localhost:8081
   - Infinity Interceptor 2: http://localhost:8082

4. To stop the services:
   ```bash
   docker-compose down
   ```


## API Documentation

### Dispatch a Message

```
POST /v1/dispatch
```

Example request to dispatch a message:

```bash
curl -X POST http://localhost:8080/v1/dispatch \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "age": 25,
    "height": 1.75,
    "is_student": true,
    "grades": [90, 85, 92, 88],
    "address": {
      "street": "123 Main St",
      "city": "Anytown",
      "zipcode": "12345"
    },
    "birth_date": "19990515",
    "is_employed": true,
    "salary": 55000.50,
    "skills": ["programming", "communication", "problem-solving"],
    "achievements": {
      "certifications": ["AWS Certified Developer", "CompTIA Security+"],
      "awards": {
        "employee_of_the_month": true,
        "innovation_award": false
      }
    }
  }'
```

Example response:

```json
"Request dispatched with ID: ff5a5cf3-0157-448c-a0d8-312f2f9a7805"
```

### Check Message Status

```
GET /v1/dispatch/status/{messageId}
```

Example request:

```bash
curl -X GET http://localhost:8080/v1/dispatch/status/550e8400-e29b-41d4-a716-446655440000 | jq .
```

### View All Messages in Interceptor

```
GET /v1/interceptor/messages
```

Example request:

```bash
curl -X GET http://localhost:8081/v1/interceptor/messages | jq .
curl -X GET http://localhost:8082/v1/interceptor/messages | jq .
```

## Testing the Application

1. Send a message to the dispatcher using the API example above
2. Check the message status using the status API
3. View the logs to see the message processing flow:
   ```bash
   docker-compose logs -f
   ```
4. Check which interceptor processed the message by querying both instances:
   ```bash
   curl -X GET http://localhost:8081/v1/interceptor/messages
   curl -X GET http://localhost:8082/v1/interceptor/messages
   ```
   Only one of the instances should have the message in its store.

## Competing Consumers Pattern

The Infinity Interceptor instances are configured to use Kafka's consumer group functionality. This ensures that:

- Messages are distributed among available consumers in the same group
- Each message is processed by only one consumer in the group
- If a consumer fails, its partitions are reassigned to other consumers

This is achieved through the configuration in the `application.yml` of the interceptors:

```yaml
spring:
  kafka:
    consumer:
      group-id: ${INTERCEPTOR_ID}-group
```

And in the `docker-compose.yml`, each interceptor instance is given a unique ID:

```yaml
INTERCEPTOR_ID: instance-1
```

Instead, if we keep the same consumer group ID, both instances will compete to process messages.

### For debugging
```bash
docker logs guna-cipher-dispatcher-cipher-dispatcher-1 -f
docker logs guna-cipher-dispatcher-infinity-interceptor-1-1 -f
docker logs guna-cipher-dispatcher-infinity-interceptor-1-2 -f
```