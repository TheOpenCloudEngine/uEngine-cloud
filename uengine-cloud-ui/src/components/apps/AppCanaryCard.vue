<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout v-if="devApp">
    <div style="width: 100%">
      <md-layout md-align="center">
        <md-input-container>
          <label>로드밸런스 - 신규 : {{newProdWeight}} % |
            이전 : {{100 - newProdWeight}} %
          </label>
          <md-input v-model="newProdWeight" type="range" min="0" max="100"
                    step="1"></md-input>
          <md-button class="md-raised"
                     @click="updateApp">
            <md-icon>update</md-icon>
            <md-tooltip md-direction="bottom">로드 밸런스 비율을 조정합니다.</md-tooltip>
          </md-button>
        </md-input-container>
      </md-layout>
    </div>
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
      categoryItem: Object
    },
    data() {
      return {
        stageApp: null,
        resourceUpdated: false,
        newProdWeight: 0,
        diff: false
      }
    },
    mounted() {
      var me = this;
      this.makeItems();
    },
    watch: {
      stage: function () {
        this.resourceUpdated = false;
        this.makeItems();
      },
      devApp: {
        handler: function (newVal, oldVal) {
          this.makeItems();
        },
        deep: true
      }
    },
    methods: {
      updateApp: function () {
        var me = this;
        var stageCopy = JSON.parse(JSON.stringify(me.stageApp));
        stageCopy.deploymentStrategy.canary.weight = me.newProdWeight;
        var data = JSON.parse(JSON.stringify(me.devApp));
        data[me.stage] = stageCopy;

        me.updateDevApp(me.appName, data, function (response) {
          me.resourceUpdated = false;
        });
      },
      updateResource: function (val) {
        var me = this;
        me.newProdWeight = val;
        this.makeItems();
      },
      makeItems: function () {
        var me = this;
        if (!me.devApp) {
          return;
        }
        me.stageApp = me.devApp['prod'];

        //최초 리소스 로딩인경우 값 배정
        if (!me.resourceUpdated) {
          me.newProdWeight = me.stageApp.deploymentStrategy.canary.weight;
          me.resourceUpdated = true;
        }
        me.diff = me.newProdWeight != me.stageApp.deploymentStrategy.canary.weight;
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
