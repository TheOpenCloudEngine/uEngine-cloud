<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table>
          <md-table-header>
            <md-table-row>
              <md-table-head>Job Name</md-table-head>
              <md-table-head>Status</md-table-head>
              <md-table-head>Last run</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="job in list">
              <md-table-cell>
                <a v-on:click="moveJob(job.id)" style="cursor: pointer">{{job.id}}</a>
              </md-table-cell>
              <md-table-cell>{{job.status}}</md-table-cell>
              <md-table-cell>{{job.lastrun}}</md-table-cell>
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
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        list: [],
        total: 10,
        size: 5,
        page: 1
      }
    },
    mounted(){

    },
    watch: {
      'dcosData.jobs': {
        handler: function (newVal, oldVal) {
          var me = this;
          var list = [];
          me.list = [];
          if (!newVal) {
            return;
          }
          $.each(newVal, function (i, job) {
            var row = {
              id: job.id
            };

            //status
            if (job.activeRuns && job.activeRuns.length) {
              row.status = 'Running';
            } else if (job.schedules && job.schedules.length) {
              $.each(job.schedules, function (s, schedule) {
                if (schedule.enabled) {
                  row.status = 'Scheduled';
                }
              });
            }
            if (!row.status) {
              if (job.historySummary.lastSuccessAt || job.historySummary.lastFailureAt) {
                row.status = 'Completed';
              } else {
                row.status = 'Unscheduled';
              }
            }

            //last run
            if (job.historySummary.lastSuccessAt || job.historySummary.lastFailureAt) {
              if (job.historySummary.lastSuccessAt && !job.historySummary.lastFailureAt) {
                row.lastrun = 'Success';
              }
              else if (!job.historySummary.lastSuccessAt && job.historySummary.lastFailureAt) {
                row.lastrun = 'Failed';
              } else {
                var lastSuccessAt = new Date(job.historySummary.lastSuccessAt).getTime();
                var lastFailureAt = new Date(job.historySummary.lastFailureAt).getTime();
                if (lastSuccessAt >= lastFailureAt) {
                  row.lastrun = 'Success';
                } else {
                  row.lastrun = 'Failed';
                }
              }
            } else {
              row.lastrun = 'N/A';
            }
            list.push(row);
          });

          //페이지네이션
          var offset = (this.page - 1) * this.size;
          var limit = (this.page) * this.size - 1;

          var count = 0;
          for (var i = 0; i < list.length; i++) {
            //서브 항목일 경우
            if (list[i].role) {
              me.list.push(list[i]);
            }
            //그 외의 경우
            else {
              if (count >= offset && count <= limit) {
                me.list.push(list[i]);
              }
              count++;
            }
          }
          this.total = count;
        },
        deep: true
      }
    }
    ,
    methods: {
      onPagination: function (val) {
        this.focusedList = [];
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      moveJob: function (jobId) {
        var me = this;
        this.$router.push(
          {
            name: 'jobRunList',
            params: {jobId: jobId}
          }
        )
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
