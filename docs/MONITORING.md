# 📊 System Monitoring and Observability

UDOM Secure Cloud Storage comes with a fully configured observability stack out-of-the-box using Prometheus, Grafana, Loki, and Promtail.

## Architecture

1. **Prometheus**: Scrapes metrics from the Spring Boot backend (`/actuator/prometheus`).
2. **Grafana**: Provides visual dashboards for all metrics.
3. **Loki**: Aggregates logs.
4. **Promtail**: Collects logs from Docker containers and ships them to Loki.
5. **Alertmanager**: Triggers notifications for system alerts.

## Dashboards

The system comes pre-configured with the following Grafana dashboards (located in `grafana/dashboards/`):
- **Spring Boot Metrics**: JVM Memory, CPU usage, Garbage Collection, and Threads.
- **API Performance**: HTTP request latencies, error rates (5xx, 4xx), and active sessions.
- **MinIO Storage**: Object count, bucket sizes, and bandwidth.

## Alerting Rules

Pre-configured alerts in `prometheus/rules/alerts.yml` include:
- **BackendDown**: Triggered if the Spring Boot service goes offline.
- **HighMemoryUsage**: Triggered if JVM memory exceeds 80%.
- **HighDiskUsage**: Triggered if server storage exceeds 90%.
- **HighErrorRate**: Triggered if HTTP 5xx errors spike.
- **SlowResponseTime**: Triggered if API responses take longer than 2 seconds.

## Access

- **Grafana URL**: `http://localhost:3001`
- **Default Credentials**: `admin` / `admin` (Requires password change on first login).
- **Prometheus UI**: `http://localhost:9090`

## Adding Custom Metrics

Developers can add custom metrics in the Spring Boot backend using the Micrometer registry (`MeterRegistry`). Metrics are automatically exposed to Prometheus.
