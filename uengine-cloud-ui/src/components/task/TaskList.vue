<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
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
              <md-table-cell v-if="!simple">{{task.cpus}}</md-table-cell>
              <md-table-cell v-if="!simple">{{task.mem}}</md-table-cell>
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
      marathonAppId: String,
      simple: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        list: [],
        total: 10,
        size: 25,
        page: 1
      }
    },
    computed: {
      targetAppId: function () {
        //appId 는 패스값.
        //marathonAppId 는 프롭스값
        return this.marathonAppId ? this.marathonAppId : this.appId;
      }
    },
    mounted() {
      var me = this;
      me.makeList();
      window.busVue.$on('marathonApp', function (event) {
        var marathonAppId = event.body.app.id;
        if (me.targetAppId && marathonAppId == me.targetAppId) {
          me.makeList();
        }
      });
    },
    watch: {},
    methods: {
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      makeList: function () {
        var me = this;
        if (this.targetAppId) {
          me.getMarathonAppById(me.targetAppId, function (response) {
            if (response) {
              console.log(response);
              var list = response.data.app.tasks;
              list.forEach(function (task, index) {
                task.cpus = response.data.app.cpus;
                task.mem = response.data.app.mem;
                task.disk = response.data.app.disk;
              })
              completeList(list);
            }
          })
        } else if (this.nodeId) {
          me.getTasksBySlaveId(me.nodeId, function (response) {
            if (response) {
              var list = response.data;
              completeList(list);
            }
          })
        }

        var completeList = function (list) {
          me.list = [];
          $.each(list, function (index, task) {
            if (task.healthCheckResults && task.healthCheckResults.length) {
              task.healthCheckResults = task.healthCheckResults[0].alive;
            }
            task.name = task.id.split('.')[0];
          });

          //페이지네이션
          var offset = (me.page - 1) * me.size;
          var limit = (me.page) * me.size - 1;

          var count = 0;
          for (var i = 0; i < list.length; i++) {
            if (count >= offset && count <= limit) {
              me.list.push(list[i]);
            }
            count++;
          }
          me.total = count;
        }
      },
      moveCadvisor: function (task) {
        var me = this;
        me.getMesosTaskById(task.id, function (response) {
          if (response) {
            var mesosTask = response.data;
            if (mesosTask['statuses'][0]) {
              var dockerName = 'mesos-' + mesosTask['statuses'][0]['container_status']['container_id'].value;
              var url = me.getCadvisorUrlBySlaveId(mesosTask['slave_id']);
              if (url) {
                window.open(url + '/docker/' + dockerName);
              } else {
                me.$root.$children[0].warning('등록된 메트릭스 서비스가 없습니다.');
              }
            }
          }
        })
      },
      moveLog: function (task) {
        var me = this;
        if (this.targetAppId) {
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
        }
      },
      moveDetail: function (task) {
        var me = this;
        if (this.targetAppId) {
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
        }
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
