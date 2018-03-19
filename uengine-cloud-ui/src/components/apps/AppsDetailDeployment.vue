<template>
  <div v-if="devApp">

    <gitlab-deploy
      ref="gitlab-deploy"
      :stage="selectedStage"
      :devApp="devApp"
      :categoryItem="categoryItem"
      :appName="appName"
      :role="'deploy'"
    ></gitlab-deploy>

    <md-layout style="z-index: 1">
      <md-layout md-flex-offset="20" md-flex="60" class="bar-wrapper">
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'pipeline' }"
                     v-on:click="changeMenu('pipeline')">빌드 진행 상황
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'config' }"
                     v-on:click="changeMenu('config')">빌드 설정
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'strategies' }"
                     v-on:click="changeMenu('strategies')">배포 전략 및 세션
          </md-button>
        </md-layout>
      </md-layout>
      <md-layout md-flex-offset="5" md-flex="10">

        <md-menu md-align-trigger>
          <md-button md-menu-trigger class="md-raised md-primary">
            <md-tooltip md-direction="bottom">CI 작업을 시작합니다.</md-tooltip>
            <md-icon>play_circle_outline</md-icon>
            빌드 시작
          </md-button>

          <md-menu-content>
            <md-menu-item v-on:click="openGitlabDeploy('dev')">개발</md-menu-item>
            <md-menu-item v-on:click="openGitlabDeploy('stg')">스테이징</md-menu-item>
            <md-menu-item v-on:click="openGitlabDeploy('prod')">프로덕션</md-menu-item>
          </md-menu-content>
        </md-menu>
      </md-layout>
    </md-layout>

    <br><br>

    <div v-if="menu == 'pipeline'">
      <app-pipeline
        :stage="stage"
        :devApp="devApp"
        :categoryItem="categoryItem"
      ></app-pipeline>
    </div>
    <div v-if="menu == 'config'">
      <div style="text-align: center">
        <span class="md-body-1">빌드 작업이 실행되는 때를 선택합니다.</span>
        <md-layout md-align="center">
          <md-radio v-model="when" :mdValue="'commit'">
            <span class="md-caption">커밋 마다 실행</span>
          </md-radio>
          <md-radio v-model="when" :mdValue="'manual'">
            <span class="md-caption">UI 에서만 실행</span>
          </md-radio>
        </md-layout>
      </div>
      <br><br>
      <div style="text-align: center">
        <span class="md-body-1">빌드 작업 시작시 자동 배포될 환경을 선택합니다.</span>
        <md-layout md-align="center">
          <md-checkbox id="my-test10" name="my-test10" md-value="dev" v-model="autoDeploy" class="md-primary">개발 서버
          </md-checkbox>
          <md-checkbox id="my-test11" name="my-test11" md-value="stg" v-model="autoDeploy" class="md-primary">스테이징 서버
          </md-checkbox>
          <md-checkbox id="my-test12" name="my-test12" md-value="prod" v-model="autoDeploy" class="md-primary">프로덕션 서버
          </md-checkbox>
        </md-layout>
      </div>
      <br><br>
      <div style="text-align: center">
        <span class="md-body-1">빌드 작업이 허용되는 브런치 또는 태그를 설정합니다.</span>
        <md-layout md-align="center">
          <md-layout md-flex="50" md-align="center">
            <md-chips v-model="refs"></md-chips>
          </md-layout>
        </md-layout>
      </div>
      <div>
        <md-layout md-align="center">
          <md-button class="md-raised md-primary" v-on:click="savePipeLine()">저장
          </md-button>
        </md-layout>
      </div>
    </div>
    <div v-if="menu == 'strategies'">
      <div v-if="deploymentStrategy" style="text-align: center">
        <span class="md-body-1">새로운 인스턴스가 유입되는 방식을 선택합니다.</span>
        <md-layout md-align="center">
          <md-radio v-model="deploymentStrategy.instanceStrategy" :mdValue="'RECREATE'">
            <span>RECREATE - 이전 버전이 종료되고 신규 버전이 출시됩니다.</span>
          </md-radio>
        </md-layout>
        <md-layout md-align="center">
          <md-radio v-model="deploymentStrategy.instanceStrategy" :mdValue="'RAMP'">
            <span>RAMP - 이전 버전이 느리게 롤아웃되고 신규 버전이 느리게 유입됩니다.</span>
          </md-radio>
        </md-layout>

        <md-layout md-align="center" v-if="deploymentStrategy.instanceStrategy == 'RAMP'">
          <div style="width: 500px">
            <md-input-container>
              <label>롤 아웃 비 : {{deploymentStrategy.ramp.maximumOverCapacity * 100}} %</label>
              <md-input v-model="deploymentStrategy.ramp.maximumOverCapacity" type="range" min="0" max="1"
                        step="0.1"></md-input>
            </md-input-container>

            <span class="md-body-1" v-if="deploymentStrategy.ramp.maximumOverCapacity == 0">
              인스턴스가 하나씩 롤아웃 됩니다.
            </span>
            <span class="md-body-1" v-else-if="deploymentStrategy.ramp.maximumOverCapacity == 1">
              모든 인스턴스가 한번에 롤아웃 됩니다.
            </span>
            <span class="md-body-1" v-else>
              전체 인스턴스의 {{deploymentStrategy.ramp.maximumOverCapacity * 100}} % 가 단계적으로 롤아웃 됩니다.
            </span>
          </div>
        </md-layout>

        <md-divider></md-divider>
        <br><br>
        <span class="md-body-1">세션을 유지하는 방식을 선택합니다.</span>
        <md-layout md-align="center">
          <md-radio v-model="deploymentStrategy.sticky" :mdValue="true">
            <span>STICKY - 항상 같은 서버로 연결되도록 합니다.</span>
          </md-radio>
        </md-layout>
        <md-layout md-align="center">
          <md-radio v-model="deploymentStrategy.sticky" :mdValue="false">
            <span>ROUND ROBIN - 순차적으로 서버에 연결시킵니다.</span>
          </md-radio>
        </md-layout>


        <div v-if="stage == 'prod'">
          <md-divider></md-divider>
          <br><br>
          <span class="md-body-1">신규 버전과 이전 버전을 관리하는 방식을 선택합니다.</span>
          <md-layout md-align="center">
            <md-checkbox v-model="deploymentStrategy.bluegreen" class="md-primary">
              신규 버젼과 이전 버전을 나누어 관리합니다.
            </md-checkbox>
          </md-layout>
          <span class="md-caption">신규버젼에 대한 별도의 테스트 포트를 항시 제공하며, 신규 버젼 출시 전 안전단계를 거쳐갈 수 있습니다.</span>

          <div v-if="deploymentStrategy.bluegreen">
            <md-layout md-align="center">
              <md-checkbox v-model="deploymentStrategy.canary.active" class="md-primary">
                프로덕션 호스트의 신규 버젼과 이전 버전 사이의 로드밸런싱 비율을 나눕니다.
              </md-checkbox>
            </md-layout>

            <md-layout md-align="center" v-if="deploymentStrategy.canary.active">
              <div style="width: 500px">
                <md-input-container>
                  <label>신규 : {{deploymentStrategy.canary.weight}} % |
                    이전 : {{100 - deploymentStrategy.canary.weight}} %
                  </label>
                  <md-input v-model="deploymentStrategy.canary.weight" type="range" min="0" max="100"
                            step="1"></md-input>
                </md-input-container>
              </div>
            </md-layout>

            <br>

            <md-layout md-align="center">
              <md-checkbox v-model="deploymentStrategy.abtest.active" class="md-primary">
                A/B 테스트. 특정 조건에 해당할 경우 트래픽이 신규 버전으로 향합니다.
              </md-checkbox>
            </md-layout>

            <md-layout md-align="center" v-if="deploymentStrategy.abtest.active">
              <div style="width: 500px;text-align: left">
                <codemirror ref="abtestRef"
                            :options="{
              theme: 'dracula',
              mode: 'yaml',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
              readOnly: false
            }"
                            v-on:change="onAclChange"
                            :value="deploymentStrategy.abtest.aclToB"></codemirror>
              </div>
            </md-layout>
          </div>
        </div>
      </div>
      <div>
        <md-layout md-align="center">
          <md-button class="md-raised md-primary" v-on:click="saveStrategy()">저장
          </md-button>
        </md-layout>
      </div>
    </div>
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
        refs: ['master'],
        autoDeploy: ['dev'],
        when: 'commit',
        menu: 'pipeline',
        selectedStage: null,
        deploymentStrategy: null,
        search: 0
      }
    },
    mounted() {

    },
    watch: {
      menu: function (val) {
        if (val == 'config') {
          this.loadPipeLine();
        }
        if (val == 'strategies') {
          this.loadStrategies();
        }
      },
      stage: function (val) {
        this.loadStrategies();
      }
    },
    methods: {
      onAclChange: function (val) {

      },
      openGitlabDeploy: function (stage) {
        this.selectedStage = stage;
        this.$nextTick(function () {
          this.$refs['gitlab-deploy'].open();
        })
      },
      loadStrategies: function () {
        if (this.devApp[this.stage]) {
          this.deploymentStrategy = JSON.parse(JSON.stringify(this.devApp[this.stage]['deploymentStrategy']))
        }
      },
      loadPipeLine: function () {
        var me = this;
        me.getAppPipeLineJson(me.appName, function (response) {
          me.when = response.data.when;
          me.refs = response.data.refs;
          me.autoDeploy = response.data['auto-deploy'];
        })
      },
      changeMenu: function (menu) {
        this.menu = menu;
      },
      savePipeLine: function () {
        var me = this;
        var data = {
          when: me.when,
          refs: me.refs,
          'auto-deploy': me.autoDeploy
        };
        me.updateAppPipeLineJson(me.appName, data, function (response) {

        })
      },
      saveStrategy: function(){
        var me = this;
        var copy = JSON.parse(JSON.stringify(me.devApp));
        copy[this.stage]['deploymentStrategy'] = this.deploymentStrategy;

        me.updateAppExcludeDeployJson(copy.name, copy, function (response) {
          if (response) {

          }
        });
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
</style>
