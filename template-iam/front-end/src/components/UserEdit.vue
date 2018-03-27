<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog md-open-from="#open" md-close-to="#open" ref="open">
    <md-dialog-title>
      <span v-if="!userName">유저 생성</span>
      <span v-else>유저 편집 "{{userName}}"</span>
    </md-dialog-title>

    <md-dialog-content style="overflow-x: hidden;padding: inherit;overflow-y: hidden;">
      <div style="min-width: 800px;padding: 12px;">
        <md-layout md-flex="100" :md-gutter="16">
          <md-layout>
            <div class="bold">사용자 이름:</div>
            <md-input-container>
              <label>고유한 이름 입력</label>
              <md-input v-model="user.userName" required :readonly="userName ? true : false"></md-input>
            </md-input-container>
          </md-layout>
          <md-layout>
            <div class="bold">패스워드:</div>
            <md-input-container>
              <label>패스워드</label>
              <md-input type="password" v-model="user.userPassword" required></md-input>
            </md-input-container>
          </md-layout>
        </md-layout>

        <div class="bold">스코프:</div>
        <md-layout md-flex="100">
          <md-checkbox v-for="(scope, index) in checkedScopes" v-model="scope.checked"
                       class="md-primary">
            {{scope.name}}
          </md-checkbox>
        </md-layout>

        <div class="bold">메타데이터:</div>
        <md-layout md-flex="100" style="height: 270px">
          <div style="width: 100%">
            <codemirror v-if="opened"
                        :options="{
                                  theme: 'dracula',
                                  mode: 'javascript',
                                  extraKeys: {'Ctrl-Space': 'autocomplete'},
                                  lineNumbers: true,
                                  lineWrapping: true
                                }"
                        :value="editorData"
                        v-on:change="editorToModel"
            >
            </codemirror>
          </div>
        </md-layout>

        <md-layout md-flex="100">
          <md-layout>
            <md-button class="md-primary md-raised" @click="save">저장</md-button>
            <md-button v-if="userName" class="md-primary md-raised" @click="remove">삭제</md-button>
          </md-layout>
          <md-layout md-align="end">
            <md-button class="md-primary md-raised" @click="close">닫기</md-button>
          </md-layout>
        </md-layout>
      </div>
    </md-dialog-content>
  </md-dialog>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        userName: null,
        opened: false,
        editorData: '{}',
        user: {
          userName: null,
          userPassword: null,
          metaData: {
            scopes: []
          },
        },
        systemScopes: [],
        checkedScopes: []
      }
    },
    mounted() {

    },
    watch: {
      'user.metaData': {
        handler: function () {
          this.createCheckedScopes();
        },
        deep: true
      },
      'systemScopes': {
        handler: function () {
          this.createCheckedScopes();
        },
        deep: true
      },
      'checkedScopes': {
        handler: function () {
          var me = this;
          $.each(me.checkedScopes, function (i, scope) {
            //선택 되었을 경우 추가
            if (scope.checked) {
              if (me.user.metaData.scopes.indexOf(scope.name) < 0) {
                me.user.metaData.scopes.push(scope.name);
              }
            }
            //선택 해제되었을 경우 삭제
            else {
              var index = me.user.metaData.scopes.indexOf(scope.name);
              if (index > -1) {
                me.user.metaData.scopes.splice(index, 1);
              }
            }
          });
          this.modelToEditor();
          //this.createScopeMap();
        },
        deep: true
      }
    }
    ,
    methods: {
      createCheckedScopes: function () {
        var me = this;
        me.checkedScopes = [];
        var userScopes = JSON.parse(JSON.stringify(me.user.metaData.scopes));
        $.each(me.systemScopes, function (i, scope) {
          me.checkedScopes.push({
            checked: userScopes.indexOf(scope.name) > -1,
            name: scope.name
          });
        });
      },
      open(userName) {
        var me = this;
        this.$refs['open'].open();

        this.userName = userName;
        if (!userName) {
          me.user = {
            userName: null,
            userPassword: null,
            metaData: {
              scopes: []
            },
          };
        } else {
          window.iam.getUser(userName)
            .done(function (user) {
              me.user.userName = user.userName;
              me.user.userPassword = user.userPassword;
              me.user.metaData = user.metaData;
              if (!me.user.metaData.scopes) {
                me.user.metaData.scopes = [];
              }
            })
            .fail(function () {
              me.$root.$children[0].error('사용자를 정보를 얻을 수 없습니다.');
              me.close();
            })
            .always(function () {
              me.edited();
            })
        }

        window.iam.getAllScope()
          .done(function (scopes) {
            me.systemScopes = scopes;
          });

        me.opened = true;
        setTimeout(function () {
          $(me.$el).find('.CodeMirror').height(270);
          me.modelToEditor();
        }, 200);
      },
      close(ref) {
        this.$refs['open'].close();
      },
      save: function () {
        var me = this;
        //업데이트
        if (me.userName) {
          window.iam.updateUser(me.userName, me.user)
            .done(function () {
              me.$root.$children[0].success('사용자를 업데이트 했습니다.');
            })
            .fail(function () {
              me.$root.$children[0].error('사용자를 업데이트 할 수 없습니다.');
            })
            .always(function () {
              me.edited();
            })
        }
        //신규
        else {
          window.iam.createUser(me.user)
            .done(function () {
              me.$root.$children[0].success('사용자를 추가했습니다.');
              me.close();
            })
            .fail(function () {
              me.$root.$children[0].error('사용자를 추가할 수 없습니다.');
            })
            .always(function () {
              me.edited();
            })
        }
      },
      editorToModel: function (val) {
        this.user.metaData = JSON.parse(val);
      },
      modelToEditor: function () {
        this.editorData = JSON.stringify(this.user.metaData, null, 2);
      },
      remove: function () {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: '사용자를 삭제합니다.',
            okText: '삭제하기',
            cancelText: '취소',
            callback: function () {
              window.iam.deleteUser(me.userName)
                .done(function () {
                  me.$root.$children[0].error('사용자를 삭제하였습니다.');
                  me.close();
                })
                .fail(function () {
                  me.$root.$children[0].error('사용자를 삭제할 수 없습니다.');
                })
                .always(function () {
                  me.edited();
                })
            }
          });
      },
      edited: function () {
        this.$emit('edited', true);
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-dialog-title {
    background-color: #f5f5f6;
    height: 15%;
    border-bottom: solid 1px #e6e6e6
  }

</style>
