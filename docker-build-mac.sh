docker login

REGISTRY_URL=sppark
CURRENT_DIR="$(pwd)"

cd $CURRENT_DIR/uengine-cloud-config
mvn package -B
docker build -t $REGISTRY_URL/uengine-cloud-config:v1 ./
docker push $REGISTRY_URL/uengine-cloud-config:v1


cd $CURRENT_DIR/uengine-eureka-server
mvn package -B
docker build -t $REGISTRY_URL/uengine-eureka-server:v1 ./
docker push $REGISTRY_URL/uengine-eureka-server:v1


cd $CURRENT_DIR/uengine-eureka-zuul
mvn package -B
docker build -t $REGISTRY_URL/uengine-eureka-zuul:v1 ./
docker push $REGISTRY_URL/uengine-eureka-zuul:v1


cd $CURRENT_DIR/uengine-cloud-server
mvn package -B
docker build -t $REGISTRY_URL/uengine-cloud-server:v1 ./
docker push $REGISTRY_URL/uengine-cloud-server:v1


cd $CURRENT_DIR/uengine-cloud-ui
npm run build
docker build -t $REGISTRY_URL/uengine-cloud-ui:v1 ./
docker push $REGISTRY_URL/uengine-cloud-ui:v1









