<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-layout md-align="center">
        <div>
          <md-avatar class="md-large">
            <img v-if="group['avatar_url']" :src="group['avatar_url']">
          </md-avatar>
        </div>
      </md-layout>
    </md-layout>
    <md-layout>
      <md-layout md-align="center">
        <h1>{{group.name}}</h1>
      </md-layout>
    </md-layout>

    <md-layout :md-gutter="40">
      <md-layout md-flex="50">
        <div style="width:100%">
          <md-table-card style="width: 100%;margin-top: 20px;">
            <div class="header-top-line" style="width: 100%;"></div>
            <md-table class="info-table">
              <md-table-header>
                <md-table-head>Group Info</md-table-head>
              </md-table-header>
              <md-table-body>
                <md-table-row>
                  <md-table-cell><span>Group ID :</span></md-table-cell>
                  <md-table-cell>
                    <span>{{group.id}}</span>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span>Group Name :</span></md-table-cell>
                  <md-table-cell>
                    {{group.name}}
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span>Path :</span></md-table-cell>
                  <md-table-cell>
                    <span>{{group.path}}</span>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span>Description :</span></md-table-cell>
                  <md-table-cell>
                    <span>{{group.description}}</span>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span>Visibility level:</span></md-table-cell>
                  <md-table-cell>
                    <span>{{group.visibility}}</span>
                  </md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span>Web URL :</span></md-table-cell>
                  <md-table-cell>
                    <span>{{group['web_url']}}</span>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </md-table-card>
          <div class="header-top-line" style="margin-top: 30px;"></div>
          <md-table-card>
            <md-table>
              <md-table-header>
                <md-table-head>Projects</md-table-head>
              </md-table-header>
              <md-table-body>
                <md-table-row v-for="project in group.projects">
                  <md-table-cell>
                    <md-layout>
                      <md-layout>
                        <span class="md-subheading">{{project.name}}</span>
                      </md-layout>
                      <md-layout md-flex="10" md-align="end">
                        <md-icon v-if="project.visibility=='private'" md-iconset="fa fa-lock"
                                 style="color:#757575"></md-icon>
                        <md-icon v-if="project.visibility=='public'" md-iconset="fa fa-globe"
                                 style="color:#757575"></md-icon>
                        <!--<md-icon md-iconset="fa fa-globe" style="color:#757575"></md-icon>-->
                      </md-layout>
                    </md-layout>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </md-table-card>
        </div>
      </md-layout>

      <md-layout md-column md-gutter="16" md-flex="50">
        <md-layout md-flex="30" style="min-height:320px;">
          <md-table-card style="width: 100%;margin-top: 20px;">
            <div class="header-top-line" style="width: 100%;"></div>
            <md-table>
              <md-table-header>
                <md-table-head>Add user(s) to the group</md-table-head>
              </md-table-header>
              <md-table-body>
                <md-table-row>
                  <md-table-cell>
                    <div style="width: 100%;">
                      <md-layout style="height: 60px;">
                        <md-layout md-flex="90">
                          <md-input-container v-if="items">
                            <md-input v-model="searchKeyword" placeholder="Search for User" v-on:keyup.enter="getUserSearch"></md-input>
                          </md-input-container>
                        </md-layout>
                        <md-layout md-flex="10">
                          <md-button class="md-icon-button" style="width: 50px;height:50px;" v-on:click="getUserSearch">
                            <md-icon style="font-size: 25px;">search</md-icon>
                          </md-button>
                        </md-layout>
                      </md-layout>
                      <md-input-container>
                        <md-select ref="searched-user" v-on:change="changeValue" v-model="selectedIndex">
                          <md-option value="" class="md-body-1">Select User</md-option>
                          <md-option v-for="(item,index) in items" :value="index">
                            <md-avatar>
                              <img v-if="item['avatar_url']" :src="item['avatar_url']">
                            </md-avatar>
                            {{item.name}} | {{item.email}}
                          </md-option>
                        </md-select>
                      </md-input-container>
                      <md-button class="md-raised md-primary" style="width: 200px;" v-on:click="addUsersToGroup">
                        Add users to group
                      </md-button>
                    </div>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </md-table-card>
        </md-layout>
        <md-layout>
          <md-table-card style="width: 100%;">
            <div class="header-top-line"></div>
            <md-table>
              <md-table-header>
                <md-table-head>{{group.name}} group members</md-table-head>
              </md-table-header>
              <md-table-body>
                <md-table-row v-for="member in members">
                  <md-table-cell>
                    <md-layout>
                      <md-layout md-flex="15">
                        <md-avatar>
                          <img v-if="member.avatar_url" :src="member.avatar_url">
                        </md-avatar>
                      </md-layout>
                      <md-layout md-flex="60">
                        <span class="md-subheading">{{member.name}}</span>
                      </md-layout>
                      <md-layout md-align="end">
                        <span v-if="member['access_level']==50">Owner</span>
                        <span v-else-if="member['access_level']==40">Master</span>
                        <span v-else-if="member['access_level']==30">Developer</span>
                        <span v-else-if="member['access_level']==20">Reporter</span>
                        <span v-else-if="member['access_level']==10">Guest</span>

                        <md-button class="md-icon-button" v-on:click="deleteUsersToGroup(member.id)">
                          <md-tooltip md-direction="left">Remove this member from group</md-tooltip>
                          <md-icon md-iconset="fa fa-trash"></md-icon>
                        </md-button>
                      </md-layout>
                    </md-layout>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </md-table-card>
        </md-layout>
      </md-layout>
    </md-layout>
  </div>
