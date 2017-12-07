<template>
  <div>
    <router-view></router-view>

    <!--서비스 로케이터 리스트-->
    <service-locator v-if="config" :host="'http://' + config.vcap.services['uengine-cloud-server'].external" path="/"
                     resource-name="backend"></service-locator>

    <service-locator v-if="config" :host="'http://' + config.vcap.services['uengine-cloud-server'].external"
                     path="/dcos/"
                     resource-name="dcos"></service-locator>

    <service-locator v-if="config" :host="'http://' + config.vcap.services['uengine-cloud-server'].external"
                     path="/gitlab/"
                     resource-name="gitlab"></service-locator>
    <!--<service-locator v-if="config" :host="'http://localhost:8080'" path="/"-->
                     <!--resource-name="backend"></service-locator>-->

    <!--<service-locator v-if="config" :host="'http://localhost:8080'"-->
                     <!--path="/dcos/"-->
                     <!--resource-name="dcos"></service-locator>-->

    <!--<service-locator v-if="config" :host="'http://localhost:8080'"-->
                     <!--path="/gitlab/"-->
                     <!--resource-name="gitlab"></service-locator>-->

    <service-locator v-if="config" :host="configServerUrl"
                     path="/"
                     resource-name="config"></service-locator>

    <!--글로벌 알림 컴포넌트-->
    <md-snackbar md-position="top right" ref="snackbar" :md-duration="4000">
      <span class="md-primary">{{snackbar.text}}</span>
      <md-button class="md-accent" md-theme="light-blue" @click="$refs.snackbar.close()">Close</md-button>
    </md-snackbar>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        configServerUrl: configServerUrl,
        config: window.config,
        snackbar: {
          top: true,
          right: true,
          timeout: 6000,
          trigger: false,
          mode: 'multi-line',
          context: 'info',
          text: ''
        },
      }
    },
    mounted() {
      this.fetchData();
    },
    methods: {
      fetchData: function () {
        var me = this;
        var p1 = this.$root.dcos('dcos-history-service/history/last').get();
        var p2 = this.$root.dcos('service/metronome/v1/jobs?embed=activeRuns&embed=schedules&embed=historySummary').get();
        var p3 = this.$root.dcos('service/marathon/v2/groups?embed=group.groups&embed=group.apps&embed=group.pods&embed=group.apps.deployments&embed=group.apps.counts&embed=group.apps.tasks&embed=group.apps.taskStats&embed=group.apps.lastTaskFailur').get();
        var p4 = this.$root.dcos('service/marathon/v2/queue').get();
        var p5 = this.$root.dcos('service/marathon/v2/deployments').get();
        var p6 = this.$root.dcos('system/health/v1/units').get();
        var p7 = this.$root.dcos('mesos/master/state').get();
        var p8 = this.$root.config('dcos-apps.json').get();

        Promise.all([p1, p2, p3, p4, p5, p6, p7, p8])
          .then(function ([r1, r2, r3, r4, r5, r6, r7, r8]) {
            me.$root.dcosData = {
              last: r1.data,
              jobs: r2.data,
              groups: r3.data,
              queue: r4.data,
              deployments: r5.data,
              units: r6.data,
              state: r7.data,
              devopsApps: r8.data,
              config: me.config
            };
            setTimeout(function () {
              me.fetchData();
            }, 1000);
          })
          .catch(function (b) {
            setTimeout(function () {
              me.fetchData();
            }, 1000);
          });
      },
      info: function (msg) {
        this.snackbar.context = 'info';
        this.snackbar.text = msg;
        this.$refs.snackbar.open();
      },
      error: function (msg) {
        this.snackbar.context = 'error';
        this.snackbar.text = msg;
        this.$refs.snackbar.open();
      },
      warning: function (msg) {
        this.snackbar.context = 'warning';
        this.snackbar.text = msg;
        this.$refs.snackbar.open();
      },
      success: function (msg) {
        this.snackbar.context = 'success';
        this.snackbar.text = msg;
        this.$refs.snackbar.open();
      }
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import '/static/css/custom.css';
</style>
