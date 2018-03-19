<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>

    <scale-app ref="scale-app"></scale-app>
    <app-editor ref="app-editor" :appId="targetAppId" :mode="mode"></app-editor>
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
              <md-table-head style="text-align: left">조치</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="app in list">
              <md-table-cell>
                <a v-if="app.role" v-on:click="moveService(app.id)" style="cursor: pointer;margin-left: 30px">
                <span v-if="app.role == 'prod'">
                  프로덕션(신규)
                </span>
                  <span v-if="app.role == 'oldProd'">
                  프로덕션(이전)
                </span>
                  <span v-if="app.role == 'stg'">
                  스테이징
                </span>
                  <span v-if="app.role == 'dev'">
                  개발
                </span>
                </a>
                <a v-else-if="app.type == 'service'" v-on:click="moveService(app.id)"
                   style="cursor: pointer">{{app.id}}</a>

                <div v-else-if="app.type == 'app'" style="cursor: pointer">
                  <span v-on:click="focusId(app.id)"><md-icon>keyboard_arrow_right</md-icon></span>
                  <a v-on:click="moveApp(app.id)">{{app.id}}</a>
                </div>
              </md-table-cell>

              <md-table-cell>
                {{app.appType}}
              </md-table-cell>

              <md-table-cell>
                {{app.instancesLabel || app.instances || 0}}
              </md-table-cell>

              <md-table-cell>
                <span v-if="app.role && !app.active" style="color: red">미배포</span>
                <service-progress v-else :app="app"></service-progress>
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
              <md-table-cell>
                <md-menu
                  v-if="(app.type == 'service' && app.role && app.active) || (app.type == 'service' && !app.role)"
                  md-size="4" md-direction="bottom left">
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
      mode: String,
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
      this.makeList();
    },
    watch: {
      dcosData: {
        handler: function (newVal, oldVal) {
          this.makeList();
        },
        deep: true
      },
      search: function (newVal, oldVal) {
        this.makeList();
      }
    }
    ,
    methods: {
      onPagination: function (val) {
        this.focusedList = [];
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      focusId: function (id) {
        if (this.focusedList.indexOf(id) != -1) {
          this.focusedList.splice(this.focusedList.indexOf(id), 1);
        } else {
          this.focusedList.push(id);
        }
        this.makeList();
      },
      makeList: function () {
        var list = [];
        var me = this;
        me.list = [];
        var excludeServiced = [];
        if (!me.dcosData.devopsApps) {
          return;
        }
        //appId 를 추린다.
        for (var appId in me.dcosData.devopsApps) {
          //var appId = key;
          excludeServiced.push('/' + appId + '-green');
          excludeServiced.push('/' + appId + '-blue');
          excludeServiced.push('/' + appId + '-dev');
        }

        if (me.mode == 'app') {
          for (var appId in me.dcosData.devopsApps) {

            //search 적용
            if (this.search && this.search.length > 0 && appId.indexOf(this.search) == -1) {
              continue;
            }

            var app = {
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
              accessLevel: me.dcosData.devopsApps[appId].accessLevel,
              appType: me.dcosData.devopsApps[appId].appType
            };

            //메소스 app 를 합산.
            var additionalList = [];
            var isFocus = me.focusedList.indexOf(appId) == -1 ? false : true;
            var appsMap = me.getAppsByDevopsId(appId);
            for (var key in appsMap) {
              //key 는 dev,prod,stg
              //key 에 해당하는 메소스 app 이 있을 경우
              if (appsMap[key]) {
                for (var filed in app) {
                  if (filed == 'deployments') {
                    app[filed] = app[filed].concat(appsMap[key][filed]);
                  }
                  //스페이지별 인스턴스 수
                  else if (filed == 'instances') {
                    if (key == 'dev' || key == 'stg' || key == 'prod') {
                      app['instancesLabel'] += key + '(' + appsMap[key][filed] + ') ';
                    }
                    app[filed] = parseFloat((app[filed] + appsMap[key][filed]).toFixed(1));
                  } else if (appsMap[key][filed] && filed != 'instancesLabel') {
                    app[filed] = parseFloat((app[filed] + appsMap[key][filed]).toFixed(1));
                  }
                }
                //포커스 상태일 경우 리스트 노출
                if (isFocus) {
                  appsMap[key].active = true;
                  appsMap[key].type = 'service';
                  appsMap[key].role = key;
                  additionalList.push(JSON.parse(JSON.stringify(appsMap[key])));
                }
              }
              //key 에 해당하는 메소스 app 이 없을 경우
              else {
                var defaultApp = {
                  active: false,
                  type: 'service',
                  role: key
                };
                //포커스 상태일 경우 리스트 노출
                if (isFocus) {
                  additionalList.push(JSON.parse(JSON.stringify(defaultApp)));
                }
              }
            }

            app.id = appId;
            app.type = 'app';
            list.push(app);
            list = list.concat(additionalList);
          }
        } else if (me.mode == 'service') {
          $.each(me.dcosData.groups.apps, function (i, service) {
            if (excludeServiced.indexOf(service.id) == -1) {
              //search 적용
              if (me.search && me.search.length > 0 && service.id.indexOf(me.search) == -1) {
                return;
              }
              service.type = 'service';
              list.push(service);
            }
          })
        }

        //페이지네이션
        var offset = (this.page - 1) * this.size;
        var limit = (this.page) * this.size - 1;

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
        this.total = count;
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
