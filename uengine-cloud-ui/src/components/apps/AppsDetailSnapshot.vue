<template>
  <div v-if="devApp">
    <span class="md-subheading">스냅샷</span>

    <md-table-card style="width: 100%">
      <div class="header-top-line"></div>
      <md-table md-sort="regDate">
        <md-table-header>
          <md-table-row>
            <md-table-head md-sort-by="id">번호</md-table-head>
            <md-table-head md-sort-by="name">이름</md-table-head>
            <md-table-head md-sort-by="timecompare">시간</md-table-head>
            <md-table-head md-sort-by="regDate">생성일</md-table-head>
            <md-table-head md-sort-by="action">조치</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="snapshot in list">
            <md-table-cell>
              {{(snapshot._links.self.href).substring(snapshot._links.self.href.lastIndexOf('/') + 1)}}
            </md-table-cell>
            <md-table-cell>{{snapshot.name}}</md-table-cell>
            <md-table-cell>{{snapshot.timecompare}}</md-table-cell>
            <md-table-cell>{{new Date(snapshot.regDate).toString()}}</md-table-cell>
            <md-table-cell>조치</md-table-cell>
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
      categoryItem: Object,
      marathonApps: Object,
      deployJson: Object
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
      window.busVue.$on('snapshotCreated', function (val) {
        me.makeList();
      });
    },
    watch: {},
    methods: {
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      makeList: function () {
        var me = this;
        me.$root.backend('snapshots/search/findByAppName?appName=' + me.appName +
          '&page=' + (me.page - 1) + '&size=' + me.size + '&sort=regDate,desc')
          .get()
          .then(function (response) {
            me.list = response.data['_embedded'].snapshots;
            for (var i in me.list) {
              var diffTime = (new Date().getTime() - me.list[i].regDate) / 1000;
              if (diffTime < 60) {
                me.list[i].timecompare = "방금 전";
              } else if ((diffTime /= 60) < 60) {
                me.list[i].timecompare = Math.floor(diffTime) + "분 전";
              } else if ((diffTime /= 60) < 24) {
                me.list[i].timecompare = Math.floor(diffTime) + "시간 전";
              } else if ((diffTime /= 24) < 30) {
                me.list[i].timecompare = Math.floor(diffTime) + "일 전";
              } else if ((diffTime /= 30) < 12) {
                me.list[i].timecompare = Math.floor(diffTime) + "달 전";
              } else {
                me.list[i].timecompare = Math.floor(diffTime) + "년 전";
              }
            }
            me.total = me.list.length;
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
</style>
