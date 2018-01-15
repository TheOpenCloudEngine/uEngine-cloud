<template>
  <div>
    <span class="md-subheading" v-if="iamData">현재시각: {{iamData.currentTime}}</span>
    <br><br>
    <span class="md-title">토큰 발급 현황</span>
    <token-table :data="tokenData"></token-table>
    <br><br>

    <span class="md-title">유저 현황</span>
    <user-list></user-list>
    <br><br>

    <span class="md-title">시스템 스테이터스</span>
    <md-layout>
      <md-layout>
        <div style="width: 100%;padding: 5px">
          <span class="md-subheading">시스템</span>
          <key-value-table :data="systemData"></key-value-table>
        </div>
      </md-layout>
      <md-layout>
        <div style="width: 100%;padding: 5px">
          <span class="md-subheading">스코프</span>
          <key-value-table :data="scopesData"></key-value-table>
        </div>
      </md-layout>
      <md-layout>
        <div style="width: 100%;padding: 5px">
          <span class="md-subheading">이메일</span>
          <key-value-table :data="emailData"></key-value-table>
        </div>
      </md-layout>
    </md-layout>
    <br><br>

    <templates ref="templates"></templates>
    <span class="md-title">클라이언트</span>
    <br><br>
    <md-layout>
      <md-layout md-flex="50" v-for="(client, index) in clients">
        <div style="width: 100%;padding: 5px">
          <span class="md-subheading">{{client.name}}</span>
          <md-button class="md-primary md-raised" @click="showTemplate(client.clientKey)">템플릿 보기</md-button>
          <key-value-table :data="client"></key-value-table>
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
          this.createSystemData(newVal);
          this.createTokenData(newVal);
        },
        deep: true
      }
    },
    methods: {
      showTemplate: function (clientKey) {
        this.$refs['templates'].open(clientKey);
      },
      getData: function () {
        var me = this;
        window.iam.getAllClient()
          .done(function (clients) {
            me.clients = clients;
          });
        window.iam.getAllScope()
          .done(function (scopes) {
            var scopesData = {};
            $.each(scopes, function (i, scope) {
              scopesData[scope.name] = scope.description;
            });
            me.scopesData = scopesData;
          })
      },
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
      createSystemData: function (data) {
        var systemData = {};
        var emailData = {};
        for (var key in data) {
          if (key != 'currentTime' && key != 'token') {
            for (var secondKey in data[key]) {
              if (key == 'mail') {
                emailData[key + '.' + secondKey] = data[key][secondKey];
              } else {
                systemData[key + '.' + secondKey] = data[key][secondKey];
              }
            }
          }
        }
        this.systemData = systemData;
        this.emailData = emailData;
      },
      createTokenData: function (data) {
        var list = [];
        for (var clientKey in data.token) {
          var client = this.getClientByKey(clientKey);
          if (client) {

          }
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
