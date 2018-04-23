<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <md-table md-sort="name">
      <md-table-header>
        <md-table-row>
          <md-table-head md-sort-by="name">Name</md-table-head>
          <md-table-head md-sort-by="permissions">PERMISSIONS</md-table-head>
          <md-table-head md-sort-by="owner">OWNER</md-table-head>
          <md-table-head md-sort-by="size">SIZE</md-table-head>
          <md-table-head md-sort-by="modified">MODIFIED</md-table-head>
        </md-table-row>
      </md-table-header>

      <md-table-body>
        <md-table-row v-for="file in filteredFiles">
          <md-table-cell><a :href="file.href" download>{{file.name}}</a></md-table-cell>
          <md-table-cell>{{file.mode}}</md-table-cell>
          <md-table-cell>{{file.gid}}</md-table-cell>
          <md-table-cell>{{file.size}}</md-table-cell>
          <md-table-cell>{{file.mtime}}</md-table-cell>
        </md-table-row>
      </md-table-body>
    </md-table>
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
        task: null,
        files: [],
        filteredFiles: []
      }
    },
    mounted() {
      var me = this;
      me.getMesosTaskById(me.taskId, function (response) {
        if (response) {
          me.task = response.data;
          me.init();
        }
      })
    },
    watch: {},
    methods: {
      init: function () {
        var me = this;
        var slaveId = me.task['slave_id'];
        var frameworkId = me.task['framework_id'];
        var containerId = me.task.statuses[me.task.statuses.length - 1]['container_status']['container_id']['value'];
        var fileUrl = 'agent/' + slaveId + '/files/browse?path=/var/lib/mesos/slave/slaves/' + slaveId + '/frameworks/' + frameworkId + '/executors/' + me.taskId + '/runs/' + containerId + '/';

        var DDHHMMSS = function (secs) {
          var days = Math.floor(secs / 86400);
          secs -= days * 86400;
          var hours = Math.floor(secs / 3600) % 24;
          secs -= hours * 3600;
          var minutes = Math.floor(secs / 60) % 60;
          secs -= minutes * 60;
          var seconds = secs % 60;  // in theory the modulus is not required
          return {
            days: days,
            hours: hours,
            minutes: minutes,
            seconds: seconds
          }
        };
        me.$root.dcos(fileUrl).get()
          .then(function (response) {
            me.files = response.data;
            me.filteredFiles = [];
            var currentTimeStamp = new Date().getTime();
            $.each(me.files, function (i, file) {

              //사이즈 계산
              var size;
              if (file.size < 1000) {
                size = file.size + ' B';
              } else if (file.size < 1000000) {
                size = Math.ceil(file.size / 1000) + ' K';
              } else {
                size = Math.ceil(file.size / 1000000) + ' MiB';
              }

              //시간 계산
              var diff = Math.ceil(currentTimeStamp / 1000) - file.mtime;
              var mtime;
              var ddhhmmss = DDHHMMSS(diff);
              if (ddhhmmss.days) {
                mtime = ddhhmmss.days + ' days ago';
              } else if (ddhhmmss.hours) {
                mtime = ddhhmmss.hours + ' hours ago';
              } else if (ddhhmmss.minutes) {
                mtime = ddhhmmss.minutes + ' minutes ago';
              } else {
                mtime = ddhhmmss.seconds + ' sec ago';
              }

              //이름
              let split = file.path.split('/');
              var name = split[split.length - 1];

              //다운로드
              //var downloadUrl = 'http://' + window.config.vcap.services['uengine-cloud-server'].external + '/dcos/agent/' + slaveId + '/files/download?path=/var/lib/mesos/slave/slaves/' + slaveId + '/frameworks/' + frameworkId + '/executors/' + me.taskId + '/runs/' + containerId + '/' + name;
              var downloadUrl = 'http://localhost:8080/dcos/agent/' + slaveId + '/files/download?path=/var/lib/mesos/slave/slaves/' + slaveId + '/frameworks/' + frameworkId + '/executors/' + me.taskId + '/runs/' + containerId + '/' + name;

              var row = {
                name: name,
                href: downloadUrl,
                mode: file.mode,
                gid: file.gid,
                size: size,
                mtime: mtime
              };
              me.filteredFiles.push(row);
            })
          });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
