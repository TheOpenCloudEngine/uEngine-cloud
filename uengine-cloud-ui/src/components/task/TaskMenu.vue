<template>
  <div>
    <md-button class="md-raised md-primary" v-on:click="move(from + 'TaskDetail')">Detail</md-button>
    <md-button class="md-raised md-primary" v-on:click="move(from + 'TaskFiles')">Files</md-button>
    <md-button class="md-raised md-primary" v-on:click="move(from + 'TaskLog')">Log</md-button>
    <br>

    <router-view>
    </router-view>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      from: String
    },
    data() {
      return {
        task: null
      }
    },
    mounted() {

    },
    watch: {},
    methods: {
      move: function (routeName) {
        var me = this;
        this.$router.push(
          {
            name: routeName,
            params: {appId: me.appId, taskId: me.taskId}
          }
        )
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
