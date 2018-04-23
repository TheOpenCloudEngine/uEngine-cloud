<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>

    <scale-app ref="scale-app"></scale-app>
    <app-editor ref="app-editor" :appId="targetAppId" :mode="'service'"></app-editor>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table md-sort="name">
          <md-table-header>
            <md-table-row>
              <md-table-head md-sort-by="name">이름</md-table-head>
              <md-table-head md-sort-by="instances">인스턴스</md-table-head>
              <md-table-head md-sort-by="status">상태</md-table-head>
              <md-table-head md-sort-by="cpu" md-numeric style="text-align: left">CPU</md-table-head>
              <md-table-head md-sort-by="mem" md-numeric style="text-align: left">메모리(MB)</md-table-head>
              <md-table-head md-sort-by="disk" md-numeric style="text-align: left">디스크(MB)</md-table-head>
              <md-table-head style="text-align: left">조치</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="app in list">
              <md-table-cell>
                <a v-on:click="moveService(app.id)" style="cursor: pointer">{{app.id}}</a>
              </md-table-cell>

              <md-table-cell>
                {{app.instances || 0}}
              </md-table-cell>

              <md-table-cell>
                <span v-if="app.role && !app.active" style="color: red">미배포</span>
                <service-progress v-else :app="app"></service-progress>
              </md-table-cell>

              <md-table-cell>{{app.cpus}}</md-table-cell>
              <md-table-cell>{{app.mem}}</md-table-cell>
              <md-table-cell>{{app.disk}}</md-table-cell>
              <md-table-cell>
                <md-menu md-size="4" md-direction="bottom left">
                  <md-button class="md-icon-button" md-menu-trigger>
                    <md-icon>more_vert</md-icon>
                  </md-button>

                  <md-menu-content>
                    <md-menu-item v-on:click="openEdit(app.id)">
                      <span>수정</span>
                    </md-menu-item>
                    <md-menu-item v-on:click="openScaleApp(app.id,'scale')">
                      <span>인스턴스 수 조정</span>
                    </md-menu-item>
                    <md-menu-item v-on:click="openScaleApp(app.id,'restart')">
                      <span>재시작</span>
                    </md-menu-item>
                    <md-menu-item v-on:click="openScaleApp(app.id,'suspend')">
                      <span>중단</span>
                    </md-menu-item>
                    <md-menu-item v-on:click="openScaleApp(app.id,'delete')">
                      <span>삭제</span>
                    </md-menu-item>
                  </md-menu-content>
                </md-menu>
              </md-table-cell>
            </md-table-row>
          </md-table-body>
        </md-table>
        <md-table-pagination
          :md-size="size"
          :md-total="total"
          :md-page="page"
          md-label="페이지당 항목"
          md-separator="총"
          :md-page-options="[5, 10, 25, 50]"
          @pagination="onPagination">
        </md-table-pagination>
      </md-table-card>
    </md-layout>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'

  export default {
    mixins: [DcosDataProvider],
    props: {
      search: String
    },
    data() {
      return {
        targetAppId: null,
        list: [],
        total: 10,
        size: 10,
        page: 1
      }
    },
    mounted() {
      this.makeList();
      var me = this;
      window.busVue.$on('marathonApp', function (event) {
        console.log(event);
        if (!event.appName) { //service
          var marathonAppId = event.body.app.id;
          me.list.forEach(function (app, index) {
            if (marathonAppId == app.id) {
              Vue.set(me.list, index, event.body.app);
            }
          })
        }
      });
    },
    watch: {
      search: function (newVal, oldVal) {
        this.makeList();
      }
    }
    ,
    methods: {
      onPagination: function (val) {
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      makeList: function () {
        var list = [];
        var me = this;
        me.list = [];

        me.getMarathonServiceApps(function (response) {
          $.each(response.data, function (i, service) {
            //search 적용
            if (me.search && me.search.length > 0 && service.id.indexOf(me.search) == -1) {
              return;
            }
            list.push(service);
          })

          //페이지네이션
          var offset = (me.page - 1) * me.size;
          var limit = (me.page) * me.size - 1;

          var count = 0;
          for (var i = 0; i < list.length; i++) {
            //서브 항목일 경우
            if (list[i].role) {
              me.list.push(list[i]);
            }
            //그 외의 경우
            else {
              if (count >= offset && count <= limit) {
                me.list.push(list[i]);
              }
              count++;
            }
          }
          me.total = count;
        })

      },
      openDeployments: function () {
        this.$refs['service-deployments'].open();
      }
      ,
      openScaleApp: function (appId, role) {
        this.$refs['scale-app'].open(appId, role);
      }
      ,
      moveService: function (appId) {
        var me = this;
        this.$router.push(
          {
            name: 'serviceTaskList',
            params: {appId: appId}
          }
        )
      },
      moveApp: function (appName) {
        var me = this;
        this.$router.push(
          {
            name: 'appsDetail',
            params: {appName: appName}
          }
        )
      },
      openEdit: function (appId) {
        var me = this;
        me.targetAppId = appId;
        this.$refs['app-editor'].open();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  /*tbody tr:nth-child(odd) {*/
  /*background-color: rgb(243, 243, 243);*/
  /*}*/
</style>
