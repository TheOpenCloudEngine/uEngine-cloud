<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout v-if="categoryItem && devApp" class="bg-white">
    <div class="header-top-line"></div>
    <div v-if="isActive || gitlabDeploy" style="width: 100%">
      <md-layout>
        <md-layout v-for="(item, key) in items">
          <md-card md-with-hover style="width: 100%;">
            <md-card-area>
              <md-card-content>

                <!--원 부분-->
                <md-layout md-align="center">
                  <md-avatar v-if="item.type == 'profile'">
                    <img :src="item.image" alt="item.title">
                  </md-avatar>
                  <md-avatar v-else v-bind:class="{ 'diff': item.diff }" style="overflow: visible;">
                    <div style="position: relative;width: 100%;height: 100%;">
                      <div class="avatar-circle left-circle" v-on:click="updateResource(item, 'up')">
                        <md-icon>
                          add_circle_outline
                        </md-icon>
                      </div>
                      <div class="avatar-circle right-circle" v-on:click="updateResource(item, 'down')">
                        <md-icon>
                          remove_circle_outline
                        </md-icon>
                      </div>
                      <div class="sizing">{{item.size}}</div>
                    </div>
                  </md-avatar>
                </md-layout>

                <!--디스크립션 부분-->
                <div style="text-align: center" v-if="!item.diff">
                  <span class="md-caption" style="font-weight: bold">{{item.title}}</span>
                  <br>
                  <span v-if="item.type != 'instances' || gitlabDeploy" class="md-caption">{{item.subTitle}}</span>
                  <div v-else>
                    <service-progress fullWidth :app="marathonApp"></service-progress>

                    <!--app.deployments-->
                    <service-progress v-if="rollbackMarathonApp" fullWidth
                                      :app="rollbackMarathonApp"
                                      :rollback="true"
                    ></service-progress>
                  </div>

                </div>
                <div v-else>
                  <md-layout md-align="center">
                    <md-button v-on:click="updateDeploy" class="md-raised md-primary">저장
                    </md-button>
                    <md-button v-on:click="updateCancel" class="md-raised">재설정
                    </md-button>
                  </md-layout>
                </div>

              </md-card-content>
            </md-card-area>
          </md-card>
        </md-layout>
      </md-layout>
    </div>
    <md-layout v-else style="height: 200px">
      <md-card md-with-hover style="width: 100%;">
        <md-card-area>
          <md-card-content style="text-align: center">
            <!--여기 스피너와 문구 구분하는 조건값 확인-->
            <div v-if="isLoaded">현재 영역에 배포중인 어플리케이션이 없습니다. <a v-on:click="openGitlabDeploy">태그 또는 브랜치에서</a> 에서
              어플리케이션을 배포하세요.
            </div>
            <div v-else>
              <md-spinner :md-size="60" md-indeterminate></md-spinner>
            </div>
          </md-card-content>
        </md-card-area>
      </md-card>
    </md-layout>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'
  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      categoryItem: Object,
      gitlabDeploy: {
        type: Boolean,
        default: function () {
          return false;
        }
      }
    },
    data() {
      return {
        isLoaded: false,
        stageApp: null,
        marathonApp: null,
        rollbackMarathonApp: null,
        isActive: false,
        items: [],
        resourceUpdated: false,
        instances: 0,
        mem: 0,
        cpus: 0
      }
    },
    mounted() {
      var me = this;
      this.makeItems();
      window.busVue.$on('appRefresh', function (val) {
        me.resourceUpdated = false;
      });
    },
    watch: {
      devApp: {
        handler: function (newVal, oldVal) {
          this.makeItems();
          this.isLoaded = true;
        },
        deep: true
      }
    },
    methods: {
      openGitlabDeploy: function () {
        window.busVue.$emit('openGitlabDeploy', true);
      },
      updateApp: function (data) {
        //어플리케이션 업데이트
        var me = this;
        if (me.gitlabDeploy) {
          me.resourceUpdated = false;
          me.$emit('update:devApp', data);
        } else {
          me.updateDevApp(me.appName, data, function (response) {
            me.resourceUpdated = false;
            //스테이지 디플로이
            me.runDeployedApp(me.appName, me.stage, null, function (response) {

            });
          });
        }

      },
      updateCancel: function () {
        this.resourceUpdated = false;
        this.makeItems();
      },
      updateDeploy: function () {
        var me = this;
        var stageCopy = JSON.parse(JSON.stringify(me.stageApp));
        stageCopy['deployJson'].instances = me.instances;
        stageCopy['deployJson'].mem = me.mem;
        stageCopy['deployJson'].cpus = me.cpus;
        var data = JSON.parse(JSON.stringify(me.devApp));
        data[me.stage] = stageCopy;

        me.updateApp(data);
      },
      updateResource: function (item, updown) {
        var me = this;
        var size = item.size;
        switch (item.type) {
          case 'instances':
            if (updown == 'up') {
              me.instances++;
            } else {
              if (me.instances > 0) {
                me.instances--;
              }
            }
            break;
          case 'mem':
            if (updown == 'up') {
              me.mem = me.mem + 128;
            } else {
              if (me.mem >= 256) {
                me.mem = me.mem - 128;
              }
            }
            break;
          case 'cpus':
            if (updown == 'up') {
              me.cpus = parseFloat((me.cpus + 0.1).toFixed(1));
            } else {
              if (me.cpus > 0.1) {
                me.cpus = parseFloat((me.cpus - 0.1).toFixed(1));
              }
            }
            break;
        }
        this.makeItems();
      },
      makeItems: function () {
        var me = this;
        if (!me.categoryItem || !me.devApp) {
          return;
        }
        //isActive
        me.stageApp = me.devApp[me.stage];
        var marathonAppId = me.stageApp['marathonAppId'];
        me.marathonApp = me.getAppById(marathonAppId);

        //프로덕션인 경우 배포중 표기
        if (me.stage == 'prod') {
          var rollbackDeployment;
          var rollbackMarathonAppId;
          var deployment = me.stageApp['deployment'];
          if (deployment == 'blue') {
            rollbackMarathonAppId = '/' + me.appName + '-green';
          } else {
            rollbackMarathonAppId = '/' + me.appName + '-blue';
          }
          me.rollbackMarathonApp = me.getAppById(rollbackMarathonAppId);
        } else {
          me.rollbackMarathonApp = null;
        }

        if (me.marathonApp) {
          me.isActive = true;
        } else {
          me.isActive = false;
        }
        //최초 리소스 로딩인경우 값 배정
        if (!me.resourceUpdated) {
          me.instances = me.stageApp['deployJson'].instances;
          me.mem = me.stageApp['deployJson'].mem;
          me.cpus = me.stageApp['deployJson'].cpus;
          me.resourceUpdated = true;
        }

        me.items = [
          {
            image: me.categoryItem.logoSrc,
            title: me.categoryItem.type,
            subTitle: me.categoryItem.title,
            type: 'profile',
            size: 0,
          },
          {
            image: me.categoryItem.logoSrc,
            title: '인스턴스',
            subTitle: '',
            type: 'instances',
            size: me.instances,
            diff: me.stageApp['deployJson'].instances != me.instances
          },
          {
            image: me.categoryItem.logoSrc,
            title: '인스턴스당 메모리(MB)',
            subTitle: '',
            type: 'mem',
            size: me.mem,
            diff: me.stageApp['deployJson'].mem != me.mem
          },
          {
            image: me.categoryItem.logoSrc,
            title: '인스턴스당 CPU',
            subTitle: '',
            type: 'cpus',
            size: me.cpus,
            diff: me.stageApp['deployJson'].cpus != me.cpus
          }
        ]
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-avatar {
    width: 120px;
    min-width: 120px;
    height: 120px;
    min-height: 120px;
    margin: auto;
    display: inline-block;
    overflow: hidden;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    position: relative;
    border-radius: 50%;
    vertical-align: middle;
    border: 1px solid #5480B7;

    .sizing {
      line-height: 110px;
      text-align: center;
      width: 100%;
      height: 100%;
      font-size: 35px;
    }

    .left-circle {
      left: 0px;
    }
    .right-circle {
      right: 0px;
      left: auto;
    }
    .avatar-circle {
      position: absolute;
      top: 50%;
      .md-icon {
        background: white;
      }
    }
    .avatar-circle:hover {
      color: #4071AF;
    }
  }

  .diff {
    background: #DFE6EB;
  }
</style>
