<template>
  <div>
    <span class="md-subheading" v-if="iamData">현재시각: {{iamData.currentTime}}</span>
    <br><br>
    <span class="md-title">토큰 발급 현황</span>
    <token-table :data="tokenData"></token-table>
    <br><br>

    <span class="md-title">유저 현황</span>
    <user-list></user-list>
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        interval: true,
        iamData: null,
        systemData: {},
        emailData: {},
        tokenData: [],
        clients: [],
        scopesData: {}
      }
    },
    mounted() {
      this.fetchData();
      this.getData();
    },
    destroyed: function () {
      this.interval = false;
    },
    watch: {
      iamData: {
        handler: function (newVal, oldVal) {
          this.createTokenData(newVal);
        },
        deep: true
      }
    },
    methods: {
      /**
       * 2초에 한번 전체 데이터를 갱신하도록 조정.
       */
      fetchData: function () {
        var me = this;
        window.iam.system()
          .done(function (data) {
            me.iamData = data;
            if (me.interval) {
              setTimeout(function () {
                me.fetchData();
              }, 2000);
            }
          })
          .fail(function () {
            if (me.interval) {
              setTimeout(function () {
                me.fetchData();
              }, 2000);
            }
          });
      },
      getData: function () {
        var me = this;
        window.iam.getAllClient()
          .done(function (clients) {
            me.clients = clients;
          });
      },
      createTokenData: function (data) {
        var list = [];
        for (var clientKey in data.token) {
          var client = this.getClientByKey(clientKey);
          list.push({
            client: clientKey,
            active: data.token[clientKey].active,
            expired: data.token[clientKey].expired,
            accessTokenLifetime: client ? client.accessTokenLifetime : 0,
            refreshTokenLifetime: client ? client.refreshTokenLifetime : 0
          })
        }
        this.tokenData = list;
      },
      getClientByKey: function (clientKey) {
        var selected = null;
        if (this.clients && this.clients.length) {
          $.each(this.clients, function (i, client) {
            if (client['clientKey'] == clientKey) {
              selected = client;
            }
          })
        }
        return selected;
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
