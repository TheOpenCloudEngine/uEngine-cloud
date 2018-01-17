<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <div>
      <h1>Organization</h1>
    </div>
    <md-layout>
      <md-layout md-align="end">
        <md-input-container style="width: 30%;">
          <md-icon>search</md-icon>
          <md-input placeholder="사용자명검색" v-on:input="searchUser" v-model="searchKeyword"></md-input>
        </md-input-container>
        <md-button class="md-raised md-primary" style="height: 40px;"
                   v-on:click="$refs['user-create'].open()">
          <!--v-on:click="move(null,'create')">-->
          사용자 추가
          <md-icon>control_point</md-icon>
        </md-button>
        <user-create v-if="user" :_user="user" ref="user-create"></user-create>
      </md-layout>
    </md-layout>
    <div class="md-title">Gitlab Groups</div>
    <div class="header-top-line" style="margin-top: 10px;"></div>
    <md-list>
      <md-list-item v-if="groups" v-for="(group,groupIndex) in groups">
        <md-avatar>
          <img v-if="group['avatar_url']" :src="group['avatar_url']">
        </md-avatar>
        <div>
          <md-layout>
            <md-layout>
              <span>{{group.name}}</span>
            </md-layout>
            <md-layout md-align="end">
              <div v-if="group.members">
                <md-icon>people</md-icon>
                <span>{{group.members.length}}</span>
                <md-icon>flag</md-icon>
                <span v-if="group.projects">{{group.projects.length}}</span>
                <md-button class="md-icon-button" v-on:click="move(group.id,'group')"
                           style="background-color:#D2D2D2; ">
                  <md-icon md-iconset="fa fa-cogs" style="margin-top:-5px;color:#757575"></md-icon>
                </md-button>
              </div>
            </md-layout>
          </md-layout>

        </div>
        <md-list-expand>
          <md-list v-if="group.members" v-for="(member,memberIndex) in group.members">
            <md-list-item class="md-inset">
              <md-avatar>
                <img :src="member.avatar_url">
              </md-avatar>
              <!--<a v-on:click="move(member.iam,'user')">{{member.name}}</a>-->
              <a
                v-on:click="getIamInfomation(groupIndex,memberIndex,member.id)">
                <!--v-on:click="move(member.iam,'user')">-->
                {{member.name}}
              </a>
            </md-list-item>
          </md-list>
        </md-list-expand>
      </md-list-item>

      <md-list-item>
        <md-layout>
          <md-layout>Ungroup</md-layout>
          <md-layout md-align="end">
            <div>
              <md-icon>people</md-icon>
              <span>{{users.length}}</span>
            </div>
          </md-layout>
        </md-layout>
        <md-list-expand>
          <md-list v-for="user in users">
            <md-list-item class="md-inset">
              <a v-on:click="move(user.userName,'user')">{{user.userName}}</a>
            </md-list-item>
          </md-list>
        </md-list-expand>
      </md-list-item>
    </md-list>
    <user-detail v-model="userName" :userName="userName" ref="user-detail"></user-detail>
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
        groups: [],
        users: [],
        user: {},
        userName: "",
        total: 0,
        page: 1,
        size: 10,
        searchKeyword: "",
      }
    },
    mounted() {
      var me = this;
      if (localStorage['acl'] != 'admin') {
        var myGroups = [];
        var isDuplicated = function (groupId) {
          var flag = false;
          $.each(myGroups, function (g, group) {
            if (group.id == groupId) {
              flag = true;
            }
          });
          return flag;
        };

        this.$root.gitlab('api/v4/groups').get()
          .then(function (response) {
            var groups = response.data;
            var promises = [];
            if (groups.length) {
              $.each(groups, function (i, group) {
                promises.push(
                  me.$root.gitlab('api/v4/groups/' + group.id + '/members').get()
                )
              });
              Promise.all(promises)
                .then(function (results) {
                  $.each(results, function (r, members) {
                    $.each(members.data, function (m, member) {
                      if (member.id == localStorage['gitlab-id']) {
                        if (!isDuplicated(groups[r].id)) {
                          var group = groups[r];
                          me.$set(group, 'members', members.data);
                          myGroups.push(group);
                        }
                      }
                    })
                  });
                })
              me.groups = myGroups;
            }
          })
      } else {
        this.getGroups();
      }
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
            var copy = []
            for (var i in response.data) {
              if (!response.data[i].metaData['gitlab-id']) {
                copy.push(response.data[i]);
              }
            }
            me.users = copy;
            me.total = response.total;
          });
      },
      move: function (param, routerName) {
        if (routerName == 'user') {
          this.$router.push({
            name: "userDetail",
            params: {
              userName: param,
            },
          });
        } else if (routerName == 'group') {
          this.$router.push({
            name: "groupDetail",
            params: {
              groupId: param + "",
            }
          });
        } else {
          this.$router.push({
            name: "userCreate",
          });
        }
      },
      getGroups: function () {
        var me = this;
        me.$root.gitlab('api/v4/groups').get()
          .then(function (response) {
            me.groups = response.data;
            for (var i in response.data) {
              me.getGroupMembers(i, response.data[i].id);
              me.getGroupProject(i, response.data[i].id);
            }
          })


      },
      getGroupMembers: function (index, id) {
        var me = this;
        me.$root.gitlab('api/v4/groups/' + id + '/members').get()
          .then(function (response) {
            me.$set(me.groups[index], 'members', response.data);
//            for (var i in response.data) {
//              me.getIamInfomation(index, i, response.data[i].id);
//            }

          })
      },
      getGroupProject: function (index, groupId) {
        var me = this;
        me.$root.gitlab('api/v4/groups/' + groupId).get()
          .then(function (response) {
            me.groups[index].projects = response.data.projects;
            return response.data.projects;
          })
      },
      getIamInfomation: function (groupIndex, memberIndex, gitlabUserId) {
        var me = this;
        me.userName = "";
        me.$root.gitlab('api/v4/users/' + gitlabUserId + '/custom_attributes').get()
          .then(function (response) {
            for (var i in response.data) {
              if (response.data[i].key == 'iam') {
                me.userName = response.data[i].value;
              }
            }
          })
          .then(function (response) {
            me.$refs['user-detail'].loadUserInfo(me.userName);
            if (me.userName) {
              me.$refs['user-detail'].open();
            } else {

              me.user = me.groups[groupIndex].members[memberIndex];
              me.$refs['user-create'].loadUserInfo(me.user);
              me.$refs['user-create'].open();
            }
          })
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
