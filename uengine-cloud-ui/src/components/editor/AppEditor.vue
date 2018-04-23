<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#open" md-close-to="#open" ref="open" class="fullscreen">
    <md-dialog-title style="margin-bottom: 0px;">
      <md-layout md-gutter>
        <md-layout md-flex="10">
          <md-button class="md-primary" @click="close">CANCEL</md-button>
        </md-layout>
        <md-layout md-flex="50" style="text-align: center;display: inline-block;">
          <span v-if="!appId">Run a Service</span>
          <span v-else>Edit "{{appId}}"</span>
        </md-layout>
        <md-layout md-flex="40" md-align="end">
          <md-switch id="my-test1" name="my-test1" class="md-primary" v-model="jsonEditor" @change="toggleRightSidenav"><span
            style="color: #aaaaaa;font-size:15px; align-items: right;">Json Editor</span></md-switch>
          <md-button class="md-primary" v-if="!reviewFlag" v-on:click="reviewService">REVIEW&RUN</md-button>
          <md-button class="md-primary" v-else v-on:click="runService">RUN SERVICE</md-button>
          <md-button class="md-primary" v-if="reviewFlag" v-on:click="back">EDIT SERVICE</md-button>
        </md-layout>
      </md-layout>
    </md-dialog-title>

    <md-dialog-content ref="container" style="overflow-x: hidden;padding: inherit;overflow-y: hidden;">
      <container-editor :_service.sync="service" :jsonEditor.sync="jsonEditor"
                        :editable.sync="editable"
                        ref="rightSidenav"
                        style="height:87vh;"
      ></container-editor>
    </md-dialog-content>
  </md-dialog>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'

  export default {
    mixins: [DcosDataProvider],
    props: {
      appId: String,
      mode: String,
    },
    data() {
      return {
        editable: true,
        jsonEditor: false,
        reviewFlag: false,
        service: undefined,
        appName: "",
        stage: ""
      }
    },
    mounted() {
      this.bindService();
    },
    watch: {
      appId: function () {
        this.bindService();
      }
    }
    ,
    methods: {
      bindService: function () {
        var me = this;
        me.editable = true;
        me.service = undefined;
        if (this.appId) {
          if (me.mode == 'app') {
            //this.appId 는 <appName>-dev,-stg,-blue,-green

            //appId 로부터 appName 을 추출하기
            var appName = this.appId;
            appName = appName.replace('-dev', '');
            appName = appName.replace('-stg', '');
            appName = appName.replace('-blue', '');
            appName = appName.replace('-green', '');

            //stage 추출하기. dev,stg,prod
            var stage = this.appId.replace(appName + '-', '');
            if (stage == 'blue' || stage == 'green') {
              stage = 'prod';
            }
            me.stage = stage;

            me.getDeployJson(appName, stage, function (response, fail) {
              if (response) {
                me.service = response.data;
              } else {
                me.$root.$children[0].error('앱정보를 불러올 수 없습니다.');
              }
            });
            me.appName = appName;
          } else {
            me.getMarathonAppById(me.appId, function (response) {
              me.service = response.data.app;
              me.appName = me.appId;
            });
          }
        }
      },
      open(ref) {
        this.editable = true;
        this.jsonEditor = false;
        this.reviewFlag = false;
        this.service = undefined;
        this.appName = null;
        this.deployment = null;
        this.bindService();
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      },
      toggleRightSidenav() {
        this.$refs.rightSidenav.openSlideEditor();
      },
      closeRightSidenav() {
        this.$refs.rightSidenav.close();
      },
      reviewService: function () {
        if (!this.$refs.rightSidenav.validation()) {
          this.$refs.rightSidenav.changeView('reviewview');
          this.reviewFlag = true;
        }
      },
      back: function () {
        this.$refs.rightSidenav.changeView('serviceview');
        this.reviewFlag = false;
      },
      runService: function () {
        //앱일때
        var me = this;
        var devApp = null;
        if (this.mode == 'app') {
          me.updateDeployJson(me.appName, me.stage, me.service, function (response) {
            if (response) {
              me.$root.$children[0].confirm(
                {
                  contentHtml: '앱의 구동 설정이 변경되었습니다. 지금 앱을 다시 시작하시겠습니까?',
                  okText: '진행하기',
                  cancelText: '취소',
                  callback: function () {
                    //runDeployedApp
                    me.deployApp(me.appName, me.stage, null, function (response) {
                      me.reviewFlag = false;
                      me.close();
                    });
                  }
                });
            }
          });
        }
        //서비스일때
        else {
          if (this.appId) {
            //update
            me.updateMarathonApp(me.appId, me.service, true, function (response) {
              me.close();
            });
          } else {
            //new
            me.createMarathonApp(me.service, function (response) {
              me.close();
            });
          }
        }
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-dialog-title {
    background-color: #f5f5f6;
    height: 12%;
    border-bottom: solid 1px #e6e6e6
  }

</style>
