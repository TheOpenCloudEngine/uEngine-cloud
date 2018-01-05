<template>
  <md-layout>
    <md-layout md-vertical-align md-flex="50" md-flex-offset="25">
      <form v-if="command === 'login'" @submit.prevent="login" style="width: 100%">
        <md-card>
          <md-card-area>
            <md-card-media>
              <!--<img src="assets/card-image-1.jpg" alt="People">-->
            </md-card-media>

            <md-card-header>
              <div class="md-title">로그인</div>
            </md-card-header>

            <md-card-content>
              <md-input-container>
                <label>E-mail</label>
                <md-input v-model="username"
                          type="email"
                          required></md-input>
              </md-input-container>

              <md-input-container>
                <label>Enter your password</label>
                <md-input v-model="password"
                          label="Enter your password"
                          type="password"
                          required></md-input>
              </md-input-container>
            </md-card-content>
          </md-card-area>

          <md-card-actions>
            <md-button type="submit">Login</md-button>
            <md-button v-on:click="move('signup')">Sign Up</md-button>
            <md-button v-on:click="move('forgot')">Forgot Password?</md-button>
          </md-card-actions>
        </md-card>
      </form>

      <form v-if="command === 'signup'" @submit.prevent="signup" style="width: 100%">
        <md-card>
          <md-card-area>
            <md-card-media>
              <!--<img src="assets/card-image-1.jpg" alt="People">-->
            </md-card-media>

            <md-card-header>
              <div class="md-title">회원 가입</div>
            </md-card-header>

            <md-card-content>
              <md-input-container>
                <label>E-mail</label>
                <md-input v-model="username"
                          type="email"
                          required></md-input>
              </md-input-container>

              <md-input-container>
                <label>Enter your password</label>
                <md-input v-model="password"
                          label="Enter your password"
                          type="password"
                          required></md-input>
              </md-input-container>

              <md-input-container>
                <label>이름</label>
                <md-input v-model="name"
                          type="text"
                          required></md-input>
              </md-input-container>
            </md-card-content>
          </md-card-area>

          <md-card-actions>
            <md-button type="submit">제출</md-button>
            <md-button v-on:click="move('login')">로그인 화면으로</md-button>
            <md-button v-on:click="move('forgot')">Forgot Password?</md-button>
          </md-card-actions>
        </md-card>
      </form>

      <form v-if="command === 'forgot'" @submit.prevent="forgot" style="width: 100%">
        <md-card>
          <md-card-area>
            <md-card-media>
              <!--<img src="assets/card-image-1.jpg" alt="People">-->
            </md-card-media>

            <md-card-header>
              <div class="md-title">비밀번호 분실</div>
            </md-card-header>

            <md-card-content>
              <md-input-container>
                <label>E-mail</label>
                <md-input v-model="username"
                          type="email"
                          required></md-input>
              </md-input-container>

            </md-card-content>
          </md-card-area>

          <md-card-actions>
            <md-button type="submit">제출</md-button>
            <md-button v-on:click="move('login')">로그인 화면으로</md-button>
            <md-button v-on:click="move('signup')">Sign Up</md-button>
          </md-card-actions>
        </md-card>
      </form>

      <!--패스워드 분실 후 재설정 화면-->
      <form v-if="command === 'edit-password'" @submit.prevent="editPassword" style="width: 100%">
        <md-card>
          <md-card-area>
            <md-card-media>
              <!--<img src="assets/card-image-1.jpg" alt="People">-->
            </md-card-media>

            <md-card-header>
              <div class="md-title">새로운 비밀번호를 입력하십시오.</div>
            </md-card-header>

            <md-card-content>
              <md-input-container>
                <label>Enter your password</label>
                <md-input v-model="password"
                          type="password"
                          required></md-input>
              </md-input-container>

            </md-card-content>
          </md-card-area>

          <md-card-actions>
            <md-button type="submit">제출</md-button>
            <md-button v-on:click="move('login')">로그인 화면으로</md-button>
            <md-button v-on:click="move('signup')">Sign Up</md-button>
          </md-card-actions>
        </md-card>
      </form>

    </md-layout>
  </md-layout>
</template>

