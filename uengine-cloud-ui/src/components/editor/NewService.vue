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
          <md-button class="md-primary" v-on:click="reviewAndRun">REVIEW&RUN</md-button>
        </md-layout>
      </md-layout>
    </md-dialog-title>

    <md-dialog-content ref="container" style="overflow-x: hidden;padding: inherit;overflow-y: hidden;">
      <new-single-container v-if="containerView" :_service.sync="service" :jsonEditor.sync="jsonEditor"
                            :newSingleContainer.sync="newSingleContainer"
                            ref="rightSidenav"></new-single-container>
      <div v-if="!containerView && !appId">
        <md-layout md-gutter="16">
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
        newSingleContainer: false,
        service: undefined,
        beforeService: {}
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

            me.getDevAppByName(appName,
              function (response, fail) {
                if (response) {
                  //성공
                  //프로덕션은 프로덕션(신규), 프로덕션(롤백), 프로덕션(현재)
                  //프로덕션은 현재만 수정가능.  현재인지 아닌지 구별법은 prod.deployment 값이 deployment 랑 같은지 보기
                  if ((deployment == 'blue' || deployment == 'green') && response.data.prod.deployment != deployment) {
                    //현재 프로덕션이 아니므로 수정불가능 (롤백 또는 신규버젼 배포중){}
                    me.editable = false;
                  }
                  me.service = response.data[stage]['deploy-json'];
                } else if (fail) {
                  //실패
                  me.$root.$children[0].error('앱정보를 불러올 수 없습니다.');
                }
              });
          } else {
            this.service = this.getAppById(this.appId);
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
        this.service = undefined;
      },
      toggleRightSidenav() {
        this.$refs.rightSidenav.openSlideEditor();
      },
      closeRightSidenav() {
        this.$refs.rightSidenav.close();
      },
      reviewAndRun: function () {

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
