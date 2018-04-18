<template>
  <div v-if="copyDevApp">
    <md-table-card style="width: 100%">
      <div class="header-top-line"></div>

      <md-layout md-gutter="16" style="padding: 8px">
        <md-layout md-flex="40" class="vertical-right-border">

          <div style="width: 100%">
            <info-box :border="true">
              <template>
                현재 버젼: <span v-if="!currentCommitRef">없음</span>
                <commit-info :project-id="copyDevApp.projectId" :commit-ref="currentCommitRef"></commit-info>
              </template>
            </info-box>
            <br>
            <md-input-container
              v-bind:class="{ 'md-input-invalid': selectedVersionIndex == null }">
              <label>새 버전 선택</label>
              <md-select v-model="selectedVersionIndex" required>
                <md-option v-for="(version, index) in versionList" :value="index">
                  {{version.name}}
                </md-option>
              </md-select>
              <span class="md-error">새 버전이 필요합니다.</span>
            </md-input-container>

            <md-input-container>
              <label>이름(선택사항)</label>
              <md-input v-model="params.name" type="text"></md-input>
            </md-input-container>

            <md-input-container>
              <label>설명(선택사항)</label>
              <md-textarea v-model="params.description" type="textarea"></md-textarea>
            </md-input-container>
          </div>
        </md-layout>

        <md-layout md-flex="60">
          <div style="width: 100%">

            <md-layout class="bar-wrapper">
              <md-layout v-for="strategy in instanceStrategy" md-flex="25">
                <md-button class="md-raised"
                           v-bind:class="{ 'md-primary': appStage.deploymentStrategy.instanceStrategy == strategy.val }"
                           v-on:click="changeInstanceStrategy(strategy.val)">{{strategy.text}}
                </md-button>
              </md-layout>
            </md-layout>

            <div style="margin-top: 16px">
              <div class="strategy-detail" v-if="appStage.deploymentStrategy.instanceStrategy == 'RAMP'">
                <div>
                  <span class="md-caption">전체</span>
                  <span class="md-subheading"> {{maxInstances}} 개 </span>
                  <span class="md-caption">인스턴스 중</span>
                  <span class="md-subheading"> {{capacityInstances}} 개 </span>
                  <span class="md-caption">씩 인스턴스 교체</span>
                </div>
                <md-layout>
                  <md-layout md-flex="70">
                    <vue-slider
                      width="100%"
                      :tooltip="false"
                      :min="0"
                      :max="10"
                      :height="10"
                      :dot-size="12"
                      ref="slider" v-model="maximumOverCapacity"></vue-slider>
                  </md-layout>
                  <md-layout md-flex="30">
                    {{(maximumOverCapacity * 10) + '%'}}
                  </md-layout>
                </md-layout>
              </div>

              <div class="strategy-detail"
                   v-if="appStage.deploymentStrategy.instanceStrategy == 'CANARY' && enableStrategyUse">
                <div v-if="appStage.deploymentStrategy.canary.auto">
                  <md-layout md-gutter="16">
                    <md-layout class="vertical-right-border"
                               v-for="phase in ['increase','test','decrease']">
                      <div>
                      <span v-if="phase == 'increase'">
                        증가 단계:
                      </span>
                        <span v-else-if="phase == 'test'">
                        테스트 단계:
                      </span>
                        <span v-else-if="phase == 'decrease'">
                        감소 단계:
                      </span>
                      </div>
                      <div>
                        <md-layout md-gutter="16">
                          <md-layout>
                            <md-input-container>
                              <label>시</label>
                              <md-input v-if="phase == 'increase'" min="0" type="number"
                                        v-model.number="increaseH"></md-input>
                              <md-input v-else-if="phase == 'test'" min="0" type="number"
                                        v-model.number="testH"></md-input>
                              <md-input v-else-if="phase == 'decrease'" min="0" type="number"
                                        v-model.number="decreaseH"></md-input>
                            </md-input-container>
                          </md-layout>
                          <md-layout>
                            <md-input-container>
                              <label>분</label>
                              <md-input v-if="phase == 'increase'" min="0" type="number"
                                        v-model.number="increaseM"></md-input>
                              <md-input v-else-if="phase == 'test'" min="0" type="number"
                                        v-model.number="testM"></md-input>
                              <md-input v-else-if="phase == 'decrease'" min="0" type="number"
                                        v-model.number="decreaseM"></md-input>
                            </md-input-container>
                          </md-layout>
                        </md-layout>
                      </div>
                    </md-layout>
                    <md-layout>
                      <div>총 {{parseInt(canaryTotalTime / 60)}} 시간 {{canaryTotalTime % 60}}분</div>
                    </md-layout>
                  </md-layout>
                </div>
                <div v-else>
                  <div class="md-subheading">시작 트래픽:</div>
                  <traffic-progress
                    editable
                    :weight.sync="weight"
                  >
                  </traffic-progress>
                  <br><br>
                </div>

                <div>상태전이:</div>
                <md-layout>
                  <md-layout md-flex="30">
                    <div>
                      <md-button-toggle md-single>
                        <md-button class="md-raised"
                                   v-bind:class="{ 'md-primary': appStage.deploymentStrategy.canary.auto}"
                                   v-on:click="appStage.deploymentStrategy.canary.auto = true">자동
                        </md-button>
                        <md-button class="md-raised"
                                   v-bind:class="{ 'md-primary': !appStage.deploymentStrategy.canary.auto}"
                                   v-on:click="appStage.deploymentStrategy.canary.auto = false">수동
                        </md-button>
                      </md-button-toggle>
                    </div>
                  </md-layout>
                  <md-layout md-flex="70">
                    <info-box style="width: 100%">
                      <template>
                        <div v-if="appStage.deploymentStrategy.canary.auto">
                          자동 상태 전이를 사용하면 트래픽을 수동으로 조정 할 필요없이 단계 사이를 이동할 수 있습니다.
                        </div>
                        <div v-else>
                          수동 상태 전이를 사용하면 배포 기간동안 트래픽을 자유롭게 조정할 수 있으며, 수동으로 배포를 종료해야 합니다.
                        </div>
                      </template>
                    </info-box>
                  </md-layout>
                </md-layout>
              </div>

              <div class="strategy-detail"
                   v-if="appStage.deploymentStrategy.instanceStrategy == 'ABTEST' && enableStrategyUse">

              </div>
            </div>


            <md-divider></md-divider>

            <div v-if="enableStrategyUse">
              <md-layout>
                <md-checkbox v-model="changeResource" class="md-primary">컴퓨팅 자원 변경
                </md-checkbox>
              </md-layout>

              <app-runtime-card v-if="changeResource"
                                :stage="stage"
                                :devApp.sync="copyDevApp"
                                :categoryItem="categoryItem"
                                :deploy="false"
                                style="margin-bottom: 32px"
              ></app-runtime-card>


              <md-layout>
                <md-layout md-flex="60">
                  <info-box style="width: 100%">
                    <template>
                      {{infoText}}
                    </template>
                  </info-box>
                </md-layout>
                <md-layout md-flex="40" md-align="end">
                  <div>
                    <md-button v-on:click="cancel" class="md-raised">
                      취소
                    </md-button>
                    <md-button v-on:click="run" class="md-primary md-raised">
                      시작
                    </md-button>
                  </div>
                </md-layout>
              </md-layout>
            </div>
            <div v-else>
              <info-box type="warn">
                <template>
                  먼저 기본 배포 를 통해서 현재 버전을 생성 후 사용할 수 있습니다.
                </template>
              </info-box>
            </div>
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
      menu: String
    },
    computed: {
      increaseM: {
        get: function () {
          var increase = this.appStage.deploymentStrategy.canary.increase;
          return increase % 60;
        },
        set: function (val) {
          this.appStage.deploymentStrategy.canary.increase = (this.increaseH * 60) + val;
        }
      },
      increaseH: {
        get: function () {
          var increase = this.appStage.deploymentStrategy.canary.increase;
          return parseInt(increase / 60);
        },
        set: function (val) {
          this.appStage.deploymentStrategy.canary.increase = (val * 60) + this.increaseM;
        }
      },
      testM: {
        get: function () {
          var test = this.appStage.deploymentStrategy.canary.test;
          return test % 60;
        },
        set: function (val) {
          this.appStage.deploymentStrategy.canary.test = (this.testH * 60) + val;
        }
      },
      testH: {
        get: function () {
          var test = this.appStage.deploymentStrategy.canary.test;
          return parseInt(test / 60);
        },
        set: function (val) {
          this.appStage.deploymentStrategy.canary.test = (val * 60) + this.testM;
        }
      },
      decreaseM: {
        get: function () {
          var decrease = this.appStage.deploymentStrategy.canary.decrease;
          return decrease % 60;
        },
        set: function (val) {
          this.appStage.deploymentStrategy.canary.decrease = (this.decreaseH * 60) + val;
        }
      },
      decreaseH: {
        get: function () {
          var decrease = this.appStage.deploymentStrategy.canary.decrease;
          return parseInt(decrease / 60);
        },
        set: function (val) {
          this.appStage.deploymentStrategy.canary.decrease = (val * 60) + this.decreaseM;
        }
      },
    },
    data() {
      return {
        enableStrategyUse: true,
        weight: 0,
        maximumOverCapacity: 0,
        instanceStrategy: [],
        instanceStrategyData: [
          {
            val: 'RECREATE',
            text: '기본',
            infoText: '이전 버전이 즉시 종료되고 신규 버전이 배포됩니다.'
          },
          {
            val: 'RAMP',
            text: '롤아웃',
            infoText: '이전 버전과 신규 버전이 느리게 교체됩니다.'
          },
          {
            val: 'CANARY',
            text: '타이머',
            infoText: '이전 버전과 신규 버전 사이의 트래픽을 타이머로 조정합니다.'
          },
          {
            val: 'ABTEST',
            text: '테스트',
            infoText: '이전 버전과 신규 버전 사이의 트래픽을 테스트 조건에 따라 나눕니다.'
          }],
        copyDevApp: null,
        appStage: null,
        tagList: [],
        imageList: [],
        selectedVersion: null,
        selectedVersionIndex: null,
        versionList: [],
        currentCommitRef: null,
        params: {
          name: null,
          description: null
        },
        changeResource: false,
        infoText: '',
        maxInstances: 0,
        capacityInstances: 0,
        canaryTotalTime: 0
      }
    },
    mounted() {
      var me = this;
      me.createData();
    },
    watch: {
      copyDevApp: {
        handler: function (val) {
          this.appStage = this.copyDevApp[this.stage];
          this.setCapacityRate();
          this.setCanaryTotalTime();
        },
        deep: true
      },
      maximumOverCapacity: function (val) {
        this.setCapacityRate(val);
      },
      'appStage.deploymentStrategy.instanceStrategy': function (val) {
        var me = this;
        me.infoText = '';
        $.each(me.instanceStrategy, function (i, strategy) {
          if (strategy.val == val) {
            me.infoText = strategy.infoText;
          }
        })
        me.setEnableStrategyUse();
      },
      stage: function () {
        this.createData();
      },
      selectedVersionIndex: function (val) {
        this.selectedVersion = this.versionList[val];
      }
    },
    methods: {
      setEnableStrategyUse: function () {
        var me = this;
        me.enableStrategyUse = true;
        //if not deployed anything yet, disable canary and abtest
        if (me.stage == 'prod' && me.appStage.marathonAppId &&
          (me.appStage.deploymentStrategy.instanceStrategy == 'CANARY' ||
            me.appStage.deploymentStrategy.instanceStrategy == 'ABTEST')
        ) {
          var marathonApp = me.getMarathonAppById(me.appStage.marathonAppId);
          if (!marathonApp) {
            me.enableStrategyUse = false;
          } else {
            me.enableStrategyUse = true;
          }
        }
      },
      setCanaryTotalTime: function () {
        this.canaryTotalTime =
          this.appStage.deploymentStrategy.canary.increase +
          this.appStage.deploymentStrategy.canary.test +
          this.appStage.deploymentStrategy.canary.decrease
      },
      setCapacityRate: function (val) {
        var me = this;
        //슬라이더에 의해 조정된 경우
        if (val != undefined) {
          me.appStage.deploymentStrategy.ramp.maximumOverCapacity = val / 10;
        } else {
          me.maximumOverCapacity = me.appStage.deploymentStrategy.ramp.maximumOverCapacity * 10;
        }
        me.maxInstances = me.appStage.deployJson.instances;
        me.capacityInstances = me.appStage.deployJson.instances * me.appStage.deploymentStrategy.ramp.maximumOverCapacity;
        me.capacityInstances = Math.ceil(me.capacityInstances);
        //
        //비율이 0% 이고, 인스턴스 세팅이 1 개 이상이면 최소 1개 인스턴스 표기.
        if (me.capacityInstances == 0 && me.appStage.deployJson.instances > 0) {
          me.capacityInstances = 1;
        }
      },
      changeInstanceStrategy: function (val) {
        this.appStage.deploymentStrategy.instanceStrategy = val;
      },
      stageName: function (stage) {
        if (stage == 'dev') {
          return '개발'
        }
        else if (stage == 'stg') {
          return '스테이징'
        }
        else if (stage == 'prod') {
          return '프로덕션'
        }
      },
      createData: function () {
        var me = this;
        me.copyDevApp = JSON.parse(JSON.stringify(me.devApp));
        me.appStage = me.copyDevApp[me.stage];

        me.enableStrategyUse = true;
        me.instanceStrategy = [];
        if (me.stage == 'prod') {
          me.instanceStrategy.push(me.instanceStrategyData[0]);
          me.instanceStrategy.push(me.instanceStrategyData[1]);
          me.instanceStrategy.push(me.instanceStrategyData[2]);
          me.instanceStrategy.push(me.instanceStrategyData[3]);

        } else {
          me.instanceStrategy.push(me.instanceStrategyData[0]);
          me.instanceStrategy.push(me.instanceStrategyData[1]);
        }

        me.setEnableStrategyUse();
        me.setCapacityRate();
        me.setCanaryTotalTime();
        me.updateRefs();
      },
      updateRefs: function () {
        var me = this;
        this.selectedVersionIndex = null;
        this.tagList = [];
        this.hasImage = false;

        var projectId = me.copyDevApp.projectId;
        var p1 = this.$root.gitlab('api/v4//projects/' + projectId + '/repository/tags').get();
        var p2 = this.$root.backend('app/' + me.appName + '/tags').get();

        Promise.all([p1, p2])
          .then(function ([r1, r2]) {
            me.tagList = r1.data;
            me.imageList = r2.data.tags;
            me.createVersionList();
          });
      },
      createVersionList: function () {
        var me = this;
        me.versionList = [];

        //버전이 없을경우
        if (!me.imageList.length) {
          return;
        }

        var addVersion = function (name, commitRef) {
          me.versionList.push({
            name: name,
            commitRef: commitRef
          })
        }
        //현재 스테이지 버전 가져오기
        var projectId = me.copyDevApp.projectId;
        var marathonAppId = me.copyDevApp[me.stage].marathonAppId;
        var commitRef = me.getCommitRefFromMarathonApp(me.getMarathonAppById(marathonAppId));
        if (commitRef) {
          addVersion('현재 버전 유지', commitRef);
          me.currentCommitRef = commitRef;
        } else {
          me.currentCommitRef = null;
        }

        //그 외 스테이지 버젼 가져오기
        var stages = ['dev', 'stg', 'prod'];
        $.each(stages, function (i, stage) {
          if (stage != me.stage) {
            var marathonAppId = me.copyDevApp[stage].marathonAppId;
            var commitRef = me.getCommitRefFromMarathonApp(me.getMarathonAppById(marathonAppId));
            if (commitRef) {
              addVersion(me.stageName(stage) + ' 버전 바로쓰기', commitRef);
            }
          }
        })

        //태그 리스트에서 가져오기
        $.each(me.tagList, function (i, tag) {
          if (me.imageList.includes(tag.commit.id)) {
            addVersion('tag:' + tag.name, tag.commit.id);
          }
        })

        //이미지 리스트 뿌리기
        $.each(me.imageList, function (i, image) {
          addVersion('commit:' + image, image);
        })
      },
      run: function () {
        var me = this;
        console.log('selectedVersion', me.selectedVersion.commitRef);
        me.appStage.deploymentStrategy.canary.weight = me.weight;
        me.copyDevApp[me.stage] = me.appStage;
        me.updateDevApp(me.appName, me.copyDevApp, function (response) {
          //스테이지 디플로이
          //commit
          me.runDeployedApp(me.appName, me.stage, me.selectedVersion.commitRef,
            function (response) {
              if (response) {
                me.cancel();
              }
            });
        });
      },
      cancel: function () {
        this.$emit('update:menu', 'history')
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
</style>
