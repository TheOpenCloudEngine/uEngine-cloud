<template>
  <div>
    <router-view></router-view>

    <!--서비스 로케이터 리스트-->
    <!--<service-locator v-if="config" :host="'http://' + config.vcap.services['SERVICE_TO_CONNECT'].external" path="/"-->
    <!--resource-name="backend"></service-locator>-->


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

    },
    methods: {
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

</style>
