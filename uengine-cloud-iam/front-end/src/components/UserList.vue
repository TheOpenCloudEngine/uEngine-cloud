<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <user-edit ref="user-edit" v-on:edited="searchUser"></user-edit>
    <md-layout>
      <md-input-container>
        <md-icon>search</md-icon>
        <label>사용자명 검색</label>
        <md-input v-on:input="searchUser" v-model="searchKeyword"></md-input>
        <md-button class="md-raised md-primary" style="height: 40px;" v-on:click="addUser">사용자 추가
          <md-icon>control_point</md-icon>
        </md-button>
      </md-input-container>
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
              <a v-on:click="editUser(user.userName)">{{user.userName}}</a>
            </md-table-cell>
            <md-table-cell>*****</md-table-cell>
            <md-table-cell>{{user.metaData}}</md-table-cell>
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
  export default {
    data() {
      return {
        iam: window.iam,
        users: [],
        tablehead: ["사용자명(userName)", "패스워드(userPassword)", "부가정보(metaData)", "등록일"],
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
        me.iam.getUserSearch(me.searchKeyword, me.page - 1, me.size)
          .then(function (response) {
            me.users = response.data;
            me.total = response.total;
          });
      },
      editUser: function (userName) {
        this.$refs['user-edit'].open(userName);
      },
      addUser: function () {
        this.$refs['user-edit'].open();
      },
      move: function (id, userName, routerName) {
        if (routerName == 'user') {
          this.$router.push({
            name: "userDetail",
            params: {
              id: id,
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
