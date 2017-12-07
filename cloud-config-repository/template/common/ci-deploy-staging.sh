#!/bin/bash


#----------------------------------------------------------------------
# 환경 변수 받아오기

STATUS="$(curl --request GET \
            -s -o /dev/null -w "%{http_code}" \
            -H 'content-type: application/json' \
            ${CONFIG_SERVER_URL}/uengine-cloud-server.json)"

# 환경 변수 세팅을 받을 수 없는 경우
if [ $STATUS -eq 200 ];then
   echo "Success to connect cloud config server"
else
   echo "Failed to connect cloud config server"
   exit 1
fi


#----------------------------------------------------------------------
# 유엔진 클라우드 환경변수

JSON1="$(curl --request GET \
              -H 'content-type: application/json' \
              ${CONFIG_SERVER_URL}/uengine-cloud-server.json)"

REGISTRY_URL=$( echo $JSON1 | jq -r '.registry.host' )
CONFIG_REPO_ID=$( echo $JSON1 | jq -r '.gitlab["config-repo"].projectId' )
DCOS_URL=$( echo $JSON1 | jq -r '.dcos.host' )

JSON2="$(curl --request GET \
              -H 'content-type: application/json' \
              ${CONFIG_SERVER_URL}/dcos-apps.json)"

ZUUL_PROD_URL=$( echo $JSON2 | jq -r '.vcap.services["zuul-prod-server"].external' )
UENGINE_CLOUD_URL=$( echo $JSON2 | jq -r '.vcap.services["uengine-cloud-server"].external' )
APP_TYPE=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].appType' )
APP_OWNER=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].owner' )
PROD_SERVICE_PORT=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].prod["service-port"]' )
PROD_EXTERNAL_URL=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].prod.external' )
PROD_INTERNAL_URL=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].prod.internal' )
PROD_DEPLOYMENT=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].prod.deployment' )

STG_SERVICE_PORT=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].stg["service-port"]' )
STG_EXTERNAL_URL=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].stg.external' )
STG_INTERNAL_URL=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].stg.internal' )
STG_DEPLOYMENT=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].stg.deployment' )

DEV_SERVICE_PORT=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].dev["service-port"]' )
DEV_EXTERNAL_URL=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].dev.external' )
DEV_INTERNAL_URL=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].dev.internal' )
DEV_DEPLOYMENT=$( echo $JSON2 | jq -r '.dcos.apps["'${APP_NAME}'"].dev.deployment' )

echo "REGISTRY_URL: $REGISTRY_URL"
echo "CONFIG_REPO_ID: $CONFIG_REPO_ID"
echo "DCOS_URL: $DCOS_URL"
echo "APP_TYPE: $APP_TYPE"
echo "APP_OWNER: $APP_OWNER"
echo "PROD_SERVICE_PORT: $PROD_SERVICE_PORT"
echo "PROD_EXTERNAL_URL: $PROD_EXTERNAL_URL"
echo "PROD_INTERNAL_URL: $PROD_INTERNAL_URL"
echo "PROD_DEPLOYMENT: $PROD_DEPLOYMENT"
echo "STG_SERVICE_PORT: $STG_SERVICE_PORT"
echo "STG_EXTERNAL_URL: $STG_EXTERNAL_URL"
echo "STG_INTERNAL_URL: $STG_INTERNAL_URL"
echo "STG_DEPLOYMENT: $STG_DEPLOYMENT"
echo "DEV_SERVICE_PORT: $DEV_SERVICE_PORT"
echo "DEV_EXTERNAL_URL: $DEV_EXTERNAL_URL"
echo "DEV_INTERNAL_URL: $DEV_INTERNAL_URL"
echo "DEV_DEPLOYMENT: $DEV_DEPLOYMENT"

#----------------------------------------------------------------------
# 깃랩 환경 변수
echo "IMAGE_NAME: ${IMAGE_NAME}"
echo "APP_NAME: ${APP_NAME}"










#----------------------------------------------------------------------
# 스테이징 서버 마라톤 어플 이름
MARATHON_APP_ID="${APP_NAME}-stg"
echo "MARATHON_APP_ID: $MARATHON_APP_ID"


#----------------------------------------------------------------------
# 디플로이 파일 치환 작업
# 깃랩 파일 받기
DEPLOY_FILE_NAME=ci-deploy-staging.json
DEPLOY_JSON="$(curl --request GET \
            -H 'content-type: application/json' \
            $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/deployment%2F${APP_NAME}%2F$DEPLOY_FILE_NAME/raw?ref=master)"

