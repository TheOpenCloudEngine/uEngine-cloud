<template>
  <div style="width: 100%;">
    <md-table-card>
      <md-table>
        <md-table-header>
          <md-table-head>AppName</md-table-head>
          <md-table-head>Owner name</md-table-head>
          <md-table-head>Update User name</md-table-head>
          <md-table-head>App Action Info</md-table-head>
          <md-table-head>RegDate</md-table-head>
        </md-table-header>
        <md-table-body>
          <md-table-row v-for="appLog in appLogs">
            <md-table-cell>{{appLog.appName}}</md-table-cell>
            <md-table-cell>{{appLog.ownerName}}</md-table-cell>
            <md-table-cell>{{appLog.updateUserName}}</md-table-cell>
            <md-table-cell>{{appLog.appInfo.action}}</md-table-cell>
            <md-table-cell>{{appLog.updateDate}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>
    </md-table-card>
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
        appLogs: [],
      }
    },
    mounted() {
      var me = this;

      this.$root.backend('appLogs/search/findAppLogByAppName?appName=' + this.appName).get()
        .then(function (response) {
          console.log(response);
          me.appLogs = response.data['_embedded'].appLogs;
        });
    },
    watch: {
      appLogs: {
        handler: function (newVal, oldVal) {
          var copy = JSON.parse(JSON.stringify(newVal));
          for (var i in copy) {
            copy[i].appInfo = JSON.parse(copy[i].appInfo);
          }
          this.appLogs = copy;
        },
        deep: true
      }
    },
    methods: {}
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
