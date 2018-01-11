<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <div v-if="!taskId">
      <md-layout>
        <md-layout>
          <span class="md-subheading">노드 : {{getHostBySlaveId(nodeId)}}</span>
        </md-layout>
        <md-layout md-align="end">
          <md-button class="md-raised md-primary" v-on:click="openCadvisor">더 자세히
            <md-icon>search</md-icon>
          </md-button>
        </md-layout>
      </md-layout>
      <div>
        <metrics :nodeId="nodeId"></metrics>
      </div>
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
    methods: {
      openCadvisor: function () {
        var url = this.getCadvisorUrlBySlaveId(this.nodeId);
        if (url) {
          window.open(url);
        } else {
          this.$root.$children[0].warn('등록된 메트릭스 서비스가 없습니다.');
        }
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
