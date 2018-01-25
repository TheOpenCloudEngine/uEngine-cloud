<template>
  <div>
    <router-view></router-view>

    <!--서비스 로케이터 리스트-->
    <service-locator :host="backendUrl" path="/"
                     resource-name="backend"></service-locator>

    <service-locator :host="backendUrl"
                     path="/dcos/"
                     resource-name="dcos"></service-locator>

    <service-locator :host="backendUrl"
                     path="/gitlab/"
                     resource-name="gitlab"></service-locator>

    <service-locator v-if="config" :host="'http://' + config.vcap.services['eureka-server'].external"
                     path="/eureka/"
                     resource-name="eureka"></service-locator>

    <!--글로벌 알림 컴포넌트-->
    <md-snackbar md-position="top right" ref="snackbar" :md-duration="4000">
      <span class="md-primary">{{snackbar.text}}</span>
      <md-button class="md-accent" md-theme="light-blue" @click="$refs.snackbar.close()">Close</md-button>
    </md-snackbar>

    <confirm
      title="Are you sure?"
      content-html="현재 배포가 중단되고, 서비스를 이전 버전으로 되돌리기 위해 새 배포가 시작될 것입니다."
      ok-text="배포 중단 하기"
      cancel-text="취소"
      ref="confirm"
    ></confirm>
  </div>
</template>
<script>
  export default {
    props: {
      dcosData: Object
    },
    data () {
      return {
        backendUrl: backendUrl,
        config: window.config,
        snackbar: {
          top: true,
          right: true,
          timeout: 6000,
          trigger: false,
          mode: 'multi-line',
          context: 'info',
          text: ''
        }
      }
    },
    mounted() {
      console.log("config", this.config);
      this.fetchData();
    },
    methods: {
      /**
       * 2초에 한번 전체 데이터를 갱신하도록 조정.
       */
      fetchData: function () {
        var me = this;
        var p1 = this.$root.backend('fetchData').get();

        Promise.all([p1])
          .then(function ([r1]) {
            me.$root.dcosData = {
              last: r1.data.last,
              jobs: r1.data.jobs,
              groups: r1.data.groups,
              queue: r1.data.queue,
              deployments: r1.data.deployments,
              units: r1.data.units,
              state: r1.data.state,
              devopsApps: r1.data.devopsApps,
              config: me.config
            };
            setTimeout(function () {
              me.fetchData();
            }, 2000);
          })
          .catch(function (b) {
            setTimeout(function () {
              me.fetchData();
            }, 2000);
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
      },
      confirm: function (options) {
        this.$refs.confirm.open(options);
      }
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import '/static/css/custom.css';
</style>
