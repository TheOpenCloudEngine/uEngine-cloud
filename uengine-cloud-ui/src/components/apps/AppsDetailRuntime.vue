<template>
  <div v-if="devApp">
    <md-layout md-align="center">
      <md-layout md-flex="60" class="bar-wrapper">
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'runtime' }"
                     v-on:click="changeMenu('runtime')">메모리 및 인스턴스
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'config' }"
                     v-on:click="changeMenu('config')">환경변수
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised" v-bind:class="{ 'md-primary': menu == 'ssh' }"
                     v-on:click="changeMenu('ssh')">SSH
          </md-button>
        </md-layout>
        <md-layout>
          <md-button class="md-raised"
                     v-on:click="openEdit">고급 설정
          </md-button>
        </md-layout>
      </md-layout>
    </md-layout>

    <new-service ref="new-service" :appId="targetAppId" :mode="'app'"></new-service>

    <br><br>
    <div v-if="menu == 'runtime'">
      <app-runtime-card
        :stage="stage"
        :devApp="devApp"
        :catalogItem="catalogItem"
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
        :catalogItem="catalogItem"
      >
      </app-cloud-config>
    </div>
    <div v-if="menu == 'ssh'">
      <app-ssh
        :stage="stage"
        :devApp="devApp"
        :catalogItem="catalogItem"
      >
      </app-ssh>
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
      catalogItem: Object
    },
    data() {
      return {
        menu: 'runtime',
        targetAppId: null
      }
    },
    mounted() {

    },
    watch: {},
    methods: {
      openEdit: function () {
        var me = this;
        var marathonAppId = me.devApp[me.stage]['marathonAppId'];
        me.targetAppId = marathonAppId;
        this.$refs['new-service'].open();
      },
      changeMenu: function (menu) {
        this.menu = menu;
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
</style>
