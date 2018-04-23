<template>
  <md-layout>
    <div class="header-top-line"></div>
    <md-layout v-for="(item, key) in metrics">
      <md-card md-with-hover style="width: 100%;">
        <md-card-area>
          <md-card-header>
            <span>{{item.title}}</span>
          </md-card-header>

          <md-card-content>
            <div v-if="item.header[0]">
              <span class="md-subheading">{{item.header[0]}}</span>
              <span class="md-caption" style="margin-left: 8px">{{item.header[1]}}</span>
            </div>
            <moving-chart :data="item.data" :minY="0" :maxY="item.totalNodes || 100"></moving-chart>
          </md-card-content>
        </md-card-area>
      </md-card>
    </md-layout>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {
      nodeId: String
    },
    data() {
      return {
        metrics: {
          cpus: {
            title: 'CPU 사용량',
            data: [],
            header: []
          },
          mem: {
            title: '메모리 사용량',
            data: [],
            header: []
          },
          disk: {
            title: '디스크 사용량',
            data: [],
            header: []
          },
          node: {
            title: '노드 사용량',
            data: [],
            header: [],
            totalNodes: 10
          }
        }
      }
    },
    mounted() {
      //this.fetchData();
    },
    watch: {
      last: {
        handler: function (newVal, oldVal) {
          this.fetchMetricsData(newVal);
        },
        deep: true
      }
    },
    methods: {
      fetchMetricsData: function (data) {
        var me = this;
        var totalMem = 0;
        var usedMem = 0;
        var totalCpus = 0;
        var usedCpus = 0;
        var totalDisk = 0;
        var usedDisk = 0;

        $.each(data['slaves'], function (i, slave) {
          if (me.nodeId) {
            if (slave.id != me.nodeId) {
              return;
            }
          }
          totalMem += slave.resources.mem;
          usedMem += slave['used_resources'].mem;
          totalCpus += slave.resources.cpus;
          usedCpus += slave['used_resources'].cpus;
          totalDisk += slave.resources.disk;
          usedDisk += slave['used_resources'].disk;
        });

        if (!me.nodeId) {
          var usedNodes = data['slaves'].length;
          if (usedNodes > 10) {
            this.metrics.node.totalNodes = 100;
          } else {
            this.metrics.node.totalNodes = 10;
          }
        }

        var maxSec = 60;
        var keys = ['cpus', 'mem', 'disk', 'node'];
        if (me.nodeId) {
          keys = ['cpus', 'mem', 'disk'];
        }
        $.each(keys, function (k, key) {
          if (!me.metrics[key]) {
            return;
          }
          if (!me.metrics[key].data.length) {
            for (var i = 0; i < maxSec; i++) {
              me.metrics[key].data[i] = [i, 0];
            }
          }
          if (me.metrics[key].data.length >= maxSec) {
            me.metrics[key].data.splice(0, 1);
          }
          if (key == 'cpus') {
            me.metrics[key].data.push([0, Math.round((usedCpus / totalCpus) * 100)]);
            me.metrics[key].header[0] = Math.round((usedCpus / totalCpus) * 100) + '%';
            me.metrics[key].header[1] = usedCpus.toFixed(1) + ' of ' + totalCpus.toFixed(1) + ' Shares';
          }
          else if (key == 'mem') {
            me.metrics[key].data.push([0, Math.round((usedMem / totalMem) * 100)]);
            me.metrics[key].header[0] = Math.round((usedMem / totalMem) * 100) + '%';
            me.metrics[key].header[1] = (usedMem / 1000).toFixed(1) + ' GiB of ' + (totalMem / 1000).toFixed(1) + ' GiB';
          }
          if (key == 'disk') {
            me.metrics[key].data.push([0, Math.round((usedDisk / totalDisk) * 100)]);
            me.metrics[key].header[0] = Math.round((usedDisk / totalDisk) * 100) + '%';
            me.metrics[key].header[1] = (usedDisk / 1000).toFixed(1) + ' GiB of ' + (totalDisk / 1000).toFixed(1) + ' GiB';
          }
          if (key == 'node') {
            me.metrics[key].data.push([0, usedNodes]);
            me.metrics[key].header[0] = usedNodes;
            me.metrics[key].header[1] = 'Connected Nodes';
          }
          //인덱스 재할당.
          $.each(me.metrics[key].data, function (i, dataItem) {
            dataItem[0] = i;
          })
        });

        if (me.nodeId) {
          delete me.metrics['node'];
        }
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
