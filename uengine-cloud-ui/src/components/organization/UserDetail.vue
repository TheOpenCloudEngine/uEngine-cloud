<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog md-open-from="#open" md-close-to="#open" ref="open" class="bigscreen">
    <md-dialog-title>사용자 정보</md-dialog-title>
    <md-dialog-content>
      <div v-if="userName">
        <div style="text-align: right;">
          <md-button class="md-raised md-primary" v-on:click="deleteUser">
            삭제
          </md-button>
          <md-button class="md-raised md-primary" v-if="editable" v-on:click="updateUser">
            저장
          </md-button>
        </div>
        <md-table-card style="margin-top: 10px;">
          <div class="header-top-line" style="width: 100%;"></div>
          <md-table class="detail-table">
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
                <md-table-cell><span>아이디</span></md-table-cell>
                <md-table-cell>
                  <span>{{user.userName}}</span>
                </md-table-cell>
              </md-table-row>
              <md-table-row v-if="editable">
                <md-table-cell><span>패스워드</span></md-table-cell>
                <md-table-cell>
                  <md-input-container>
                    <md-input type="password" v-model="userPassword" placeholder="변경하지 않을 경우 공란으로 남겨두세요."></md-input>
                  </md-input-container>
                </md-table-cell>
              </md-table-row>
              <md-table-row>
                <md-table-cell><span>이름</span></md-table-cell>
                <md-table-cell>
                  <span v-if="!editable">{{user.metaData.name}}</span>
                  <md-input-container v-else>
                    <md-input v-model="user.metaData.name"></md-input>
                  </md-input-container>
                </md-table-cell>
              </md-table-row>
              <md-table-row>
                <md-table-cell><span>가입일</span></md-table-cell>
                <md-table-cell>
                  <span>{{new Date(user.regDate).toString()}}</span>
                </md-table-cell>
              </md-table-row>
              <md-table-row>
                <md-table-cell><span>수정일</span></md-table-cell>
                <md-table-cell>
                  <span>{{new Date(user.updDate).toString()}}</span>
                </md-table-cell>
              </md-table-row>
              <md-table-row>
                <md-table-cell><span>관리자 여부</span></md-table-cell>
                <md-table-cell>
                  <div v-if="!editable">
                    <span v-if="user.metaData.acl=='admin'">관리자</span>
                    <span v-else>사용자</span>
                  </div>
                  <md-input-container v-else>
                    <md-select v-model="user.metaData.acl">
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

        <div style="margin-top: 30px;">
          <md-table-card>
            <div class="header-top-line" style="width: 100%;"></div>
            <md-table>
              <md-table-body>
                <md-table-row>
                  <md-table-cell>
                    Body :
                  </md-table-cell>
                  <md-table-cell style="width: 70%;">
                    <div style="width: 100%">
                      <codemirror
                        :options="{
                        theme: 'dracula',
                        mode: 'javascript',
                        extraKeys: {'Ctrl-Space': 'autocomplete'},
                        lineNumbers: false,
                        lineWrapping: true
                      }"
                        :value="userString"
                        v-on:change="editorToModel">
                      </codemirror>
                    </div>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </md-table-card>
        </div>
      </div>
    </md-dialog-content>
  </md-dialog>
</template>
<script>
  export default {
    props: {
      userName: String,
    },
    data() {
      return {
        user: {
          metaData: {}
        },
        userString: "",
        editable: false,
        userPassword: null
      }
    },
    mounted() {
      var me = this;
      if (me.userName) {
        me.loadUserInfo(me.userName);
      }
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
      loadUserInfo: function (userName) {
        var me = this;
        window.iam.getUser(userName).then(function (response) {
          me.user = response;
          delete me.user.userPassword;
        }, function (response) {
          me.$root.$children[0].error("클라우드 플랫폼에 가입한 사용자가 아닙니다.");
        });

      },
      chageMode: function () {
        this.editable = !this.editable;
      },
      updateUser: function () {
        var me = this;
        console.log("update User", this.userName);
        var data = JSON.parse(JSON.stringify(me.user));
        if (me.userPassword && me.userPassword.length > 0) {
          data.userPassword = me.userPassword;
        }
        window.iam.updateUser(me.userName, data).then(function (response) {
          me.$root.$children[0].success("수정하였습니다.");
        })
      },
      deleteUser: function () {
        var me = this;
        console.log("delete User", this.userName);
        window.iam.deleteUser(me.userName).then(function (response) {
          me.$root.$children[0].success("삭제하였습니다.");
          me.$router.push({name: "organization"});
        })
      },
      editorToModel: function (text) {
        var me = this;
        me.user = JSON.parse(text);
      },
      open(ref) {
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .detail-table {
    .md-table-cell {
      min-height: 0px;
    }
    .md-input-container {
      min-height: 35px;
      margin-top: 5px;
      margin-bottom: 10px;
    }
  }
</style>
