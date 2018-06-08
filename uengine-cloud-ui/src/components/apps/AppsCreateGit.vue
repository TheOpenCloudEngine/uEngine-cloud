<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-table-card style="width: 100%">
    <div class="header-top-line"></div>
    <md-table>
      <md-table-header>
        <md-table-row>
          <md-table-head>From GitHub</md-table-head>
          <md-table-head>Status</md-table-head>
        </md-table-row>
      </md-table-header>

      <md-table-body>
        <md-table-row v-for="repository in repositories">
          <md-table-cell>
            {{repository.full_name}}
          </md-table-cell>
          <md-table-cell>
            <md-button
              v-if="repository.permissions.admin"
              style="min-width: 100px"
              class="md-primary md-raised" @click="select(repository)">
              Import
            </md-button>
            <md-button
              v-else
              style="min-width: 100px"
              class="md-raised">
              <span class="md-caption">Not admin</span>
            </md-button>
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
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      githubRepoId: Number,
      githubRepoName: String
    },
    data() {
      return {
        autoDeploy: null,
        repositories: [],
        total: 100,
        size: 25,
        page: 1
      }
    },
    mounted() {
      this.makeList();
    },
    watch: {}
    ,
    methods: {
      select: function (repository) {
        this.$emit('update:githubRepoId', repository.id);
        this.$emit('update:githubRepoName', repository.full_name);
      },
      makeList: function () {
        var me = this;
        me.$root.$children[0].block();
        me.$root.github('user/repos?page=' + me.page + '&per_page=' + me.size + '').get()
          .then(function (response) {
            me.repositories = response.data;
          }, function (response) {
            me.$root.$children[0].error('Github 레파지토리 목록을 불러올 수 없습니다.');
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
