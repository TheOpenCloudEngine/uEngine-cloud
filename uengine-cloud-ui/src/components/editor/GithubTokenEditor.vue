<template xmlns:v-on="http://www.w3.org/1999/xhtml">

</template>
<script>
  export default {
    props: {},
    data() {
      return {}
    },
    mounted() {

    }
    ,
    methods: {
      validate: function (cb) {
        var me = this;
        me.$root.$children[0].block();
        me.$root.github('user').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            me.openEditor();
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      openEditor: function () {
        var me = this;
        me.$root.$children[0].confirm(
          {
            title: 'Github 토큰 저장',
            contentHtml: 'Github Personal access token 이 필요합니다. (입력후에 재로그인 필요)',
            prompt: true,
            promptValue: localStorage['githubToken'] || '',
            promptLabel: 'Personal access token',
            okText: '저장',
            cancelText: '취소',
            callback: function (token) {
              if (!token || token.length < 1) {
                me.$root.$children[0].error('토큰값이 필요합니다.');
              } else {
                var userName = localStorage['userName'];
                me.$root.$children[0].block();
                window.iam.getUser(userName).then(
                  function (response) {
                    var user = response;
                    user.metaData.githubToken = token;
                    window.iam.updateUser(user.userName, user)
                      .then(function (response) {
                        window.iam.logout();
                        localStorage.removeItem('access_token');
                        me.$router.push({
                          path: '/auth/login'
                        })
                      })
                      .finally(function () {
                        me.$root.$children[0].unblock();
                      });
                  },
                  function () {
                    me.$root.$children[0].unblock();
                  });
              }
            }
          });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
