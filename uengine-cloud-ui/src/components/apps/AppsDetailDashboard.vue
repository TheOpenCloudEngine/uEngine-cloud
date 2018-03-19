<template>
  <div v-if="devApp">
    <app-canary-card
      v-if="hasRollback && devApp.prod.deploymentStrategy.canary.active"
      :stage="stage"
      :devApp="devApp"
      :categoryItem="categoryItem"
    ></app-canary-card>

    <span class="md-subheading">런타임</span>
    <app-runtime-card
      :stage="stage"
      :devApp="devApp"
      :categoryItem="categoryItem"
      :isRollback="isRollback"
    ></app-runtime-card>
    <div style="margin-top: 5%;">
      <md-layout md-gutter="16">
        <md-layout md-flex="40">
          <div style="width: 100%">
            <div class="md-subheading">활동 피드</div>
            <apps-logs-list></apps-logs-list>
          </div>
        </md-layout>
        <md-layout md-flex="60">
          <div style="width: 100%">
            <div class="md-subheading">인스턴스</div>
            <task-list
              simple
              :appIds="stage == 'prod' && isRollback ? [devApp[stage]['marathonAppIdOld']] : [devApp[stage]['marathonAppId']]">
            </task-list>
          </div>
        </md-layout>
      </md-layout>
    </div>
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
      categoryItem: Object,
      isRollback: Boolean,
      hasRollback: Boolean
    },
    data() {
      return {}
    },
    mounted() {

    },
    watch: {},
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
