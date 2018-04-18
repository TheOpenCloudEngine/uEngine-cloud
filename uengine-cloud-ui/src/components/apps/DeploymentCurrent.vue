<template>
  <div v-if="devApp">
    <md-table-card style="width: 100%">
      <div class="header-top-line"></div>
      <md-table md-sort="regDate">
        <md-table-header>
          <md-table-row>
            <md-table-head>상태</md-table-head>
            <md-table-head>이름</md-table-head>
            <md-table-head>현재 버전</md-table-head>
            <md-table-head>새 버전</md-table-head>
            <md-table-head>현재 단계</md-table-head>
            <md-table-head>상태 전이</md-table-head>
            <md-table-head>조치</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-if="data">
            <md-table-cell>
              <div v-if="data.status == 'RUNNING'">
                <span class="healthCheck running"></span>
                <span> 진행중</span>
              </div>
              <div v-else-if="data.status == 'RUNNING_ROLLBACK'">
                <span class="healthCheck rollback"></span>
                <span> 롤백중</span>
              </div>
            </md-table-cell>
            <md-table-cell>
              <a v-on:click="$emit('showDetail')">
                {{data.name}}
              </a>
            </md-table-cell>
            <md-table-cell>
              <a v-on:click="openGitlab(devApp.projectId, 'commit', data.oldVersion)">
                {{data.oldVersionText}}
              </a>
            </md-table-cell>
            <md-table-cell>
              <a v-on:click="openGitlab(devApp.projectId, 'commit', data.newVersion)">
                {{data.newVersionText}}
              </a>
            </md-table-cell>
            <!--if deploymentEndTime is null, it is on create instance-->
            <md-table-cell>
              <service-progress v-if="data.currentStep == 'deploying'" fullWidth :app="marathonApp"></service-progress>
              <span v-else>
                {{data.currentStep}}
              </span>
            </md-table-cell>
            <md-table-cell>{{data.auto}}</md-table-cell>
            <md-table-cell>
              <md-menu md-size="4" md-direction="bottom left" v-if="data.status == 'RUNNING'">
                <md-button class="md-icon-button" md-menu-trigger>
                  <md-icon>more_vert</md-icon>
                </md-button>

                <md-menu-content>
                  <md-menu-item v-on:click="rollback(appName, stage)">
                    <span>롤백</span>
                  </md-menu-item>
                </md-menu-content>
              </md-menu>
            </md-table-cell>

          </md-table-row>
        </md-table-body>
      </md-table>
    </md-table-card>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      categoryItem: Object
    },
    data() {
      return {
        data: null,
        marathonApp: null
      }
    },
    mounted() {
      var me = this;
      me.makeData();
    },
    watch: {
      stage: function () {
        this.makeData();
      },
      devApp: {
        handler: function () {
          this.makeData();
        },
        deep: true
      }
    },
    methods: {
      makeData: function () {
        var me = this;
        me.data = null;
        me.marathonApp = null;
        var stageApp = me.devApp[me.stage];
        if (stageApp.tempDeployment.status == 'RUNNING' || stageApp.tempDeployment.status == 'RUNNING_ROLLBACK') {
          me.marathonApp = me.getMarathonAppById(stageApp.marathonAppId);

          me.data = {
            status: stageApp.tempDeployment.status,
            name: stageApp.tempDeployment.name,
            oldVersion: stageApp.tempDeployment.commitOld,
            newVersion: stageApp.tempDeployment.commit,
            oldVersionText: stageApp.tempDeployment.commitOld ? stageApp.tempDeployment.commitOld.substring(0, 5) + '...' : null,
            newVersionText: stageApp.tempDeployment.commit ? stageApp.tempDeployment.commit.substring(0, 5) + '...' : null,
            currentStep: 'deploying',
            auto: null,
            deploymentEndTime: stageApp.tempDeployment.deploymentEndTime
          }

          //for canary
          if (stageApp.deploymentStrategy.canary.active) {
            me.data.auto = stageApp.deploymentStrategy.canary.auto ? '자동' : '수동'

            //if not deploymentEndTime exist, step is deploying
            if (!stageApp.tempDeployment.deploymentEndTime) {
              me.data.currentStep = 'deploying';
            }
            //if not auto, step is manual
            else if (!stageApp.deploymentStrategy.canary.auto) {
              me.data.currentStep = '대기중';
            }
            //else
            else {
              var currentTime = new Date().getTime();
              var text;
              var overTime;
              if (stageApp.tempDeployment.currentStep == 'increase') {
                text = '증가';
                overTime = currentTime - stageApp.tempDeployment.deploymentEndTime;
              }
              else if (stageApp.tempDeployment.currentStep == 'test') {
                text = '테스트';
                overTime = currentTime
                  - (stageApp.deploymentStrategy.canary.increase * 1000)
                  - stageApp.tempDeployment.deploymentEndTime;
              }
              else if (stageApp.tempDeployment.currentStep == 'decrease') {
                text = '감소';
                overTime = currentTime
                  - ((stageApp.deploymentStrategy.canary.increase + stageApp.deploymentStrategy.canary.test) * 1000)
                  - stageApp.tempDeployment.deploymentEndTime;
              }
              if (overTime) {
                me.data.currentStep = text + ' ' + Math.ceil((overTime / (1000 * 60))) + ' 분';
              } else {
                me.data.currentStep = '';
              }
            }
          }
        }
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .bar-wrapper {
    .md-button {
      width: 100%;
      margin: 0px;
    }
  }

  .md-theme-default.md-chip {
    margin-top: 8px;
  }

  .healthCheck {
    display: inline-block;
    border: solid 1px;
    border-color: grey;
    border-radius: 50%;
    width: 15px;
    height: 15px;
    text-align: center;
  }

  ,
  .running {
    background-color: #adff21;
  }

  .rollback {
    background-color: yellow;
  }
</style>
