<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <div>
          <md-layout md-align="center">
            <md-button v-if="codeChanged" class="md-primary md-raised"
                       v-on:click="saveConfig">저장
            </md-button>
            <md-radio v-model="menu" :mdValue="'vcap'">
              <span class="md-caption">VCAP_SERVICES</span>
            </md-radio>
            <md-radio v-model="menu" :mdValue="'config'">
              <span class="md-caption">클라우드 콘피그</span>
            </md-radio>
            <md-radio v-model="menu" :mdValue="'url'">
              <span class="md-caption">클라우드 콘피그 접속 주소</span>
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
        <div v-show="menu == 'config'">
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
                  <a target="_blank" :href="url.href" style="cursor: pointer">{{url.href}}</a>
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
      catalogItem: Object
    },
    data() {
      return {
        configToggled: false,
        vcapCode: '',
        configCode: '',
        menu: 'vcap',
        codeChanged: false,
        configUrlList: []
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
        this.getCodes();
        this.getConfigUrlList();
      },
      menu: function (val) {
        var me = this;
        if (val == 'config') {
          this.configToggled = true;
        }
        this.$nextTick(function () {
          $(me.$el).find('.CodeMirror').height(600);
        })
      }
    },
    methods: {
      getConfigUrlList: function () {
        var me = this;
        this.configUrlList = [
          {
            text: 'springboot',
            href: configServerUrl + '/' + me.appName + '/' + me.stage
          },
          {
            text: 'json',
            href: configServerUrl + '/' + me.appName + '-' + me.stage + '.json'
          },
          {
            text: 'yml',
            href: configServerUrl + '/' + me.appName + '-' + me.stage + '.yml'
          }
        ]
      },
      getCodes: function () {
        var me = this;
        me.codeChanged = false;
        me.getDevAppVcapYml(me.appName, function (response) {
          me.vcapCode = response.data;
        });
        me.getDevAppConfigYml(me.appName, me.stage, function (response) {
          me.configCode = response.data;
        });
      },
      onCodeChange: function (val) {
        this.codeChanged = true;
        this.configCode = val;
      },
      saveConfig: function () {
        var me = this;
        me.updateDevAppConfigYml(me.appName, me.stage, me.configCode, function (response) {
          me.codeChanged = false;
          me.getCodes();
        })
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
