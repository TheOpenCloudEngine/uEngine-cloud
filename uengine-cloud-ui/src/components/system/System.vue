<template>
  <div>
    <md-layout>
      <iframe
        :src="frameSrc"
        style="width:100%;height: 800px;border: none"></iframe>
    </md-layout>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        frameSrc: '',
        kibanaHost: config.elk.kibana.web,
        dashboard: {
          appLog: config.elk.kibana['dashboard-id']['app-log'],
          dockerMetric: config.elk.kibana['dashboard-id']['docker-metric'],
          systemMetric: config.elk.kibana['dashboard-id']['system-metric']
        }
      }
    },
    mounted() {
      this.updateFrame();
    },
    watch: {},
    methods: {
      updateFrame: function () {
        this.frameSrc = this.createFrameSrc();
      },
      createFrameSrc: function () {
        var src = this.kibanaHost +
          "/app/kibana#/dashboard/" + this.dashboard.systemMetric +
          "?embed=true&_g=(refreshInterval%3A('%24%24hashKey'%3A'object%3A8383'%2Cdisplay%3A'10%20seconds'%2Cpause%3A!f%2Csection%3A1%2Cvalue%3A10000)%2Ctime%3A(from%3Anow-15m%2Cmode%3Aquick%2Cto%3Anow))";
        return src;
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
