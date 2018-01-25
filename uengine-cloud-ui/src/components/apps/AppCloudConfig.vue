<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-dialog
      md-open-from="#confirm" md-close-to="#confirm" ref="open">
      <md-dialog-title>설정 파일 미리보기</md-dialog-title>
      <md-dialog-content>
        <div>
          <p>* 어플리케이션이 다운로드 받게 될 최종 환경설정의 미리보기 입니다. </p>
          <p>* 외부 네트워크에서는 이 설정에 대해 접근이 차단됩니다. </p>
          <br>
        </div>
        <div style="width: 100%">
          <codemirror :options="{
              theme: 'dracula',
              mode: 'json',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
              readOnly: true
            }"
                      :value="previewConfig"></codemirror>
        </div>
      </md-dialog-content>
      <md-dialog-actions>
        <md-button class="md-primary" @click="close">닫기</md-button>
      </md-dialog-actions>
    </md-dialog>

    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <div>
          <md-layout md-align="center">
            <md-radio v-model="menu" :mdValue="'vcap'">
              <span class="md-caption">서비스 링크</span>
            </md-radio>
            <md-radio v-model="menu" :mdValue="'common'">
              <span class="md-caption">공통 설정 파일</span>
            </md-radio>
            <md-radio v-model="menu" :mdValue="'config'">
              <span class="md-caption">
                <span v-if="stage == 'dev'">개발</span>
                <span v-if="stage == 'stg'">스테이징</span>
                <span v-if="stage == 'prod'">프로덕션</span>

                설정 파일</span>
            </md-radio>
            <md-radio v-model="menu" :mdValue="'url'">
              <span class="md-caption">설정 파일 미리보기</span>
            </md-radio>
          </md-layout>
        </div>
        <div v-show="menu == 'vcap'">
          <codemirror
            ref="vcapRef"
            :options="{
              theme: 'dracula',
              mode: 'yaml',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
              readOnly: true
            }"
            :value="vcapCode"></codemirror>
        </div>
        <div v-show="menu == 'common'">
          <md-button v-if="commonChanged" class="md-primary md-raised"
                     v-on:click="saveCommon">저장
          </md-button>
          <codemirror v-if="commonToggled"
                      ref="commonRef"
                      :options="{
              theme: 'dracula',
              mode: 'yaml',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
              readOnly: false
            }"
                      v-on:change="onCommonChange"
                      :value="commonCode"></codemirror>
        </div>
        <div v-show="menu == 'config'">
          <md-button v-if="codeChanged" class="md-primary md-raised"
                     v-on:click="saveConfig">저장
          </md-button>
          <codemirror v-if="configToggled"
                      ref="configRef"
                      :options="{
              theme: 'dracula',
              mode: 'yaml',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
              readOnly: false
            }"
                      v-on:change="onCodeChange"
                      :value="configCode"></codemirror>
        </div>
        <div v-show="menu == 'url'">
          <md-table>
            <md-table-header>
              <md-table-row>
                <md-table-head>유형</md-table-head>
                <md-table-head>주소</md-table-head>
              </md-table-row>
            </md-table-header>

            <md-table-body>
              <md-table-row v-for="url in configUrlList">
                <md-table-cell>
                  {{url.text}}
                </md-table-cell>
                <md-table-cell>
                  <a v-on:click="openConfigUrl(url.path)" style="cursor: pointer">{{url.href}}</a>
                </md-table-cell>
              </md-table-row>
            </md-table-body>
          </md-table>
        </div>
      </md-table-card>
    </md-layout>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      categoryItem: Object
    },
    data() {
      return {
        commonToggled: false,
        configToggled: false,
        vcapCode: '',
        commonCode: '',
        configCode: '',
        menu: 'vcap',
        commonChanged: false,
        codeChanged: false,
        configUrlList: [],
        previewConfig: ''
      }
    },
    mounted() {
      var me = this;
      $(this.$el).find('.CodeMirror').height(600);
      this.getCodes();
      this.getConfigUrlList();
    },
    watch: {
      stage: function (val) {
        this.commonChanged = false;
        this.codeChanged = false;
        this.getCodes();
        this.getConfigUrlList();
      },
      menu: function (val) {
        var me = this;
        if (val == 'config') {
          this.configToggled = true;
        }
        if (val == 'common') {
          this.commonToggled = true;
        }
        this.$nextTick(function () {
          $(me.$el).find('.CodeMirror').height(600);
        })
      }
    },
    methods: {
      openConfigUrl: function (path) {
        var me = this;
        this.$root.backend('config/' + path).get()
          .then(function (response) {
            if (path.indexOf('.yml') == -1) {
              me.previewConfig = JSON.stringify(response.data, null, 2);
            } else {
              me.previewConfig = response.data;
            }
            console.log('me.previewConfig', me.previewConfig);
            me.open();
          }, function (response) {
            me.$root.$children[0].error('클라우드 콘피그 주소에 접속할 수 없습니다.');
          })
      },
      getConfigUrlList: function () {
        var me = this;
        var configServerUrl = 'http://' + window.config.vcap.services['uengine-cloud-config'].external;
        this.configUrlList = [
          {
            text: 'springboot 요청시',
            href: configServerUrl + '/' + me.appName + '/' + me.stage,
            path: '/' + me.appName + '/' + me.stage
          },
          {
            text: 'json 요청시',
            href: configServerUrl + '/' + me.appName + '-' + me.stage + '.json',
            path: '/' + me.appName + '-' + me.stage + '.json'
          },
          {
            text: 'yml 요청시',
            href: configServerUrl + '/' + me.appName + '-' + me.stage + '.yml',
            path: '/' + me.appName + '-' + me.stage + '.yml'
          }
        ]
      },
      getCodes: function () {
        var me = this;
        me.getDevAppVcapYml(me.appName, function (response) {
          me.vcapCode = response.data;
        });
        me.getDevAppConfigYml(me.appName, '', function (response) {
          me.commonCode = response.data;
        });
        me.getDevAppConfigYml(me.appName, me.stage, function (response) {
          me.configCode = response.data;
        });
      },
      onCodeChange: function (val) {
        this.codeChanged = true;
        this.configCode = val;
      },
      onCommonChange: function (val) {
        this.commonChanged = true;
        this.commonCode = val;
      },
      saveCommon: function () {
        var me = this;
        //커먼 컨피그 저장
        me.updateDevAppConfigYml(me.appName, null, me.commonCode, function (response) {
          me.commonChanged = false;
          me.getCodes();
        })
      },
      saveConfig: function () {
        var me = this;
        //스테이지 컨피그 저장
        me.updateDevAppConfigYml(me.appName, me.stage, me.configCode, function (response) {
          me.codeChanged = false;
          me.getCodes();
        })
      },
      open() {
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
