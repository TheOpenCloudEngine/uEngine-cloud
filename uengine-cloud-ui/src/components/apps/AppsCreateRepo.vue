<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <div style="width: 100%">
      <div class="bold">레파지토리 위치:</div>
      <md-layout>
        <md-radio v-model="repoType" :mdValue="'gitlab'">
          <md-tooltip md-direction="bottom">Gitlab 에 프로젝트를 생성합니다.</md-tooltip>
          <span class="md-caption">Gitlab</span>
        </md-radio>
        <md-radio v-model="repoType" :mdValue="'github'">
          <md-tooltip md-direction="bottom">Github 에 프로젝트를 생성합니다.</md-tooltip>
          <span class="md-caption">Github</span>
        </md-radio>
      </md-layout>
    </div>
    <div style="width: 100%">
      <md-layout md-gutter="16">
        <md-layout>
          <div style="width: 100%" class="bold">레파지토리 패스:</div>
          <md-layout>
            <md-layout>
              <md-input-container>
                <label>host</label>
                <md-input type="text" v-model="gitHost" required readonly/>
              </md-input-container>
            </md-layout>
            <md-layout>
              <md-input-container>
                <!--내 기본패스도 추가-->
                <label>namespace</label>
                <md-select v-model="namespace" required>
                  <md-option v-for="(namespace, index) in namespaces"
                             :value="namespace.value"
                  >
                    {{namespace.name}}
                  </md-option>
                </md-select>
              </md-input-container>
            </md-layout>
          </md-layout>
        </md-layout>
        <md-layout>
          <div style="width: 100%" class="bold">레파지토리 이름:</div>
          <md-input-container>
            <md-input type="text" v-model="repositoryName" required/>
            <label>name</label>
          </md-input-container>
        </md-layout>
      </md-layout>
    </div>

    <github-token-editor ref="github-token-editor"></github-token-editor>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      repo: Object,
      appEnv: Object
    },
    data() {
      return {
        namespaces: [],
        usernamespase: null,
        defaultAppName: null,
        gitHost: null,
        gitlabHost: window.config.gitlab.host + '/',
        githubHost: 'https://github.com/',
        orgs: [],
        groups: [],
        repoType: 'gitlab',
        namespace: null,
        repositoryName: null
      }
    },
    mounted() {
      var me = this;
      me.gitHost = me.gitlabHost;
      me.makeGitlabNameSpaces();
    },
    watch: {
      appEnv: {
        handler: function (val) {
          if (val) {
            this.repositoryName = val.appName;
          }
        },
        deep: true
      },
      repoType: function (val) {
        var me = this;
        if (val == 'gitlab') {
          me.gitHost = me.gitlabHost;
          me.makeGitlabNameSpaces();
        } else {
          me.gitHost = me.githubHost;
          me.makeGithubNameSpaces();
        }
        this.updateAppRepo();
      },
      repositoryName: function () {
        this.updateAppRepo();
      },
      namespace: function () {
        this.updateAppRepo();
      }
    }
    ,
    methods: {
      updateAppRepo: function () {
        var me = this;
        var repo = {
          repoType: me.repoType,
          repositoryName: me.repositoryName,
          namespace: me.namespace
        }
        var invalid = false;
        for (var key in repo) {
          if (!repo[key] || repo[key].length < 1) {
            invalid = true;
          }
        }
        if (invalid) {
          me.$emit('update:repo', null);
        } else {
          //namespace return null if not org namespace
          if (repo.namespace == me.usernamespase) {
            repo.namespace = null;
          }
          me.$emit('update:repo', repo);
        }
      },
      makeGitlabNameSpaces: function () {
        var me = this;
        me.namespaces = [];
        me.$root.gitlab('api/v4/users/' + localStorage['gitlab-id']).get()
          .then(function (response) {
            //get user
            me.usernamespase = response.data.username;
            me.namespace = response.data.username;
            me.namespaces.push({
              name: response.data.username,
              value: response.data.username
            })
          })
        me.getGroupsIncludeMe(localStorage['gitlab-id'], function (groups) {
          if (groups) {
            me.groups = groups;
            me.groups.forEach(function (group, i) {
              me.namespaces.push({
                name: group.path,
                value: group.path
              })
            })
          }
        });
      },
      makeGithubNameSpaces: function () {
        var me = this;
        me.namespaces = [];
        me.$refs['github-token-editor'].validate(function (response) {
          //get user
          me.usernamespase = response.data.login;
          me.namespace = response.data.login;
          me.namespaces.push({
            name: response.data.login,
            value: response.data.login
          })

          //get orgs
          me.$root.github('user/orgs').get()
            .then(function (response) {
              me.orgs = response.data;
              me.orgs.forEach(function (org, i) {
                me.namespaces.push({
                  name: org.login,
                  value: org.login
                })
              })
            })
        })
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-input-container {
    padding-top: 16px;
    background: transparent;
  }

  .md-input-container label {
    top: 0px;
  }
</style>
