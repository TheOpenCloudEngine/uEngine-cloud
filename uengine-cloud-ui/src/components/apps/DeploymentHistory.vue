<template>
  <div v-if="devApp">
    <md-table-card style="width: 100%">
      <div class="header-top-line"></div>
      <md-table md-sort="regDate">
        <md-table-header>
          <md-table-row>
            <md-table-head>이름</md-table-head>
            <md-table-head>초기 버전</md-table-head>
            <md-table-head>새 버전</md-table-head>
            <md-table-head>완료됨</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="history in list">
            <md-table-cell>
              <a
                v-on:click="$emit('showDetail', (history._links.self.href).substring(history._links.self.href.lastIndexOf('/') + 1))">
                {{history.name}}
              </a>
            </md-table-cell>
            <md-table-cell>
              <a v-on:click="openGitlab(devApp.projectId, 'commit', history.appStage.tempDeployment.commitOld)">
                {{history.oldVersionText}}
              </a>
            </md-table-cell>
            <md-table-cell>
              <a v-on:click="openGitlab(devApp.projectId, 'commit', history.appStage.tempDeployment.commit)">
                {{history.newVersionText}}
              </a>
            </md-table-cell>
            <md-table-cell>
              <span v-if="history.status == 'SUCCEED'" class="success">
                완료됨
              </span>
              <span v-if="history.status == 'ROLLBACK_SUCCEED'" class="rollback-success">
                롤백됨
              </span>
              <span v-if="history.status == 'FAILED' || history.status == 'ROLLBACK_FAILED'" class="failed">
                실패됨
              </span>
              <span v-if="history.status == 'CANCELED'" class="canceled">
                취소됨
              </span>
              {{new Date(history.endTime)}}
            </md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>
      <md-table-pagination
        :md-size="size"
        :md-total="total"
        :md-page="page"
        md-label="페이지당 항목"
        md-separator="총"
        :md-page-options="[5, 10, 25, 50]"
        @pagination="onPagination">
      </md-table-pagination>
    </md-table-card>
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
        list: [],
        total: 100,
        size: 10,
        page: 1
      }
    },
    mounted() {
      var me = this;
      me.makeList();
    },
    watch: {
      stage: function () {
        this.makeList();
      }
    },
    methods: {
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      makeList: function () {
        var me = this;
        me.$root.backend('deploymentHistory/search/findByAppNameAndStage?appName=' + me.appName +
          '&stage=' + me.stage +
          '&page=' + (me.page - 1) + '&size=' + me.size + '&sort=endTime,desc')
          .get()
          .then(function (response) {
            me.list = response.data['_embedded'].deploymentHistory;
            for (var i in me.list) {
              var history = me.list[i];
              history.oldVersionText =
                history.appStage.tempDeployment.commitOld ? history.appStage.tempDeployment.commitOld.substring(0, 5) + '...' : null;
              history.newVersionText =
                history.appStage.tempDeployment.commit ? history.appStage.tempDeployment.commit.substring(0, 5) + '...' : null;
            }
            me.total = response.data.page.totalElements;
          }, function (response) {
            me.list = [];
          })
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .bar-wrapper {
    .md-button {
      width: 100%;
      margin: 0px;
    }
  }

  .md-theme-default.md-chip {
    margin-top: 8px;
  }

  .success {
    color: #006d18;
  }

  .rollback-success {
    color: #9b9e00;
  }

  .failed {
    color: #ac0000;
  }

  .canceled {
    color: #ac0000;
  }
</style>
