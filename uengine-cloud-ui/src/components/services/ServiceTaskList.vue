<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <task-list></task-list>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {},
    data() {
      return {}
    },
    mounted() {
      //console.log(this.$parent);
      //this.fetchData();
    },
    watch: {},
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
