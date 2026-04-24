# Open Liberty Reactive Messaging Hello World

A comprehensive demonstration of MicroProfile Reactive Messaging 3.0 on Open Liberty, showcasing message publishing, consuming, and processing patterns.

## Features

- ✅ **Message Publishing** - Send messages via REST API or programmatically
- ✅ **Message Consumption** - Receive and process messages asynchronously
- ✅ **Stream Processing** - Transform messages using @Incoming/@Outgoing
- ✅ **Multiple Patterns** - Emitter-based and Publisher-based messaging
- ✅ **REST API** - Interact with the messaging system via HTTP endpoints
- ✅ **Kafka Ready** - Pre-configured for Apache Kafka (with in-memory fallback)

## Architecture

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│ REST API    │─────▶│ HelloPublisher   │─────▶│ Message     │
│             │      │ (Emitter/Stream) │      │ Channel     │
└─────────────┘      └──────────────────┘      └──────┬──────┘
                                                       │
                     ┌──────────────────┐      ┌──────▼──────┐
                     │ HelloConsumer    │◀─────│ Message     │
                     │ (@Incoming)      │      │ Channel     │
                     └──────────────────┘      └─────────────┘
```

## Prerequisites

- Java 17 or later
- Maven 3.8+
- Open Liberty 23.0.0.3+ (will be downloaded automatically)
- Optional: Apache Kafka (for production use)

## Project Structure

```
reactive-messaging-hello/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/reactive/
│   │   │   ├── ReactiveMessagingApplication.java
│   │   │   ├── model/
│   │   │   │   └── HelloMessage.java
│   │   │   ├── publisher/
│   │   │   │   └── HelloPublisher.java
│   │   │   ├── consumer/
│   │   │   │   └── HelloConsumer.java
│   │   │   └── resource/
│   │   │       └── HelloResource.java
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── microprofile-config.properties
│   │   ├── liberty/config/
│   │   │   └── server.xml
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── beans.xml
└── README.md
```

## Quick Start

### 1. Build the Application

```bash
mvn clean package
```

### 2. Start Open Liberty

```bash
mvn liberty:run
```

The server will start on http://localhost:9080

### 3. Test the Application

**Welcome Endpoint:**
```bash
curl http://localhost:9080/api/hello
```

**Publish a Message:**
```bash
curl -X POST http://localhost:9080/api/hello/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello Reactive Messaging!"}'
```

**View Received Messages:**
```bash
curl http://localhost:9080/api/hello/messages
```

**Health Check:**
```bash
curl http://localhost:9080/api/hello/health
```

**Clear Messages:**
```bash
curl -X DELETE http://localhost:9080/api/hello/messages
```

## Configuration

### Message Channels

The application uses two main channels configured in `microprofile-config.properties`:

- **`hello-out`** - Outgoing channel for publishing messages
- **`hello-in`** - Incoming channel for consuming messages

### Connector Options

#### Option 1: In-Memory Connector (Default for Testing)

Uncomment in `microprofile-config.properties`:
```properties
mp.messaging.outgoing.hello-out.connector=smallrye-in-memory
mp.messaging.incoming.hello-in.connector=smallrye-in-memory
```

#### Option 2: Apache Kafka (Production)

Use the default Kafka configuration:
```properties
mp.messaging.outgoing.hello-out.connector=liberty-kafka
mp.messaging.outgoing.hello-out.topic=hello-topic

mp.messaging.incoming.hello-in.connector=liberty-kafka
mp.messaging.incoming.hello-in.topic=hello-topic
mp.messaging.incoming.hello-in.group.id=hello-consumer-group
```

**Start Kafka locally:**
```bash
# Using Docker
docker run -d --name kafka \
  -p 9092:9092 \
  apache/kafka:latest
```

## Key Concepts Demonstrated

### 1. Emitter Pattern

```java
@Inject
@Channel("hello-out")
Emitter<String> helloEmitter;

