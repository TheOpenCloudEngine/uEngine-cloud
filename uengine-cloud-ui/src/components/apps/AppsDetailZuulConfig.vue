<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout class="bg-white">
      <div class="header-top-line"></div>
      <div style="width: 100%">
        <md-layout>
          <md-layout>
            <md-card md-with-hover style="width: 100%;">
              <md-card-area>
                <md-card-content>
                  <md-layout>
                    <md-layout>
                      <md-checkbox v-model="zuul.addCorsHeaders">CORS Header</md-checkbox>
                      <md-checkbox v-model="zuul.addIamFilters">IAM 인증필터</md-checkbox>
                      <md-checkbox v-model="zuul.addProxyHeaders">Proxy Header 추가</md-checkbox>
                    </md-layout>
                    <md-button class="md-primary md-raised" v-on:click="saveConfig">Zuul Config 저장</md-button>
                  </md-layout>

                  <div v-if="zuul.addIamFilters" class="add-input mt10">
                    <h3>IAM 인증 필터</h3>
                    <div>Host:</div>
                    <div>
                      <md-input-container>
                        <md-input v-model="iam.host"></md-input>
                      </md-input-container>
                    </div>
                    <div class="md-subheading" style="margin-bottom: 20px;">Admin</div>
                    <md-layout>
                      <md-layout md-flex="20">
                        UserName :
                      </md-layout>
                      <md-layout md-flex="80">
                        <md-input-container>
                          <md-input v-model="iam.userName"></md-input>
                        </md-input-container>
                      </md-layout>
                    </md-layout>
                    <md-layout>
                      <md-layout md-flex="20">
                        Password :
                      </md-layout>
                      <md-layout md-flex="80">
                        <md-input-container>
                          <md-input v-model="iam.password" type="password"></md-input>
                        </md-input-container>
                      </md-layout>
                    </md-layout>
                  </div>

                  <div class="add-input mt10" v-for="(route,index) in routes">
                    <button style="border: hidden; background-color: inherit;float: right;"
                            v-on:click="routes.splice(index,1)"><b>X</b></button>
                    <md-layout>
                      <md-layout md-flex="20">
                        <h3>라우트 : </h3>
                      </md-layout>
                      <md-layout md-flex="80">
                        <md-input-container>
                          <md-input v-model="route.routeName"></md-input>
                        </md-input-container>
                      </md-layout>
                    </md-layout>
                    <div>Path Name</div>
                    <md-input-container>
                      <md-input v-model="route.path"></md-input>
                    </md-input-container>

                    <md-radio v-model="route.routeWay" mdValue="serviceID">Service ID로 설정</md-radio>
                    <md-radio v-model="route.routeWay" mdValue="url">URL로 설정</md-radio>


                    <div v-if="route.routeWay!='url'">
                      <div>Service ID</div>
                      <md-input-container>
                        <md-select v-model="route.serviceId">
                          <md-option value="">== 선택 ==</md-option>
                          <md-option v-for="serviceId in serviceIds" :value="serviceId">{{serviceId}}</md-option>
                        </md-select>
                      </md-input-container>
                    </div>
                    <div v-if="route.routeWay=='url'">
                      <div>URL</div>
                      <md-input-container>
                        <md-input v-model="route.url"></md-input>
                      </md-input-container>
                    </div>
                    <div>Required-Scopes:</div>
                    <div>
                      <md-input-container>
                        <md-input v-model="route['required-scopes']"></md-input>
                      </md-input-container>
                    </div>
                    <div style="width:100%;">
                      <md-checkbox v-model="route.stripPrefix">Strip prefix</md-checkbox>
                    </div>
                  </div>
                  <div>
                    <md-button class="md-primary md-raised"
                               v-on:click="routes.push({routeWay: 'serviceID'})"
                               style="margin-bottom: 5%;margin-top: 20px;">
                      라우트 추가
                    </md-button>
                  </div>

                  <codemirror
                    ref="vcapRef"
                    :options="{
              theme: 'dracula',
              mode: 'yaml',
              extraKeys: {'Ctrl-Space': 'autocomplete'},
              lineNumbers: true,
              lineWrapping: true,
            }"
                    :value="zuulConfigCode"
                    v-on:change="editorToObject"></codemirror>

                </md-card-content>
              </md-card-area>
            </md-card>
          </md-layout>
        </md-layout>
      </div>
    </md-layout>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  var YAML = require('js-yaml');

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      catalogItem: Object,
    },
    data() {
      var me = this;
      return {
        routes: [],
        serviceIds: null,
        codeChanged: false,
        zuulConfigCode: '',
        zuul: {},
        iam: {},
        model: null,
        separate: {
          zuul: function (val) {
            var copy = YAML.load(YAML.dump(val));
            me.routes = [];
            for (var key in copy.routes) {
              copy.routes[key].routeName = key;
              if (copy.routes[key].serviceId) {
                copy.routes[key].routeWay = "serviceID"
              } else {
                copy.routes[key].routeWay = "url"
              }
              me.routes.push(copy.routes[key]);
            }
            me.zuul = copy;
          },
          iam: function (val) {
            var copy = YAML.load(YAML.dump(val));
            me.iam = iam;
          },
        },
        //조합
        combine: {
          zuul: function (val) {
            var copy = YAML.load(YAML.dump(val));
            if (copy.addCorsHeaders != undefined) {
              me.model.zuul.addCorsHeaders = copy.addCorsHeaders;
            }
            if (copy.addIamFilters != undefined) {
              me.model.zuul.addIamFilters = copy.addIamFilters;
              if (!copy.addIamFilters) {
                delete me.model.iam;
              }
            }
            if (copy.addProxyHeaders != undefined) {
              me.model.zuul.addProxyHeaders = copy.addProxyHeaders;
            }
            for (var key in copy.routes) {
              if (copy.routes[key].routeWay == 'url') {
                delete copy.routes[key].serviceId;
              } else {
                delete copy.routes[key].url;
              }
              delete copy.routes[key].routeWay;
              delete copy.routes[key].routeName;
              if (me.model.zuul.routes && key != undefined) {
                me.model.zuul.routes[key] = copy[key];
              }
            }

          },
          iam: function (val) {
            var copy = JSON.parse(JSON.stringify(val));
            if (Object.keys(copy).length) {
              var admin = {};
              for (var key in copy) {
                if (key != 'host') {
                  admin[key] = copy[key];
                  delete copy[key];
                }
              }
              if (Object.keys(admin).length) {
                copy.admin = admin;
              }
              me.model.iam = copy;
            }
          },
          routes: function (val) {
            var copy = YAML.load(YAML.dump(val));
            var routesObj = {};
            for (var i in copy) {
              if (copy[i].routeWay == 'url') {
                delete copy[i].serviceId;
              } else {
                delete copy[i].url;
              }
              if (copy[i].routeName != undefined && Object.keys(copy[i]).length > 2) {
                routesObj[copy[i].routeName] = copy[i];
              }
              delete copy[i].routeWay;
              delete copy[i].routeName;

            }
            if (Object.keys(routesObj).length) {
              me.model.zuul.routes = routesObj;
            } else {
              delete me.model.zuul.routes;
            }

          },
        }
      }
    },
    mounted() {
      var me = this;
      this.$root.eureka('apps').get()
        .then(function (response) {
          response.data.applications.application.forEach(function (application) {
            if (me.serviceIds == null) me.serviceIds = [];
            me.serviceIds.push(application.name.toLowerCase());
          });
        });
      me.getDevAppConfigYml(me.appName, me.stage, function (response) {
        me.model = YAML.load(response.data);
        me.zuulConfigCode = response.data;
        me.separation();
      });

    },
    watch: {
      routes: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      iam: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      zuul: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
    },
    methods: {
      editorToObject: function (text) {
        this.codeChanged = true;
        this.model = YAML.load(text);
        this.separation();
      },
      combination: function () {
        if (this.working) {
          return;
        }
        this.working = true;
        console.log('combination Start!!');

        this.combine.iam(this.iam);
        this.combine.zuul(this.zuul);

        this.combine.routes(this.routes);

        this.zuulConfigCode = YAML.dump(this.model, null, 2);

        this.$nextTick(function () {
          this.working = false;
        });
      }
      ,
      /**
       * 모델을 분리한다.
       **/
      separation: function () {
        if (this.working) {
          return;
        }
        this.working = true;
        console.log('separation Start!!', this.model);

        if (this.model.iam) {
          this.separate.iam(this.model.iam);
        }
        this.separate.zuul(this.model.zuul);


        this.$nextTick(function () {
          this.working = false;
        });
      },
      getCodes: function () {
        var me = this;
        me.codeChanged = false;
        me.getDevAppConfigYml(me.appName, me.stage, function (response) {
          me.zuulConfigCode = response.data;
        });
      },
      saveConfig: function () {
        var me = this;
        var jsonObj = "";
        jsonObj = JSON.stringify(YAML.load(me.zuulConfigCode));
        // config저장
        me.updateDevAppConfigYml(me.appName, me.stage, me.zuulConfigCode, function (response) {
          me.codeChanged = false;
          me.getCodes();
        })
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .add-input {
    width: 95%;
    min-height: 100px;
    border: solid 1px #cccccc;
    border-radius: 5px;
    padding: 10px;
  }

  .add-input:hover {
    width: 95%;
    min-height: 100px;
    border: solid 2px #4A78B3;
    border-radius: 5px;
    padding: 10px;
  }

  /*.exclamation {*/
  /*width: 25px;*/
  /*height: 25px;*/
  /*text-align: center;*/
  /*margin-top: 10px;*/
  /*color: #CD0000;*/
  /*border: solid #CD0000 2px;*/
  /*border-radius: 20px;*/
  /*}*/

  /*.exclamation:hover {*/
  /*width: 25px;*/
  /*height: 25px;*/
  /*text-align: center;*/
  /*margin-top: 10px;*/
  /*color: #CD7E83;*/
  /*border: solid #CD7E83 2px;*/
  /*border-radius: 20px;*/
  /*}*/
</style>
