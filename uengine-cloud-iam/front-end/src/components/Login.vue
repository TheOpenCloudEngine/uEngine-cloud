<template>
  <div class="fullscreen" v-if="command">

    <form v-if="command === 'login'" method="post" :action="backendUrl + '/oauth/login'">
      <md-card class="login-box">
        <img class="logo" src="/static/logo/main.png">

        <md-card-area>
          <md-card-header>
            <div class="md-subhead">Log in to your account</div>
            <div v-if="status == 'fail'" style="color: red">
              Invalid Account
            </div>
            <div class="md-subhead">{{oauthClient.name}} 에서 사용자의 다음 권한을 요청합니다.
            </div>
            <div v-for="(oauthScope, index) in oauthScopes">
              {{oauthScope.name}}
            </div>

            <div v-if="status == 'fail' && missingScopes && userScopeCheckAll" style="color: red">
              <br>
              <div class="md-subhead">사용자에게 다음의 권한이 없습니다.
              </div>
              <div v-for="(missingScope, index) in missingScopes">
                {{missingScope.name}}
              </div>
            </div>

            <div v-if="status == 'fail' && missingScopes && !userScopeCheckAll" style="color: red">
              <br>
              <div class="md-subhead">요청된 권한이 사용자에게 없습니다.</div>
              <div class="md-caption">최소 하나 이상의 권한이 필요합니다.</div>
            </div>

          </md-card-header>

          <md-card-content>
            <md-input-container>
              <label>E-mail</label>
              <md-input name="userName"
                        v-model="userName"
                        type="text"
                        required></md-input>
            </md-input-container>

            <md-input-container>
              <label>패스워드</label>
              <md-input name="userPassword"
                        v-model="userPassword"
                        label="Enter your password"
                        type="password"
                        required></md-input>
            </md-input-container>
          </md-card-content>
        </md-card-area>

        <md-input-container style="display: none">
          <md-input name="authorizeResponse" v-model="authorizeResponseString" type="hidden"></md-input>
        </md-input-container>

        <md-card-actions>
          <md-button type="submit">Login</md-button>
          <md-button v-on:click="move('signup')">Sign Up</md-button>
          <md-button v-on:click="move('forgot')">Forgot?</md-button>
        </md-card-actions>
      </md-card>
    </form>

    <form v-if="command === 'signup'" @submit.prevent="signup">
      <md-card class="login-box">
        <img class="logo" src="/static/logo/main.png">

        <md-card-area>
          <md-card-header>
            <div class="md-subhead">회원 가입</div>
          </md-card-header>

          <md-card-content>
            <md-input-container>
              <label>E-mail</label>
              <md-input name="userName"
                        v-model="userName"
                        type="text"
                        required></md-input>
            </md-input-container>

            <md-input-container>
              <label>패스워드</label>
              <md-input name="userPassword"
                        v-model="userPassword"
                        label="Enter your password"
                        type="password"
                        required></md-input>
            </md-input-container>

            <md-input-container>
              <label>성함</label>
              <md-input v-model="name"
                        type="text"
                        required></md-input>
            </md-input-container>
          </md-card-content>
        </md-card-area>

        <md-card-actions>
          <md-button type="submit">Sign up</md-button>
          <md-button v-on:click="move('login')">Go login</md-button>
          <md-button v-on:click="move('forgot')">Forgot?</md-button>
        </md-card-actions>
      </md-card>
    </form>

    <form v-if="command === 'forgot'" @submit.prevent="forgot">
      <md-card class="login-box">
        <img class="logo" src="/static/logo/main.png">

        <md-card-area>
          <md-card-header>
            <div class="md-subhead">비밀번호 분실</div>
            <div class="md-subhead">회원가입시 등록한 이메일을 입력하세요</div>
          </md-card-header>

          <md-card-content>
            <md-input-container>
              <label>E-mail</label>
              <md-input name="userName"
                        v-model="userName"
                        type="text"
                        required></md-input>
            </md-input-container>
          </md-card-content>
        </md-card-area>

        <md-card-actions>
          <md-button type="submit">제출</md-button>
          <md-button v-on:click="move('login')">Go login</md-button>
          <md-button v-on:click="move('signup')">Sign Up</md-button>
        </md-card-actions>
      </md-card>
    </form>

    <!--패스워드 분실 후 재설정 화면-->
    <form v-if="command === 'edit-password'" @submit.prevent="editPassword">
      <md-card class="login-box">
        <img class="logo" src="/static/logo/main.png">

        <md-card-area>
          <md-card-header>
            <div class="md-subhead">새로운 비밀번호를 입력하십시오.</div>
          </md-card-header>

          <md-card-content>
            <md-input-container>
              <label>Enter your password</label>
              <md-input v-model="userPassword"
                        type="password"
                        required></md-input>
            </md-input-container>
          </md-card-content>
        </md-card-area>

        <md-card-actions>
          <md-button type="submit">제출</md-button>
          <md-button v-on:click="move('login')">Go login</md-button>
          <md-button v-on:click="move('signup')">Sign Up</md-button>
        </md-card-actions>
      </md-card>
    </form>

    <form v-if="command === 'error'">
      <md-card class="login-box">
        <img class="logo" src="/static/logo/logo_bright.png">

        <md-card-area>
          <md-card-header>
            <div class="md-title"><span style="font-weight: bold">유엔진</span> 솔루션즈</div>
            <br>
            <div class="md-subhead">잘못된 요청입니다.</div>
          </md-card-header>
        </md-card-area>
      </md-card>
    </form>

    <div align="center">
      <img src="http://iam.pas-mini.io/static/logo/logo_bright.png" height="50" style="
        height: 20px;
        margin-top: 20px;
        margin-left: auto;
        margin-right: auto;">
    </div>
  </div>
