<template xmlns:v-bind="http://www.w3.org/1999/xhtml">
  <div>
    <md-toolbar class="fixed-toolbar">
      <md-button class="md-icon-button">
        <md-icon>menu</md-icon>
      </md-button>

      <md-button class="md-raised md-primary" @click="logout">로그 아웃</md-button>

      <!--<span>Hanhwa S&C</span>-->
      <span>Open Cloud Engine</span>

    </md-toolbar>

    <div class="fluid">
      <md-layout md-align="center">
        <router-view class="width-95"></router-view>
      </md-layout>
    </div>
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        drawer: null,
        items: [
          {title: 'Dashboard', icon: 'dashboard', routerPath: '/dashboard'},
          {title: 'Users', icon: 'dashboard', routerPath: '/users'}
        ],
        mini: false
      }
    },
    mounted() {
      this.updateActive();
    },
    watch: {
      '$route'(to, from) {
        this.updateActive();
      }
    },
    methods: {
      toggleLeftSidenav() {
        this.$refs.leftSidenav.toggle();
      },
      logout: function () {
        window.iam.adminLogout();

        //Additional access_token storage
        localStorage.removeItem('access_token');

        this.$router.push({
          path: '/admin/login'
        })
      },
      updateActive: function () {
        var me = this;
        var routers = me.$route.matched;
        $.each(me.items, function (i, item) {
          var isActive = false;
          $.each(routers, function (r, router) {
            if (router.name == item.routerPath) {
              isActive = true;
            }
          });
          item.isActive = isActive;
        });
      },
      move(routerPath) {
        this.$router.push(routerPath)
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  @import '/static/css/custom.css';

  .fixed-toolbar {
    height: 64px;
    overflow: hidden;
  }

  .fluid {
    padding: 16px 0px 16px 0px;
    position: relative;
    height: calc(100vh - 64px);
    overflow-y: scroll;
    overflow-x: hidden;
    width: 100%;
  }

  .width-95 {
    width: 95%;
  }

  .width-100 {
    width: 100%;
  }
</style>
