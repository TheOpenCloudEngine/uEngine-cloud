<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-layout>
        <span class="md-subheading">앱 목록</span>
      </md-layout>
      <md-layout md-align="end">
        <md-button class="md-raised md-primary" @click="openNewApp">작성
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>
    <app-list :mode="'app'"></app-list>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        devopsApps: null
      }
    },
    mounted(){

    },
    watch: {

    }
    ,
    methods: {
      openNewApp: function () {
        this.$router.push(
          {
            name: 'appsCatalog'
          }
        )
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