</template>

<script>
  export default {
    name: 'Login',
    data: function () {
      return {
        command: null,
        iam: window.iam,
        userName: null,
        userPassword: null,
        name: null,
        authorizeResponse: null,
        authorizeResponseString: null,
        oauthClient: null,
        oauthScopes: [],
        status: null,
        missingScopes: null,
        userScopeCheckAll: false,
        token: null,
        backendUrl: window.backendUrl,
        browserUrl: window.browserUrl
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
        try {
          var me = this;
          this.userName = null;
          this.userPassword = null;
          this.command = this.$route.params.command;
          me.status = this.$route.query['status'];
          me.userScopeCheckAll = this.$route.query['userScopeCheckAll'] == 'true' ? true : false;

          var missingScopes = this.$route.query['missingScopes'];
          if (missingScopes && missingScopes.length > 0) {
            me.missingScopes = JSON.parse(missingScopes);
          }

          var authorizeResponse = this.$route.query['authorizeResponse'];
          if (authorizeResponse && authorizeResponse.length > 0) {
            me.authorizeResponse = JSON.parse(authorizeResponse);
            me.authorizeResponseString = authorizeResponse;
          }
          else if (this.$route.query.token && this.$route.query.token.length > 0) {
            me.token = this.$route.query.token;
            me.iam.registTokenVerification(me.token)
              .done(function (regist) {
                me.authorizeResponse = JSON.parse(regist.authorizeResponse);
                me.authorizeResponseString = regist.authorizeResponse;
              })
          }

          me.oauthClient = me.authorizeResponse['oauthClient'];
          me.oauthScopes = me.authorizeResponse['oauthScopes'];
          me.oauthUser = me.authorizeResponse['oauthUser'];

          //verification 인 경우
          if (this.command == 'sign-up-verification' || this.command == 'forgot-verification') {
            this.verification();
          }
        } catch (e) {
          me.command = 'error';
        }
      },
      move: function (command) {
        var me = this;
        this.$router.push({
          path: '/auth/' + command,
          query: {
            authorizeResponse: JSON.stringify(me.authorizeResponse)
          }
        })
      },

      verification: function () {
        var me = this;
        //회원가입 verification 인 경우
        if (this.command == 'sign-up-verification') {
          //Compete signup
          me.iam.signUpAccept(me.token)
            .done(function (User) {
              me.$root.$children[0].success('가입 신청이 완료되었습니다. 로그인 해 주세요.');
              me.$router.push({
                path: '/auth/login',
                query: {
                  authorizeResponse: JSON.stringify(me.authorizeResponse)
                }
              })
            })
            .fail(function () {
              me.$root.$children[0].error('회원 가입 신청을 완료할 수 없습니다.');
              me.$router.push({
                path: '/auth/login',
                query: {
                  authorizeResponse: JSON.stringify(me.authorizeResponse)
                }
              })
            })
        }

        //forgot 패스워드 verification 인 경우
        if (this.command == 'forgot-verification') {
          //Move to password edit route
          me.$router.push({
            path: '/auth/edit-password',
            query: {
              token: me.$route.query.token
            }
          })
        }
      },
      editPassword: function (e) {
        e.preventDefault();
        var me = this;

        //Compete repassword
        me.iam.forgotPasswordAccept(me.token, me.userPassword)
          .done(function (User) {
            me.$root.$children[0].success('비밀 번호 변경이 처리되었습니다. 로그인 해 주세요.');
            me.$router.push({
              path: '/auth/login',
              query: {
                authorizeResponse: JSON.stringify(me.authorizeResponse)
              }
            })
          })
          .fail(function () {
            me.$root.$children[0].error('비밀 번호 변경 요청을 완료할 수 없습니다.');
          })
      },
      forgot: function (e) {
        e.preventDefault();
        var me = this;
        me.iam.forgotPassword(me.browserUrl + '/#/auth/forgot-verification', me.userName, JSON.stringify(me.authorizeResponse))
          .done(function () {
            //비밀번호 변경 신청 성공 후 알림
            me.$root.$children[0].success('비밀번호 변경 신청을 완료하였습니다. 이메일을 확인해주세요.');
            me.$router.push({
              path: '/auth/login',
              query: {
                authorizeResponse: JSON.stringify(me.authorizeResponse)
              }
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
        me.iam.signUp(me.browserUrl + '/#/auth/sign-up-verification', {
          userName: me.userName,
          userPassword: me.userPassword,
          metaData: {
            email: me.userName,
            locale: 'ko_KR',
            name: me.name
          }
        }, JSON.stringify(me.authorizeResponse))
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
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" rel="stylesheet/scss">
  .fullscreen {
    position: relative;
    height: calc(100vh);
    overflow-y: hidden;
    overflow-x: hidden;
    width: 100%;
    background: #242A37;
    font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  }

  .login-box {
    border-radius: 10px;
    position: relative;
    max-width: 350px;
    margin-left: auto;
    margin-right: auto;
    margin-top: 180px;
    text-align: center;
    overflow: visible;

    .md-card-header {

    }

    .logo {
      position: relative;
      margin-top: -50px;
      margin-left: auto;
      margin-right: auto;
      width: 100%;
      border-top-left-radius: 10px;
      border-top-right-radius: 10px;
    }
  }

</style>
