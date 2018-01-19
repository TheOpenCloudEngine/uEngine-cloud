<template xmlns:v-bind="http://www.w3.org/1999/xhtml">
  <div>
    <md-toolbar class="fixed-toolbar">
      <md-button class="md-icon-button" @click="toggleLeftSidenav">
        <md-icon>menu</md-icon>
      </md-button>
      <span>Vue js Template</span>
      <span style="margin-left: 15px">
        <breadcrumbs style="width: 100%"></breadcrumbs>
      </span>
    </md-toolbar>

    <md-sidenav class="md-left" ref="leftSidenav">
      <md-toolbar class="md-account-header">
        <md-layout>
          <iam-avatar :iam="iam"></iam-avatar>
        </md-layout>
      </md-toolbar>

      <md-list>
        <md-list-item v-for="item in items" v-on:click="move(item.routerPath)" class="md-primary">
          <md-icon>{{ item.icon }}</md-icon>
          <span>{{ item.title }}</span>
        </md-list-item>
      </md-list>
    </md-sidenav>
    <div class="fluid" v-bind:class="{ 'bg-white': isAppDetail }">
      <md-layout md-align="center">
        <router-view
          v-bind:class="{ 'width-95': !isAppDetail,'width-100': isAppDetail }"
        ></router-view>
      </md-layout>
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      iam: Object
    },
    data() {
      return {
        isAppDetail: true,
        drawer: null,
        items: [
          {title: 'Dashboard', icon: 'dashboard', routerPath: '/dashboard'},
          {title: 'Apps', icon: 'dashboard', routerPath: '/apps'},
          {title: 'Settings', icon: 'settings_applications', routerPath: '/settings'},
          {title: 'Organization', icon: 'people', routerPath: '/organization'}
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
        var me = this;
        this.iam.logout();

        //Additional access_token storage
        localStorage.removeItem('access_token');

        this.$router.push({
          path: '/auth/login'
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

        var isAppDetail = false;
        $.each(routers, function (r, router) {
          //appDetail 라우트가 있다면 바탕화면 하얀색
          if (router.name == 'appsDetail') {
            isAppDetail = true;
          }
        });
        this.isAppDetail = isAppDetail;
      },
      move(routerPath) {
        this.$router.push(routerPath)
      },
      profile: function () {

      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
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
