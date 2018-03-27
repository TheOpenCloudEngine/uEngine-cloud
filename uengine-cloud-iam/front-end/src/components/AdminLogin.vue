<template>
  <md-layout>
    <md-layout md-vertical-align md-flex="50" md-flex-offset="25">
      <form @submit.prevent="login" style="width: 100%">
        <md-card>
          <md-card-area>
            <md-card-media>
              <!--<img src="assets/card-image-1.jpg" alt="People">-->
            </md-card-media>

            <md-card-header>
              <div class="md-title">시스템 로그인</div>
            </md-card-header>

            <md-card-content>
              <md-input-container>
                <label>시스템 사용자</label>
                <md-input v-model="username"
                          type="text"
                          required></md-input>
              </md-input-container>

              <md-input-container>
                <label>패스워드</label>
                <md-input v-model="password"
                          label="Enter your password"
                          type="password"
                          required></md-input>
              </md-input-container>
            </md-card-content>
          </md-card-area>

          <md-card-actions>
            <md-button type="submit">Login</md-button>
          </md-card-actions>
        </md-card>
      </form>
    </md-layout>
  </md-layout>
</template>

<script>
  export default {
    name: 'Login',
    data: function () {
      return {
        username: null,
        password: null
      };
    },
    mounted() {

    },
    methods: {
      login: function (e) {
        e.preventDefault();
        var me = this;
        window.iam.adminLogin(me.username, me.password)
          .done(function (response) {
            //원래 가고자 했던 화면으로 리다이렉트
            if (me.$route.query.redirect) {
              me.$router.push({
                path: me.$route.query.redirect
              })
            }
            //홈으로 이동
            else {
              me.$router.push({
                path: '/'
              })
            }
          })
          .fail(function () {
            //로그인 실패 후 알림
            me.$root.$children[0].error('로그인을 할 수 없습니다.');
          });
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
