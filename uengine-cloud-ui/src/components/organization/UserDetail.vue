<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <h2>사용자 정보</h2>
    <div style="text-align: right;">
      <md-button class="md-raised md-primary" v-on:click="deleteUser">
        삭제
      </md-button>
      <md-button class="md-raised md-primary" v-if="!editable" v-on:click="updateUser">
        수정
      </md-button>
    </div>
    <md-table-card>
      <md-table>
        <md-table-header>
          <md-table-head></md-table-head>
          <md-table-head style="float:right;">
            <md-button class="md-icon-button" v-on:click="chageMode">
              <md-icon>edit</md-icon>
            </md-button>
          </md-table-head>
        </md-table-header>
        <md-table-body>
          <md-table-row>
            <md-table-cell><span class="md-subheader">ID</span></md-table-cell>
            <md-table-cell>
              <span>{{user._id}}</span>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">이름</span></md-table-cell>
            <md-table-cell>
              <span v-if="editable">{{user.userName}}</span>
              <md-input-container v-else>
                <md-input v-model="user.userName"></md-input>
              </md-input-container>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">E-mail</span></md-table-cell>
            <md-table-cell>
              <span v-if="editable">{{user.email}}</span>
              <md-input-container v-else>
                <md-input v-model="user.email"></md-input>
              </md-input-container>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">가입일</span></md-table-cell>
            <md-table-cell>
              <span>{{user.regDate}}</span>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">수정일</span></md-table-cell>
            <md-table-cell>
              <span>{{user.updDate}}</span>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">관리자 여부</span></md-table-cell>
            <md-table-cell>
              <div v-if="editable">
                <span v-if="user.acl=='admin'">관리자</span>
                <span v-else>사용자</span>
              </div>
              <md-input-container v-else>
                <md-select v-model="user.acl">
                  <md-option value="">선택..</md-option>
                  <md-option value="admin">관리자</md-option>
                  <md-option value="user">사용자</md-option>
                </md-select>
              </md-input-container>
            </md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>
    </md-table-card>
    <md-layout md-gutter="8">
      <md-layout md-flex="20"><h3 style="text-align: right; width: 100%;margin-top: 20%">Body : </h3></md-layout>
      <md-layout md-flex="80">
        <div style="margin-top: 5%;width: 50%">
          <codemirror
            :options="{
                        theme: 'default',
                        mode: 'javascript',
                        extraKeys: {'Ctrl-Space': 'autocomplete'},
                        lineNumbers: false,
                        lineWrapping: true
                      }"
            :value="userString"
            v-on:change="editorToModel">
          </codemirror>
        </div>
      </md-layout>
    </md-layout>
  </div>
</template>
<script>
  export default {
    props: {
      id: String,
    },
    data() {
      return {
        user: {},
        userString: "",
        editable: false,
      }
    },
    mounted() {
      var me = this;
      me.$parent.iam.getUser(me.id).then(function (response) {
        me.user = response;
        delete me.user.userPassword;
      });
    }
    ,
    watch: {
      user: {
        handler: function (newVal, oldVal) {
          this.userString = JSON.stringify(newVal, null, 2);
        },
        deep: true
      },
    },
    methods: {
      chageMode: function () {
        this.editable = !this.editable;
      },
      updateUser: function () {
        var me = this;
        console.log("update User", this.id);
        me.$parent.iam.updateUser(me.id, me.user).then(function (response) {
          me.$root.$children[0].success("수정하였습니다.");
        })
      },
      deleteUser: function () {
        var me = this;
        console.log("delete User", this.id);
        me.$parent.iam.deleteUser(me.id).then(function (response) {
          me.$root.$children[0].success("삭제하였습니다.");
          me.$router.push({name:"organization"});
        })
      },
      editorToModel: function (text) {
        var me = this;
        me.user = JSON.parse(text);
        console.log(me.user);
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
