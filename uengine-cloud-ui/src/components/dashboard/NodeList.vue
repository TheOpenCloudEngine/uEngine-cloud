<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <md-table md-sort="host">
          <md-table-header>
            <md-table-row>
              <md-table-head md-sort-by="host">호스트</md-table-head>
              <md-table-head md-sort-by="status">상태</md-table-head>
              <md-table-head md-sort-by="tasks">타스크</md-table-head>
              <md-table-head md-sort-by="cpus">CPU</md-table-head>
              <md-table-head md-sort-by="mem">메모리</md-table-head>
              <md-table-head md-sort-by="disk">디스크</md-table-head>
            </md-table-row>
          </md-table-header>

          <md-table-body>
            <md-table-row v-for="node in list">
              <md-table-cell>
                <a v-on:click="moveNode(node.id)" style="cursor: pointer;">{{node.host}}</a>
              </md-table-cell>

              <md-table-cell>{{node.status}}</md-table-cell>
              <md-table-cell>{{node.tasks}}</md-table-cell>
              <md-table-cell>{{node.cpus}}</md-table-cell>
              <md-table-cell>{{node.mem}}</md-table-cell>
              <md-table-cell>{{node.disk}}</md-table-cell>
            </md-table-row>
          </md-table-body>
        </md-table>
        <md-table-pagination
          :md-size="size"
          :md-total="total"
          :md-page="page"
          md-label="페이지당 항목"
          md-separator="총"
          :md-page-options="[5, 10, 25, 50]"
          @pagination="onPagination">
        </md-table-pagination>
      </md-table-card>
    </md-layout>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {
      mode: String
    },
    data() {
      return {
        list: [],
        total: 10,
        size: 5,
        page: 1
      }
    },
    mounted(){
      this.makeList();
    },
    watch: {
      last: {
        handler: function (newVal, oldVal) {
          this.makeList();
        },
        deep: true
      }
    }
    ,
    methods: {
      onPagination: function (val) {
        this.focusedList = [];
        this.size = val.size;
        this.page = val.page;
        this.makeList();
      },
      makeList: function () {
        var list = [];
        var me = this;
        me.list = [];

        if (!me.last) {
          return;
        }
        $.each(me.last.slaves, function (i, slave) {
          list.push({
            id: slave.id,
            host: slave.hostname,
            status: slave.active ? 'Healthy' : 'UnHealthy',
            tasks: slave['TASK_RUNNING'] + ' tasks',
            cpus: Math.round((slave['used_resources'].cpus / slave.resources.cpus) * 100) + '%',
            mem: Math.round((slave['used_resources'].mem / slave.resources.mem) * 100) + '%',
            disk: Math.round((slave['used_resources'].disk / slave.resources.disk) * 100) + '%'
          });
        });

        //페이지네이션
        var offset = (this.page - 1) * this.size;
        var limit = (this.page) * this.size - 1;

        var count = 0;
        for (var i = 0; i < list.length; i++) {
          if (count >= offset && count <= limit) {
            me.list.push(list[i]);
          }
          count++;
        }
        this.total = count;
      }
      ,
      moveNode: function (nodeId) {
        var me = this;
        this.$router.push(
          {
            name: 'nodeDetail',
            params: {nodeId: nodeId}
          }
        )
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  /*tbody tr:nth-child(odd) {*/
  /*background-color: rgb(243, 243, 243);*/
  /*}*/
</style>
