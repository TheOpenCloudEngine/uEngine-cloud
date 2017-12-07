<template>
  <div>
    <span class="md-subheading">{{jobId}}</span>
    <br>
    <div v-if="!taskId">
      <md-button class="md-raised md-primary" v-on:click="move('jobRunList')">Run History</md-button>
      <md-button class="md-raised md-primary" v-on:click="move('jobConfiguration')">Configuration</md-button>
    </div>
    <br>
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
      return {}
    },
    mounted() {

    },
    watch: {

    },
    methods: {
      move: function (routeName) {
        var me = this;
        this.$router.push(
          {
            name: routeName,
            params: {jobId: me.jobId}
          }
        )
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
