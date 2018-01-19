<template>
  <div>
    <service-deployments ref="service-deployments"></service-deployments>

    <md-layout>
      <md-input-container>
        <md-icon>search</md-icon>
        <label>항목 검색</label>
        <md-input type="text"></md-input>
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
          배포중 {{dcosData.deployments.length}}
        </md-button>
      </md-input-container>
    </md-layout>

    <md-layout>
      <md-layout>
        <span class="md-subheading">앱 목록</span>
      </md-layout>
      <md-layout md-align="end">
        <md-button class="md-raised md-primary" @click="openNewApp">작성
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>
    <app-list :mode="'app'"></app-list>

    <br><br><br>
    <md-layout v-if="isAdmin">
      <md-layout>
        <span class="md-subheading">서비스 목록</span>
      </md-layout>
      <md-layout md-align="end">
        <md-button class="md-raised md-primary" @click="openNewService">작성
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>

    <div v-if="isAdmin">
      <new-service ref="new-service"></new-service>
      <app-list :mode="'service'"></app-list>
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
        this.$refs['new-service'].open();
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
