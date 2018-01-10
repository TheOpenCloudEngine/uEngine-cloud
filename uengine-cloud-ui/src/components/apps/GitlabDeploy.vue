<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#open" md-close-to="#open" ref="open">
    <!--<md-dialog-title v-if="force">-->
    <!--서비스가 현재 하나 이상의 배포에 의해 잠겨 있습니다.<br>-->
    <!--버튼을 다시 누르면 새 구성을 강제로 변경하고 배포합니다.-->
    <!--</md-dialog-title>-->
    <div v-if="role == 'deploy'">
      <md-dialog-title>{{stageName}} 환경 앱 배포</md-dialog-title>
      <md-dialog-content>
        <md-stepper style="min-width: 800px"
                    :md-elevation="elevation"
                    v-on:completed="onCompleted"
        >
          <md-step>
            <div style="text-align: center">
              <span class="md-body-1">배포할 브런치 또는 태그를 선택하세요.</span>
              <md-layout md-align="center">
                <md-radio v-model="refType" :mdValue="'branch'">
                  <span class="md-caption">브런치</span>
                </md-radio>
                <md-radio v-model="refType" :mdValue="'tag'">
                  <span class="md-caption">태그</span>
                </md-radio>
              </md-layout>
            </div>
            <div v-if="refType == 'branch'">
              <md-input-container>
                <md-select v-model="selectedRef">
                  <md-option v-for="branch in branchList" :value="branch.name">{{branch.name}}</md-option>
                </md-select>
              </md-input-container>
            </div>
            <div v-if="refType == 'tag'">
              <md-input-container>
                <md-select v-model="selectedRef">
                  <md-option v-for="tag in tagList" :value="tag.name + ':' + tag.commit.id">{{tag.name}}</md-option>
                </md-select>
              </md-input-container>
            </div>
          </md-step>
          <md-step>
            <div style="text-align: center" v-if="!hasImage">
              <span class="md-body-1"
                    v-if="refType == 'branch'">{{selectedRef}} 브런치 를 선택하셨습니다. 배포 작업은 소스코드 빌드부터 진행됩니다.</span>
              <span class="md-body-1"
                    v-if="refType == 'tag'">{{selectedRefTag}} 태그 를 선택하셨습니다. 사전 빌드된 도커 이미지가 없으므로 배포 작업은 소스코드 빌드부터 진행됩니다.</span>
            </div>
            <div style="text-align: center" v-if="hasImage">
              <span class="md-body-1">{{selectedRefTag}} 태그 를 선택하셨습니다. 기존 빌드된 도커 이미지가 있습니다.</span>
              <md-layout md-align="center">
                <md-radio v-model="usePreImage" :mdValue="true">
                  <span class="md-caption">기존 빌드된 도커 이미지를 사용하기.</span>
                </md-radio>
                <md-radio v-model="usePreImage" :mdValue="false">
                  <span class="md-caption">처음부터 빌드하기</span>
                </md-radio>
              </md-layout>
            </div>
          </md-step>
          <md-step>
            <span class="md-body-1">런타임 환경을 설정합니다.</span>
            <div v-if="preStage">
              <md-layout md-align="center">
                <md-radio v-model="usePreStage" :mdValue="true">
                  <span class="md-body-1">{{preStage}} 런타임 환경을 그대로 사용하기.</span>
                </md-radio>
                <md-radio v-model="usePreStage" :mdValue="false">
                  <span class="md-body-1">런타임 환경을 새로 설정하기.</span>
                </md-radio>
              </md-layout>
            </div>
            <div v-if="!usePreStage || !preStage">
              <app-runtime-card v-if="copyDevApp"
                                :stage="stage"
                                :devApp.sync="copyDevApp"
                                :catalogItem="catalogItem"
                                :gitlabDeploy="true"
              ></app-runtime-card>
            </div>
          </md-step>
        </md-stepper>
      </md-dialog-content>
    </div>
  </md-dialog>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {
      stage: String,
      devApp: Object,
      catalogItem: Object,
      appName: String,
      role: String
    },
    computed: {
      stageName: function () {
        if (this.stage == 'dev') {
          return '개발'
        }
        else if (this.stage == 'stg') {
          return '스테이징'
        }
        else if (this.stage == 'prod') {
          return '프로덕션'
        }
      }
    },
    data() {
      return {
        copyDevApp: null,
        elevation: 0,
        refType: 'branch',
        selectedRef: null,
        selectedRefBranch: null,
        selectedRefTag: null,
        selectedRefCommit: null,
        branchList: [],
        tagList: [],
        imageList: [],
        hasImage: false,
        usePreImage: true,
        preStage: null,
        usePreStage: true,
        updatedApp: null
      }
    },
    mounted(){

    },
    watch: {
      usePreImage: function (val) {

      },
      selectedRef: function (val) {
        //selectedRef
        var me = this;
        me.hasImage = false;
        if (me.refType == 'tag') {
          var split = val.split(':');
          var commitId = split[1];
          me.selectedRefTag = split[0];
          me.selectedRefCommit = split[1];
          if (me.imageList.indexOf(commitId) != -1) {
            me.hasImage = true;
          }
          me.selectedRefBranch = null;
        } else {
          me.selectedRefBranch = val;
          me.selectedRefTag = null;
        }
      },
      refType: function (val) {
        this.selectedRef = null;
      }
    }
    ,
    methods: {
      onCompleted: function () {
        var me = this;
        var appData = JSON.parse(JSON.stringify(me.copyDevApp));
        //dev 일 경우, 런타임 환경을 수정한 경우 데이터 교체.
        if (me.stage == 'dev') {
          if (me.updatedApp) {
            appData = me.updatedApp;
          }
        }
        //그 외의 경우
        else {
          //전단계의 런타임을 그대로 사용하는 경우
          if (me.usePreStage) {
            var deployJson = JSON.parse(JSON.stringify(appData[me.preStage]['deploy-json']));
            appData[me.stage]['deploy-json'] = deployJson;
          }
          //신규 런타임을 구성하는 경우
          else {
            if (me.updatedApp) {
              appData = me.updatedApp;
            }
          }
        }

        //기존 이미지를 활용하는 경우
        if (me.hasImage && me.usePreImage) {
          me.updateDevApp(me.appName, appData, function (response) {
            //스테이지 디플로이
            //commit
            me.runDeployedApp(me.appName, me.stage, me.selectedRefCommit, function (response) {

            });
          });
        }
        //코드 빌드부터 시작하는 경우
        else {
          me.updateDevApp(me.appName, appData, function (response) {
            var ref = me.selectedRefBranch ? me.selectedRefBranch : me.selectedRefTag;
            me.excutePipelineTrigger(me.appName, ref, me.stage, function (response) {

            })
          });
        }
      },
      updateRefs: function () {
        var me = this;
        this.selectedRef = null;
        this.tagList = [];
        this.branchList = [];
        this.hasImage = false;

        var projectId = me.copyDevApp.gitlab.projectId;
        var p1 = this.$root.gitlab('api/v4//projects/' + projectId + '/repository/tags').get();
        var p2 = this.$root.gitlab('api/v4//projects/' + projectId + '/repository/branches').get();
        var p3 = this.$root.backend('app/' + me.appName + '/tags').get();

        Promise.all([p1, p2, p3])
          .then(function ([r1, r2, r3]) {
            me.tagList = r1.data;
            me.branchList = r2.data;
            me.imageList = r3.data.tags;
          });
      },
      open() {
        this.elevation = 1;
        this.copyDevApp = JSON.parse(JSON.stringify(this.devApp));
        this.preStage = null;
        if (this.stage == 'stg') {
          this.preStage = 'dev';
        } else if (this.stage == 'prod') {
          this.preStage = 'stg';
        }
        this.updateRefs();
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
