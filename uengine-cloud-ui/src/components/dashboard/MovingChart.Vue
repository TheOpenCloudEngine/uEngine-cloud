<template>
  <div class="flot-chart-content"></div>
</template>
<script>
  export default {
    props: {
      height: {
        default: function () {
          return 150;
        },
        type: Number
      },
      data: Array,
      minY: Number,
      maxY: Number
    },
    data() {
      return {
        container: null,
        series: [
          {
            data: [],
            lines: {
              fill: true
            }
          }
        ],
        plot: null
      }
    },
    watch: {
      height: function () {
        if (this.container) {

        }
        this.container.height(this.height);
      },
      maxY: function () {
        this.updateChart();
      },
      data: function () {
        this.updateChart();
      }
    },
    mounted() {
      var me = this;
      this.container = $(this.$el); //$("#flot-line-chart-moving");
      this.container.height(this.height);

      var plot = $.plot(this.container, this.series, {
        grid: {
          color: "#999999",
          hoverable: true,
          clickable: true,
          tickColor: "#D4D4D4",
          borderWidth: 0,
          minBorderMargin: 20,
          labelMargin: 10,
          backgroundColor: {
            colors: ["#ffffff", "#ffffff"]
          },
          margin: {
            top: 8,
            bottom: 20,
            left: 20
          },
          markings: function (axes) {
            var markings = [];
            var xaxis = axes.xaxis;
            for (var x = Math.floor(xaxis.min); x < xaxis.max; x += xaxis.tickSize * 2) {
              markings.push({
                xaxis: {
                  from: x,
                  to: x + xaxis.tickSize
                },
                color: "#fff"
              });
            }
            return markings;
          }
        },
        colors: ["#1ab394"],
        xaxis: {
          ticks: [[0, '60s'], [30, '30s'], [60, '0s']]
//          tickFormatter: function () {
//            return "";
//          }
        },
//        xaxis: {
//          tickDecimals: 0
//        },
        yaxis: {
          min: me.minY || 0,
          max: me.maxY || 100
        },
        tooltip: true,
        legend: {
          show: true
        },
        tooltipOpts: {
          content: "x: %x, y: %y"
        }
      });

      this.plot = plot;

      //this.plot.setData(series);
      this.plot.setupGrid();
      this.plot.draw();

      // Update the random dataset at 25FPS for a smoothly-animating chart

//      setInterval(function updateRandom() {
//        series[0].data = getRandomData();
//        plot.setData(series);
//        plot.setupGrid();
//        plot.draw();
//      }, 1000);

//      plot.getOptions().xaxes[0].min = time.start;
//      plot.getOptions().xaxes[0].max = time.end;
//      plot.setupGrid();
//      plot.draw();
    },
    methods: {
      updateChart: function () {
        this.plot.getOptions().yaxis.min = this.minY;
        this.plot.getOptions().yaxis.max = this.maxY;
        this.series[0].data = this.data;
        this.plot.setData(this.series);
        this.plot.setupGrid();
        this.plot.draw();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .flot-chart-content {
    /*width: 300px;*/
    /*height: 400px;*/
  }
</style>
