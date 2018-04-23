<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <div v-if="task" style="width: 100%">
      <md-layout md-align="center">
        <md-radio v-model="menu" :mdValue="'stdout'">
          <span class="md-caption">일반 로그</span>
        </md-radio>
        <md-radio v-model="menu" :mdValue="'stderr'">
          <span class="md-caption">에러 로그</span>
        </md-radio>
      </md-layout>

      <div v-show="menu == 'stdout'">
        <codemirror
          ref="stdout"
          :options="{
              theme: 'dracula',
              mode: 'log',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
              readOnly: true
            }"
          :value="stdLog"></codemirror>
      </div>

      <div v-show="menu == 'stderr'">
        <codemirror
          ref="stderr"
          :options="{
              theme: 'dracula',
              mode: 'log',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
              readOnly: true
            }"
          :value="errLog"></codemirror>
      </div>
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
        menu: 'stdout',
        errLog: '',
        stdLog: '',
        task: null,
        isFocus: false,
        interval: true
      }
    },
    destroyed: function () {
      this.interval = false;
    },
    mounted() {
      var me = this;
      me.getMesosTaskById(me.taskId, function (response) {
        if (response) {
          me.task = response.data;
          me.startTailLog();
        }
      })
    },
    watch: {
      menu: function (val) {
        this.isFocus = false;
        this.focusLog();
      }
    },
    methods: {
      startTailLog: function () {
        var me = this;
        var slaveId = me.task['slave_id'];
        var frameworkId = me.task['framework_id'];
        var containerId = me.task.statuses[me.task.statuses.length - 1]['container_status']['container_id']['value'];
        var fileUrl = 'agent/' + slaveId + '/files/read?path=/var/lib/mesos/slave/slaves/' + slaveId + '/frameworks/' + frameworkId + '/executors/' + me.taskId + '/runs/' + containerId + '/';

        var intervalLog = function () {
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
                  me.focusLog();
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
                  me.focusLog();
                })
            });
          if (me.interval) {
            setTimeout(function () {
              intervalLog();
            }, 2000);
          }
        }
        intervalLog();
      },
      focusLog: function () {
        var me = this;
        $(me.$el).find('.CodeMirror').height(600).css('font-size', '11px');
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
