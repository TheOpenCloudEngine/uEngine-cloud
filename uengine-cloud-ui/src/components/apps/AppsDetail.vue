<template xmlns:v-on="http://www.w3.org/1999/xhtml" xmlns:v-bind="http://www.w3.org/1999/xhtml">
  <div v-if="appName">
    <gitlab-deploy
      v-if="devApp && categoryItem"
      ref="gitlab-deploy"
      :stage="stage"
      :devApp="devApp"
      :categoryItem="categoryItem"
      :appName="appName"
      :role="'deploy'"
    ></gitlab-deploy>

    <service-deployments
      v-on:rows="onDeploymentRows"
      :appIds="['/'+ appName + '-blue', '/'+ appName + '-green', '/'+ appName + '-dev', '/'+ appName + '-stg']"
      ref="service-deployments"></service-deployments>

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
          <div style="width: 100%;padding: 16px" v-if="categoryItem">
            <div>
              <md-layout>
                <md-layout md-flex="20">
                  <div>
                    <md-avatar>
                      <img :src="categoryItem.logoSrc" alt="categoryItem.title">
                    </md-avatar>
                    <span class="md-subheading">{{appName}}</span>
                  </div>
                </md-layout>
                <md-layout md-flex="80" md-align="end">

                  <!--배포-->
                  <md-button class="md-raised"
                             @click="openDeployments">
                    <md-spinner v-if="deploymentsRowNumber > 0" :md-size="20" md-indeterminate class="md-accent"
                                style="margin-top: 5px"
                    ></md-spinner>
                    ({{deploymentsRowNumber}}) 배포중
                    <md-tooltip md-direction="bottom">배포중인 앱을 복원할 수 있습니다.</md-tooltip>
                  </md-button>

                  <!--롤백-->
                  <md-menu v-if="hasRollback" md-size="4" md-direction="bottom left">
                    <md-button class="md-raised md-primary" md-menu-trigger>롤백
                      <md-tooltip md-direction="bottom">프로덕션 앱의 New/Old 버전을 관리합니다.</md-tooltip>
                      <md-icon>arrow_drop_down</md-icon>
                    </md-button>
                    <md-menu-content style="width: 400px">
                      <md-menu-item v-if="devApp.accessLevel >= 40 || isAdmin"
                                    v-on:click="rollbackApp(appName)">
                        <span>신규 버젼을 삭제합니다. (이전 버전이 신규 버젼으로 대체됩니다.)</span>
                      </md-menu-item>
                      <md-menu-item v-if="devApp.accessLevel >= 40 || isAdmin"
                                    v-on:click="removeRollbackApp(appName)">
                        <span>이전 버전을 삭제합니다. (더이상 유지할 필요가 없습니다.)</span>
                      </md-menu-item>
                    </md-menu-content>
                  </md-menu>

                  <!--라우트-->
                  <md-button v-on:click="$refs['app-route'].open()" class="md-raised md-primary">라우트
                    <md-tooltip md-direction="bottom">앱 호스트를 목록을 봅니다.</md-tooltip>
                    <md-icon>arrow_drop_down</md-icon>
                  </md-button>
                  <app-route ref="app-route" :app="devApp"></app-route>

                  <!--스냅샷-->
                  <md-button :disabled="devApp.accessLevel < 30 && !isAdmin"
                             v-on:click="createAppSnapshot" class="md-raised md-primary">
                    <md-tooltip md-direction="bottom">앱의 스냅샷을 생성합니다..</md-tooltip>
                    <md-icon>add_a_photo</md-icon>
                  </md-button>

                  <!--재시작-->
                  <md-button :disabled="devApp.accessLevel < 30 && !isAdmin"
                             v-on:click="restartAppStage" class="md-raised md-primary">
                    <md-tooltip md-direction="bottom">앱을 재시작 합니다.</md-tooltip>
                    <md-icon>replay</md-icon>
                  </md-button>

                  <!--중지-->
                  <md-button :disabled="devApp.accessLevel < 30 && !isAdmin"
                             v-on:click="suspendAppStage" class="md-raised md-primary">
                    <md-tooltip md-direction="bottom">앱을 중지시킵니다.</md-tooltip>
                    <md-icon>pause_circle_outline</md-icon>
                  </md-button>

                  <!--삭제-->
                  <md-menu md-size="4" md-direction="bottom left">
                    <md-button :disabled="devApp.accessLevel < 30 && !isAdmin"
                               class="md-raised md-primary" md-menu-trigger>
                      <md-tooltip md-direction="bottom">앱을 삭제합니다.</md-tooltip>
                      <md-icon>delete_forever</md-icon>
                    </md-button>

                    <md-menu-content>
                      <md-menu-item v-if="devApp.accessLevel >= 40 || isAdmin"
                                    v-on:click="remove(appName)">
                        <span>어플리케이션 삭제</span>
                      </md-menu-item>
                      <md-menu-item v-if="devApp.accessLevel >= 30 || isAdmin"
                                    v-on:click="removeAppStage('dev')">
                        <span>개발 영역만 삭제</span>
                      </md-menu-item>
                      <md-menu-item v-if="devApp.accessLevel >= 40 || isAdmin"
                                    v-on:click="removeAppStage('stg')">
                        <span>스테이징 영역만 삭제</span>
                      </md-menu-item>
                      <md-menu-item v-if="devApp.accessLevel >= 40 || isAdmin"
                                    v-on:click="removeAppStage('prod')">
                        <span>프로덕션 영역만 삭제</span>
                      </md-menu-item>
                    </md-menu-content>
                  </md-menu>
                </md-layout>
              </md-layout>
            </div>
            <div>
              <md-layout>
                <md-layout md-flex="50">
                  <div style="width: 100%">
                    <md-layout>
                      <md-layout>
                        <div>
                          <span class="md-caption">
                            <md-tooltip md-direction="bottom">앱의 소유자 입니다.</md-tooltip>
                            소유자: {{devApp.iam}}
                          </span><br>
                          <span class="md-caption">
                            <md-tooltip md-direction="bottom">앱에 대한 어세스 권한입니다. 깃랩 프로젝트 멤버를 통해 조정가능합니다.</md-tooltip>
                            프로젝트 권한:
                            <span v-if="devApp.accessLevel == 50">
                              Owner
                            </span>
                            <span v-if="devApp.accessLevel == 40">
                              Master
                            </span>
                            <span v-if="devApp.accessLevel == 30">
                              Developer
                            </span>
                            <span class="md-caption" v-if="devApp.accessLevel == 20">
                              Reporter
                            </span>
                            <span class="md-caption" v-if="devApp.accessLevel == 10">
                              Guest
                            </span>
                            <span class="md-caption" v-if="devApp.accessLevel == 0">
                              None
                            </span>
                          </span><br>
                          <span class="md-caption">
                            <md-tooltip md-direction="bottom">로그인한 사용자의 클라우드 플랫폼 이용권한 입니다.</md-tooltip>
                            시스템 권한:
                          <span v-if="isAdmin">
                              관리자
                            </span>
                            <span v-else>
                              일반
                            </span>
                          </span>
                        </div>
                      </md-layout>
                      <md-layout>
                        <div style="margin-top: -10px">
                          <md-radio v-model="stage" :mdValue="'dev'" :disabled="devApp.accessLevel < 30 && !isAdmin">
                            <md-tooltip md-direction="bottom">Developer 부터 사용가능합니다.</md-tooltip>
                            <span class="md-caption">개발</span>
                          </md-radio>
                          <md-radio v-model="stage" :mdValue="'stg'" :disabled="devApp.accessLevel < 40 && !isAdmin">
                            <md-tooltip md-direction="bottom">Master, Owner 부터 사용가능합니다.</md-tooltip>
                            <span class="md-caption">스테이징</span>
                          </md-radio>
                          <md-radio v-model="stage" :mdValue="'prod'" :disabled="devApp.accessLevel < 40 && !isAdmin">
                            <md-tooltip md-direction="bottom">Master, Owner 부터 사용가능합니다.</md-tooltip>
                            <span class="md-caption">프로덕션</span>
                          </md-radio>
                        </div>
                        <div
                          v-if="hasRollback && (currentRoute == 'appsDetailRuntime' || currentRoute == 'appsDetailDashboard')"
                          style="text-align: center;margin-top: -15px">
                          <md-switch v-model="isRollback" name="my-test1" class="md-primary">
                            <span v-if="isRollback">
                              이전 버전
                            </span>
                            <span v-else>
                              신규 버전
                            </span>
                          </md-switch>
                        </div>
                      </md-layout>
                    </md-layout>
                  </div>
                </md-layout>
                <md-layout md-flex="50">
                  <div v-if="commitInfo">
                    <span
                      class="md-caption">
                      커미터: {{commitInfo.committer_name}} | 날짜: {{commitInfo.committed_date}}</span><br>
                    <span class="md-caption">
                      커밋:
                      <a v-on:click="moveGitlab('commit',commitInfo.id)">
                        <md-tooltip md-direction="bottom">커밋 이력으로 이동합니다.</md-tooltip>
                        {{commitInfo.id}}
                      </a>
                    </span><br>
                    <span class="md-caption">
                      태그:
                      <a v-if="commitInfo.tag" v-on:click="moveGitlab('tag',commitInfo.tag)">
                        <md-tooltip md-direction="bottom">태그 보기로 이동합니다.</md-tooltip>
                        {{commitInfo.tag}}
                      </a>
                    <span v-else>없음</span> |
                      <a v-on:click="openGitlabDeploy">
                        <md-tooltip md-direction="bottom">새 버전의 앱 배포합니다.</md-tooltip>
                        태그 또는 브랜치 선택하여 배포하기
                      </a>
                    </span><br>
                  </div>
                </md-layout>
              </md-layout>
            </div>
          </div>
          <div style="width: 100%;padding: 16px" class="bg-default">
            <router-view
              v-if="devApp.accessLevel >= 30 || isAdmin"
              :stage="stage"
              :devApp="devApp"
              :categoryItem="categoryItem"
              :isRollback="isRollback"
              :hasRollback="hasRollback"
              style="width: 100%"></router-view>

            <md-layout v-else class="bg-white">
              <div class="header-top-line"></div>
              <md-layout style="height: 200px">
                <md-card md-with-hover style="width: 100%;">
                  <md-card-area>
                    <md-card-content style="text-align: center">
                      <div>프로젝트에 접근 권한이 없습니다. 프로젝트 멤버또는 그룹으로 등록이 필요합니다.</div>
                    </md-card-content>
                  </md-card-area>
                </md-card>
              </md-layout>
            </md-layout>
          </div>
        </div>
      </md-layout>
    </md-layout>
    <md-layout v-else-if="status == 'repository-create-failed'">
      <md-card md-with-hover style="width: 100%;">
        <md-card-area>
          <md-card-content style="text-align: center">
            <div>
              레파지토리 생성에 실패했습니다.
              <a v-on:click="remove(appName)">
                <md-tooltip md-direction="bottom">생성시 사용된 모든 자원을 삭제합니다.</md-tooltip>
                어플리케이션 삭제하기
              </a>
            </div>
          </md-card-content>
        </md-card-area>
      </md-card>
    </md-layout>
    <md-layout :md-gutter="16" v-else-if="status == 'repository-create'">
      <md-card md-with-hover style="width: 100%;">
        <md-card-area>
          <md-card-content style="text-align: center">
            <div>
              <md-spinner :md-size="20" md-indeterminate class="md-accent"></md-spinner>
              레파지토리 생성중입니다.
            </div>
          </md-card-content>
        </md-card-area>
      </md-card>
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
        isAdmin: false,
        pipeline: null,
        hasRollback: false,
        isRollback: false,
        tagList: [],
        commitInfo: null,
        currentRoute: 'appsDetailDashboard',
        interval: true,
        appRoutes: [],
        stage: 'dev',
        categoryItem: null,
        devApp: null,
        status: null,
        deploymentsRowNumber: 0,
        items: [
          {title: '시작하기', icon: 'question_answer', routerName: 'appsDetailDocs'},
          {title: '개요', icon: 'question_answer', routerName: 'appsDetailDashboard'},
          {title: '런타임 및 환경', icon: 'question_answer', routerName: 'appsDetailRuntime'},
          {title: '소스코드', icon: 'question_answer', routerName: 'gitlab'},
          {title: '빌드 및 배포', icon: 'question_answer', routerName: 'appsDetailDeployment'},
          {title: '스냅샷', icon: 'question_answer', routerName: 'appsDetailSnapshot'},
          {title: '로그', icon: 'question_answer', routerName: 'appsDetailLog'},
          {title: '모니터링', icon: 'question_answer', routerName: 'appsDetailMonitor'},
          {title: '레지스트리', icon: 'question_answer', routerName: 'registry'},
          {title: '변경이력', icon: 'question_answer', routerName: 'appLogsList'}
        ],
      }
    },
    mounted() {
      var me = this;
      me.isAdmin = window.localStorage['acl'] == 'admin' ? true : false;
      me.categoryItem = null;

      window.busVue.$on('openGitlabDeploy', function (val) {
        me.openGitlabDeploy();
      });

      this.updateActive();

      var intervalDevApp = function () {
        me.getDevAppByName(me.appName, function (response, error) {
          if (response) {
            me.devApp = response.data;
            me.status = response.data['createStatus'];
            if (!me.categoryItem) {
              me.getCategoryItem(me.devApp.appType, function (item) {
                me.categoryItem = item;
              });
            }
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
      openDeployments: function () {
        this.$refs['service-deployments'].open();
      },
      onDeploymentRows: function (rowNumbers) {
        this.deploymentsRowNumber = rowNumbers;
      },
      //백엔드로 가져갈 필요성 있음.
      updateCIInfo: function () {
        var me = this;
        var projectId = me.devApp.projectId;
        me.$root.gitlab('api/v4/projects/' + projectId + '/pipelines?page=1&per_page=1').get()
          .then(function (response) {
            if (response.data && response.data.length) {
              me.pipeline = response.data[0];
            }
          })
      },
      rollbackApp: function () {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: '신규 프로덕션 앱 버전을 삭제합니다. 진행하시겠습니까?',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.rollbackDevApp(me.appName, function (response) {

              })
            }
          });
      },
      removeRollbackApp: function () {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: '이전 버전의 앱을 삭제합니다. 진행하시겠습니까?',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.removeRollbackDevApp(me.appName, function (response) {

              })
            }
          });
      },
      openGitlabDeploy: function () {
        this.$refs['gitlab-deploy'].open();
      },
      updateRollbackInfo: function () {
        var me = this;
        var marathonApps = me.getAppsByDevopsId(me.appName);
        me.hasRollback = false;
        if (me.stage == 'prod' && marathonApps && marathonApps.oldProd) {
          me.hasRollback = true;
        }
      },
      updateCommitInfo: function () {
        var me = this;
        if (!me.devApp) {
          me.commitInfo = null;
          return;
        }
        var projectId = me.devApp.projectId;
        var marathonAppId = me.devApp[me.stage]['marathonAppId'];
        var marathonApp = me.getDcosAppById(marathonAppId);
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
        me.$root.$children[0].confirm(
          {
            contentHtml: '앱을 재시작합니다.',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.runDeployedApp(me.appName, me.stage, null, function (response) {

              });
            }
          });
      },
      suspendAppStage: function () {
        var me = this;
        if (!me.devApp) {
          return;
        }
        me.$root.$children[0].confirm(
          {
            contentHtml: '앱을 중지합니다. 배포된 앱이 삭제되지는 않으며, 스케일 조정을 통해 복구하실 수 있습니다.',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              var data = JSON.parse(JSON.stringify(me.devApp));
              data[me.stage]['deployJson'].instances = 0;
              //업데이트
              me.updateDevApp(me.appName, data, function (response) {
                window.busVue.$emit('appRefresh', true);
                //스테이지 디플로이
                me.runDeployedApp(me.appName, me.stage, null, function (response) {
                });
              });
            }
          });
      },
      removeAppStage: function (stage) {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: stage + ' 서버에 배포된 앱을 완전히 삭제합니다. 복구를 위해서는 재배포가 필요합니다.',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.removeDevAppStage(me.appName, stage);
            }
          });
      },
      remove: function (appName) {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: '앱과, 소스코드를 완전히 삭제합니다. (주의: 중요 소스코드는 백업해두세요)',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.removeDevAppByName(appName, function () {
                me.$router.push(
                  {
                    name: 'appsOverview'
                  }
                )
              });
            }
          });
      },
      createAppSnapshot: function () {
        //createSnapshot
        var me = this;
        var today = new Date();
        var defaultSnapshotName = today.getFullYear() + '-' + (today.getMonth() + 1) + '-' + today.getDate()
          + ' ' + me.appName + ' Snapshot';
        me.$root.$children[0].confirm(
          {
            contentHtml: '스냅샷을 생성합니다.',
            okText: '진행하기',
            cancelText: '취소',
            prompt: true,
            promptValue: defaultSnapshotName,
            promptLabel: '스냅샷',
            callback: function (snapshotName) {
              me.createSnapshot(me.appName, snapshotName, function (response) {
                window.busVue.$emit('snapshotCreated', true);
              })
            }
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
        this.getProject(this.devApp.projectId, function (response, err) {
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
