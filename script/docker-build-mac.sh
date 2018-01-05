docker login

REGISTRY_URL=sppark
CURRENT_DIR="$(dirname $(pwd))"

cd $CURRENT_DIR/uengine-cloud-config
mvn package -B
docker build -t $REGISTRY_URL/uengine-cloud-config:v1 ./
docker push $REGISTRY_URL/uengine-cloud-config:v1


cd $CURRENT_DIR/uengine-eureka-server
mvn package -B
docker build -t $REGISTRY_URL/uengine-eureka-server:v1 ./
docker push $REGISTRY_URL/uengine-eureka-server:v1


cd $CURRENT_DIR/uengine-cloud-iam
mvn package -B
docker build -t $REGISTRY_URL/uengine-cloud-iam:v1 ./
docker push $REGISTRY_URL/uengine-cloud-iam:v1


cd $CURRENT_DIR/uengine-cloud-server
mvn package -B
docker build -t $REGISTRY_URL/uengine-cloud-server:v1 ./
docker push $REGISTRY_URL/uengine-cloud-server:v1


cd $CURRENT_DIR/uengine-cloud-ui
npm install
npm run build
docker build -t $REGISTRY_URL/uengine-cloud-ui:v1 ./
docker push $REGISTRY_URL/uengine-cloud-ui:v1









