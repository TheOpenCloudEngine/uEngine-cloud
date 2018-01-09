<template>
  <div></div>
</template>

<script>
  export default {
    name: 'Login',
    props: {},
    data: function () {
      return {
        command: null
      };
    },
    mounted() {
      this.authorize();
    },
    watch: {
      '$route'(to, from) {
        this.authorize();
      }
    },

    methods: {
      authorize: function () {
        var me = this;
        this.command = this.$route.params.command;
        //토큰 받는 url 인 경우
        if (this.command == 'verification') {
          var access_token = me.$route.query['access_token'];

          //토큰 유효 체크
          window.iam.validateToken(access_token)
            .done(function (info) {
              localStorage['access_token'] = access_token;
              //원래 가고자 했던 화면으로 리다이렉트
              if (localStorage['redirect'] && localStorage['redirect'] != 'undefined') {
                me.$router.push({
                  path: localStorage['redirect']
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
        //로그인 하는 url 인 경우
        else {
          //원래 가고자 했던 url 저장
          localStorage['redirect'] = me.$route.query.redirect;

          //로그인 url 이동
          var query_string = $.param({
            redirect_uri: location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '') + '/#/auth/verification',
            scope: 'cloud-server',
            token_type: 'JWT',
            response_type: 'token',
            client_id: localStorage.getItem('uengine-iam-client-key')
          });
          window.location.href = window.iam.baseUrl + '/oauth/authorize?' + query_string;
        }
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
