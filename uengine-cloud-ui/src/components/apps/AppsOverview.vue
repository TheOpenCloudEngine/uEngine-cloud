<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-layout>
        <span class="md-subheading">앱 목록</span>
      </md-layout>
      <md-layout md-align="end">
        <md-button class="md-raised md-primary" @click="openNewApp">
          <md-tooltip md-direction="bottom">신규 앱을 작성합니다.</md-tooltip>
          작성
          <md-icon>control_point</md-icon>
        </md-button>
      </md-layout>
    </md-layout>
    <md-layout>
      <md-input-container>
        <md-icon>search</md-icon>
        <label>항목 검색</label>
        <md-input v-model="search" type="text"></md-input>
        <md-button class="md-raised">
          <md-icon>swap_vert</md-icon>
        </md-button>
        <md-button class="md-raised" v-on:click="changeView">
          <md-icon v-if="view == 'list'">view_module</md-icon>
          <md-icon v-if="view == 'card'">view_list</md-icon>
        </md-button>
      </md-input-container>
    </md-layout>
    <app-list :mode="'app'" :search="search"></app-list>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        search: '',
        view: 'list'
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
      },
      changeView: function () {
        if (this.view == 'list') {
          this.view = 'card';
        } else {
          this.view = 'list'
        }
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
