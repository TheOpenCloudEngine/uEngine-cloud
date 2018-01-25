<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog v-if="deploymentsRows"
             md-open-from="#open" md-close-to="#open" ref="open"
  >

    <md-dialog-title>{{deploymentsRows.length}} Active Deployments</md-dialog-title>
    <md-dialog-content>
      <md-table>
        <md-table-header>
          <md-table-row>
            <md-table-head>AFFECTED SERVICES</md-table-head>
            <md-table-head>Action</md-table-head>
            <md-table-head>Started</md-table-head>
            <md-table-head>Status</md-table-head>
            <md-table-head></md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="row in deploymentsRows">
            <md-table-cell>
              <div v-if="row.appId">
                <span v-for="appId in row.appId">{{appId}}</span>
              </div>
            </md-table-cell>
            <md-table-cell>
              <div v-if="row.appStatus">
                <span v-for="appStatus in row.appStatus">{{appStatus}}</span>
              </div>
            </md-table-cell>
            <md-table-cell>{{row.started}}
            </md-table-cell>
            <md-table-cell>
              <div>
                <md-progress style="width: 100px;" class="md-accent" md-indeterminate></md-progress>
              </div>
            </md-table-cell>

            <md-table-cell>
              <md-menu md-size="4" md-direction="bottom left">
                <md-button :disabled="row.accessLevel < 30 && !isAdmin"
                           class="md-icon-button" md-menu-trigger>
                  <md-icon>more_vert</md-icon>
                </md-button>

                <md-menu-content>
                  <md-menu-item v-on:click="action(row.id)">
                    <span>배포 중단</span>
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

    <confirm
      title="Are you sure?"
      content-html="현재 배포가 중단되고, 서비스를 이전 버전으로 되돌리기 위해 새 배포가 시작될 것입니다."
      ok-text="배포 중단 하기"
      cancel-text="취소"
      ref="confirm"
    ></confirm>
  </md-dialog>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {
      //:appIds="stage == 'prod' ? ['/'+ appName + '-blue', '/'+ appName + '-green'] : [devApp[stage]['marathonAppId']]"
      appIds: Array
    },
    data() {
      return {
        deploymentsRows: [],
        deployments: [],
        isAdmin: false
      }
    },
    mounted(){
      this.isAdmin = window.localStorage['acl'] == 'admin' ? true : false;
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
        var deploymentsRows = [];
        if (!me.deployments) {
          return;
        }
        $.each(me.deployments, function (i, deployment) {
          var row = {
            id: deployment.id,
            started: deployment.version ? me.ddhhmmssDifFromDate(new Date(deployment.version)) : 'N/A',
            status: '',
            accessLevel: 0
          };
          //런 아이디가 포커스 상태일 경우
          row.appId = [];
          row.appStarted = [];
          row.appStatus = [];
          $.each(deployment.currentActions, function (t, currentAction) {
            row.appId.push(currentAction.app);
            row.appStarted.push('');
            row.appStatus.push(currentAction.action);
          });


          //필터링한다.
          if (row.appId && row.appId.length) {
            //appId 로부터 앱이름을 추출한다.

            var isMine = false;

            $.each(row.appId, function (a, id) {
                var appName = id;
                appName = appName.replace('/', '');
                appName = appName.replace('-dev', '');
                appName = appName.replace('-stg', '');
                appName = appName.replace('-blue', '');
                appName = appName.replace('-green', '');
                var app = me.dcosData.devopsApps[appName];
                if (app) {
                  row.accessLevel = app.accessLevel;
                }

                //앱 별 보기 목록인 경우
                if (me.appIds && me.appIds.length) {
                  //appIds 에 포함되어있다면 통과.
                  if (me.appIds.indexOf(id) != -1) {
                    isMine = true;
                  }
                }
                //전체 보기 목록인 경우
                else {
                  isMine = true;
                }
              }
            );
            if (isMine) {
              deploymentsRows.push(row);
            }
          }
        });
        me.deploymentsRows = deploymentsRows;
        me.$emit('rows', deploymentsRows.length);
      },
      action: function (deploymentId) {
        var me = this;
        me.$refs['confirm'].open(function () {
          me.rollback(deploymentId);
        })
      }
      ,
      open()
      {
        this.$refs['open'].open();
      }
      ,
      close(ref)
      {
        this.$refs['open'].close();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
