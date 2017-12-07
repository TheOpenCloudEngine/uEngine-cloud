<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog v-if="deploymentsRows"
             md-open-from="#open" md-close-to="#open" ref="open">

    <md-dialog-title>{{deploymentsRows.length}} Active Deployments</md-dialog-title>
    <md-dialog-content>
      <md-table>
        <md-table-header>
          <md-table-row>
            <md-table-head>AFFECTED SERVICES</md-table-head>
            <md-table-head>Started</md-table-head>
            <md-table-head>Status</md-table-head>
            <md-table-head></md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="row in deploymentsRows">
            <md-table-cell>
              <a v-on:click="focusDeploymentId(row.id)" style="cursor: pointer">{{row.id}}</a>
              <div v-if="row.appId">
                <br><span v-for="appId in row.appId">{{appId}}</span>
              </div>
            </md-table-cell>
            <md-table-cell>{{row.started}}
              <div v-if="row.appStarted">
                <br><span v-for="appStarted in row.appStarted">{{appStarted}}</span>
              </div>
            </md-table-cell>
            <md-table-cell>
              <div>
                <md-progress style="width: 100px;" class="md-accent" md-indeterminate></md-progress>
              </div>
              <div v-if="row.appStatus">
                <br><span v-for="appStatus in row.appStatus">{{appStatus}}</span>
              </div>
            </md-table-cell>

            <md-table-cell>
              <md-menu md-size="4" md-direction="bottom left">
                <md-button class="md-icon-button" md-menu-trigger>
                  <md-icon>more_vert</md-icon>
                </md-button>

                <md-menu-content>
                  <md-menu-item v-on:click="action(row.id)">
                    <span>Rollback</span>
                  </md-menu-item>
                </md-menu-content>
              </md-menu>
            </md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>
    </md-dialog-content>
    <md-dialog-actions>
      <md-button class="md-primary" @click="close">Close</md-button>
    </md-dialog-actions>
  </md-dialog>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        deploymentsRows: [],
        deployments: [],
        focusedList: []
      }
    },
    mounted(){

    },
    watch: {
      dcosData: {
        handler: function (newVal, oldVal) {
          this.deployments = newVal.deployments;
          this.createDeploymentsRows();
        },
        deep: true
      }
    }
    ,
    methods: {
      createDeploymentsRows: function () {
        var me = this;
        me.deploymentsRows = [];
        if(!me.deployments){
            return;
        }
        $.each(me.deployments, function (i, deployment) {
          var row = {
            id: deployment.id,
            started: deployment.version ? me.ddhhmmssDifFromDate(new Date(deployment.version)) : 'N/A',
            status: ''
          };
          //런 아이디가 포커스 상태일 경우
          if (me.focusedList.indexOf(deployment.id) != -1) {
            row.appId = [];
            row.appStarted = [];
            row.appStatus = [];
            $.each(deployment.currentActions, function (t, currentAction) {
              row.appId.push(currentAction.app);
              row.appStarted.push('');
              row.appStatus.push(currentAction.action);
            })
          }
          me.deploymentsRows.push(row);
        });
      },
      focusDeploymentId: function (deploymentId) {
        if (this.focusedList.indexOf(deploymentId) != -1) {
          this.focusedList.splice(this.focusedList.indexOf(deploymentId), 1);
        } else {
          this.focusedList.push(deploymentId);
        }
        this.createDeploymentsRows();
      },
      action: function (deploymentId) {
        this.rollback(deploymentId);
      },
      open() {
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
