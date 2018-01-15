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
                     v-on:click="changeMenu('pipeline')">배포 단계 진행 상황
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'config' }"
                     v-on:click="changeMenu('config')">배포 설정
          </md-button>
        </md-layout>
      </md-layout>
      <md-layout md-flex-offset="5" md-flex="10">

        <md-menu md-align-trigger>
          <md-button md-menu-trigger class="md-raised md-primary">
            <md-icon>play_circle_outline</md-icon>
            시작
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
        <span class="md-body-1">배포 작업이 실행되는 때를 선택합니다.</span>
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
        <span class="md-body-1">배포 작업 시작시 자동 배포될 환경을 선택합니다.</span>
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
        <span class="md-body-1">배포 작업이 허용되는 브런치 또는 태그를 설정합니다.</span>
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
        selectedStage: null
      }
    },
    mounted() {

    },
    watch: {
      menu: function (val) {
        if (val == 'config') {
          this.loadPipeLine();
        }
      }
    },
    methods: {
      openGitlabDeploy: function (stage) {
        this.selectedStage = stage;
        this.$nextTick(function () {
          this.$refs['gitlab-deploy'].open();
        })
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