</template>
<script>
  export default {
    props: {
      groupId: String,
    },
    data() {
      return {
        group: {},
        members: [],
        selectedUser: {},
        items: [{}],
        searchKeyword: "",
        selectedIndex: ""
      }
    },
    mounted() {
      var me = this;
      me.$root.gitlab('api/v4/groups/' + me.groupId).get()
        .then(function (response) {
          me.group = response.data;
        })
      me.$root.gitlab('api/v4/users').get()
        .then(function (response) {
          me.items = response.data;
        })
      me.getGroupProject(me.groupId);
      me.getGroupMembers();
    }
    ,
    watch: {},
    methods: {
      getGroupProject: function (groupId) {
        var me = this;
        me.$root.gitlab('api/v4/groups/' + groupId).get()
          .then(function (response) {
            me.group.projects = response.data.projects;
          })
      },
      getGroupMembers: function () {
        var me = this;
        me.$root.gitlab('api/v4/groups/' + me.groupId + '/members').get()
          .then(function (response) {
            me.members = response.data;
          })
      },
      getUserSearch: function () {
        var me = this;
        var url = 'api/v4/users';
        if (me.searchKeyword) {
          url = url + '?search=' + me.searchKeyword;
        }
        var searchUsers = [];
        me.$root.gitlab(url).get()
          .then(function (response) {
            searchUsers = response.data;
            me.callbackUsers(response.data);
          })
        me.callbackUsers(me.items);

      },
      callbackUsers: function (users) {
        var me = this;
        me.items = users;
      },
      selectCallback: function (item) {
        var me = this;
        me.searchKeyword = item.name;
        me.selectedUser = item;
      },
      addUsersToGroup: function () {
        var me = this;
        var addUser = {user_id: me.selectedUser.id, access_level: 30};
        me.$root.gitlab('api/v4/groups/' + me.groupId + '/members').save(null, addUser)
          .then(
            function (response) {
              me.getGroupMembers();
              me.$root.$children[0].success("회원을 " + me.group.name + "그룹에 추가하였습니다.");
            },
            function (response) {
              if (response.status == 409){
                me.$root.$children[0].error("이미 그룹에 등록된 회원입니다.");
              } else {
                me.$root.$children[0].error("회원을 " + me.group.name + "그룹에 추가할 수 없습니다.");
              }
            })
      },
      deleteUsersToGroup: function (id) {
        var me = this;
        me.$root.gitlab('api/v4/groups/' + me.groupId + '/members/' + id).delete()
          .then(function (response) {
            me.$root.$children[0].success("회원을 그룹에서 제거하였습니다.");
            me.getGroupMembers();
          })
      },
      changeValue: function () {
        this.searchKeyword = this.items[this.selectedIndex].email;
        this.selectedUser = this.items[this.selectedIndex];
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .info-table {
    .md-table-row {
      border-top: none;
    }

    .md-table-cell {
      height: 28px;
      font-size: 11px;
    }
  }
</style>
