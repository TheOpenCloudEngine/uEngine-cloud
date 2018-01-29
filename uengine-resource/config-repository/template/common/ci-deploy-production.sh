#!/bin/bash

echo "ACCESS_TOKEN: ${ACCESS_TOKEN}"
echo "UENGINE_CLOUD_URL: ${UENGINE_CLOUD_URL}"
echo "APP_NAME: ${APP_NAME}"
echo "CI_COMMIT_SHA: ${CI_COMMIT_SHA}"

#----------------------------------------------------------------------
# 배포 요청
RESULT="$(curl --request POST \
              -s -o /dev/null -w "%{http_code}" \
              -H "access_token: ${ACCESS_TOKEN}" \
              -H "content-type: application/json" \
              "${UENGINE_CLOUD_URL}/app/${APP_NAME}/deploy?stage=prod&commit=${CI_COMMIT_SHA}")"

echo "RESULT: $RESULT"

#----------------------------------------------------------------------
# 배포 요청 성공
if [ $RESULT -eq 200 ];then

   echo "Request Dev Application to cloud server succeeded."
   echo ""
   exit 0

#----------------------------------------------------------------------
# 배포 요청 실패

else

   echo "Request Dev Application to cloud server failed."
   echo ""
   exit 1
fi

