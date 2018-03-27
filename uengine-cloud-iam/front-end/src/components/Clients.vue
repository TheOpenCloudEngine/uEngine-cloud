<template>
  <div>
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
      this.getData();
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
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
