<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#open" md-close-to="#open" ref="open" class="fullscreen">
    <md-dialog-title style="margin-bottom: 0px;">
      <md-layout md-gutter>
        <md-layout md-flex="10">
          <md-button class="md-primary" @click="back" v-if="containerView && !appId">BACK</md-button>
          <md-button class="md-primary" @click="close" v-else>CANCEL</md-button>
        </md-layout>
        <md-layout md-flex="70" style="text-align: center;display: inline-block;">
          <span>Run a Service</span>
        </md-layout>
        <md-layout md-flex="20">
          <md-switch id="my-test1" name="my-test1" class="md-primary" v-model="jsonEditor" @change="toggleRightSidenav"
                     v-if="containerView"><span
            style="color: #aaaaaa;font-size:15px; align-items: right;">Json Editor</span></md-switch>
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
      appId: String
    },
    data() {
      return {
        containerView: false,
        jsonEditor: false,
        newSingleContainer : false,
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
        if (this.appId) {
          this.containerView = true;
          this.service = this.getAppById(this.appId);
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
