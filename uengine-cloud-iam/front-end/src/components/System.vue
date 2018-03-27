<template>
  <div>
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
    watch: {
      iamData: {
        handler: function (newVal, oldVal) {
          this.createSystemData(newVal);
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
        window.iam.system()
          .done(function (data) {
            me.iamData = data;
          })

        window.iam.getAllScope()
          .done(function (scopes) {
            var scopesData = {};
            $.each(scopes, function (i, scope) {
              scopesData[scope.name] = scope.description;
            });
            me.scopesData = scopesData;
          })
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
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
