<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#open" md-close-to="#open" ref="open" class="fullscreen">
    <md-dialog-title style="margin-bottom: 0px;">
      <md-layout md-gutter>
        <md-layout md-flex="5">
          <md-button class="md-primary" @click="back" v-if="containerView && !appId">BACK</md-button>
          <md-button class="md-primary" @click="close" v-else>CANCEL</md-button>
        </md-layout>
        <md-layout md-flex="60" style="text-align: center;display: inline-block;">
          <span v-if="!appId">Run a Service</span>
          <span v-else>Edit "{{appId}}"</span>
        </md-layout>
        <md-layout md-flex="15">
          <md-switch id="my-test1" name="my-test1" class="md-primary" v-model="jsonEditor" @change="toggleRightSidenav"
                     v-if="containerView"><span
            style="color: #aaaaaa;font-size:15px; align-items: right;">Json Editor</span></md-switch>
        </md-layout>
        <md-layout md-flex="20" v-if="containerView">
          <md-button class="md-primary" v-if="!reviewFlag" v-on:click="reviewService">REVIEW&RUN</md-button>
          <md-button class="md-primary" v-else v-on:click="runService">RUN SERVICE</md-button>
        </md-layout>
      </md-layout>
    </md-dialog-title>

    <md-dialog-content ref="container" style="overflow-x: hidden;padding: inherit;overflow-y: hidden;">
      <new-single-container v-if="containerView" :_service.sync="service" :jsonEditor.sync="jsonEditor"
                            :editable.sync="editable"
                            :newSingleContainer.sync="newSingleContainer"
                            ref="rightSidenav"></new-single-container>
      <div v-if="!containerView && !appId">
        <md-layout md-gutter="16" style="margin-top: 20px;">
          <md-layout>
            <md-card md-with-hover>
              <md-card-media-cover md-text-scrim>
                <md-card-media md-ratio="1:1" style="width: 200px;">
                  <img src="/static/image/container/con-1.png" alt="Skyscraper">
                </md-card-media>

                <md-card-area>
                  <md-card-header>
                    <div class="md-title" @click="changeView">Single Container</div>
                  </md-card-header>
                </md-card-area>
              </md-card-media-cover>
            </md-card>
          </md-layout>
          <md-layout>
            <md-card md-with-hover>
              <md-card-media-cover md-text-scrim>
                <md-card-media md-ratio="1:1" style="width: 200px;">
                  <img src="/static/image/container/con-1.png" alt="Skyscraper">
                </md-card-media>

                <md-card-area>
                  <md-card-header>
                    <div class="md-title">Multi-container (Pod)</div>
                  </md-card-header>
                </md-card-area>
              </md-card-media-cover>
            </md-card>
          </md-layout>
        </md-layout>
        <br>
        <md-layout md-gutter="16">
          <md-layout>
            <md-card md-with-hover>
              <md-card-media-cover md-text-scrim>
                <md-card-media md-ratio="1:1" style="width: 200px;">
                  <img src="/static/image/container/con-2.png" alt="Skyscraper">
                </md-card-media>

                <md-card-area>
                  <md-card-header>
                    <div class="md-title">JSON Configuration</div>
                  </md-card-header>
                </md-card-area>
              </md-card-media-cover>
            </md-card>
          </md-layout>
          <md-layout>
            <md-card md-with-hover>
              <md-card-media-cover md-text-scrim>
                <md-card-media md-ratio="1:1" style="width: 200px;">
                  <img src="/static/image/container/con-3.png" alt="Skyscraper">
                </md-card-media>

                <md-card-area>
                  <md-card-header>
                    <div class="md-title">Devops Container</div>
                  </md-card-header>
                </md-card-area>
              </md-card-media-cover>
            </md-card>
          </md-layout>
        </md-layout>
      </div>
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
        containerView: false,
        jsonEditor: false,
        reviewFlag: false,
        newSingleContainer: false,
        service: undefined,
        beforeService: {},
        appName: "",
        deployment: "",
      }
    },
    mounted() {
      this.bindService();
      this.beforeService = this.service;
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
          this.containerView = true;
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
                  me.service = response.data[stage]['deploy-json'];
                  me.$set(me.service,"servicePort",response.data[stage]['service-port']);
                  console.log("newService response.data[stage]", response.data[stage]);
                } else if (fail) {
                  //실패
                  me.$root.$children[0].error('앱정보를 불러올 수 없습니다.');
                }
              });
            me.appName = appName;
          } else {
            this.service = this.getAppById(this.appId);
            me.appName = this.appId;
          }
        }
      },
      open(ref) {
        this.bindService();
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
        this.containerView = false;
      },
      back(ref) {
        if (this.service != this.beforeService) {
          if (!confirm("모든내용이 삭제됩니다. 뒤로가시겠습니까?")) {
            return;
          }
          this.service = this.beforeService;
        }
        this.containerView = false;
      },
      changeView: function () {
        this.containerView = true;
        this.newSingleContainer = true;
        this.jsonEditor = false;
        this.service = undefined;
      },
      toggleRightSidenav() {
        this.$refs.rightSidenav.openSlideEditor();
      },
      closeRightSidenav() {
        this.$refs.rightSidenav.close();
      },
      reviewService: function () {
        if (!this.$refs.rightSidenav.validation()){
          this.reviewFlag = this.$refs.rightSidenav.changeView('reviewview');
        }
      },
      runService: function () {
        //앱일때
        var me = this;
        var devApp = null;
        if (this.mode == 'app') {
          me.getDevAppByName(me.appName.replace("/", ""), function (response, error) {
            console.log("response", response)
            if (response) {
              devApp = response.data;
              devApp[me.deployment]['deploy-json'] = me.service;
              me.$root.backend('app' + me.appName).update(devApp)
                .then(
                  function (response) {
                    //성공
                    console.log("success", response);
                    me.$root.backend('app' + me.appName + "/deploy?stage=" + me.deployment).save({})
                      .then(function (response) {
                          // deploy 성공 메시지 변경
                        console.log('me.deployment', me.deployment, response);
                          me.reviewFlag = false;
                          me.$root.$children[0].success("수정하였습니다.");
                          me.close();
                        },
                        function (response) {
                          // deploy 실패 메시지 변경
                          me.reviewFlag = false;
                          me.$root.$children[0].error("수정에 실패하였습니다.");
                        });

                  },
                  function (response) {
                    //실패 메시지 변경
                    me.reviewFlag = false;
                    me.$root.$children[0].error("서버에 접속할 수 없습니다.");
                  }
                );
            }
          })
        } else {
          //서비스일때
          console.log("service");
          if (this.newSingleContainer) {
            //new
            //POST http://cloud-server.pas-mini.io/dcos/service/marathon/v2/apps
            me.$root.backend('dcos/service/marathon/v2/apps').save(me.service)
              .then(function (response) {
                  // deploy 성공 메시지 변경
                  me.$root.$children[0].success("저장하였습니다.");
                  me.close();
                },
                function (response) {
                  // deploy 실패 메시지 변경
                  console.log("failed",response);
                  me.$root.$children[0].error("저장에 실패하였습니다.");
                });
          } else {
            //update
            //PUT http://cloud-server.pas-mini.io/dcos/service/marathon/v2/apps//uengine-cloud-ui?partialUpdate=false&force=false
            me.$root.backend('dcos/service/marathon/v2/apps/'+me.appId+"?partialUpdate=false&force=false").update(me.service)
              .then(function (response) {
                  // deploy 성공 메시지 변경
                  me.$root.$children[0].success("수정하였습니다.");
                  me.close();
                },
                function (response) {
                  // deploy 실패 메시지 변경
                  console.log("failed",response);
                  me.$root.$children[0].error("수정에 실패하였습니다.");
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
    height: 15%;
    border-bottom: solid 1px #e6e6e6
  }

</style>
