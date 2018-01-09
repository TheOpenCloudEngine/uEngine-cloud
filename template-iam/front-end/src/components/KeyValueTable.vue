<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table md-sort="name">
          <md-table-header>
            <md-table-row>
              <md-table-head md-sort-by="key">key</md-table-head>
              <md-table-head md-sort-by="value">value</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="(value, key) in data">
              <md-table-cell>
                {{key}}
              </md-table-cell>
              <md-table-cell>
                {{value}}
              </md-table-cell>
            </md-table-row>
          </md-table-body>
        </md-table>
      </md-table-card>
    </md-layout>
  </div>
</template>
<script>
  export default {
    props: {
      data: Object
    },
    data() {
      return {}
    },
    mounted(){

    },
    watch: {}
    ,
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  /*tbody tr:nth-child(odd) {*/
  /*background-color: rgb(243, 243, 243);*/
  /*}*/
</style>
