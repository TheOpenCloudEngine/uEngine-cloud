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
        deployment: "",
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

            //deployment 추출하기. dev,stg,blue,green
            var deployment = this.appId.replace(appName + '-', '');

            //stage 구하기
            var stage = deployment;
            if (deployment == 'blue' || deployment == 'green') {
              stage = 'prod';
            }
            me.deployment = stage;
            me.getDevAppByName(appName,
              function (response, fail) {
                if (response) {
                  //성공
                  //프로덕션은 프로덕션(신규), 프로덕션(롤백), 프로덕션(현재)
                  //프로덕션은 현재만 수정가능.  현재인지 아닌지 구별법은 prod.deployment 값이 deployment 랑 같은지 보기
                  if ((deployment == 'blue' || deployment == 'green') && (response.data.prod.deployment != deployment)) {
                    //현재 프로덕션이 아니므로 수정불가능 (롤백 또는 신규버젼 배포중){}
                    me.editable = false;
                  }
                  me.service = response.data[stage]['deployJson'];
                  me.$set(me.service, "servicePort", response.data[stage]['servicePort']);
                  console.log("newService response.data[stage]", response.data[stage]);
                } else if (fail) {
                  //실패
                  me.$root.$children[0].error('앱정보를 불러올 수 없습니다.');
                }
              });
            me.appName = appName;
          } else {
            this.service = this.getMarathonAppById(this.appId);
            me.appName = this.appId;
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
          me.getDevAppByName(me.appName.replace("/", ""), function (response, error) {
            if (response) {
              devApp = response.data;
              devApp[me.deployment]['deployJson'] = me.service;
              me.updateApp(me.appName, devApp, function (response) {
                if (response) {
                  me.$root.$children[0].confirm(
                    {
                      contentHtml: '앱의 구동 설정이 변경되었습니다. 지금 앱을 다시 시작하시겠습니까?',
                      okText: '진행하기',
                      cancelText: '취소',
                      callback: function () {
                        //runDeployedApp
                        me.runDeployedApp(me.appName, me.deployment, null, function (response) {
                          me.reviewFlag = false;
                          me.close();
                        });
                      }
                    });
                }
              });
            }
          })
        }
        //서비스일때
        else {
          if (this.appId) {
            //update
            me.updateDcosApp(me.appId, me.service, true, function (response) {
              me.close();
            });
          } else {
            //new
            me.createDcosApp(me.service, function (response) {
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
