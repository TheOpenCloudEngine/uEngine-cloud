<template>
  <div v-if="devApp" style="width: 100%;">
    <div style="width: 100%;">
      <md-layout>
        <md-button class="md-raised md-primary">
          타임 레인지, 자동 갱신 ON/OFF TODO
        </md-button>
        <md-button v-on:click="moveKibana" class="md-raised md-primary">
          Kibana 에서 앱 보기
        </md-button>
      </md-layout>
    </div>
    <iframe
      :src="frameSrc"
      style="width:100%;height: 800px"></iframe>
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
      categoryItem: Object,
      marathonApps: Object,
      deployJson: Object
    },
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
    watch: {
      stage: function (newVal, oldVal) {
        this.updateFrame();
      }
    },
    methods: {
      updateFrame: function () {
        this.frameSrc = this.createFrameSrc();
      },
      moveKibana: function () {
        var src = this.createFrameSrc();
        src = src.replace('embed=true&', '');
        window.open(src);
      },
      createFrameSrc: function () {
        var appName = this.devApp.name;
        var src = this.kibanaHost +
          "/app/kibana#/dashboard/" + this.dashboard.appLog +
          "?embed=true&_g=(refreshInterval%3A('%24%24hashKey'%3A'object%3A8383'%2Cdisplay%3A'10%20seconds'%2Cpause%3A!f%2Csection%3A1%2Cvalue%3A10000)%2Ctime%3A(from%3Anow%2Fd%2Cmode%3Aquick%2Cto%3Anow%2Fd))" +
          "&_a=(query:(language:lucene,query:'docker.container.labels.PROFILE:" + this.stage + "%20AND%20docker.container.labels.APP_NAME:" + appName + "'))";

        return src;
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
