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
                      <md-checkbox v-model="zuulObject.zuul.addCorsHeaders">CORS Header</md-checkbox>
                      <md-checkbox v-model="zuulObject.zuul.addIamFilters">IAM 인증필터</md-checkbox>
                      <md-checkbox v-model="zuulObject.zuul.addProxyHeaders">Proxy Header 추가</md-checkbox>
                    </md-layout>
                    <md-button class="md-primary md-raised">Zuul Config 저장</md-button>
                  </md-layout>

                  <div v-if="zuulObject.zuul.addIamFilters" class="add-input mt10">
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
                          <md-input v-model="iam.admin.userName"></md-input>
                        </md-input-container>
                      </md-layout>
                    </md-layout>
                    <md-layout>
                      <md-layout md-flex="20">
                        Password :
                      </md-layout>
                      <md-layout md-flex="80">
                        <md-input-container>
                          <md-input v-model="iam.admin.password" type="password"></md-input>
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
      catalogItem: Object
    },
    data() {
      return {
        routes: [{routeWay: "serviceID"}],
        serviceIds: null,
        addProxyHeaders: false,
        addCorsHeaders: false,
        addIamFilters: false,
        zuulConfigCode: '',
        configObject: {},
        zuulObject: {zuul: {routes: {}}},
        iam: {admin: {}},
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
      var yamlObj = {};
      me.getDevAppConfigYml(me.appName, me.stage, function (response) {
        yamlObj = YAML.load(response.data);
        me.configObject = yamlObj;
        me.objectToCode(yamlObj);
        me.editorToObject(response.data);
      });

    },
    watch: {
      routes: {
        handler: function (newVal, oldVal) {
          var copy = newVal;
          var zuulObj = {};
          $.each(copy, function (i, value) {
            zuulObj[copy[i].routeName] = JSON.parse(JSON.stringify(value));
            delete zuulObj[copy[i].routeName].routeName;
            delete zuulObj[copy[i].routeName].routeWay;
            if (copy[i].routeWay == 'url') {
              delete zuulObj[copy[i].routeName].serviceId;
            } else {
              delete zuulObj[copy[i].routeName].url;
            }
          });
          this.zuulObject.zuul.routes = zuulObj;
        },
        deep: true
      },
      zuulObject: {
        handler: function (newVal, oldVal) {
          this.configObject.zuul = newVal.zuul;
          this.objectToCode(this.configObject);
        },
        deep: true
      },
      configObject: {
        handler: function (newVal, oldVal) {
          console.log("configObject", newVal);
          this.objectToCode(newVal);
        },
        deep: true
      },
      iam: {
        handler: function (newVal, oldVal) {

          var copy = JSON.stringify(newVal);
          this.configObject.iam = JSON.parse(copy);
          this.objectToCode(this.configObject);
        },
        deep: true
      },
    },
    methods: {
      objectToCode: function (data) {
        this.zuulConfigCode = YAML.dump(data);
      },
      editorToObject: function (text) {
        var me = this;
        var yamlObj = YAML.load(text);
        this.zuulObject.zuul = yamlObj.zuul;
        this.routes = yamlObj.zuul.routes;
        for (var key in yamlObj.zuul.routes) {
          me.routes[key].routeName = key;
          if (me.routes[key].serviceId) {
            me.routes[key].routeWay = "serviceID"
          } else {
            me.routes[key].routeWay = "url"
          }
        }
      },
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
</style>
