<template>
  <div v-if="devApp">

    <md-button v-if="menu == 'history'"
               class="md-raised md-primary" @click="changeMenu('new')">
      <md-tooltip md-direction="bottom">새로운 배포를 시작합니다.</md-tooltip>
      새 배포 작성
      <md-icon>control_point</md-icon>
    </md-button>

    <md-button v-if="menu == 'detail'"
               class="md-raised md-primary" @click="changeMenu('history')">
      <md-tooltip md-direction="bottom">모든 배치로 돌아가기</md-tooltip>
      모든 배치로 돌아가기
      <md-icon>undo</md-icon>
    </md-button>

    <deployment-new v-if="menu == 'new'"
                    :stage="stage"
                    :devApp="devApp"
                    :categoryItem="categoryItem"
                    :menu.sync="menu"
    ></deployment-new>

    <div v-if="menu == 'history'">
      <deployment-current
        :stage="stage"
        :devApp="devApp"
        :categoryItem="categoryItem"
        v-on:showDetail="showCurrentDetail"
      ></deployment-current>

      <br><br>
      <span class="md-subheading">배포 히스토리</span>
      <deployment-history
        :stage="stage"
        :devApp="devApp"
        :categoryItem="categoryItem"
        v-on:showDetail="showHistoryDetail"
      ></deployment-history>
    </div>

    <deployment-detail v-if="menu == 'detail'"
                       :stage="stage"
                       :devApp="devApp"
                       :categoryItem="categoryItem"
                       :historyId="historyId"
                       :menu.sync="menu"
    ></deployment-detail>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      categoryItem: Object
    },
    data() {
      return {
        menu: 'history',
        historyId: null
      }
    },
    mounted() {

    },
    watch: {},
    methods: {
      showCurrentDetail: function () {
        this.historyId = null;
        this.menu = 'detail'
      },
      showHistoryDetail: function (historyId) {
        this.historyId = historyId;
        this.menu = 'detail'
      },
      changeMenu: function (val) {
        this.menu = val;
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .bar-wrapper {
    .md-button {
      width: 100%;
      margin: 0px;
    }
  }

  .md-theme-default.md-chip {
    margin-top: 8px;
  }
</style>
