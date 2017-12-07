<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog v-if="app" md-open-from="#open" md-close-to="#open" ref="open">

    <md-dialog-title>라우트 목록</md-dialog-title>
    <md-dialog-content>
      <md-table>
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
          }
        ];
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
