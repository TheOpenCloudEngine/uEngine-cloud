# First, Run marathon lb apps.
dcos marathon app add marathon-lb.json
dcos marathon app add marathon-lb-internal.json

# If you use nexus, Run nexus app.
dcos marathon app add nexus.json

# Run these three apps.
dcos marathon app add config.json
dcos marathon app add eureka-server.json
dcos marathon app add db.json
dcos marathon app add redis.json

# Run kafka in your public node, "docker run -d --name kafka -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=[public node's private ip] --env ADVERTISED_PORT=9092 --env NUM_PARTITIONS=3 sppark/kafka:v1"

# Check pre three apps are all healthy, then run under three apps.
dcos marathon app add cloud-server.json
dcos marathon app add cloud-ui.json
dcos marathon app add iam.json