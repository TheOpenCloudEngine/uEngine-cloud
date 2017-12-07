<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <md-table-card style="width: 100%">
      <div class="header-top-line"></div>
      <div>TODO 체크박스 선택 후 잡 정지 명령 기능</div>
      <md-table>
        <md-table-header>
          <md-table-row>
            <md-table-head></md-table-head>
            <md-table-head>Job ID</md-table-head>
            <md-table-head>Status</md-table-head>
            <md-table-head>Started</md-table-head>
            <md-table-head>Finished</md-table-head>
            <md-table-head>Run time</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="row in list">
            <md-table-cell>

            </md-table-cell>
            <md-table-cell>
              <a v-if="row.isActive" v-on:click="focusRunId(row.id)" style="cursor: pointer">{{row.jobId}}</a>
              <span v-else>{{row.jobId}}</span>

              <div v-if="row.taskId">
                <br><a v-for="taskId in row.taskId" v-on:click="moveDetail(taskId)" style="cursor: pointer">{{taskId}}</a>
              </div>
            </md-table-cell>
            <md-table-cell>{{row.status}}
              <div v-if="row.taskStatus">
                <br><span v-for="taskStatus in row.taskStatus">{{taskStatus}}</span>
              </div>
            </md-table-cell>
            <md-table-cell>{{row.started}}
              <div v-if="row.taskStarted">
                <br><span v-for="taskStarted in row.taskStarted">{{taskStarted}}</span>
              </div>
            </md-table-cell>
            <md-table-cell>{{row.finished}}
              <div v-if="row.taskFinished">
                <br><span v-for="taskFinished in row.taskFinished">{{taskFinished}}</span>
              </div>
            </md-table-cell>
            <md-table-cell>{{row.runtime}}
              <div v-if="row.taskRuntime">
                <br><span v-for="taskRuntime in row.taskRuntime">{{taskRuntime}}</span>
              </div>
            </md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>
      <md-table-pagination
        :md-size="size"
        :md-total="total"
        :md-page="page"
        md-label="페이지당 항목"
        md-separator="총"
        :md-page-options="[5, 10, 25, 50]"
        @pagination="onPagination">
      </md-table-pagination>
    </md-table-card>
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
        list: [],
        focusedList: [],
        runList: null,
        total: 10,
        size: 25,
        page: 1
      }
    },
    mounted() {
      //console.log(this.$parent);
      //this.fetchData();
    },
    watch: {
      'dcosData.jobs': {
        handler: function (newVal, oldVal) {
          var me = this;
          this.$root.dcos('service/metronome/v1/jobs/' + me.jobId + '?embed=activeRuns&embed=history&embed=schedules').get()
            .then(function (response) {
              //activeRuns
              me.runList = response.data;
              me.createRunListRows();
            })
        },
        deep: true
      }
    },
    methods: {
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.createRunListRows();
      },
      createRunListRows: function () {
        var me = this;
        me.list = [];
        var list = [];
        if(!me.runList){
            return;
        }
        if (me.runList.activeRuns && me.runList.activeRuns.length) {
          $.each(me.runList.activeRuns, function (i, activeRun) {
            var id = activeRun.id;

            var row = {
              isActive: true,
              id: id,
              jobId: activeRun.jobId,
              status: activeRun.status,
              started: activeRun.createdAt ? me.ddhhmmssDifFromDate(new Date(activeRun.createdAt)) : 'N/A',
              finished: activeRun.completedAt ? me.ddhhmmssDifFromDate(new Date(activeRun.completedAt)) : 'N/A',
              runtime: activeRun.completedAt ? me.ddhhmmssDifFromDate(new Date(activeRun.createdAt), new Date(activeRun.completedAt)) : 'N/A',
            };
            //런 아이디가 포커스 상태일 경우
            if (me.focusedList.indexOf(id) != -1) {
              row.taskId = [];
              row.taskStatus = [];
              row.taskStarted = [];
              row.taskFinished = [];
              row.taskRuntime = [];
              $.each(activeRun.tasks, function (t, task) {
                row.taskId.push(task.id);
                row.taskStatus.push(task.status);
                row.taskStarted.push(task.startedAt ? me.ddhhmmssDifFromDate(new Date(task.startedAt)) : 'N/A');
                row.taskFinished.push('N/A');
                row.taskRuntime.push('N/A');
              })
            }

            list.push(row);
          });
        }

        if (me.runList.history.successfulFinishedRuns && me.runList.history.successfulFinishedRuns.length) {
          $.each(me.runList.history.successfulFinishedRuns, function (i, run) {
            var row = {
              id: run.id,
              jobId: me.jobId,
              status: 'Completed',
              started: run.createdAt ? me.ddhhmmssDifFromDate(new Date(run.createdAt)) : 'N/A',
              finished: run.finishedAt ? me.ddhhmmssDifFromDate(new Date(run.finishedAt)) : 'N/A',
              runtime: run.finishedAt ? me.ddhhmmssDifFromDate(new Date(run.createdAt), new Date(run.finishedAt)) : 'N/A',
            };
            list.push(row);
          })
        }

        if (me.runList.history.failedFinishedRuns && me.runList.history.failedFinishedRuns.length) {
          $.each(me.runList.history.failedFinishedRuns, function (i, run) {
            var row = {
              id: run.id,
              jobId: me.jobId,
              status: 'Failed',
              started: run.createdAt ? me.ddhhmmssDifFromDate(new Date(run.createdAt)) : 'N/A',
              finished: run.finishedAt ? me.ddhhmmssDifFromDate(new Date(run.finishedAt)) : 'N/A',
              runtime: run.finishedAt ? me.ddhhmmssDifFromDate(new Date(run.createdAt), new Date(run.finishedAt)) : 'N/A',
            };
            list.push(row);
          })
        }

        //페이지네이션
        var offset = (this.page - 1) * this.size;
        var limit = (this.page) * this.size - 1;

        var count = 0;
        for (var i = 0; i < list.length; i++) {
          if (count >= offset && count <= limit) {
            me.list.push(list[i]);
          }
          count++;
        }
        this.total = count;
      }
      ,
      moveDetail: function (taskId) {
        var me = this;
        this.$router.push(
          {
            name: 'jobTaskDetail',
            params: {jobId: me.jobId, taskId: taskId}
          }
        )
      },
      focusRunId: function (runId) {
        if (this.focusedList.indexOf(runId) != -1) {
          this.focusedList.splice(this.focusedList.indexOf(runId), 1);
        } else {
          this.focusedList.push(runId);
        }
        this.createRunListRows();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
