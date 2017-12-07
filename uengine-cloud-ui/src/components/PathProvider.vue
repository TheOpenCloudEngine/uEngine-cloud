<template>
  <div>
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        appId: null,
        jobId: null,
        taskId: null,
        appName: null,
        nodeId: null,
        categoryItemId: null
      }
    },
    mounted() {
      this.setPathParams();
    },
    watch: {
      '$route'(to, from) {
        this.setPathParams();
      }
    },
    methods: {
      setPathParams: function () {
        this.appId = this.$route.params.appId;
        this.jobId = this.$route.params.jobId;
        this.taskId = this.$route.params.taskId;
        this.appName = this.$route.params.appName;
        this.nodeId = this.$route.params.nodeId;
        this.categoryItemId = this.$route.params.categoryItemId;
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
