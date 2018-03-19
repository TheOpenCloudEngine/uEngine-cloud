<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout v-if="!simple">
      <md-input-container>
        <md-icon>search</md-icon>
        <label>항목 검색</label>
        <md-input type="text" v-model="searchKeyword"></md-input>
        <md-button-toggle md-single class="md-primary">
          <md-button v-on:click="doFilter" value="ALL">ALL</md-button>
          <md-button class="md-toggle" v-on:click="doFilter" value="RUNNING">ACTIVE</md-button>
          <md-button v-on:click="doFilter" value="COMPLETED">COMPLETED</md-button>
        </md-button-toggle>
      </md-input-container>
    </md-layout>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table md-sort="name">
          <md-table-header>
            <md-table-row>
              <md-table-head md-sort-by="id">ID</md-table-head>
              <md-table-head v-if="!simple" md-sort-by="name">Name</md-table-head>
              <md-table-head v-if="!simple" md-sort-by="host">HOST</md-table-head>
              <md-table-head md-sort-by="status">STATUS</md-table-head>
              <md-table-head md-sort-by="health">HEALTH</md-table-head>
              <md-table-head md-sort-by="metrics">METRICS</md-table-head>
              <md-table-head md-sort-by="log">LOG</md-table-head>
              <md-table-head v-if="!simple" md-sort-by="cpu">CPU</md-table-head>
              <md-table-head v-if="!simple" md-sort-by="mem">MEM</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="(task, index) in list">
              <md-table-cell>
                <a v-on:click="moveDetail(task)" style="cursor: pointer">{{task.id}}</a>
              </md-table-cell>
              <md-table-cell v-if="!simple">{{task.name}}</md-table-cell>
              <md-table-cell v-if="!simple">{{task.host}}</md-table-cell>
              <md-table-cell>{{task.state}}</md-table-cell>
              <md-table-cell>
                <span class="healthCheck running" v-if="task.healthCheckResults===true"></span>
                <span class="healthCheck dead" v-else-if="task.healthCheckResults===false"></span>
                <span class="healthCheck" v-else></span>
              </md-table-cell>
              <md-table-cell>
                <a v-on:click="moveCadvisor(task)" style="cursor: pointer">
                  <span><md-icon>trending_up</md-icon></span>
                  <span>metrics</span>
                </a>
              </md-table-cell>
              <md-table-cell>
                <a v-on:click="moveLog(task)" style="cursor: pointer">로그보기</a>
              </md-table-cell>
              <md-table-cell v-if="!simple">{{task.resources.cpus}}</md-table-cell>
              <md-table-cell v-if="!simple">{{task.resources.mem}}</md-table-cell>
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
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      appIds: Array,
      simple: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        list: [],
        searchKeyword: "",
        filter: "RUNNING",
        total: 10,
        size: 25,
        page: 1
      }
    },
    mounted() {

    },
    watch: {
      'dcosData.state': {
        handler: function (newVal, oldVal) {
          if (newVal) {
            this.makeList();
          }
        },
        deep: true
      }
    },
    methods: {
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      makeList: function () {
        var me = this;
        var filtered = [];
        var list = [];
        me.list = [];

        if (this.appId) {
          list = me.getTasksByAppId(me.appId);
        } else if (this.nodeId) {
          list = me.getTasksByNodeId(me.nodeId);
        } else if (this.appIds && this.appIds.length) {
          $.each(me.appIds, function (i, id) {
            var temp = me.getTasksByAppId(id);
            if (temp != null) {
              list = list.concat(temp);
            }
          });
        }

        if (list == null) {
          return;
        }

        var metronomeId;
        var marathonId;
        $.each(me.dcosData.state.frameworks, function (f, framework) {
          if (framework.name == 'metronome') {
            metronomeId = framework.id;
          } else {
            marathonId = framework.id;
          }
        });

        //호스트
        $.each(list, function (taskIndex, taskValue) {
          taskValue.host = me.getHostBySlaveId(taskValue['slave_id']);
        });

        //필터
        $.each(list, function (taskIndex, taskValue) {
          if (me.filter == "ALL") {
            filtered.push(taskValue);
          } else if (me.filter == "RUNNING" && (taskValue.state == 'TASK_RUNNING' || taskValue.state == 'TASK_STAGING')) {
            filtered.push(taskValue);
          } else if (me.filter == "COMPLETED" &&
            (taskValue.state == 'TASK_FINISHED' || taskValue.state == 'TASK_KILLED' || taskValue.state == 'TASK_FAILED')) {
            filtered.push(taskValue);
          }
        });
        list = filtered;

        //서치
        filtered = [];
        if (me.searchKeyword && me.searchKeyword != "") {
          $.each(list, function (taskIndex, taskValue) {
            if (taskValue.id.indexOf(me.searchKeyword) != -1) {
              filtered.push(taskValue);
            }
          });
          list = filtered;
        }

        //프레임워크 구분
        $.each(list, function (taskIndex, taskValue) {
          //마라톤 일 경우 헬스 체크
          if (marathonId == taskValue['framework_id']) {
            taskValue.framework = 'marathon';
            var app = me.getDcosAppById('/' + taskValue.name);
            if (app) {
              $.each(app.tasks, function (key, value) {
                if (taskValue.id == value.id) {
                  if (value.healthCheckResults && value.healthCheckResults.length) {
                    taskValue.healthCheckResults = value.healthCheckResults[0].alive;
                  }
                } else {
                }
              })
            }
          } else {
            taskValue.framework = 'metronome';
          }
        });

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
      },
      moveCadvisor: function (task) {
        if (task['statuses'][0]) {
          var dockerName = 'mesos-' + task['statuses'][0]['container_status']['container_id'].value;
          var url = this.getCadvisorUrlBySlaveId(task['slave_id']);
          if (url) {
            window.open(url + '/docker/' + dockerName);
          } else {
            this.$root.$children[0].warning('등록된 메트릭스 서비스가 없습니다.');
          }
        }
      },
      moveLog: function (task) {
        var me = this;
        if (this.appId || (this.appIds && this.appIds.length)) {
          this.$router.push(
            {
              name: 'serviceTaskLog',
              params: {appId: '/' + task.name, taskId: task.id}
            }
          )
        } else if (this.nodeId) {
          this.$router.push(
            {
              name: 'nodeTaskLog',
              params: {nodeId: me.nodeId, taskId: task.id}
            }
          )
        } else if (this.jobId) {
          this.$router.push(
            {
              name: 'jobTaskLog',
              params: {jobId: me.jobId, taskId: task.id}
            }
          )
        }
      },
      moveDetail: function (task) {
        var me = this;
        if (this.appId || (this.appIds && this.appIds.length)) {
          this.$router.push(
            {
              name: 'serviceTaskDetail',
              params: {appId: task.name, taskId: task.id}
            }
          )
        } else if (this.nodeId) {
          this.$router.push(
            {
              name: 'nodeTaskDetail',
              params: {nodeId: me.nodeId, taskId: task.id}
            }
          )
        } else if (this.jobId) {
          this.$router.push(
            {
              name: 'jobTaskDetail',
              params: {jobId: me.jobId, taskId: task.id}
            }
          )
        }
      },
      doFilter: function (event) {
        this.filter = event.target.value;
        this.makeList();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .healthCheck {
    display: inline-block;
    border: solid 1px;
    border-color: grey;
    border-radius: 50%;
    width: 15px;
    height: 15px;
    text-align: center;
  }

  ,
  .running {
    background-color: #adff21;
  }

  .dead {
    background-color: red;
  }
</style>
