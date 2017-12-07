<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#openScale" md-close-to="#openScale" ref="open">
    <md-dialog-title v-if="force">
      서비스가 현재 하나 이상의 배포에 의해 잠겨 있습니다.<br>
      버튼을 다시 누르면 새 구성을 강제로 변경하고 배포합니다.
    </md-dialog-title>
    <div v-if="role == 'suspend'">
      <md-dialog-title>Suspend Service</md-dialog-title>
      <md-dialog-content>
        <span>{{appId}} 서비스를 일시 중단하면 현재 실행중인 모든 서비스 인스턴스가 제거됩니다. 서비스는 삭제되지 않습니다.</span>
      </md-dialog-content>
      <md-dialog-actions>
        <md-button class="md-primary" @click="action">Suspend Service</md-button>
        <md-button class="md-primary" @click="close">Close</md-button>
      </md-dialog-actions>
    </div>
    <div v-if="role == 'scale'">
      <md-dialog-title>Scale Service</md-dialog-title>
      <md-dialog-content>
        <span>몇 개의 인스턴스로 확장 하시겠습니까?</span>
        <md-input-container>
          <label>Scale</label>
          <md-input type="number" v-model.number="instances"></md-input>
        </md-input-container>
      </md-dialog-content>
      <md-dialog-actions>
        <md-button class="md-primary" @click="action">Scale Service</md-button>
        <md-button class="md-primary" @click="close">Close</md-button>
      </md-dialog-actions>
    </div>
    <div v-if="role == 'restart'">
      <md-dialog-title>Restart Service</md-dialog-title>
      <md-dialog-content>
        <span>{{appId}} 서비스를 다시 시작하면 현재 실행중인 서비스 인스턴스가 모두 제거 된 다음 제거 된 인스턴스와 동일한 새 인스턴스를 만들려고 시도합니다.</span>
      </md-dialog-content>
      <md-dialog-actions>
        <md-button class="md-primary" @click="action">Restart Service</md-button>
        <md-button class="md-primary" @click="close">Close</md-button>
      </md-dialog-actions>
    </div>
    <div v-if="role == 'delete'">
      <md-dialog-title>Delete Service</md-dialog-title>
      <md-dialog-content>
        <span>이 작업은 실행 취소 할 수 없습니다. {{appId}} 서비스가 영구적으로 삭제됩니다.</span>
      </md-dialog-content>
      <md-dialog-actions>
        <md-button class="md-primary" @click="action">Delete Service</md-button>
        <md-button class="md-primary" @click="close">Close</md-button>
      </md-dialog-actions>
    </div>
  </md-dialog>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        appId: null,
        role: null,
        force: false,
        instances: 0
      }
    },
    mounted(){

    }
    ,
    //TODO newservice 도 다이어로그창 빼기
    methods: {
      action: function () {
        //409
        var me = this;
        if (this.role == 'scale') {
          this.scaleApp(this.appId, this.instances, this.force, function (response) {
            if (response.status == 409) {
              me.force = true;
            } else {
              me.force = false;
            }
          });
        }
        if (this.role == 'suspend') {
          this.suspendApp(this.appId, this.force, function (response) {
            if (response.status == 409) {
              me.force = true;
            } else {
              me.force = false;
            }
          });
        }
        if (this.role == 'restart') {
          this.restartApp(this.appId, this.force, function (response) {
            if (response.status == 409) {
              me.force = true;
            } else {
              me.force = false;
            }
          });
        }
        if (this.role == 'delete') {
          this.deleteApp(this.appId, this.force, function (response) {
            if (response.status == 409) {
              me.force = true;
            } else {
              me.force = false;
            }
          });
        }
      },
      open(appId, role) {
        this.appId = appId;
        this.role = role;
        this.force = false;
        this.$refs['open'].open();
        var app = this.getAppById(appId);
        if (app) {
          this.instances = app.instances;
        }
      },
      close(ref) {
        this.$refs['open'].close();
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
