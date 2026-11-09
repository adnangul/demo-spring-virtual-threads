# Getting Started

### Virtual Thread Demo
This sample application demonstrates how Java virtual threads can handle a blocking I/O workload without tying up platform threads.

`VirtualThreadDemoController` exposes a single endpoint:

* `GET /io-bound-task`
* Simulates a slow I/O-bound task with `Thread.sleep(2000)`
* Logs the starting thread and the thread that completes the request
* Returns a response describing the initial and final thread names

Start the application with:

```bash
./mvnw spring-boot:run
```

Then call the endpoint in a browser or with curl:

```bash
curl http://localhost:8080/io-bound-task
```

### Load Test 2
`LoadTest2` is a concurrent load test for the `/io-bound-task` endpoint. It sends 400 requests in parallel using a shared `HttpClient` backed by `Executors.newVirtualThreadPerTaskExecutor()` and records the number of successful and failed requests.

Run it after starting the application:

```bash
./mvnw exec:java -Dexec.mainClass="com.example.demo.LoadTest2"
```

The program prints summary metrics, including:

* total requests executed
* successful HTTP 200 responses
* failed requests
* total elapsed time in seconds

This is useful for observing how a virtual-thread-based executor scales under concurrent blocking work.

### Evaluate

Run LoadTest2 once with virtual threads enabled, then repeat the test with virtual threads disabled (application.properties). Compare the results to evaluate the performance difference.

Try with 1,000, 2,000 or more concurrent requests 