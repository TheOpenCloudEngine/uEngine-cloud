<template>
  <div>
    <service-deployments ref="service-deployments"></service-deployments>

    <md-layout>
      <md-input-container>
        <md-icon>search</md-icon>
        <label>항목 검색</label>
        <md-input v-model="search" type="text"></md-input>
        <md-button class="md-raised">
          <md-icon>swap_vert</md-icon>
        </md-button>
        <md-button class="md-raised" v-on:click="changeView">
          <md-icon v-if="view == 'list'">view_module</md-icon>
          <md-icon v-if="view == 'card'">view_list</md-icon>
        </md-button>
        <md-button class="md-raised md-primary"
                   v-if="dcosData.deployments"
                   @click="openDeployments">
          <md-tooltip md-direction="bottom">배포중인 앱 또는 서비스를 중단할 수 있습니다.</md-tooltip>
          배포중 {{dcosData.deployments.length}}
        </md-button>
      </md-input-container>
    </md-layout>

    <md-layout>
      <md-layout>
        <span class="md-subheading">앱 목록</span>
      </md-layout>
      <md-layout md-align="end">
        <md-button class="md-raised md-primary" @click="openNewApp">
          <md-tooltip md-direction="bottom">앱을 새로 작성합니다.</md-tooltip>
          작성
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>
    <app-list :mode="'app'" :search="search"></app-list>

    <br><br><br>
    <md-layout v-if="isAdmin">
      <md-layout>
        <span class="md-subheading">서비스 목록</span>
      </md-layout>
      <md-layout md-align="end">
        <md-button class="md-raised md-primary" @click="openNewService">
          <md-tooltip md-direction="bottom">서비스를 새로 작성합니다.</md-tooltip>
          작성
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>

    <div v-if="isAdmin">
      <app-editor ref="app-editor"></app-editor>
      <app-list :mode="'service'" :search="search"></app-list>
      <br><br><br>
    </div>

    <md-layout>
      <md-layout>
        <span class="md-subheading">잡 목록</span>
      </md-layout>
      <md-layout md-align="end">
        <md-button class="md-raised md-primary" @click="openNewJob">작성
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>
    <job-list></job-list>

    <br><br><br>
    <span class="md-subheading">시스템 사용량</span>
    <metrics></metrics>

    <br><br><br>
    <span class="md-subheading">노드</span>
    <node-list></node-list>

  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        view: 'list',
        search: '',
        isAdmin: false,
      }
    },
    mounted() {
      this.isAdmin = window.localStorage['acl'] == 'admin' ? true : false;
    },
    watch: {},
    methods: {
      openDeployments: function () {
        this.$refs['service-deployments'].open();
      },
      openNewApp: function () {
        this.$router.push(
          {
            name: 'appsCatalog'
          }
        )
      },
      openNewJob: function () {

      },
      openNewService: function () {
        this.$refs['app-editor'].open();
      },
      changeView: function () {
        if (this.view == 'list') {
          this.view = 'card';
        } else {
          this.view = 'list'
        }
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
