<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table md-sort="name">
          <md-table-header>
            <md-table-row>
              <md-table-head md-sort-by="key">클라이언트 키</md-table-head>
              <md-table-head md-sort-by="key">유효 토큰</md-table-head>
              <md-table-head md-sort-by="value">만료 토큰</md-table-head>
              <md-table-head md-sort-by="accessTokenLifetime">토큰 유효 시간</md-table-head>
              <md-table-head md-sort-by="refreshTokenLifetime">토큰 재발급 유효 시간</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="row in data">
              <md-table-cell>
                {{row.client}}
              </md-table-cell>
              <md-table-cell>
                {{row.active}}
              </md-table-cell>
              <md-table-cell>
                {{row.expired}}
              </md-table-cell>
              <md-table-cell>
                {{row.accessTokenLifetime}} (s)
              </md-table-cell>
              <md-table-cell>
                {{row.refreshTokenLifetime}} (s)
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
      data: Array
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
