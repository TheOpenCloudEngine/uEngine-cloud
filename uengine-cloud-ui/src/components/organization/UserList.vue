<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <div>
      <h1>Organization</h1>
    </div>
    <md-layout>
      <md-layout md-align="end">
        <md-input-container style="width: 30%;float: right;">
          <md-icon>search</md-icon>
          <md-input placeholder="사용자명검색" v-on:input="searchUser" v-model="searchKeyword"></md-input>
        </md-input-container>
        <md-button class="md-raised md-primary" style="height: 40px;" v-on:click="move('create')">사용자 추가
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>
    <md-table-card style="width: 100%">
      <md-table md-sort="email" md-sort-type="desc">
        <md-table-header>
          <md-table-row>
            <md-table-head md-sort-by="userName" v-for="value in tablehead">{{value}}</md-table-head>
          </md-table-row>
        </md-table-header>
        <md-table-body>
          <md-table-row v-for="user in users">
            <md-table-cell>
              <a v-on:click="move(user.userName,'user')">
                {{user.userName}}
              </a>
            </md-table-cell>
            <md-table-cell>{{user.metaData.name}}</md-table-cell>
            <md-table-cell v-if="user.metaData.acl=='admin'">관리자</md-table-cell>
            <md-table-cell v-else>사용자</md-table-cell>
            <md-table-cell>{{new Date(user.regDate).toString()}}</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>
      <md-table-pagination
        :md-size="size"
        :md-total="total"
        :md-page="page"
        md-label="Rows"
        md-separator="of"
        :md-page-options="[5, 10, 25, 50]"
        @pagination="onPagination"></md-table-pagination>
    </md-table-card>
    <router-view></router-view>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'

  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        iam: window.iam,
        users: [],
        tablehead: ["ID", "Name", "Manager", "RegDate"],
        total: 0,
        page: 1,
        size: 10,
        searchKeyword: "",
      }
    },
    mounted() {
      this.searchUser();
    }
    ,
    methods: {
      onPagination: function (value) {
        var me = this;
        me.page = value.page;
        me.size = value.size;
        this.searchUser();
      },
      searchUser: function () {
        var me = this;
        me.iam.getUserSearch(me.searchKeyword, (me.page - 1) * me.size, me.size)
          .then(function (response) {
            me.users = response.data;
            me.total = response.total;
          });
      },
      move: function (userName, routerName) {
        if (routerName == 'user') {
          this.$router.push({
            name: "userDetail",
            params: {
              userName: userName,
            },
          });
        } else {
          this.$router.push({
            name: "userCreate",
          });
        }

      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
