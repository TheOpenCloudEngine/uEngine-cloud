<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout v-if="categoryItem && devApp" class="bg-white">
    <div class="header-top-line"></div>
    <div v-if="marathonApp || !deploy" style="width: 100%">
      <md-layout>
        <md-layout v-for="(item, key) in items">
          <md-card md-with-hover style="width: 100%;">
            <md-card-area>
              <md-card-content>

                <!--원 부분-->
                <md-layout md-align="center">
                  <md-avatar v-if="item.type == 'profile'">
                    <img :src="item.image" alt="item.title">
                  </md-avatar>
                  <md-avatar v-else v-bind:class="{ 'diff': item.diff }" style="overflow: visible;">
                    <div style="position: relative;width: 100%;height: 100%;">
                      <div v-if="!isRollback">
                        <div class="avatar-circle left-circle" v-on:click="updateResource(item, 'up')">
                          <md-icon>
                            add_circle_outline
                          </md-icon>
                        </div>
                        <div class="avatar-circle right-circle" v-on:click="updateResource(item, 'down')">
                          <md-icon>
                            remove_circle_outline
                          </md-icon>
                        </div>
                      </div>
                      <div class="sizing">{{item.size}}</div>
                    </div>
                  </md-avatar>
                </md-layout>

                <!--디스크립션 부분-->
                <div v-if="!item.diff" style="text-align: center">
                  <span class="md-caption" style="font-weight: bold">{{item.title}}</span>
                  <br>
                  <div v-if="item.type == 'instances' && deploy">
                    <service-progress fullWidth :app="marathonApp"></service-progress>
                  </div>
                  <div v-else>
                    <span class="md-caption">{{item.subTitle}}</span>
                  </div>
                </div>
                <div v-else>
                  <md-layout md-align="center">
                    <md-button v-on:click="updateDeploy" class="md-raised md-primary">저장
                    </md-button>
                    <md-button v-on:click="updateCancel" class="md-raised">재설정
                    </md-button>
                  </md-layout>
                </div>

              </md-card-content>
            </md-card-area>
          </md-card>
        </md-layout>
      </md-layout>
    </div>
    <md-layout v-else-if="marathonApps" style="height: 200px">
      <md-card md-with-hover style="width: 100%;">
        <md-card-area>
          <md-card-content style="text-align: center">
            현재 영역에 배포중인 어플리케이션이 없습니다. <a v-on:click="moveDeployment">배포 메뉴</a> 에서
            어플리케이션을 배포하세요.
          </md-card-content>
        </md-card-area>
      </md-card>
    </md-layout>
    <md-layout v-else style="height: 200px">
      <md-card md-with-hover style="width: 100%;">
        <md-card-area>
          <md-card-content style="text-align: center">
            <md-spinner :md-size="60" md-indeterminate></md-spinner>
          </md-card-content>
        </md-card-area>
      </md-card>
    </md-layout>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      categoryItem: Object,
      deploy: {
        type: Boolean,
        default: function () {
          return true;
        }
      },
      isRollback: Boolean,
      marathonApps: Object,
      deployJson: Object
    },
    data() {
      return {
        stageDeployJson: null,
        marathonApp: null,
        isActive: false,
        items: [],
        resourceUpdated: false,
        instances: 0,
        mem: 0,
        cpus: 0
      }
    },
    mounted() {
      var me = this;
      this.makeItems();
    },
    watch: {
      isRollback: function (val) {
        this.makeItems();
      },
      stage: function () {
        this.resourceUpdated = false;
        this.makeItems();
      },
      devApp: {
        handler: function (newVal, oldVal) {
          this.makeItems();
        },
        deep: true
      },
      categoryItem: {
        handler: function (newVal, oldVal) {
          this.makeItems();
        },
        deep: true
      },
      marathonApps: {
        handler: function (newVal, oldVal) {
          this.makeItems();
        },
        deep: true
      },
      deployJson: {
        handler: function (newVal, oldVal) {
          this.makeItems();
        },
        deep: true
      }
    },
    methods: {
      moveDeployment: function () {
        var me = this;
        this.$router.push(
          {
            name: 'appsDetailDeployment',
            params: {appName: me.appName}
          }
        )
      },
      updateCancel: function () {
        this.resourceUpdated = false;
        this.makeItems();
      },
      updateDeploy: function () {
        var me = this;
        var stageDeployJsonCopy = JSON.parse(JSON.stringify(me.stageDeployJson));
        stageDeployJsonCopy.instances = me.instances;
        stageDeployJsonCopy.mem = me.mem;
        stageDeployJsonCopy.cpus = me.cpus;

        var deployJsonCopy = JSON.parse(JSON.stringify(me.deployJson));
        deployJsonCopy[me.stage] = stageDeployJsonCopy;
        if (!me.deploy) {
          me.resourceUpdated = false;
          me.$emit('update:deployJson', deployJsonCopy);
        } else {
          me.updateDeployJson(me.appName, me.stage, stageDeployJsonCopy, function (response) {
            me.resourceUpdated = false;
            //스테이지 디플로이
            me.deployApp(me.appName, me.stage, null, function (response) {

            });
          });
        }
      },
      updateResource: function (item, updown) {
        var me = this;
        var size = item.size;
        switch (item.type) {
          case 'instances':
            if (updown == 'up') {
              me.instances++;
            } else {
              if (me.instances > 0) {
                me.instances--;
              }
            }
            break;
          case 'mem':
            if (updown == 'up') {
              me.mem = me.mem + 128;
            } else {
              if (me.mem >= 256) {
                me.mem = me.mem - 128;
              }
            }
            break;
          case 'cpus':
            if (updown == 'up') {
              me.cpus = parseFloat((me.cpus + 0.1).toFixed(1));
            } else {
              if (me.cpus > 0.1) {
                me.cpus = parseFloat((me.cpus - 0.1).toFixed(1));
              }
            }
            break;
        }
        this.makeItems();
      },
      makeItems: function () {
        var me = this;
        if (!me.categoryItem || !me.devApp) {
          return;
        }

        me.stageDeployJson = me.deployJson[me.stage];

        var marathonAppId;
        if (this.isRollback) {
          me.marathonApp = me.marathonApps.oldProd.app;
        } else {
          me.marathonApp = me.marathonApps[me.stage].app;
        }

        //최초 리소스 로딩인경우 값 배정
        if (!me.resourceUpdated) {
          me.instances = me.stageDeployJson.instances;
          me.mem = me.stageDeployJson.mem;
          me.cpus = me.stageDeployJson.cpus;
          me.resourceUpdated = true;
        }

        me.items = [
          {
            image: me.categoryItem.logoSrc,
            title: me.categoryItem.type,
            subTitle: me.categoryItem.title,
            type: 'profile',
            size: 0,
          },
          {
            image: me.categoryItem.logoSrc,
            title: '인스턴스',
            subTitle: '',
            type: 'instances',
            size: me.isRollback ? me.marathonApp.instances : me.instances,
            diff: me.isRollback ? false : me.stageDeployJson.instances != me.instances
          },
          {
            image: me.categoryItem.logoSrc,
            title: '인스턴스당 메모리(MB)',
            subTitle: '',
            type: 'mem',
            size: me.isRollback ? me.marathonApp.mem : me.mem,
            diff: me.isRollback ? false : me.stageDeployJson.mem != me.mem
          },
          {
            image: me.categoryItem.logoSrc,
            title: '인스턴스당 CPU',
            subTitle: '',
            type: 'cpus',
            size: me.isRollback ? me.marathonApp.cpus : me.cpus,
            diff: me.isRollback ? false : me.stageDeployJson.cpus != me.cpus
          }
        ]
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-avatar {
    width: 120px;
    min-width: 120px;
    height: 120px;
    min-height: 120px;
    margin: auto;
    display: inline-block;
    overflow: hidden;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    position: relative;
    border-radius: 50%;
    vertical-align: middle;
    border: 1px solid #5480B7;

    .sizing {
      line-height: 110px;
      text-align: center;
      width: 100%;
      height: 100%;
      font-size: 35px;
    }

    .left-circle {
      left: 0px;
    }
    .right-circle {
      right: 0px;
      left: auto;
    }
    .avatar-circle {
      position: absolute;
      top: 50%;
      .md-icon {
        background: white;
      }
    }
    .avatar-circle:hover {
      color: #4071AF;
    }
  }

  .diff {
    background: #DFE6EB;
  }
</style>
