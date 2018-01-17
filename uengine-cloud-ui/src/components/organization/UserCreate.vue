<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog md-open-from="#open" md-close-to="#open" ref="open" class="bigscreen">
    <md-dialog-title>사용자 추가</md-dialog-title>
    <md-dialog-content>
      <div style="text-align: right;">
        <md-button class="md-raised md-primary" v-on:click="createUser">
          추가
        </md-button>
      </div>
      <div style="margin-top: 30px;">
        <md-table-card>
          <div class="header-top-line" style="width: 100%;"></div>
          <md-table class="create-table">
            <md-table-body>
              <md-table-row>
                <md-table-cell><span>이름</span></md-table-cell>
                <md-table-cell>
                  <md-input-container>
                    <md-input v-model="user.metaData.name" style="margin-top: 10px;"></md-input>
                  </md-input-container>
                </md-table-cell>
              </md-table-row>
              <md-table-row>
                <md-table-cell><span>E-mail</span></md-table-cell>
                <md-table-cell>
                  <md-input-container>
                    <md-input v-model="user.metaData.email" style="margin-top: 10px;"></md-input>
                  </md-input-container>
                </md-table-cell>
              </md-table-row>
              <md-table-row>
                <md-table-cell><span>비밀번호</span></md-table-cell>
                <md-table-cell width="70%;">
                  <md-input-container required="true">
                    <md-input v-model="user.userPassword" style="margin-top: 10px;" type="password"></md-input>
                  </md-input-container>
                </md-table-cell>
              </md-table-row>
              <md-table-row>
                <md-table-cell><span>관리자 여부</span></md-table-cell>
                <md-table-cell>
                  <md-input-container>
                    <md-select v-model="user.metaData.acl">
                      <md-option>선택..</md-option>
                      <md-option value="admin">관리자</md-option>
                      <md-option value="user">사용자</md-option>
                    </md-select>
                  </md-input-container>
                </md-table-cell>
              </md-table-row>
            </md-table-body>
          </md-table>
        </md-table-card>
      </div>
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
                      :value="userString">

                    </codemirror>
                  </div>
                </md-table-cell>
              </md-table-row>
            </md-table-body>
          </md-table>
        </md-table-card>
      </div>
    </md-dialog-content>
    <md-dialog-actions>
      <md-button class="md-primary" @click="close">Close</md-button>
    </md-dialog-actions>
  </md-dialog>

</template>
<script>
  export default {
    props: {
      _user: Object,
    },
    data() {
      return {
        user: {
          metaData: {}
        },
        userString: "",
      }
    },
    mounted() {
      if (this._user) {
        this.loadUserInfo(this._user);
      }
    }
    ,
    watch: {
      user: {
        handler: function (newVal, oldVal) {
          newVal.userName = newVal.metaData.email;
          this.userString = JSON.stringify(newVal, null, 2);
        },
        deep: true
      },
    },
    methods: {
      createUser: function () {
        var me = this;
        window.iam.createUser(me.user).then(function (response) {
          me.$root.$children[0].success("회원을 추가하였습니다.");
          me.close();
        },function(response){
          me.$root.$children[0].error("회원을 추가할 수 없습니다.");
        })
      },
      open(ref) {
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      },
      loadUserInfo: function (user) {
        this.user.metaData.name = user.name;
        this.user.userName = user.username;
        this.user.metaData['gitlab-id'] = user.id;
        this.userString = JSON.stringify(this.user, null, 2);
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .create-table {
    height: 80%;
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
