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
        <md-button class="md-raised md-primary" style="height: 40px;">사용자 추가
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>
    <md-table-card style="width: 100%">
      <md-table md-sort="email" md-sort-type="desc">
        <md-table-header>
          <md-table-row>
            <md-table-head md-sort-by="email" v-for="value in tablehead">{{value}}</md-table-head>
          </md-table-row>
        </md-table-header>
        <md-table-body>
          <md-table-row v-for="user in users">
            <md-table-cell>
              <a v-on:click="moveUser(user._id,user.email)">
                {{user.email}}
              </a>
            </md-table-cell>
            <md-table-cell>{{user.userName}}</md-table-cell>
            <md-table-cell v-if="user.acl=='admin'">관리자</md-table-cell>
            <md-table-cell v-else>사용자</md-table-cell>
            <md-table-cell>{{user.regDate}}</md-table-cell>
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
    props: {
      iam: Object
    },
    data() {
      return {
        users: [],
        tablehead: ["ID", "Name", "Manager", "RegDate"],
        total: 0,
        page: 1,
        size: 10,
        searchKeyword: "",
      }
    },
    mounted() {
      var me = this;
      me.iam.getUserSearch("", 0, 10)
        .then(function (response) {
          me.users = response.data;
          me.total = response.total;
          console.log("response", response);
        });
    }
    ,
    methods: {
      onPagination: function (value) {
        console.log("value", value);
        var me = this;
//        console.log(value.page * value.size - value.size);
        me.iam.getUserSearch("", value.page * value.size - value.size, value.size)
          .then(function (response) {
            console.log(response);
            me.users = response.data;
            me.total = response.total;
            me.size = response.limit;
            me.page = value.page;
          });
      },
      searchUser: function () {
        var me = this;
        me.iam.getUserSearch(me.searchKeyword, me.page, me.size)
          .then(function (response) {
            me.users = response.data;
            console.log("response.data", response.data);
            me.total = response.total;
            me.size = response.limit;
            me.page = response.offset;
          });
      },
      moveUser: function (id, email) {
        this.$router.push({
          name:"userDetail",
          params: {
            id: id,
            email: email,
          },
        });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
