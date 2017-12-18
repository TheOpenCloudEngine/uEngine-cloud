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

PROFILE="prod"
APPLICATION_NAME=${APP_NAME}

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
echo "PROFILE: $PROFILE"
echo "APPLICATION_NAME: $APPLICATION_NAME"

#----------------------------------------------------------------------
# 깃랩 환경 변수
echo "IMAGE_NAME: ${IMAGE_NAME}"
echo "APP_NAME: ${APP_NAME}"










#----------------------------------------------------------------------
# 프로덕션 서버 현재 마라톤 어플 이름
OLD_MARATHON_APP_ID="${APP_NAME}-$PROD_DEPLOYMENT"
OLD_PROD_DEPLOYMENT="$PROD_DEPLOYMENT"
echo "OLD_MARATHON_APP_ID: $OLD_MARATHON_APP_ID"
echo "OLD_PROD_DEPLOYMENT: $OLD_PROD_DEPLOYMENT"

# 프로덕션 서버 신규 마라톤 어플 이름
NEW_MARATHON_APP_ID=""
NEW_PROD_DEPLOYMENT=""

if [ "$PROD_DEPLOYMENT" = "green" ];then

   NEW_MARATHON_APP_ID="${APP_NAME}-blue"
   NEW_PROD_DEPLOYMENT="blue"

else

   NEW_MARATHON_APP_ID="${APP_NAME}-green"
   NEW_PROD_DEPLOYMENT="green"
fi

echo "NEW_MARATHON_APP_ID: $NEW_MARATHON_APP_ID"
echo "NEW_PROD_DEPLOYMENT: $NEW_PROD_DEPLOYMENT"

#----------------------------------------------------------------------
# 디플로이 파일 치환 작업
# 깃랩 파일 받기
DEPLOY_FILE_NAME=ci-deploy-production.json
DEPLOY_JSON="$(curl --request GET \
            -H 'content-type: application/json' \
            $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/deployment%2F${APP_NAME}%2F$DEPLOY_FILE_NAME/raw?ref=master)"

echo $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/deployment%2F${APP_NAME}%2F$DEPLOY_FILE_NAME/raw?ref=master

echo $DEPLOY_JSON
echo $DEPLOY_JSON > $DEPLOY_FILE_NAME
sed -i'' -e "s|{{APP_ID}}|$NEW_MARATHON_APP_ID|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{IMAGE}}|${IMAGE_NAME}|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{DEPLOYMENT}}|$NEW_PROD_DEPLOYMENT|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|\"{{SERVICE_PORT}}\"|$PROD_SERVICE_PORT|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{EXTERNAL_URL}}|$PROD_EXTERNAL_URL|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{PROFILE}}|$PROFILE|g" $DEPLOY_FILE_NAME
sed -i'' -e "s|{{APPLICATION_NAME}}|$APPLICATION_NAME|g" $DEPLOY_FILE_NAME

echo "$NEW_MARATHON_APP_ID server update like:"
cat $DEPLOY_FILE_NAME

#----------------------------------------------------------------------
# 신규 프로덕션 서버 확인 후 디플로이

echo "Checking exist app....."

NEW_PROD_EXIST="$(curl --request GET \
            -s -o /dev/null -w "%{http_code}" \
            -H 'content-type: application/json' \
            ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$NEW_MARATHON_APP_ID)"


#----------------------------------------------------------------------
# 신규 프로덕션 서버가 존재할 경우 (중복 디플로이)
if [ $NEW_PROD_EXIST -eq 200 ];then
   echo "$NEW_MARATHON_APP_ID server is exist, will update container."
   echo ""

   JOB_WAIT="$(curl --request PUT \
               -H 'content-type: application/json' \
               -d @$DEPLOY_FILE_NAME \
               ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$NEW_MARATHON_APP_ID?force=true&partialUpdate=false)"

#----------------------------------------------------------------------
# 신규 프로덕션 서버가 없을 경우
elif [ $NEW_PROD_EXIST -eq 404 ];then
   echo "$NEW_MARATHON_APP_ID server is not exist, will create new container"
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
# 신규 프로덕션 서버 디플로이 결과 확인


echo "DEPLOY RESULT is $JOB_WAIT"

#----------------------------------------------------------------------
# 신규 프로덕션 서버 디플로이 종료 대기

echo "Start $NEW_MARATHON_APP_ID deployment complete....."

# 5분동안 기다리기. 60 * 5s = 300s = 5min
MAX_COUNT=60
CURRENT_COUNT=0