<script>
  export default {
    name: 'Login',
    props: {},
    data: function () {
      return {
        iam: window.iam,
        command: null,
        username: null,
        password: null,
        name: null,
        file: null,
        scope: 'cloud-server',
        myUrl: location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '')
      };
    },
    mounted() {
      this.renderByCommand();
    },
    watch: {
      '$route'(to, from) {
        this.renderByCommand();
      }
    },

    methods: {
      renderByCommand: function () {
        this.username = null;
        this.password = null;
        this.command = this.$route.params.command;

        //verification 인 경우
        if (this.command == 'sign-up-verification' || this.command == 'forgot-verification') {
          this.verification();
        }
      },
      verification: function () {
        var me = this;
        //회원가입 verification 인 경우
        if (this.command == 'sign-up-verification') {
          me.iam.signUpVerification(me.$route.query.token)
            .done(function (User) {
              //Validate for user...
              console.log(User);

              //Compete signup
              me.iam.signUpAccept(me.$route.query.token)
                .done(function (User) {
                  me.$root.$children[0].success('가입 신청이 완료되었습니다. 로그인 해 주세요.');
                  me.$router.push({
                    path: '/auth/login'
                  })
                })
                .fail(function () {
                  me.$root.$children[0].error('회원 가입 신청을 완료할 수 없습니다.');
                })
            })
            .fail(function () {
              me.$root.$children[0].error('회원 가입 신청을 완료할 수 없습니다.');
            })
        }

        //forgot 패스워드 verification 인 경우
        if (this.command == 'forgot-verification') {
          me.iam.forgotPasswordVerification(me.$route.query.token)
            .done(function (User) {
              //Validate for user...
              console.log(User);

              //Move to password edit route
              me.$router.push({
                path: '/auth/edit-password?token=' + me.$route.query.token + '&userName=' + User.userName
              })
            })
            .fail(function () {
              me.$root.$children[0].error('패스워드 변경 신청을 완료할 수 없습니다.');
            })
        }
      },
      onFileChange: function (file, fileName) {
        //console.log(file);
        this.file = file;
      },
      move: function (command) {
        this.$router.push({
          path: '/auth/' + command
        })
      },
      editPassword: function (e) {
        e.preventDefault();
        var me = this;

        //Compete repassword
        me.iam.forgotPasswordAccept(me.$route.query.token, me.$route.query.userName)
          .done(function (User) {
            me.$root.$children[0].success('비밀 번호 변경이 처리되었습니다. 로그인 해 주세요.');
            me.$router.push({
              path: '/auth/login'
            })
          })
          .fail(function () {
            me.$root.$children[0].error('비밀 번호 변경 요청을 완료할 수 없습니다.');
          })
      },
      forgot: function (e) {
        e.preventDefault();
        var me = this;
        me.iam.forgotPassword(me.myUrl + '/auth/forgot-verification', me.username)
          .done(function () {
            //비밀번호 변경 신청 성공 후 알림
            me.$root.$children[0].success('비밀번호 변경 신청을 완료하였습니다. 이메일을 확인해주세요.');
            me.$router.push({
              path: '/auth/login'
            })
          })
          .fail(function (response) {
            //해당 사용자가 없음
            if (response.status == 404) {
              me.$root.$children[0].error('해당 사용자는 존재하지 않습니다.');
            }
            //호출을 실패함
            else {
              me.$root.$children[0].error('비밀번호 변경 신청을 할 수 없습니다.');
            }
          })
      },
      signup: function (e) {
        e.preventDefault();
        var me = this;
        me.iam.signUp(me.myUrl + '/auth/sign-up-verification', {
          userName: me.username,
          userPassword: me.password,
          email: me.username,
          locale: 'ko_KR',
          name: me.name
        })
          .done(function () {
            //회원 가입 신청 성공 후 알림
            me.$root.$children[0].success('가입 신청을 완료하였습니다. 이메일을 확인해주세요.');
            me.$router.push({
              path: '/auth/login'
            })
          })
          .fail(function (response) {
            //이미 있는 사용자
            if (response.status == 409) {
              me.$root.$children[0].error('이미 존재하는 사용자 입니다.');
            }
            //회원 가입 실패 후
            else {
              me.$root.$children[0].error('회원 가입 신청을 할 수 없습니다.');
            }
          })
      },
      login: function (e) {
        e.preventDefault();
        var me = this;
        me.iam.passwordCredentialsLogin(me.username, me.password, me.scope, 'JWT')
          .done(function (response) {
            localStorage['access_token'] = response['access_token'];
            localStorage['username'] = me.username;
            var tenant = me.username.split("@")[1];
            tenant = tenant.split(".")[0];
            localStorage['tenant'] = tenant;

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
          })
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
