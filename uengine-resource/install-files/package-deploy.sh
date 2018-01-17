# Run these three apps.
dcos marathon app add config.json
dcos marathon app add eureka-server.json
dcos marathon app add db.json

# Check pre three apps are all healthy, then run under three apps.
dcos marathon app add iam.json
dcos marathon app add cloud-server.json
dcos marathon app add cloud-ui.json