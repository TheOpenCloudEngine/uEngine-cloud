<template>
  <div v-if="devApp" style="width: 100%;">
    <iframe
      src="https://programmaticponderings.com/2017/09/28/docker-log-aggregation-and-visualization-options-with-elk/"
      style="width:100%;height: 800px"></iframe>
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
        refs: ['master'],
        autoDeploy: ['dev'],
        when: 'commit',
        menu: 'pipeline'
      }
    },
    mounted() {

    },
    watch: {

    },
    methods: {
      changeMenu: function (menu) {
        this.menu = menu;
      },
      savePipeLine: function () {

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
