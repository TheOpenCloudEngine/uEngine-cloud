<template>
  <div v-if="detailData">
    <md-table-card style="width: 100%">
      <div class="header-top-line"></div>

      <md-layout md-gutter="16" style="padding: 8px">
        <md-layout :md-flex="detailFlex" class="vertical-right-border">

          <div style="width: 100%">
            <md-table>
              <md-table-body>
                <md-table-row>
                  <md-table-cell>Name:</md-table-cell>
                  <md-table-cell>
                    {{detailData.name}}
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell>
                    이전 버전:
                  </md-table-cell>
                  <md-table-cell>
                    <a v-on:click="openGitlab(devApp.projectId, 'commit', detailData.commitOld)">
                      {{detailData.commitOldText}}
                    </a>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell>
                    새 버전:
                  </md-table-cell>
                  <md-table-cell>
                    <a v-on:click="openGitlab(devApp.projectId, 'commit', detailData.commit)">
                      {{detailData.commitText}}
                    </a>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell>
                    배포방식:
                  </md-table-cell>
                  <md-table-cell>
                    <span v-if="detailData.strategy == 'RECREATE'">
                      기본
                    </span>
                    <span v-if="detailData.strategy == 'RAMP'">
                      롤아웃
                    </span>
                    <span v-if="detailData.strategy == 'CANARY'">
                      타이머
                      <span v-if="detailData.auto">
                        (자동)
                      </span>
                      <span v-else>
                        (수동)
                      </span>
                    </span>
                    <span v-if="detailData.strategy == 'ABTEST'">
                      테스트
                    </span>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell>
                    상태:
                  </md-table-cell>
                  <md-table-cell>
                    <div v-if="detailData.status == 'RUNNING'">
                      <span class="healthCheck running"></span>
                      <span> 진행중</span>
                    </div>
                    <div v-else-if="detailData.status == 'RUNNING_ROLLBACK'">
                      <span class="healthCheck rollback"></span>
                      <span> 롤백중</span>
                    </div>
                    <span v-if="detailData.status == 'SUCCEED'" class="success">
                      완료됨
                    </span>
                    <span v-if="detailData.status == 'ROLLBACK_SUCCEED'" class="rollback-success">
                      롤백됨
                    </span>
                    <span v-if="detailData.status == 'FAILED' || detailData.status == 'ROLLBACK_FAILED'" class="failed">
                      실패됨
                    </span>
                    <span v-if="detailData.status == 'CANCELED'" class="canceled">
                      취소됨
                    </span>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell>시작 시간:</md-table-cell>
                  <md-table-cell>
                    {{new Date(detailData.startTime)}}
                  </md-table-cell>
                </md-table-row>

                <md-table-row v-if="historyId">
                  <md-table-cell>종료 시간:</md-table-cell>
                  <md-table-cell>
                    {{new Date(detailData.endTime)}}
                  </md-table-cell>
                </md-table-row>
                <md-table-row v-if="historyId">
                  <md-table-cell>설명:</md-table-cell>
                  <md-table-cell>
                    {{detailData.description}}
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </div>
        </md-layout>


        <md-layout md-flex="60" v-if="!historyId">
          <div style="width: 100%">

            <div style="margin-top: 16px">
              <span class="md-subheading">진행 상태:</span>

              <div class="strategy-detail" v-if="detailData.appStage.deploymentStrategy.instanceStrategy == 'RECREATE'">
                <service-progress v-if="marathonApp" fullWidth :app="marathonApp"></service-progress>
              </div>

              <div class="strategy-detail" v-if="detailData.appStage.deploymentStrategy.instanceStrategy == 'RAMP'">
                <service-progress v-if="marathonApp" fullWidth :app="marathonApp"></service-progress>

                <br><br>
                <div>
                  <span class="md-caption">이전 버전</span>
                  <span class="md-subheading"> {{detailData.oldRunningCnt}} 개 </span>
                  <span class="md-caption">인스턴스 실행중. / </span>
                  <span class="md-caption">새 버전</span>
                  <span class="md-subheading"> {{detailData.newRunningCnt}} 개 </span>
                  <span class="md-caption">인스턴스 실행중.</span>
                </div>
              </div>

              <div class="strategy-detail" v-if="detailData.appStage.deploymentStrategy.instanceStrategy == 'CANARY'">
                <md-table>
                  <md-table-body>
                    <md-table-row>
                      <md-table-cell>이전 버전:</md-table-cell>
                      <md-table-cell>
                        <service-progress style="width: 100%" v-if="marathonAppOld" fullWidth
                                          :app="marathonAppOld"></service-progress>
                      </md-table-cell>
                      <md-table-cell>현재 버전:</md-table-cell>
                      <md-table-cell>
                        <service-progress style="width: 100%" v-if="marathonApp" fullWidth
                                          :app="marathonApp"></service-progress>
                      </md-table-cell>
                    </md-table-row>
                    <md-table-row>
                    </md-table-row>
                  </md-table-body>
                </md-table>

                <br>

                <div v-if="detailData.appStage.deploymentStrategy.canary.auto">
                  <div class="md-subheading">진행 단계:</div>
                  <traffic-progress
                    :editable="false"
                    :mode="'step'"
                    :increase="detailData.appStage.deploymentStrategy.canary.increase"
                    :test="detailData.appStage.deploymentStrategy.canary.test"
                    :decrease="detailData.appStage.deploymentStrategy.canary.decrease"
                    :deploymentEndTime="detailData.appStage.tempDeployment.deploymentEndTime"
                    :currentStep="detailData.appStage.tempDeployment.currentStep"
                  >
                  </traffic-progress>
                  <br><br>
                </div>

                <div class="md-subheading">현재 트래픽:</div>
                <traffic-progress v-if="detailData.appStage.deploymentStrategy.canary.auto"
                                  :editable="false"
                                  :weight.sync="detailData.appStage.deploymentStrategy.canary.weight"
                >
                </traffic-progress>
                <traffic-progress v-if="!detailData.appStage.deploymentStrategy.canary.auto"
                                  :editable="true"
                                  :weight.sync="tempWeight"
                >
                </traffic-progress>

                <md-layout md-align="end">
                  <md-button
                    v-if="tempWeightDiff"
                    v-on:click="updateTraffic" class="md-raised">
                    트래픽 저장
                  </md-button>
                </md-layout>
                <br><br>

              </div>

              <div class="strategy-detail"
                   v-if="detailData.appStage.deploymentStrategy.instanceStrategy == 'ABTEST'">

              </div>
            </div>

            <md-layout md-align="end">
              <md-button
                v-if="detailData.appStage.deploymentStrategy.bluegreen &&
                !detailData.appStage.deploymentStrategy.canary.auto"
                v-on:click="finishManualConfirm(appName, stage)" class="md-raised">
                배포 완료
              </md-button>
              <md-button
                v-if="detailData.appStage.deploymentStrategy.bluegreen &&
                detailData.appStage.deploymentStrategy.canary.auto"
                v-on:click="convertManualConfirm(appName, stage)" class="md-raised">
                수동 전환
              </md-button>
              <md-button v-on:click="rollbackAppConfirm(appName, stage)" class="md-primary md-raised">
                롤백
              </md-button>
            </md-layout>
          </div>

        </md-layout>
      </md-layout>
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
      categoryItem: Object,
      historyId: String,
      marathonApps: Object,
      deployJson: Object
    },
    computed: {},
    data() {
      return {
        detailData: null,
        detailFlex: this.historyId ? 100 : 40,
        tempWeight: null,
        tempWeightDiff: false
      }
    },
    mounted() {
      var me = this;
      me.createData();
    },
    computed: {
      marathonApp: function () {
        return this.marathonApps && this.marathonApps[this.stage] && this.marathonApps[this.stage].app ?
          this.marathonApps[this.stage].app : null;
      },
      marathonAppOld: function () {
        return this.marathonApps && this.marathonApps['oldProd'] && this.marathonApps['oldProd'].app ?
          this.marathonApps['oldProd'].app : null;
      },
      appStage: function () {
        return this.devApp ? this.devApp[this.stage] : null;
      }
    },
    watch: {
      tempWeight: function (val) {
        var weight = this.detailData.appStage.deploymentStrategy.canary.weight;
        this.tempWeightDiff = (weight != val);
      },
      devApp: {
        handler: function (val) {
          //update if current deployment detail.
          if (!this.historyId) {
            this.createData();
          }
        },
        deep: true
      },
      marathonApps: {
        handler: function () {
          if (!this.historyId) {
            this.createData();
          }
        },
        deep: true
      },
      stage: function () {
        //move to history list
        this.$emit('update:menu', 'history')
      }
    },
    methods: {
      updateTraffic: function () {
        var me = this;
        var data = JSON.parse(JSON.stringify(me.devApp));
        data[me.stage].deploymentStrategy.canary.weight = me.tempWeight;
        me.updateApp(me.appName, data, function (response) {
          if (response) {
            me.tempWeightDiff = null;
          }
        });
      },
      createData: function () {
        var me = this;
        var _addAdditionalData = function (data) {
          data.commit = data.appStage.tempDeployment.commit;
          data.commitOld = data.appStage.tempDeployment.commitOld;
          data.auto = data.appStage.deploymentStrategy.canary.auto;
          data.commitText = data.commit ? data.commit.substring(0, 8) + '...' : null;
          data.commitOldText = data.commitOld ? data.commitOld.substring(0, 8) + '...' : null;

          //if current
          if (!me.historyId) {
            //if canary && !auto
            if (data.appStage.deploymentStrategy.canary.active && !data.auto) {
              if (me.tempWeight == null) {
                me.tempWeight = data.appStage.deploymentStrategy.canary.weight;
              }
            }

            //if ramp
            if (data.appStage.deploymentStrategy.instanceStrategy == 'RAMP') {
              var oldRunningCnt = 0;
              var newRunningCnt = 0;
              var version = me.marathonApp.version;
              if (me.marathonApp.tasks.length) {
                $.each(me.marathonApp.tasks, function (i, task) {
                  if (task.version == version) {
                    newRunningCnt++;
                  } else {
                    oldRunningCnt++;
                  }
                });
              }
              if (data.appStage.tempDeployment.status == 'RUNNING') {
                data.oldRunningCnt = oldRunningCnt;
                data.newRunningCnt = newRunningCnt;
              }
              //if rollback, reverse count;
              else {
                data.oldRunningCnt = newRunningCnt;
                data.newRunningCnt = oldRunningCnt;
              }
            }
          }
        }

        me.detailData = null;
        //me.copyDevApp = JSON.parse(JSON.stringify(me.devApp));
        if (me.historyId) {
          me.$root.backend('deploymentHistory/' + me.historyId)
            .get()
            .then(function (response) {
              me.detailData = response.data;
              _addAdditionalData(me.detailData);
            })
        } else {
          var status = me.appStage.tempDeployment.status;
          //move back if deployment finish
          if (status != 'RUNNING' && status != 'RUNNING_ROLLBACK') {
            this.$emit('update:menu', 'history')
            return;
          }
          me.detailData = {
            appName: me.appName,
            appStage: me.appStage,
            description: me.appStage.tempDeployment.description,
            endTime: null,
            name: me.appStage.tempDeployment.name,
            stage: me.stage,
            startTime: me.appStage.tempDeployment.startTime,
            status: me.appStage.tempDeployment.status,
            strategy: me.appStage.deploymentStrategy.instanceStrategy
          }
          _addAdditionalData(me.detailData);
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

  .vertical-right-border {
    border-right: 1px solid #DFE3E6;
  }

  .md-input-container {
    padding-top: 16px;
    background: transparent;
  }

  .md-input-container label {
    top: 0px;
  }

  .strategy-detail {
    margin-bottom: 32px;
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

  .success {
    color: #006d18;
  }

  .rollback-success {
    color: #9b9e00;
  }

  .failed {
    color: #ac0000;
  }

  .canceled {
    color: #ac0000;
  }
</style>
