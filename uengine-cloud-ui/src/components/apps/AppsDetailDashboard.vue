<template>
  <div v-if="devApp">
    <span class="md-subheading">런타임</span>
    <app-runtime-card
      :stage="stage"
      :devApp="devApp"
      :catalogItem="catalogItem"
    ></app-runtime-card>
    <div style="margin-top: 5%;">
      <apps-logs-list></apps-logs-list>
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
      catalogItem: Object
    },
    data() {
      return {}
    },
    mounted() {

    },
    watch: {

    },
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
