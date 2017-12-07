<template xmlns:v-bind="http://www.w3.org/1999/xhtml">
  <span>
      <span v-if="app.tasksStaged > 0">
                      <md-progress v-bind:class="{ 'fullWidth': fullWidth }" class="md-accent"
                                   md-indeterminate></md-progress>
                    {{rollbackStatus}} Staging
                    </span>
        <span v-else-if="app.instances == 0">
                      {{rollbackStatus}} Suspended({{app.instances}})
                    </span>
        <span v-else-if="app.instances > app.tasksRunning">
                      <md-progress v-bind:class="{ 'fullWidth': fullWidth }" class="md-warn"
                                   :md-indeterminate="100"></md-progress>
                      {{rollbackStatus}} Waiting ({{app.tasksRunning}}/{{app.instances}})
                    </span>
        <span v-else-if="app.deployments.length > 0">
                      <md-progress v-bind:class="{ 'fullWidth': fullWidth }" class="md-accent"
                                   md-indeterminate></md-progress>
                    {{rollbackStatus}} Deploying({{app.tasksRunning}}/{{app.instances}})
                    </span>
        <span v-else-if="app.tasksUnhealthy > 0">
                      <md-progress v-bind:class="{ 'fullWidth': fullWidth }" class="md-warn"
                                   :md-progress="100"></md-progress>
                      {{rollbackStatus}} Unhealthy({{app.tasksUnhealthy}}/{{app.tasksRunning}})
                    </span>
        <span v-else-if="app.tasksHealthy == app.tasksRunning">
                      <md-progress v-bind:class="{ 'fullWidth': fullWidth }" :md-progress="100"></md-progress>
                      {{rollbackStatus}} Healthy({{app.tasksHealthy}})
                    </span>
        <span v-else>
                    Idle
                  </span>
  </span>
</template>
<script>
  export default {
    props: {
      rollback: {
        type: Boolean,
        default: false
      },
      fullWidth: {
        type: Boolean,
        default: false
      },
      app: Object
    },
    computed: {
      rollbackStatus: function () {
        var status = '';
        if (this.rollback) {
          if (this.app.deployments.length) {
            status = '(신규)'
          } else {
            status = '(롤백)'
          }
        }
        return status;
      }
    },
    data() {
      return {}
    },
    mounted() {

    },
    watch: {},
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-progress {
    width: 100px;
  }

  .md-progress.fullWidth {
    width: 100%;
  }
</style>
