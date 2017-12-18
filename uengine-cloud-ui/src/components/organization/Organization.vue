<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <router-view :iam="iam"></router-view>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {
      iam:Object
    },
    data() {
      return {}
    },
    mounted(){

    }
    ,
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
