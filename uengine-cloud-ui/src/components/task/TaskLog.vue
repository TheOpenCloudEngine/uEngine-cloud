<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <div v-if="task">
      <span class="md-subheading">StdErr</span>
      <md-input-container>
        <md-textarea style="width: 1000px" v-model="errLog" row="100">

        </md-textarea>
      </md-input-container>

      <br>
      <span class="md-subheading">StdOut</span>
      <md-input-container>
        <md-textarea style="width: 1000px" v-model="stdLog" row="150">

        </md-textarea>
      </md-input-container>
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
        errLog: null,
        stdLog: null,
        task: null
      }
    },
    mounted() {

    },
    watch: {
      'dcosData': {
        handler: function (newVal, oldVal) {
          this.task = this.getTaskById(this.taskId);
        },
        deep: true
      },
      'task': {
        handler: function (newVal, endVal) {
          var me = this;
          var slaveId = newVal['slave_id'];
          var frameworkId = newVal['framework_id'];
          var containerId = newVal.statuses[newVal.statuses.length - 1]['container_status']['container_id']['value'];
          var fileUrl = 'agent/' + slaveId + '/files/read?path=/var/lib/mesos/slave/slaves/' + slaveId + '/frameworks/' + frameworkId + '/executors/' + me.taskId + '/runs/' + containerId + '/';
          me.$root.dcos(fileUrl + 'stderr&offset=-1').get()
            .then(function (response) {
              var offset = response.data.offset;
              if (offset > 50000) {
                offset = offset - 50000;
              } else {
                offset = 0;
              }
              me.$root.dcos(fileUrl + 'stderr&offset=' + offset + '&length=50000').get()
                .then(function (logData) {
                  me.errLog = logData.data.data;
                })
            });

          me.$root.dcos(fileUrl + 'stdout&offset=-1').get()
            .then(function (response) {
              var offset = response.data.offset;
              if (offset > 50000) {
                offset = offset - 50000;
              } else {
                offset = 0;
              }
              me.$root.dcos(fileUrl + 'stdout&offset=' + offset + '&length=50000').get()
                .then(function (logData) {
                  me.stdLog = logData.data.data;
                })
            });
        },
        deep: true
      }
    },
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
