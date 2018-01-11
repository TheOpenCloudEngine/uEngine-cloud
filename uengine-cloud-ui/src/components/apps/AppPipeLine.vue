<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table md-sort="name">
          <md-table-header>
            <md-table-row>
              <md-table-head md-sort-by="status">상태</md-table-head>
              <md-table-head md-sort-by="id">파이프라인</md-table-head>
              <md-table-head md-sort-by="ref">레퍼런스</md-table-head>
              <md-table-head md-sort-by="sha">커밋</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="pipeline in list">
              <md-table-cell>
                <md-button v-if="pipeline.status == 'running'" class="md-warn" v-on:click="movePipeLine(pipeline.id)">
                  <md-spinner :md-size="20" md-indeterminate class="md-accent"></md-spinner>
                  {{pipeline.status}}
                </md-button>
                <md-button v-else-if="pipeline.status == 'success'" class="md-primary"
                           v-on:click="movePipeLine(pipeline.id)">
                  <md-icon>check_circle</md-icon>
                  {{pipeline.status}}
                </md-button>
                <md-button v-else-if="pipeline.status == 'pending' || pipeline.status == 'skipped'" class="md-warn"
                           v-on:click="movePipeLine(pipeline.id)">
                  ! {{pipeline.status}}
                </md-button>
                <md-button v-else-if="pipeline.status == 'failed'" class="md-accent"
                           v-on:click="movePipeLine(pipeline.id)">
                  <md-icon>cancel</md-icon>
                  {{pipeline.status}}
                </md-button>
                <md-button v-else v-on:click="movePipeLine(pipeline.id)">
                  ! {{pipeline.status}}
                </md-button>
              </md-table-cell>
              <md-table-cell>{{pipeline.id}}</md-table-cell>
              <md-table-cell>{{pipeline.ref}}</md-table-cell>
              <md-table-cell>{{pipeline.sha}}</md-table-cell>
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
        list: [],
        total: 100,
        size: 25,
        page: 1
      }
    },
    mounted() {
      this.makeList();
    },
    watch: {
      devApp: {
        handler: function (newVal, oldVal) {
          this.makeList();
        },
        deep: true
      }
    },
    methods: {
      movePipeLine: function (pipelineId) {
        this.getProject(this.devApp.gitlab.projectId, function (response, err) {
          var url = response.data.web_url + '/pipelines/' + pipelineId;
          window.open(url);
        });
      },
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      makeList: function () {
        var me = this;
        var projectId = me.devApp.gitlab.projectId;
        me.$root.gitlab('api/v4/projects/' + projectId + '/pipelines?page=' + me.page + '&per_page=' + me.size).get()
          .then(function (response) {
            me.list = [];
            var list = response.data;
            var offset = (me.page - 1) * me.size;
            var limit = (me.page) * me.size - 1;

            var count = 0;
            for (var i = 0; i < list.length; i++) {
              //서브 항목일 경우
              if (list[i].role) {
                me.list.push(list[i]);
              }
              //그 외의 경우
              else {
                if (count >= offset && count <= limit) {
                  me.list.push(list[i]);
                }
                count++;
              }
            }
            me.total = count;
          }, function (response) {
            me.list = [];
          });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-theme-default.md-button {
    width: 120px;
  }
</style>