echo $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/deployment%2F${APP_NAME}%2F$DEPLOY_FILE_NAME/raw?ref=master

echo $DEPLOY_JSON
echo $DEPLOY_JSON > $DEPLOY_FILE_NAME
sed -i'' -e "s|{{APP_ID}}|$MARATHON_APP_ID|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{IMAGE}}|${IMAGE_NAME}|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{DEPLOYMENT}}|$STG_DEPLOYMENT|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|\"{{SERVICE_PORT}}\"|$STG_SERVICE_PORT|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{EXTERNAL_URL}}|$STG_EXTERNAL_URL|g" $DEPLOY_FILE_NAME

echo "$MARATHON_APP_ID server update like:"
cat $DEPLOY_FILE_NAME

#----------------------------------------------------------------------
# 스테이징 서버 확인 후 디플로이

echo "Checking exist app....."

STG_EXIST="$(curl --request GET \
            -s -o /dev/null -w "%{http_code}" \
            -H 'content-type: application/json' \
            ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$MARATHON_APP_ID)"


#----------------------------------------------------------------------
# 스테이징 서버가 존재할 경우
if [ $STG_EXIST -eq 200 ];then
   echo "${APP_NAME}-stg server is exist, will update container."
   echo ""

   JOB_WAIT="$(curl --request PUT \
               -H 'content-type: application/json' \
               -d @$DEPLOY_FILE_NAME \
               ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$MARATHON_APP_ID?force=true&partialUpdate=false)"

#----------------------------------------------------------------------
# 스테이징 서버가 없을 경우
elif [ $STG_EXIST -eq 404 ];then
   echo "${APP_NAME}-stg server is not exist, will create new container"
   echo ""

   JOB_WAIT="$(curl --request POST \
               -H 'content-type: application/json' \
               -d @$DEPLOY_FILE_NAME \
               ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps)"

elif [ $STG_EXIST -eq 000 ];then
   echo "connection refused"
   exit 1
else
   echo "Failed to get response"
   exit 1
fi

#----------------------------------------------------------------------
# 스테이징 디플로이 결과 확인


echo "DEPLOY RESULT is $JOB_WAIT"

#----------------------------------------------------------------------
# 스테이징 서버 디플로이 종료 대기

echo "Start stage app deployment complete....."

# 5분동안 기다리기. 60 * 5s = 300s = 5min
MAX_COUNT=60
CURRENT_COUNT=0

while true
do
  STG_APP="$(curl --request GET \
              -H 'content-type: application/json' \
              ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$MARATHON_APP_ID)"

  STG_APP_STATUS="$(curl --request GET \
                -s -o /dev/null -w "%{http_code}" \
                -H 'content-type: application/json' \
                ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$MARATHON_APP_ID)"

  if [ $STG_APP_STATUS -eq 200 ];then
     TASKS_RUNNING=$( echo $STG_APP | jq -r '.app.tasksRunning' )
     TASKS_HEALTHY=$( echo $STG_APP | jq -r '.app.tasksHealthy' )
     DEPLOYMENTS_LENGTH=$( echo $STG_APP | jq -r '.app.deployments | length' )

     echo "TASKS_RUNNING: $TASKS_RUNNING , TASKS_HEALTHY: $TASKS_HEALTHY, DEPLOYMENTS_LENGTH: $DEPLOYMENTS_LENGTH"

     #----------------------------------------------------------------------
     # 디플로이가 완료되었을 경우

     if [ $TASKS_RUNNING -eq $TASKS_HEALTHY ] && [ $DEPLOYMENTS_LENGTH -eq 0 ];then
        echo "Deploy completed!!"
        break
     fi

     #----------------------------------------------------------------------
     # 호출 카운트 증가

     echo "Waiting....."
     CURRENT_COUNT=$((CURRENT_COUNT + 1))

     #----------------------------------------------------------------------
     # 타임아웃이 걸렸을 경우

     if [ "$CURRENT_COUNT" -gt "$MAX_COUNT" ];then
        echo "Time out. deployment will cancel.";

        # 스테이징 앱 삭제
        curl --request DELETE \
        -H 'content-type: application/json' \
        ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$MARATHON_APP_ID?force=true

        exit 1
     fi
     sleep 5

  else
     echo "Failed to get STG_APP"
     exit 1
  fi
done

#----------------------------------------------------------------------
# 스프링 부트인경우 라우트 변경

echo "Router refresh"

curl --request GET \
    -H 'content-type: application/json' \
    ${ZUUL_PROD_URL}/refreshRoute


