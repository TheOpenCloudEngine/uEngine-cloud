<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <div v-if="!taskId">
      <span class="md-subheading">노드 : {{getHostBySlaveId(nodeId)}}</span>
      <br>
      <span class="md-subheading">시스템 사용량</span>
      <metrics :nodeId="nodeId"></metrics>
      <br><br>
    </div>

    <router-view></router-view>
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
        show: true
      }
    },
    mounted() {
      if (this.$route.name == 'nodeDetail') {
        this.show = true;
      } else {
        this.show = false;
      }
    },
    watch: {
      '$route'(to, from) {
        if (this.$route.name == 'nodeDetail') {
          this.show = true;
        } else {
          this.show = false;
        }
      }
    },
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
