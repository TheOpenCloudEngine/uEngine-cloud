# Untag this, if you use docker hub to store your generated cloud package apps.
# docker login

REGISTRY_URL=darkgodarkgo
CURRENT_DIR="$(dirname $(pwd))"

cd $CURRENT_DIR/uengine-cloud-config
mvn package -B
docker build -t $REGISTRY_URL/uengine-cloud-config:v1 ./
docker push $REGISTRY_URL/uengine-cloud-config:v1


cd $CURRENT_DIR/uengine-eureka-server
mvn package -B
docker build -t $REGISTRY_URL/uengine-eureka-server:v1 ./
docker push $REGISTRY_URL/uengine-eureka-server:v1


cd $CURRENT_DIR/uengine-cloud-iam/front-end
npm install
npm run build


cd $CURRENT_DIR/uengine-cloud-iam
mvn clean package -B
docker build -t $REGISTRY_URL/uengine-cloud-iam:v1 ./
docker push $REGISTRY_URL/uengine-cloud-iam:v1


cd $CURRENT_DIR/uengine-cloud-server
mvn clean package -B
docker build -t $REGISTRY_URL/uengine-cloud-server:v1 ./
docker push $REGISTRY_URL/uengine-cloud-server:v1


cd $CURRENT_DIR/uengine-cloud-ui
npm install
npm run build
docker build -t $REGISTRY_URL/uengine-cloud-ui:v1 ./
docker push $REGISTRY_URL/uengine-cloud-ui:v1
