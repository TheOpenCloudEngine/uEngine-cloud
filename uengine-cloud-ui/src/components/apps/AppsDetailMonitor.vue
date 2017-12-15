<template>
  <div v-if="devopsApp" style="width: 100%;">
    <md-layout>
      <div class="canvas-panel">
        <deployment-canvas></deployment-canvas>
      </div>
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
        devopsApp: null
      }
    },
    mounted() {

    },
    watch: {

    },
    methods: {

    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .canvas-panel {
    width: 100%;
    height: 600px;
    position: relative;
    /*top: 0px;*/
    /*bottom: 0px;*/
    /*left: 0px;*/
    /*right: 0px;*/
    /*position: absolute;*/
    overflow: hidden;
  }
</style>