while true
do
  NEW_PROD_APP="$(curl --request GET \
              -H 'content-type: application/json' \
              ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$NEW_MARATHON_APP_ID)"

  NEW_PROD_APP_STATUS="$(curl --request GET \
                -s -o /dev/null -w "%{http_code}" \
                -H 'content-type: application/json' \
                ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$NEW_MARATHON_APP_ID)"

  if [ $NEW_PROD_APP_STATUS -eq 200 ];then
     TASKS_RUNNING=$( echo $NEW_PROD_APP | jq -r '.app.tasksRunning' )
     TASKS_HEALTHY=$( echo $NEW_PROD_APP | jq -r '.app.tasksHealthy' )
     DEPLOYMENTS_LENGTH=$( echo $NEW_PROD_APP | jq -r '.app.deployments | length' )

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

        # 신규 프로덕션 앱 삭제
        curl --request DELETE \
        -H 'content-type: application/json' \
        ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$NEW_MARATHON_APP_ID?force=true

        exit 1
     fi
     sleep 5

  else
     echo "Failed to get STG_APP"
     exit 1
  fi
done

#----------------------------------------------------------------------
# DCOS APPS 에 신규 디플로이먼트 상태와 신규 마라톤 app 아이디 업데이트

APPS_JSON_FILE_NAME=dcos-apps.json
APPS_YML_FILE_NAME=dcos-apps.yml

curl --request GET \
            -H 'content-type: application/json' \
            $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/$APPS_YML_FILE_NAME/raw?ref=master > $APPS_YML_FILE_NAME


# JSON 으로 변경
ruby -ryaml -rjson -e 'puts JSON.pretty_generate(YAML.load(ARGF))' < $APPS_YML_FILE_NAME > $APPS_JSON_FILE_NAME


# 값 변경
tmp=$(mktemp)
jq '.dcos.apps["'${APP_NAME}'"].prod.deployment = "'$NEW_PROD_DEPLOYMENT'"' $APPS_JSON_FILE_NAME > "$tmp" && mv "$tmp" $APPS_JSON_FILE_NAME

tmp=$(mktemp)
jq '.dcos.apps["'${APP_NAME}'"].prod.marathonAppId = "/'$NEW_MARATHON_APP_ID'"' $APPS_JSON_FILE_NAME > "$tmp" && mv "$tmp" $APPS_JSON_FILE_NAME

# yaml 변경
ruby -ryaml -rjson -e 'puts YAML.dump(JSON.parse(STDIN.read))' < $APPS_JSON_FILE_NAME > $APPS_YML_FILE_NAME

# 깃랩 업데이트

generate_post_data()
{
  AAA=$(IFS=''
  while read data; do
      printf "%s" "$data\n"
  done < $APPS_YML_FILE_NAME)

  cat <<EOF
{
  "branch": "master",
  "commit_message": "commit",
  "content": "$AAA"
}
EOF
}

curl --request PUT \
            -H 'content-type: application/json' \
            -d "$(generate_post_data)" \
            $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/$APPS_YML_FILE_NAME

#----------------------------------------------------------------------
# ci-deploy-rollback.json 을 생성(ci-deploy-production.json 카피)

ROLLBACK_FILE_NAME=ci-deploy-rollback.json
echo $DEPLOY_JSON > $ROLLBACK_FILE_NAME

# 깃랩 업데이트

generate_json_post_data()
{
  AAA=$(IFS=''
  while read data; do
      printf "%s" "$data\n"
  done < $ROLLBACK_FILE_NAME)

  cat <<EOF
{
  "branch": "master",
  "commit_message": "commit",
  "content": "$AAA"
}
EOF
}

curl --request PUT \
            -H 'content-type: application/json' \
            -d "$(generate_json_post_data)" \
            $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/deployment%2F${APP_NAME}%2F$ROLLBACK_FILE_NAME

curl --request POST \
            -H 'content-type: application/json' \
            -d "$(generate_json_post_data)" \
            $UENGINE_CLOUD_URL/gitlab/api/v4/projects/$CONFIG_REPO_ID/repository/files/deployment%2F${APP_NAME}%2F$ROLLBACK_FILE_NAME

echo "ci-deploy-rollback.json backup complete!!"

#----------------------------------------------------------------------
# 스프링 부트인경우 라우트 변경

echo "Router refresh"

curl --request GET \
    -H 'content-type: application/json' \
    ${UENGINE_CLOUD_URL}/refreshRoute


#----------------------------------------------------------------------
# 올드 프로덕션 삭제

# echo "Remove $OLD_MARATHON_APP_ID"

# curl --request DELETE \
# -H 'content-type: application/json' \
# ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$OLD_MARATHON_APP_ID?force=true


#----------------------------------------------------------------------
# 스프링 부트가 아닐 경우 올드 프로덕션 삭제

if [ "$APP_TYPE" != "springboot" ];then
    echo "Remove $OLD_MARATHON_APP_ID"

    curl --request DELETE \
    -H 'content-type: application/json' \
    ${UENGINE_CLOUD_URL}/dcos/service/marathon/v2/apps/$OLD_MARATHON_APP_ID?force=true

fi







