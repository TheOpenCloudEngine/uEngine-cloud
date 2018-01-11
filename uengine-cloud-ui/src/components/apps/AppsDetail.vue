<template xmlns:v-on="http://www.w3.org/1999/xhtml" xmlns:v-bind="http://www.w3.org/1999/xhtml">
  <div v-if="appName">
    <gitlab-deploy
      v-if="devApp"
      ref="gitlab-deploy"
      :stage="stage"
      :devApp="devApp"
      :catalogItem="catalogItem"
      :appName="appName"
      :role="'deploy'"
    ></gitlab-deploy>

    <md-layout v-if="status == 'repository-create-success'">
      <md-layout md-flex="15" style="border-right: 1px solid #DFE3E6;padding: 16px">
        <div>
          <div v-for="item in items">
            <div>
              <a class="md-body-1" v-bind:class="{ 'active': item.isActive }"
                 v-on:click="move(item.routerName)">
                {{item.title}}
              </a>
              <span v-if="pipeline && item.routerName == 'appsDetailDeployment'">
                <md-tooltip md-direction="right">Last CI is {{pipeline.status}}</md-tooltip>
                <md-spinner v-if="pipeline.status == 'running'" :md-size="20" md-indeterminate
                            class="md-accent"></md-spinner>
                <md-icon v-else-if="pipeline.status == 'success'" class="md-primary">check_circle</md-icon>
                <md-icon v-else-if="pipeline.status == 'pending'" class="md-warn">info</md-icon>
                <md-icon v-else-if="pipeline.status == 'failed'" class="md-accent">cancel</md-icon>
                <md-icon v-else class="md-warn">info</md-icon>
              </span>
            </div>
            <br>
          </div>
        </div>
      </md-layout>
      <md-layout md-flex="85">
        <div style="width: 100%">
          <div style="width: 100%;padding: 16px" v-if="catalogItem">
            <div>
              <md-layout>
                <md-layout md-flex="40">
                  <div>
                    <md-avatar>
                      <img :src="catalogItem.image" alt="catalogItem.title">
                    </md-avatar>
                    <span class="md-subheading">{{appName}}</span>
                  </div>
                </md-layout>
                <md-layout md-flex="60" md-align="end">

                  <!--롤백-->
                  <md-button v-if="hasRollback" v-on:click="rollbackApp" class="md-raised md-primary">
                    롤백
                  </md-button>

                  <!--라우트-->
                  <md-button v-on:click="$refs['app-route'].open()" class="md-raised md-primary">라우트
                    <md-icon>arrow_drop_down</md-icon>
                  </md-button>
                  <app-route ref="app-route" :app="devApp"></app-route>

                  <!--재시작-->
                  <md-button v-on:click="restartAppStage" class="md-raised md-primary">
                    <md-icon>replay</md-icon>
                  </md-button>

                  <!--중지-->
                  <md-button v-on:click="suspendAppStage" class="md-raised md-primary">
                    <md-icon>pause_circle_outline</md-icon>
                  </md-button>

                  <!--삭제-->
                  <md-menu md-size="4" md-direction="bottom left">
                    <md-button class="md-raised md-primary" md-menu-trigger>
                      <md-icon>delete_forever</md-icon>
                    </md-button>

                    <md-menu-content>
                      <md-menu-item v-on:click="remove(appName)">
                        <span>어플리케이션 삭제</span>
                      </md-menu-item>
                      <md-menu-item v-on:click="removeAppStage('dev')">
                        <span>개발 영역만 삭제</span>
                      </md-menu-item>
                      <md-menu-item v-on:click="removeAppStage('stg')">
                        <span>스테이징 영역만 삭제</span>
                      </md-menu-item>
                      <md-menu-item v-on:click="removeAppStage('prod')">
                        <span>프로덕션 영역만 삭제</span>
                      </md-menu-item>
                    </md-menu-content>
                  </md-menu>
                </md-layout>
              </md-layout>
            </div>
            <div v-if="currentRoute != 'appsDetailDeployment'">
              <md-layout>
                <md-layout md-flex="50">
                  <div>
                    <span class="md-caption">소유자: {{devApp.iam}}</span>
                    <span style="margin-left: 16px">
                      <md-radio v-model="stage" :mdValue="'dev'">
                        <span class="md-caption">개발</span>
                      </md-radio>
                      <md-radio v-model="stage" :mdValue="'stg'">
                        <span class="md-caption">스테이징</span>
                      </md-radio>
                      <md-radio v-model="stage" :mdValue="'prod'">
                        <span class="md-caption">프로덕션</span>
                      </md-radio>
                    </span>
                  </div>
                </md-layout>
                <md-layout md-flex="50">
                  <div v-if="commitInfo">
                    <span
                      class="md-caption">커미터: {{commitInfo.committer_name}} | 날짜: {{commitInfo.committed_date}}</span><br>
                    <span class="md-caption">커밋: <a
                      v-on:click="moveGitlab('commit',commitInfo.id)">{{commitInfo.id}}</a></span><br>
                    <span class="md-caption">태그:
                      <a v-if="commitInfo.tag" v-on:click="moveGitlab('tag',commitInfo.tag)">{{commitInfo.tag}}</a>
                      <span v-else>없음</span> |
                      <a v-on:click="openGitlabDeploy">태그 또는 브랜치 선택하여 배포하기</a></span><br>
                  </div>
                </md-layout>
              </md-layout>
            </div>
          </div>
          <div style="width: 100%;padding: 16px" class="bg-default">
            <router-view
              :stage="stage"
              :devApp="devApp"
              :catalogItem="catalogItem"
              style="width: 100%"></router-view>
          </div>
        </div>
      </md-layout>
    </md-layout>
    <md-layout v-else-if="status == 'repository-create-failed'">
      레파지토리 생성에 실패했습니다.
      <a v-on:click="remove(appName)">어플리케이션 삭제하기</a>
    </md-layout>
    <md-layout :md-gutter="16" v-else-if="status == 'repository-create'">
      레파지토리 생성중입니다.
    </md-layout>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'
  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {},
    data() {
      return {
        pipeline: null,
        hasRollback: false,
        tagList: [],
        commitInfo: null,
        currentRoute: 'appsDetailDashboard',
        interval: true,
        appRoutes: [],
        stage: 'dev',
        catalogItem: null,
        devApp: null,
        status: null,
        items: [
          {title: '시작하기', icon: 'question_answer', routerName: 'appsDetailDocs'},
          {title: '개요', icon: 'question_answer', routerName: 'appsDetailDashboard'},
          {title: '런타임 및 환경', icon: 'question_answer', routerName: 'appsDetailRuntime'},
          {title: '소스코드', icon: 'question_answer', routerName: 'gitlab'},
          {title: '빌드 및 배포', icon: 'question_answer', routerName: 'appsDetailDeployment'},
          {title: '로그', icon: 'question_answer', routerName: 'appsDetailLog'},
          {title: '모니터링', icon: 'question_answer', routerName: 'appsDetailMonitor'},
          {title: '레지스트리', icon: 'question_answer', routerName: 'registry'},
          {title: '변경이력', icon: 'question_answer', routerName: 'appLogsList'}
        ],
      }
    },
    mounted() {
      var me = this;

      window.busVue.$on('openGitlabDeploy', function (val) {
        me.openGitlabDeploy();
      });

      this.updateActive();

      //어플리케이션 레파지토리 생성 상태와 어플리케이션 정보를 풀링한다.
      var intervalStatus = function () {
        console.log('how many intervalStatus?');
        me.getDevAppStatusByName(me.appName, function (response, error) {
          if (response) {
            me.status = response.data.status;
          } else {
            me.status = null;
          }
          if (me.interval && me.status != 'repository-create-success') {
            setTimeout(function () {
              intervalStatus();
            }, 2000);
          }
        });
      };
      intervalStatus();

      var intervalDevApp = function () {
        me.getDevAppByName(me.appName, function (response, error) {
          if (response) {
            me.devApp = response.data;
            me.getCategoryItemById(me.devApp.appType, function (catalogItem) {
              me.catalogItem = catalogItem;
            });
          } else {
            me.devApp = null;
          }
          if (me.interval) {
            setTimeout(function () {
              intervalDevApp();
            }, 2000);
          }
        });
      };
      intervalDevApp();
    },
    destroyed: function () {
      this.interval = false;
    },
    watch: {
      '$route'(to, from) {
        this.updateActive();
      },
      stage: function (val) {
        var me = this;
        me.getDevAppByName(me.appName, function (response, error) {
          if (response) {
            me.devApp = response.data;
            me.getCategoryItemById(me.devApp.appType, function (catalogItem) {
              me.catalogItem = catalogItem;
            });
          } else {
            me.devApp = null;
          }
        });
      },
      devApp: {
        handler: function (newVal, oldVal) {
          this.updateCommitInfo();
          this.updateRollbackInfo();
          this.updateCIInfo();
        },
        deep: true
      }
    },
    methods: {
      updateCIInfo: function () {
        var me = this;
        var projectId = me.devApp.gitlab.projectId;
        me.$root.gitlab('api/v4/projects/' + projectId + '/pipelines?page=1&per_page=1').get()
          .then(function (response) {
            if (response.data && response.data.length) {
              me.pipeline = response.data[0];
            }
          })
      },
      rollbackApp: function () {
        this.rollbackDevApp(this.appName, function (response) {

        })
      },
      openGitlabDeploy: function () {
        this.$refs['gitlab-deploy'].open();
      },
      updateRollbackInfo: function () {
        var me = this;
        var marathonApps = me.getAppsByDevopsId(me.appName);
        me.hasRollback = false;
        if (me.stage == 'prod' && marathonApps && marathonApps.newProd && marathonApps.newProd.deployments.length == 0) {
          me.hasRollback = true;
        }
      },
      updateCommitInfo: function () {
        var me = this;
        if (!me.devApp) {
          me.commitInfo = null;
          return;
        }
        var projectId = me.devApp.gitlab.projectId;
        var marathonAppId = me.devApp[me.stage]['marathonAppId'];
        var marathonApp = me.getAppById(marathonAppId);
        if (!marathonApp) {
          me.commitInfo = null;
          return;
        }
        var dockerImage = marathonApp.container.docker.image;
        var split = dockerImage.split(':');
        var commitId = split[split.length - 1];
        me.tagList = [];
        var commitInfo;

        me.$root.gitlab('api/v4//projects/' + projectId + '/repository/commits/' + commitId).get()
          .then(function (response) {
            commitInfo = response.data;
            me.$root.gitlab('api/v4//projects/' + projectId + '/repository/tags').get()
              .then(function (response) {
                me.tagList = response.data;
                $.each(me.tagList, function (i, tag) {
                  if (tag.commit.id == commitInfo.id) {
                    commitInfo.tag = tag.name;
                  }
                });
                me.commitInfo = commitInfo;
              });
          }, function () {
            me.commitInfo = null;
          });
      },
      updateActive: function () {
        var me = this;
        var routers = me.$route.matched;
        $.each(me.items, function (i, item) {
          var isActive = false;
          $.each(routers, function (r, router) {
            if (router.name == item.routerName) {
              isActive = true;
              me.currentRoute = router.name;
            }
          });
          item.isActive = isActive;
        });
      },
      restartAppStage: function () {
        var me = this;
        var marathonAppId = me.devApp[me.stage]['marathonAppId'];
        me.restartApp(marathonAppId, true, function (response) {

        });
      },
      suspendAppStage: function () {
        var me = this;
        if (!me.devApp) {
          return;
        }
        var data = JSON.parse(JSON.stringify(me.devApp));
        data[me.stage]['deploy-json'].instances = 0;
        //업데이트
        me.updateDevApp(me.appName, data, function (response) {
          window.busVue.$emit('appRefresh', true);
          //스테이지 디플로이
          me.runDeployedApp(me.appName, me.stage, null, function (response) {
          });
        });
      },
      removeAppStage: function (stage) {
        this.removeDevAppStage(this.appName, stage);
      },
      remove: function (appName) {
        var me = this;
        this.removeDevAppByName(appName, function () {
          me.$router.push(
            {
              name: 'appsOverview'
            }
          )
        });
      },
      move: function (routeName) {
        var me = this;
        if (routeName == 'gitlab') {
          me.moveGitlab('project');
        }
        else if (routeName == 'registry') {
          me.moveRegistry();
        }
        else {
          this.$router.push(
            {
              name: routeName,
              params: {appName: me.appName}
            }
          )
        }
      },
      moveRegistry: function () {
        var url = 'http://' + config.vcap.services['eureka-server'].external;
        window.open(url);
      },
      moveGitlab: function (type, objectId) {
        this.getProject(this.devApp.gitlab.projectId, function (response, err) {
          var url = response.data.web_url;
          if (type == 'project') {
            window.open(url);
          } else if (type == 'commit') {
            url = url + '/commit/' + objectId;
            window.open(url);
          } else if (type == 'tag') {
            url = url + '/tags/' + objectId;
            window.open(url);
          }
        });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-theme-default a:not(.md-button) {
    color: #162A35;
  }

  .md-theme-default a:not(.md-button).active {
    color: #4071AF;
  }

  /*#4071AF*/
</style>
