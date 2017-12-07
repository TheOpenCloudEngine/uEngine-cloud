<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <router-view></router-view>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
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
