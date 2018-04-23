<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table md-sort="name">
          <md-table-header>
            <md-table-row>
              <md-table-head md-sort-by="name">이름</md-table-head>
              <md-table-head md-sort-by="appType">종류</md-table-head>
              <md-table-head md-sort-by="instancesLabel">인스턴스</md-table-head>
              <md-table-head md-sort-by="status">상태</md-table-head>
              <md-table-head md-sort-by="accessLevel">소유</md-table-head>
              <md-table-head md-sort-by="cpu" md-numeric style="text-align: left">CPU</md-table-head>
              <md-table-head md-sort-by="mem" md-numeric style="text-align: left">메모리(MB)</md-table-head>
              <md-table-head md-sort-by="disk" md-numeric style="text-align: left">디스크(MB)</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="app in list">
              <md-table-cell>
                <a v-on:click="moveApp(app.name)">{{app.name}}</a>
              </md-table-cell>

              <md-table-cell>
                {{app.appType}}
              </md-table-cell>

              <md-table-cell>
                {{app.instancesLabel}}
              </md-table-cell>

              <md-table-cell>
                <service-progress :app="app"></service-progress>
              </md-table-cell>

              <md-table-cell>
                <div v-if="app.accessLevel == 50">
                  Owner
                </div>
                <div v-if="app.accessLevel == 40">
                  Master
                </div>
                <div v-if="app.accessLevel == 30">
                  Developer
                </div>
                <div class="md-caption" v-if="app.accessLevel == 20">
                  Reporter
                </div>
                <div class="md-caption" v-if="app.accessLevel == 10">
                  Guest
                </div>
                <div class="md-caption" v-if="app.accessLevel == 0">
                  None
                </div>
              </md-table-cell>

              <md-table-cell>{{app.cpus}}</md-table-cell>
              <md-table-cell>{{app.mem}}</md-table-cell>
              <md-table-cell>{{app.disk}}</md-table-cell>
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
        focusedList: [],
        list: [],
        total: 10,
        size: 10,
        page: 1
      }
    },
    mounted() {
      var me = this;
      me.makeList();

      var replaceItem = function (appName) {
        me.list.forEach(function (app, index) {
          if (appName == app.name) {
            me.getApp(appName, function (response) {
              if (response) {
                Vue.set(me.list, index, me.makeItem(response.data));
              }
            })
          }
        })
      }

      window.busVue.$on('app', function (event) {
        var appName = event.appName;
        replaceItem(appName);
      });

      window.busVue.$on('marathonApp', function (event) {
        var appName = event.appName;
        if (appName) {
          replaceItem(appName);
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
      makeItem: function (app) {
        var me = this;
        var name = app.name;
        var item = {
          tasksStaged: 0,
          instances: 0,
          instancesLabel: '',
          tasksUnhealthy: 0,
          tasksHealthy: 0,
          tasksRunning: 0,
          cpus: 0,
          mem: 0,
          disk: 0,
          deployments: [],
          accessLevel: app.accessLevel,
          appType: app.appType,
          name: app.name
        };
        me.getMarathonAppsByAppName(app.name, function (response) {
          for (var stage in response.data) {
            //key 는 dev,prod,stg
            //key 에 해당하는 메소스 app 이 있을 경우
            if (response.data[stage].app) {
              var marathonApp = response.data[stage].app;
              for (var filed in item) {
                if (filed == 'deployments') {
                  item[filed] = item[filed].concat(marathonApp[filed]);
                }
                //스테이지별 인스턴스 수
                else if (filed == 'instances') {
                  if (stage == 'dev' || stage == 'stg' || stage == 'prod') {
                    item['instancesLabel'] += stage + '(' + marathonApp[filed] + ') ';
                  }
                  item[filed] = parseFloat((item[filed] + marathonApp[filed]).toFixed(1));
                }
                else if (marathonApp[filed] && filed != 'instancesLabel' && filed != 'name') {
                  item[filed] = parseFloat((item[filed] + marathonApp[filed]).toFixed(1));
                }
              }
            }
          }
        })
        return item;
      },
      makeList: function () {
        var me = this;
        me.getApps(me.search, me.page - 1, me.size, function (response, error) {
          if (error) {
            me.list = [];
            return;
          }
          me.list = [];
          me.total = response.data.totalElements;
          response.data.content.forEach(function (app, index) {

            var item = me.makeItem(app);
            me.list.push(item);
          })
        })
      },
      moveApp: function (appName) {
        var me = this;
        this.$router.push(
          {
            name: 'appsDetail',
            params: {appName: appName}
          }
        )
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  /*tbody tr:nth-child(odd) {*/
  /*background-color: rgb(243, 243, 243);*/
  /*}*/
</style>
