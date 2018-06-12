#!/bin/bash

# sync to github: push gitlab source codes to github, trigger project ci

# sync to gitlab: push github source codes to gitlab

echo "ACCESS_TOKEN: ${ACCESS_TOKEN}"
echo "UENGINE_CLOUD_URL: ${UENGINE_CLOUD_URL}"
echo "APP_NAME: ${APP_NAME}"
echo "SYNC_TO: ${SYNC_TO}"
echo "CI_RUN: ${CI_RUN}"
echo "GITHUB_REPO_NAME: ${GITHUB_REPO_NAME}"
echo "GITHUB_REPO_OWNER: ${GITHUB_REPO_OWNER}"
echo "GITLAB_REPO_NAME: ${GITLAB_REPO_NAME}"
echo "GITLAB_REPO_OWNER: ${GITLAB_REPO_OWNER}"
echo "GITHUB_TOKEN: ${GITHUB_TOKEN}"
echo "GITLAB_TOKEN: ${GITLAB_TOKEN}"
echo "GITLAB_URL: ${GITLAB_URL}"


if [ "${SYNC_TO}" = "github" ]; then

  echo "sync to github: push gitlab source codes to github, next, trigger project ci"

  git clone --bare http://oauth2:${GITLAB_TOKEN}@${GITLAB_URL}/${GITLAB_REPO_OWNER}/${GITLAB_REPO_NAME}
  cd ${GITLAB_REPO_NAME}.git
  git remote set-url --push origin https://x-access-token:${GITHUB_TOKEN}@github.com/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}
  git config remote.origin.fetch 'refs/heads/*:refs/heads/*'

  git --bare fetch -p origin
  git push --mirror -f

  if [ "${CI_RUN}" = "true" ]; then

      RESULT="$(curl --request POST \
                    -s -o /dev/null -w "%{http_code}" \
                    -H "access_token: ${ACCESS_TOKEN}" \
                    -H "content-type: application/json" \
                    "${UENGINE_CLOUD_URL}/app/${APP_NAME}/pipeline?ref=master&stage=dev")"

      echo "RESULT: $RESULT"

      #----------------------------------------------------------------------
      # 파이프라인 요청 성공
      if [ $RESULT -eq 200 ];then

         echo "Trigger pipeline Application to cloud server succeeded."
         echo ""
         exit 0

      #----------------------------------------------------------------------
      # 파이프라인 요청 실패

      else

         echo "Trigger pipeline Application to cloud server failed."
         echo ""
         exit 1
      fi
  fi

else

  echo "sync to gitlab: push github source codes to gitlab"

  git clone --bare https://x-access-token:${GITHUB_TOKEN}@github.com/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}
  cd ${GITHUB_REPO_NAME}.git
  git remote set-url --push origin http://oauth2:${GITLAB_TOKEN}@${GITLAB_URL}/${GITLAB_REPO_OWNER}/${GITLAB_REPO_NAME}
  git config remote.origin.fetch 'refs/heads/*:refs/heads/*'

  git --bare fetch -p origin
  git push --mirror -f

fi
