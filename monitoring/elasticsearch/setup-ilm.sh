#!/bin/sh
# ---------------------------------------------------------------------------
# Elasticsearch ILM Setup — log retention policies for local dev
# ---------------------------------------------------------------------------
# Run after Elasticsearch starts: ./monitoring/elasticsearch/setup-ilm.sh
# ---------------------------------------------------------------------------

set -e
ES_HOST="${ES_HOST:-http://localhost:9200}"

echo "Waiting for Elasticsearch..."
until curl -sf "$ES_HOST/_cluster/health" > /dev/null 2>&1; do
  sleep 5
done
echo "Elasticsearch is ready. Configuring ILM policies..."

# Dev: 7 days retention
curl -sSf -X PUT "$ES_HOST/_ilm/policy/ftgo-logs-dev" -H 'Content-Type: application/json' -d '{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
        }
      },
      "delete": {
        "min_age": "7d",
        "actions": { "delete": {} }
      }
    }
  }
}' && echo ""

# Staging: 30 days retention
curl -sSf -X PUT "$ES_HOST/_ilm/policy/ftgo-logs-staging" -H 'Content-Type: application/json' -d '{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "forcemerge": { "max_num_segments": 1 },
          "shrink": { "number_of_shards": 1 }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": { "delete": {} }
      }
    }
  }
}' && echo ""

# Prod: 90 days retention
curl -sSf -X PUT "$ES_HOST/_ilm/policy/ftgo-logs-prod" -H 'Content-Type: application/json' -d '{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "forcemerge": { "max_num_segments": 1 },
          "shrink": { "number_of_shards": 1 }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "allocate": { "number_of_replicas": 0 }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": { "delete": {} }
      }
    }
  }
}' && echo ""

# Index template for ftgo-logs
curl -sSf -X PUT "$ES_HOST/_index_template/ftgo-logs" -H 'Content-Type: application/json' -d '{
  "index_patterns": ["ftgo-logs-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.lifecycle.name": "ftgo-logs-dev"
    }
  }
}' && echo ""

echo "ILM policies and index templates configured successfully."
