<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <h2>사용자 추가</h2>
    <div style="text-align: right;">
      <md-button class="md-raised md-primary" v-on:click="createUser">
        추가
      </md-button>
    </div>
    <md-table-card style="margin-botton:10%;height: 50%;">
      <md-table>
        <md-table-body>
          <md-table-row>
            <md-table-cell><span class="md-subheader">LEVEL</span></md-table-cell>
            <md-table-cell>
              <md-input-container>
                <md-select v-model="user.level">
                  <md-option v-for="(i, index) in 6"  :value="index">{{index}}</md-option>
                </md-select>
                <!--<md-input v-model="user.level"></md-input>-->
              </md-input-container>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">이름</span></md-table-cell>
            <md-table-cell>
              <md-input-container>
                <md-input v-model="user.name"></md-input>
              </md-input-container>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">E-mail</span></md-table-cell>
            <md-table-cell>
              <md-input-container>
                <md-input v-model="user.email"></md-input>
              </md-input-container>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">비밀번호</span></md-table-cell>
            <md-table-cell>
              <md-input-container>
                <md-input v-model="user.userPassword"></md-input>
              </md-input-container>
            </md-table-cell>
          </md-table-row>
          <md-table-row>
            <md-table-cell><span class="md-subheader">관리자 여부</span></md-table-cell>
            <md-table-cell>
              <md-input-container>
                <md-select v-model="user.acl">
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
            :value="userString">

          </codemirror>
        </div>
      </md-layout>
    </md-layout>
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        user: {},
        userString: "",
      }
    },
    mounted() {
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
      createUser: function () {
        var me = this;
        console.log("create User");
        me.user.userName = me.user.email;
        console.log("me.user",me.user);
        me.$parent.iam.createUser(me.user).then(function (response) {
          me.$root.$children[0].success("회원을 추가하였습니다.");
          me.$router.push({name:"organization"});
        })
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
