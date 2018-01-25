<template>
  <div v-if="devApp">
    <md-layout md-align="center">
      <md-layout md-flex="60" class="bar-wrapper">
        <md-layout>
          <md-button
            v-if="appType =='zuul'"
            class="md-raised" v-bind:class="{ 'md-primary': menu == appType }"
            v-on:click="changeMenu(appType)">
            <md-tooltip md-direction="bottom">앱에 특화된 설정 도구입니다.</md-tooltip>
            {{appType}} 환경설정
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'runtime' }"
                     v-on:click="changeMenu('runtime')">
            <md-tooltip md-direction="bottom">앱의 리소스 자원과 도커 인스턴스 목록을 관리합니다.</md-tooltip>
            메모리 및 인스턴스
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'config' }"
                     v-on:click="changeMenu('config')">
            <md-tooltip md-direction="bottom">앱 구동시 환경변수를 관리합니다.</md-tooltip>
            환경변수
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'ssh' }"
                     v-on:click="changeMenu('ssh')">
            <md-tooltip md-direction="bottom">앱의 도커 인스턴스로 웹 터미널을 통해 접근합니다.</md-tooltip>
            SSH
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised"
                     v-on:click="openEdit">
            <md-tooltip md-direction="bottom">보다 자세히 앱의 구동 설정을 꾸밀 수 있습니다.</md-tooltip>
            고급 설정
          </md-button>
        </md-layout>
        <md-layout style="margin-left: 20px;">
          <div v-if="configChanged" class="exclamation">!
            <md-tooltip class="fontb" md-direction="top">Yml 설정이 변경되고 나서, 앱 디플로이(재시작)를 하셔야 적용됩니다.
            </md-tooltip>
          </div>
        </md-layout>
      </md-layout>
    </md-layout>

    <new-service ref="new-service" :appId="targetAppId" :mode="'app'"></new-service>

    <br><br>
    <div v-if="menu == appType">

      <extension-menu
        ref="extension-menu"
        :appType="appType"
        :stage="stage"
        :devApp="devApp"
        :categoryItem="categoryItem"
      ></extension-menu>
    </div>
    <div v-if="menu == 'runtime'">
      <app-runtime-card
        :stage="stage"
        :devApp="devApp"
        :categoryItem="categoryItem"
      ></app-runtime-card>

      <br><br>
      <span class="md-subheading">인스턴스</span>
      <task-list
        :appIds="stage == 'prod' ? ['/'+ appName + '-blue', '/'+ appName + '-green'] : [devApp[stage]['marathonAppId']]"></task-list>
    </div>
    <div v-if="menu == 'config'">
      <app-cloud-config
        :stage="stage"
        :devApp="devApp"
        :categoryItem="categoryItem"
      >
      </app-cloud-config>
    </div>
    <div v-if="menu == 'ssh'">
      <app-ssh
        :stage="stage"
        :devApp="devApp"
        :categoryItem="categoryItem"
      >
      </app-ssh>
    </div>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'
  import ExtensionMenu from "./AppsExtensionMenu.vue";

  export default {
    components: {ExtensionMenu},
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      categoryItem: Object
    },
    data() {
      return {
        menu: 'runtime',
        targetAppId: null,
        configChanged: false,
        appType: "",
      }
    },
    mounted() {
      var me = this;
      me.getDevAppByName(me.appName, function (response) {
        var devops = response.data;
        if (devops[me.stage]['config-changed']) {
          me.configChanged = devops[me.stage]['config-changed'];
        }
//        console.log("devops", devops);
      });
    },
    watch: {
      devApp: {
        handler: function (newVal, oldVal) {
          this.appType = newVal.appType;
        },
        deep: true
      },
      dcosData: {
        handler: function (newVal, oldVal) {
          var copy = newVal;
          this.configChanged = copy.devopsApps[this.appName][this.stage]['config-changed']?copy.devopsApps[this.appName][this.stage]['config-changed']:false;
//          console.log("configChaged",this.configChanged);
        },
        deep: true
      }
    },
    methods: {
      openEdit: function () {
        var me = this;
        var marathonAppId = me.devApp[me.stage]['marathonAppId'];
        me.targetAppId = marathonAppId;
        this.$refs['new-service'].open();
      },
      changeMenu: function (menu) {
        this.menu = menu;
      },
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

  .exclamation {
    width: 25px;
    height: 25px;
    text-align: center;
    margin-top: 10px;
    color: #CD0000;
    border: solid #CD0000 2px;
    border-radius: 20px;
  }

  .exclamation:hover {
    width: 25px;
    height: 25px;
    text-align: center;
    margin-top: 10px;
    color: #CD7E83;
    border: solid #CD7E83 2px;
    border-radius: 20px;
  }
</style>
