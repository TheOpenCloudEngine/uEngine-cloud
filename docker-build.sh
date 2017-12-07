REGISTRY_URL=gitlab.pas-mini.io:5000
CURRENT_DIR="$(pwd)"

cd $CURRENT_DIR/uengine-cloud-config
mvn package -B
sudo docker build -t $REGISTRY_URL/uengine-cloud-config:v1 ./
sudo docker push $REGISTRY_URL/uengine-cloud-config:v1


cd $CURRENT_DIR/uengine-eureka-server
mvn package -B
sudo docker build -t $REGISTRY_URL/uengine-eureka-server:v1 ./
sudo docker push $REGISTRY_URL/uengine-eureka-server:v1


cd $CURRENT_DIR/uengine-eureka-zuul
mvn package -B
sudo docker build -t $REGISTRY_URL/uengine-eureka-zuul:v1 ./
sudo docker push $REGISTRY_URL/uengine-eureka-zuul:v1


cd $CURRENT_DIR/uengine-cloud-server
mvn package -B
sudo docker build -t $REGISTRY_URL/uengine-cloud-server:v1 ./
sudo docker push $REGISTRY_URL/uengine-cloud-server:v1


cd $CURRENT_DIR/uengine-cloud-ui
npm run build
sudo docker build -t $REGISTRY_URL/uengine-cloud-ui:v1 ./
sudo docker push $REGISTRY_URL/uengine-cloud-ui:v1









