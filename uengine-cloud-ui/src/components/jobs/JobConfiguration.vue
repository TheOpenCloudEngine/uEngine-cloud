<template>
  <md-layout>
    잡 콘피그

    <br>
    <div v-if="job">
      {{JSON.stringify(job)}}
    </div>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'
  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {},
    data() {
      return {
        job: null
      }
    },
    mounted() {

    },
    watch: {
      'dcosData.jobs': {
        handler: function (newVal, oldVal) {
          var me = this;
          this.$root.dcos('service/metronome/v1/jobs/' + me.jobId + '?embed=activeRuns&embed=history&embed=schedules').get()
            .then(function (response) {
              //activeRuns
              me.job = response.data;
            })
        },
        deep: true
      }
    },
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
