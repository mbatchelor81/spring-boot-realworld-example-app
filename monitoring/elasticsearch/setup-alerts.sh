#!/bin/sh
# ---------------------------------------------------------------------------
# Elasticsearch Watcher — log-based alerts for error spikes
# ---------------------------------------------------------------------------
# Run this script after Elasticsearch is ready to configure alerting.
# Requires Elasticsearch with at least basic license for Watcher.
#
# For local dev without Watcher, these alerts are also configured as
# Prometheus alerting rules in monitoring/prometheus/alerting-rules.yml.
# ---------------------------------------------------------------------------

set -e
ES_HOST="${ES_HOST:-http://localhost:9200}"

echo "Waiting for Elasticsearch..."
until curl -sf "$ES_HOST/_cluster/health" > /dev/null 2>&1; do
  sleep 5
done
echo "Elasticsearch is ready. Configuring alerts..."

# ---------------------------------------------------------------------------
# Alert: Error rate spike — fires when > 50 ERROR logs in 5 minutes
# ---------------------------------------------------------------------------
curl -sf -X PUT "$ES_HOST/_watcher/watch/ftgo-error-spike" \
  -H 'Content-Type: application/json' -d '{
  "trigger": {
    "schedule": { "interval": "5m" }
  },
  "input": {
    "search": {
      "request": {
        "indices": ["ftgo-logs-*"],
        "body": {
          "size": 0,
          "query": {
            "bool": {
              "must": [
                { "term": { "level.keyword": "ERROR" } },
                { "range": { "@timestamp": { "gte": "now-5m" } } }
              ]
            }
          },
          "aggs": {
            "by_service": {
              "terms": { "field": "serviceName.keyword", "size": 10 }
            }
          }
        }
      }
    }
  },
  "condition": {
    "compare": { "ctx.payload.hits.total.value": { "gt": 50 } }
  },
  "actions": {
    "log_alert": {
      "logging": {
        "text": "FTGO Error Spike Alert: {{ctx.payload.hits.total.value}} errors in the last 5 minutes. Services: {{#ctx.payload.aggregations.by_service.buckets}}{{key}}({{doc_count}}) {{/ctx.payload.aggregations.by_service.buckets}}"
      }
    }
  }
}' && echo ""

# ---------------------------------------------------------------------------
# Alert: Service logging stopped — fires when a service has 0 logs in 10m
# ---------------------------------------------------------------------------
curl -sf -X PUT "$ES_HOST/_watcher/watch/ftgo-logging-stopped" \
  -H 'Content-Type: application/json' -d '{
  "trigger": {
    "schedule": { "interval": "10m" }
  },
  "input": {
    "search": {
      "request": {
        "indices": ["ftgo-logs-*"],
        "body": {
          "size": 0,
          "query": {
            "range": { "@timestamp": { "gte": "now-10m" } }
          },
          "aggs": {
            "by_service": {
              "terms": { "field": "serviceName.keyword", "size": 10 }
            }
          }
        }
      }
    }
  },
  "condition": {
    "compare": { "ctx.payload.hits.total.value": { "lt": 1 } }
  },
  "actions": {
    "log_alert": {
      "logging": {
        "text": "FTGO Logging Stopped Alert: No logs received from any FTGO service in the last 10 minutes."
      }
    }
  }
}' && echo ""

echo "Alert watches configured successfully."
