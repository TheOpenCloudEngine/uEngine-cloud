<template>
  <md-layout>
    <div v-if="app">
      <md-table v-once>
        <md-table-body>
          <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">General</span></md-table-head>
          </md-table-header>
          <md-table-row>
            <md-table-cell><span class="md-title">SERVICE ID</span></md-table-cell>
            <md-table-cell>{{app.id}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">INSTANCES</span></md-table-cell>
            <md-table-cell>{{app.instances}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">CONTAINER RUNTIME</span></md-table-cell>
            <md-table-cell>{{app.container.type}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">CPU</span></md-table-cell>
            <md-table-cell>{{app.cpus}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">MEMORY</span></md-table-cell>
            <md-table-cell>{{app.mem}} MiB</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">DISK</span></md-table-cell>
            <md-table-cell>{{app.disk}} B</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">BACKOFF SECONDS</span></md-table-cell>
            <md-table-cell>{{app.backoffSeconds}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">BACKOFF FACTOR</span></md-table-cell>
            <md-table-cell>{{app.backoffFactor}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">BACKOFF MAX LAUNCH DELAY</span></md-table-cell>
            <md-table-cell>{{app.maxLaunchDelaySeconds}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">CONTAINER IMAGE</span></md-table-cell>
            <md-table-cell>{{app.container.docker.image}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">EXTENDED RUNTIME PRIV.</span></md-table-cell>
            <md-table-cell>{{app.container.docker.privileged}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">FORCE PULL ON LAUNCH</span></md-table-cell>
            <md-table-cell>{{app.container.docker.forcePullImage}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">COMMAND</span></md-table-cell>
            <md-table-cell>
              <span v-if="app.command">{{app.command}}</span>
              <span v-else>Not Configured</span>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">RESOURCE ROLES</span></md-table-cell>
            <md-table-cell v-if="app.acceptedResourceRoles">{{app.acceptedResourceRoles[0]}}</md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-title">VERSION</span></md-table-cell>
            <md-table-cell>{{app.version}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>


      <md-table v-once style="margin-top: 10%;">

        <md-table-header>
            <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Network</span></md-table-head>
        </md-table-header>
        <md-table-row>
          <md-table-cell><span style="color: #111111;font-size: 15px;">NETWORK MODE</span></md-table-cell>
          <md-table-cell>{{app.networks[0].mode}}</md-table-cell>
        </md-table-row>
        <md-table-header>
          <md-table-head><span class="md-title" style="color: #111111;font-size: 25px;">Service Endpoints</span>
          </md-table-head>
        </md-table-header>
        <md-table-header>
          <md-table-row>
            <md-table-head><span style="font-size: 15px;">NAME</span></md-table-head>
            <md-table-head><span style="font-size: 15px;">PROTOCOL</span></md-table-head>
            <md-table-head><span style="font-size: 15px;">CONTAINER PORT</span></md-table-head>
            <md-table-head><span style="font-size: 15px;">HOST PORT</span></md-table-head>
            <md-table-head><span style="font-size: 15px;">SERVICE PORT</span></md-table-head>
            <md-table-head><span style="font-size: 15px;">LOAD BALANCED ADDRESS</span></md-table-head>
          </md-table-row>
        </md-table-header>
        <md-table-body>
          <md-table-row>
            <md-table-cell style="color: #111111;">
              <span v-if="app.container.portMappings[0].name">{{app.container.portMappings[0].name}}</span>
              <span v-else>Not Configured</span>
            </md-table-cell>
            <md-table-cell style="color: #111111;">{{app.container.portMappings[0].protocol}}</md-table-cell>
            <md-table-cell style="color: #111111;">{{app.container.portMappings[0].containerPort}}</md-table-cell>
            <md-table-cell style="color: #111111;">{{app.container.portMappings[0].hostPort}}</md-table-cell>
            <md-table-cell style="color: #111111;">{{app.container.portMappings[0].servicePort}}</md-table-cell>
            <md-table-cell style="color: #111111;">
              <span v-if="app.container.portMappings[0].loadBalancedAddress">{{app.container.portMappings[0].loadBalancedAddress}}</span>
              <span v-else>Not Enabled</span>
            </md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

      <md-table v-once style="margin-top: 10%;">
        <md-table-header>
          <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Environment Variables</span>
          </md-table-head>
        </md-table-header>
        <md-table-header>
          <md-table-row>
            <md-table-head style="color: #111111;font-size: 12px;">KEY</md-table-head>
            <md-table-head style="color: #111111;font-size: 12px;">VALUE</md-table-head>
          </md-table-row>
        </md-table-header>
        <md-table-body>
          <md-table-row v-model="app.env" v-for="(value,key) in app.env">
            <md-table-cell>{{key}}</md-table-cell>
            <md-table-cell>{{value}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

      <md-table v-once style="margin-top: 10%;">
        <md-table-header>
          <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Labels</span></md-table-head>
        </md-table-header>
        <md-table-header>
          <md-table-row>
            <md-table-head style="color: #111111;font-size: 13px;">KEY</md-table-head>
            <md-table-head style="color: #111111;font-size: 13px;">VALUE</md-table-head>
          </md-table-row>
        </md-table-header>
        <md-table-body>
          <md-table-row v-model="app.labels" v-for="(value,key) in app.labels">
            <md-table-cell>{{key}}</md-table-cell>
            <md-table-cell>{{value}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

      <md-table v-once style="margin-top: 10%;margin-bottom:10%;">
        <md-table-body>
          <md-table-header>
            <md-table-head>
              <span class="md-title" style="color: #111111;font-size: 30px;">Health Checks</span>
            </md-table-head>
          </md-table-header>
          <md-table-row>
            <!--<md-table-cell><span class="md-title">FIRST SUCCESS</span></md-table-cell>-->
            <!--<md-table-cell>{{app.tasks[0].healthCheckResults[0].firstSuccess}}</md-table-cell>-->
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
    filters: {
      app: function (value) {
        return JSON.stringify(JSON.parse(value), null, 2);
      }
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
