<template>
  <md-layout>
    <div v-if="app">
      <md-table v-once>
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Last Changes</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell><span class="md-title">SCALE OR RESTART</span></md-table-cell>
            <md-table-cell><span v-if="app">No operation since last config change</span></md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">CONFIGURATION</span></md-table-cell>
            <md-table-cell>{{app.versionInfo.lastConfigChangeAt}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>


      <md-table v-once style="margin-top: 10%;">
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Last Task Failure</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell><span class="md-subheading" v-if="app.tasks[0].healthCheckResults[0].lastFailure == null">This app does not have failed tasks</span>
            </md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>


      <md-table v-once style="margin-top: 10%;">
        <md-table-header>
          <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Task Statistics</span>
          </md-table-head>
        </md-table-header>

        <md-table-header>
          <md-table-row>
            <md-table-head></md-table-head>
            <md-table-head><span style="color: #111111;font-size: 15px;">RUNNING</span></md-table-head>
            <md-table-head><span style="color: #111111;font-size: 15px;">HEALTHY</span></md-table-head>
            <md-table-head><span style="color: #111111;font-size: 15px;">UNHEALTHY</span></md-table-head>
            <md-table-head><span style="color: #111111;font-size: 15px;">STAGED</span></md-table-head>
            <md-table-head><span style="color: #111111;font-size: 15px;">MEDIAN LIFETIME</span></md-table-head>
          </md-table-row>
        </md-table-header>
        <md-table-body>
          <md-table-row>
            <md-table-cell><span class="md-subheading">With Latest Config</span></md-table-cell>
            <md-table-cell>{{app.taskStats.withLatestConfig.stats.counts.running}}</md-table-cell>
            <md-table-cell>{{app.taskStats.withLatestConfig.stats.counts.healthy}}</md-table-cell>
            <md-table-cell>{{app.taskStats.withLatestConfig.stats.counts.unhealthy}}</md-table-cell>
            <md-table-cell>{{app.taskStats.withLatestConfig.stats.counts.staged}}</md-table-cell>
            <md-table-cell>{{app.taskStats.withLatestConfig.stats.counts.staged}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheading">Started After Last Scaling</span></md-table-cell>
            <md-table-cell>{{app.taskStats.startedAfterLastScaling.stats.counts.running}}</md-table-cell>
            <md-table-cell>{{app.taskStats.startedAfterLastScaling.stats.counts.healthy}}</md-table-cell>
            <md-table-cell>{{app.taskStats.startedAfterLastScaling.stats.counts.unhealthy}}</md-table-cell>
            <md-table-cell>{{app.taskStats.startedAfterLastScaling.stats.counts.staged}}</md-table-cell>
            <md-table-cell>{{app.taskStats.startedAfterLastScaling.stats.counts.staged}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheading">Total Summary</span></md-table-cell>
            <md-table-cell>{{app.taskStats.totalSummary.stats.counts.running}}</md-table-cell>
            <md-table-cell>{{app.taskStats.totalSummary.stats.counts.healthy}}</md-table-cell>
            <md-table-cell>{{app.taskStats.totalSummary.stats.counts.unhealthy}}</md-table-cell>
            <md-table-cell>{{app.taskStats.totalSummary.stats.counts.staged}}</md-table-cell>
            <md-table-cell>{{app.taskStats.totalSummary.stats.counts.staged}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

      <md-table v-once style="margin-top: 10%;margin-bottom: 10%;">
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Recent Resource Offers</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell><span class="md-subheading" v-if="app.tasks[0].healthCheckResults[0].lastFailure == null">Offers will appear here when your service is deploying or waiting for resources.</span>
            </md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

    </div>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {},
    data() {
      return {
        app: null
      }
    },
    mounted() {

    },
    watch: {
      'dcosData': {
        handler: function (newVal, oldVal) {
          this.app = this.getMarathonAppById(this.appId);
        },
        deep: true
      }
    },
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
