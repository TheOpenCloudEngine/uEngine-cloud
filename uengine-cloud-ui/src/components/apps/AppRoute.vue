<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog v-if="app" md-open-from="#open" md-close-to="#open" ref="open">

    <md-dialog-title>
      <div>
        <md-layout>
          <md-layout>
            라우트 목록
          </md-layout>
          <md-layout md-align="end">
            <md-button v-on:click="hostEdit" class="md-raised md-primary">
              <md-tooltip md-direction="bottom">앱 호스트를 변경합니다.</md-tooltip>
              <md-icon>mode_edit</md-icon>
            </md-button>
          </md-layout>
        </md-layout>
      </div>
    </md-dialog-title>
    <md-dialog-content>

      <md-layout v-if="editMode">
        <md-layout md-flex="100" :md-gutter="16">
          <md-layout>
            <div class="bold">외부 접속 주소:</div>
            <md-input-container>
              <label>외부 프로덕션 도메인 주소</label>
              <md-input v-model="externalProdDomain"></md-input>
            </md-input-container>
            <md-input-container>
              <label>외부 스테이징 도메인 주소</label>
              <md-input v-model="externalStgDomain"></md-input>
            </md-input-container>
            <md-input-container>
              <label>외부 개발 도메인 주소</label>
              <md-input v-model="externalDevDomain"></md-input>
            </md-input-container>
          </md-layout>
        </md-layout>
        <md-button class="md-primary md-raised" @click="save">저장하기</md-button>
      </md-layout>

      <md-table v-else>
        <md-table-header>
          <md-table-row>
            <md-table-head>역할</md-table-head>
            <md-table-head>라우트</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="route in appRoutes">
            <md-table-cell>
              {{route.text}}
            </md-table-cell>
            <md-table-cell>
              <a target="_blank" :href="route.href" style="cursor: pointer">{{route.href}}</a>
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
    props: {
      app: Object
    },
    data() {
      return {
        editMode: false,
        externalProdDomain: '',
        externalStgDomain: '',
        externalDevDomain: '',
        appRoutes: []
      }
    },
    mounted(){
      this.makeRoutes();
    },
    watch: {
      app: {
        handler: function (newVal, oldVal) {
          this.makeRoutes();
        },
        deep: true
      }
    }
    ,
    methods: {
      save: function () {
        var me = this;
        var copy = JSON.parse(JSON.stringify(me.app));
        copy.prod.external = this.externalProdDomain;
        copy.stg.external = this.externalStgDomain;
        copy.dev.external = this.externalDevDomain;
        me.updateAppExcludeDeployJson(copy.name, copy, function (response) {
          if (response) {
            me.runDeployedAppByExistStages(copy.name, null);
          }
        });
      },
      hostEdit: function () {
        this.externalProdDomain = this.app.prod.external;
        this.externalStgDomain = this.app.stg.external;
        this.externalDevDomain = this.app.dev.external;
        this.editMode = true;
      },
      makeRoutes: function () {
        this.appRoutes = [
          {
            'text': '개발 외부 주소',
            'href': 'http://' + this.app.dev.external
          },
          {
            'text': '개발 내부 주소',
            'href': 'http://' + this.app.dev.internal
          },
          {
            'text': '스테이징 외부 주소',
            'href': 'http://' + this.app.stg.external
          },
          {
            'text': '스테이징 내부 주소',
            'href': 'http://' + this.app.stg.internal
          },
          {
            'text': '프로덕션 외부 주소',
            'href': 'http://' + this.app.prod.external
          },
          {
            'text': '프로덕션 내부 주소',
            'href': 'http://' + this.app.prod.internal
          },
          {
            'text': '신규 프로덕션 외부 테스트 주소',
            'href': 'http://' + this.app.prod.external + ':' + (this.app.prod.servicePort + 10000)
          },
          {
            'text': '신규 프로덕션 내부 테스트 주소',
            'href': 'http://marathon-lb-internal.marathon.mesos:' + (this.app.prod.servicePort + 10000)
          }
        ];
      },
      open() {
        this.editMode = false;
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
