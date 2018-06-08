<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div v-if="categoryItem" style="padding: 20px">
    <span class="md-title">{{categoryItem.header}}</span>
    <br>
    <br>
    <div class="header-top-line"></div>
    <br>
    <br>

    <md-layout :md-gutter="16">
      <md-layout md-flex="30">
        <div>
          <span class="md-subheading">{{categoryItem.title}}</span>
          <br><br><br>
          <span class="md-caption">{{categoryItem.description}}</span>
          <br><br><br>
          <a>문서 보기</a>
          <br><br><br>
          <table style="width: 100%">
            <tr>
              <td>버전</td>
              <td>{{categoryItem.version}}</td>
            </tr>
            <tr>
              <td>타입</td>
              <td>{{categoryItem.type}}</td>
            </tr>
          </table>
        </div>
      </md-layout>

      <md-layout md-flex="70">
        <!--Manage exist github-->
        <md-stepper v-if="categoryItemId == 'github'">
          <md-step
            :md-editable="true"
            md-label="Github"
            :md-error="!githubSelected()"
            :md-continue="githubSelected()"
            :md-message="githubRepoName ? githubRepoName : 'Git 레파지토리를 선택하세요'">
            <apps-create-git
              :githubRepoId.sync="githubRepoId"
              :githubRepoName.sync="githubRepoName"
            ></apps-create-git>
          </md-step>
          <md-step
            :md-disabled="!githubSelected()"
            :md-error="!appEnvSelected()"
            :md-continue="appEnvSelected()"
            md-message="앱 정보를 기입하세요."
            md-label="Environment">
            <apps-create-env
              :env.sync="appEnv"
            ></apps-create-env>
          </md-step>
        </md-stepper>

        <!--Import git url-->
        <md-stepper v-else-if="categoryItemId == 'import'">
          <md-step
            md-label="Git url"
            :md-error="!importGitUrlSelected()"
            :md-continue="importGitUrlSelected()"
            md-message="임포트할 Git 주소가 필요합니다">
            <md-input-container :class="{'md-input-invalid': !importGitUrlSelected()}">
              <md-input type="text" v-model="importGitUrl" required/>
              <label>Git Url</label>
            </md-input-container>
          </md-step>
          <md-step
            :md-disabled="!importGitUrlSelected()"
            :md-error="!appEnvSelected()"
            :md-continue="appEnvSelected()"
            md-message="앱 정보를 기입하세요."
            md-label="Environment">
            <apps-create-env
              :env.sync="appEnv"
            ></apps-create-env>
          </md-step>
          <md-step
            :md-disabled="!importGitUrlSelected() || !appEnvSelected()"
            :md-error="!appRepoSelected()"
            :md-continue="appRepoSelected()"
            md-message="레파지토리 정보를 기입하세요."
            md-label="Repository">
            <apps-create-repo
              :repo.sync="appRepo"
            ></apps-create-repo>
          </md-step>
        </md-stepper>


        <md-stepper v-else>
          <md-step :md-disabled="!appEnvSelected()"
                   :md-continue="appEnvSelected()"
                   md-message="앱 정보를 기입하세요."
                   md-label="Environment">
            <apps-create-env
              :env.sync="appEnv"
            ></apps-create-env>
          </md-step>
          <md-step :md-disabled="!mailValid"
                   :md-continue="mailValid"
                   md-message="레파지토리 정보를 기입하세요."
                   md-label="Repository">
            <p>This seems something important I need to fix just right before the last step.</p>
          </md-step>
        </md-stepper>
      </md-layout>
    </md-layout>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {},
    data() {
      return {
        appEnv: null,
        githubRepoId: null,
        githubRepoName: null,
        importGitUrl: null,
        appRepo: null,

        mailValid: false,
        defaultHost: window.config['default-host'],
        categoryItem: null,
        cpu: 0.4,
        mem: 512,
        instances: 1,
        namespace: "",
        groups: [],
        projectId: "",
        projects: [],
        existRepository: false,
        appName: null,
        externalProdDomain: null,
        externalStgDomain: null,
        externalDevDomain: null,
        internalProdDomain: null,
        internalStgDomain: null,
        internalDevDomain: null,
        prodPort: null,
        stgPort: null,
        devPort: null,
        invalidAppName: false,
      }
    },
    mounted() {
      var me = this;
      this.getCategoryItem(me.categoryItemId, function (item) {
        me.categoryItem = item;
      });
    },
    watch: {}
    ,
    methods: {
      appRepoSelected: function () {
        return this.appRepo != null;
      },
      importGitUrlSelected: function () {
        return this.importGitUrl != null && this.importGitUrl.length > 0;
      },

      appEnvSelected: function () {
        return this.appEnv != null;
      },
      githubSelected: function () {
        return this.githubRepoId && this.githubRepoId > 0;
      },

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
