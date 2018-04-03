<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#open" md-close-to="#open" ref="open"
    @open="onOpen"
    @close="onClose"
  >
    <md-dialog-title>{{stageName}} 환경 앱 빌드</md-dialog-title>
    <md-dialog-content>
      <div style="width: 400px">
        <span class="md-body-1">빌드할 브랜치를 선택하세요.</span>
        <md-input-container>
          <md-select v-model="selectedRef" required>
            <md-option v-for="branch in branchList" :value="branch.name">{{branch.name}}</md-option>
          </md-select>
        </md-input-container>
      </div>
    </md-dialog-content>
    <md-dialog-actions>
      <md-button class="md-primary md-raised" @click="action">시작</md-button>
      <md-button class="md-primary" @click="close">취소</md-button>
    </md-dialog-actions>
  </md-dialog>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'

  export default {
    mixins: [DcosDataProvider],
    props: {
      stage: String,
      devApp: Object,
      categoryItem: Object,
      appName: String
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
        opened: false,
        selectedRef: null,
        branchList: [],
        hasImage: false,
        copyDevApp: null
      }
    },
    mounted() {

    },
    watch: {}
    ,
    methods: {
      action: function () {
        var me = this;

        if (!me.selectedRef) {
          me.$root.$children[0].error('브랜치를 선택해주세요.');
          return;
        }
        //창 닫기
        me.close();

        //var ref = me.selectedRef ? me.selectedRef : me.selectedRefTag;
        me.excutePipelineTrigger(me.appName, me.selectedRef, me.stage, function (response) {

        })
      },
      updateRefs: function () {
        var me = this;
        this.selectedRef = null;
        this.branchList = [];

        var projectId = me.copyDevApp.projectId;
        var p1 = this.$root.gitlab('api/v4//projects/' + projectId + '/repository/branches').get();

        Promise.all([p1])
          .then(function ([r1]) {
            me.branchList = r1.data;
          });
      },
      open() {
        this.copyDevApp = JSON.parse(JSON.stringify(this.devApp));
        this.updateRefs();
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      },
      onOpen: function () {
        this.opened = true;
      },
      onClose: function () {
        this.opened = false;
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
