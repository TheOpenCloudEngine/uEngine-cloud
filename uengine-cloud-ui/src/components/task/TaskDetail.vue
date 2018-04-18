<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <div v-if="task">
      <md-table v-once>
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Configration</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell><span class="md-title">TASK ID</span></md-table-cell>
            <md-table-cell>{{task.id}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">SERVICE</span></md-table-cell>
            <md-table-cell>{{task.framework_id}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">NODE</span></md-table-cell>
            <md-table-cell>{{host}} ({{task.slave_id}})</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">ENDPOINTS</span></md-table-cell>
            <md-table-cell v-if="app">{{host}}:{{task.container.docker.port_mappings[0].host_port}}</md-table-cell>
            <md-table-cell v-else>{{host}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">CPU</span></md-table-cell>
            <md-table-cell>{{task.resources.cpus}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">MEMORY</span></md-table-cell>
            <md-table-cell>{{task.resources.mem}} MiB</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">DISK</span></md-table-cell>
            <md-table-cell>{{task.resources.disk}} B</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>


      <md-table v-once style="margin-top: 10%;">
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Labels</span></md-table-head>
          </md-table-header>
          <md-table-row v-if="task.labels" v-model="task.labels" v-for="label in task.labels">
            <md-table-cell><span class="md-title">{{label.key}}</span></md-table-cell>
            <md-table-cell>{{label.value}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

      <md-table v-once style="margin-top: 10%;" v-if="app">
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title"
                                 style="color: #111111;font-size: 30px;">Marathon Task Configuration</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell><span class="md-title">HOST</span></md-table-cell>
            <md-table-cell>{{app.host}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">PORTS</span></md-table-cell>
            <md-table-cell>{{task.discovery.ports.ports[0].number}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">STATUS</span></md-table-cell>
            <md-table-cell>{{task.state}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">STAGED AT</span></md-table-cell>
            <md-table-cell>{{app.stagedAt}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">STARTED AT</span></md-table-cell>
            <md-table-cell>{{app.startedAt}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">VERSION</span></md-table-cell>
            <md-table-cell>{{app.version}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>


      <md-table v-once style="margin-top: 10%;" v-if="app && app.healthCheckResults">
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Health Check Result 1</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell><span class="md-title">FIRST SUCCESS</span></md-table-cell>
            <md-table-cell>{{app.healthCheckResults[0].firstSuccess}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">LAST SUCCESS</span></md-table-cell>
            <md-table-cell>{{app.healthCheckResults[0].lastSuccess}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">LAST FAILURE</span></md-table-cell>
            <md-table-cell>{{app.healthCheckResults[0].lastFailure}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">CONSECUTIVE FAILURES</span></md-table-cell>
            <md-table-cell>{{app.healthCheckResults[0].consecutiveFailures}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">ALIVE</span></md-table-cell>
            <md-table-cell>{{app.healthCheckResults[0].alive}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>


      <md-table v-if="app" v-once style="margin-top: 10%;">
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Container Configuration</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell>
              <pre>{{ task.container }}</pre>
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
        task: null,
        app: null,
        host: null
      }
    },
    mounted() {

    },
    watch: {
      'dcosData': {
        handler: function (newVal, oldVal) {
          var me = this;
          var task = me.getTaskById(this.taskId);
          var app;
          if (!task) {
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
          var host = me.getHostBySlaveId(task['slave_id']);

          //마라톤 타스크일 경우
          if (marathonId == task['framework_id']) {
            task.framework = 'marathon';
            app = me.getMarathonAppById('/' + task.name);
          } else {
            task.framework = 'metronome';
          }
          me.task = task;
          me.host = host;
          me.app = app;
        },
        deep: true
      }
    },
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-title {
    font-size: 17px;
    color: #111111;
  }

  ;
</style>