public void publishMessage(String message) {
    helloEmitter.send(message);
}
```

### 2. Simple Consumer

```java
@Incoming("hello-in")
public void consumeHelloMessage(String message) {
    LOGGER.info("Received: " + message);
}
```

### 3. Consumer with Acknowledgment

```java
@Incoming("hello-in")
public CompletionStage<Void> consumeHelloMessageWithAck(Message<String> message) {
    String payload = message.getPayload();
    // Process message
    return message.ack();
}
```

### 4. Stream Processing

```java
@Incoming("processing-in")
@Outgoing("processing-out")
public String processMessage(String incoming) {
    return incoming.toUpperCase();
}
```

### 5. Publisher Pattern

```java
@Outgoing("hello-out")
public Publisher<String> generatePeriodicMessages() {
    return PublisherBuilder.generate(() -> "Message #" + counter++)
        .buildRs();
}
```

## Testing Scenarios

### Scenario 1: Simple Message Flow

1. Publish a message via REST API
2. Consumer receives and logs it
3. Verify in received messages list

```bash
# Publish
curl -X POST http://localhost:9080/api/hello/publish \
  -H "Content-Type: application/json" \
  -d '{"message": "Test Message 1"}'

# Verify
curl http://localhost:9080/api/hello/messages
```

### Scenario 2: Bulk Publishing

```bash
for i in {1..5}; do
  curl -X POST http://localhost:9080/api/hello/publish \
    -H "Content-Type: application/json" \
    -d "{\"message\": \"Bulk Message $i\"}"
done

curl http://localhost:9080/api/hello/messages
```

### Scenario 3: Automatic Stream Generation

Uncomment the `@Outgoing` annotation in `HelloPublisher.generatePeriodicMessages()` to enable automatic message generation every 10 seconds.

## Server Logs

Watch the logs to see message flow:

```bash
tail -f target/liberty/wlp/usr/servers/reactiveMessagingServer/logs/messages.log
```

Expected output:
```
[INFO] Publishing message via Emitter: Hello Reactive Messaging!
[INFO] ✓ Received message: Hello Reactive Messaging!
[INFO] Processing: Hello Reactive Messaging!
```

## Deployment

### Package for Deployment

```bash
mvn clean package
```

The WAR file will be created at `target/reactive-messaging-hello.war`

### Deploy to Existing Open Liberty

1. Copy the WAR to Liberty's `dropins` folder:
```bash
cp target/reactive-messaging-hello.war $LIBERTY_HOME/usr/servers/defaultServer/dropins/
```

2. Update `server.xml` to include the required features

## Troubleshooting

### Issue: Messages not being received

**Solution:** Check that both publisher and consumer are using the same channel names and connector configuration.

### Issue: Kafka connection errors

**Solution:** 
- Verify Kafka is running: `docker ps`
- Check bootstrap.servers configuration
- Or switch to in-memory connector for testing

### Issue: CDI injection failures

**Solution:** Ensure `beans.xml` exists in `WEB-INF/` and `bean-discovery-mode="all"`

## Advanced Topics

### Custom Message Types

Extend `HelloMessage` model and use JSON serialization:

```java
@Incoming("hello-in")
public void consume(HelloMessage message) {
    // Process structured message
}
```

### Error Handling

Add error handling to consumer:

```java
@Incoming("hello-in")
public CompletionStage<Void> consume(Message<String> message) {
    try {
        processMessage(message.getPayload());
        return message.ack();
    } catch (Exception e) {
        return message.nack(e);
    }
}
```

### Dead Letter Queue

Configure DLQ in `microprofile-config.properties`:

```properties
mp.messaging.incoming.hello-in.failure-strategy=dead-letter-queue
mp.messaging.incoming.hello-in.dead-letter-queue.topic=hello-dlq
```

## References

- [MicroProfile Reactive Messaging Specification](https://download.eclipse.org/microprofile/microprofile-reactive-messaging-3.0/microprofile-reactive-messaging-spec-3.0.html)
- [Open Liberty Reactive Messaging](https://openliberty.io/docs/latest/reactive-messaging.html)
- [SmallRye Reactive Messaging](https://smallrye.io/smallrye-reactive-messaging/)

## License

Apache License 2.0

## Author

Created as a demonstration of MicroProfile Reactive Messaging on Open Liberty.
