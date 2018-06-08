<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <div>

    </div>
    <div>
      <md-input-container>
        <md-input type="text" v-model="namespace" required/>
        <label>Git Url</label>
      </md-input-container>
    </div>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      repo: Object
    },
    data() {
      return {
        orgs: [],
        groups: [],
        repoType: 'gitlab',
        namespace: null,
        repositoryName: null
      }
    },
    mounted() {
      var me = this;
      //TODO load each object when switch repo type.
      //validate github token when click github repo type.
      me.getGroupsIncludeMe(localStorage['gitlab-id'], function (groups) {
        if (groups) {
          me.groups = groups;
        }
      });
      me.$root.github('user/orgs').get()
        .then(function (response) {
          me.orgs = response.data;
        })
    },
    watch: {
      appName: function (val) {
        var me = this;
        if (!val) {
          val = '';
        }
        this.internalProdDomain = 'marathon-lb-internal.marathon.mesos:port';
        this.internalStgDomain = 'marathon-lb-internal.marathon.mesos:port';
        this.internalDevDomain = 'marathon-lb-internal.marathon.mesos:port';
        this.externalProdDomain = val + '.' + this.defaultHost;

        var special_pattern = /[_`~!@#$%^&*|\\\'\";:\/?]/gi;
        if (special_pattern.test(val) == true) {
          this.invalidAppName = true;
        }
        else if (val.indexOf(' ') != -1) {
          this.invalidAppName = true;
        }
        else {
          this.invalidAppName = false;
        }
      },
      externalProdDomain: function (val) {
        if (val) {
          let split = val.split('.');
          let subDomain = split[0];
          if (subDomain && subDomain.length > 0) {
            var left = val.substring(subDomain.length, val.length);
            this.externalStgDomain = subDomain + '-stg' + left;
            this.externalDevDomain = subDomain + '-dev' + left;
          }
        } else {
          this.externalStgDomain = '';
          this.externalDevDomain = '';
        }
      }
    }
    ,
    methods: {

      //steps =>
      //existGithub: list / env
      //importProject: import / env / repository
      //template : env / repository

      create: function () {
        var me = this;
        var appCreate = {
          categoryItemId: me.categoryItemId,
          cpu: me.cpu,
          mem: me.mem,
          instances: me.instances,
          appNumber: me.appNumber,
          projectId: me.projectId,
          appName: me.appName,
          externalProdDomain: me.externalProdDomain,
          externalStgDomain: me.externalStgDomain,
          externalDevDomain: me.externalDevDomain,
          namespace: me.namespace
        };
        me.createApp(appCreate, function (response) {
          if (response) {
            me.$router.push(
              {
                name: 'appsDetail',
                params: {appName: me.appName}
              }
            );
          }
        });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
