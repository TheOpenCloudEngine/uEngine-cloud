<template>
  <div style="width: 100%;">
    <div v-if="appLogs.length">
      <div class="md-subheading">활동 피드</div>
      <div class="header-top-line"></div>
      <!--<md-card md-with-hover style="width: 100%;">-->
        <!--<md-card-area>-->
          <!--<md-card-content>-->
        <md-table-card>
          <md-table>
            <md-table-body>
              <md-table-row v-for="appLog in appLogs" style="border:1px;">
                <md-table-cell class="unlined">
                  <div style="margin-top: 5px;margin-bottom:5px;">
                    <div>
                      <div v-if="appLog.status=='SUCCESS'" class="checkmark">
                        <div class="checkmark_circle"></div>
                        <div class="checkmark_stem"></div>
                        <div class="checkmark_kick"></div>
                      </div>
                      <div v-else style="display: inline-block">
                        <div class="stopmark_circle">
                          <div class="stopmark_x"></div>
                          <div class="stopmark_y"></div>
                        </div>

                      </div>
                      <span style="font-weight: 600;">{{appLog.appName}} 앱이 <span v-if="appLog.log">{{appLog.log.stage}}에</span> {{appLog.action}}됨</span>
                    </div>
                    <div style="margin-left:9px;border-left:1px solid #B0B0B1;margin-top:5px;">
                      <!--<div>{{appLog.log}}</div>-->
                      <span style="padding: 10px;">    {{new Date(appLog.regDate).toString()}}  |  {{appLog.updater}}</span>
                    </div>
                  </div>
                </md-table-cell>
              </md-table-row>
            </md-table-body>
          </md-table>
          <md-table-pagination
            md-size="5"
            :md-total="total"
            md-page="1"
            md-label="Rows"
            md-separator="of"
            :md-page-options="[5, 10, 25, 50]"
            @pagination="onPagination"></md-table-pagination>
        </md-table-card>

    </div>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      stage: String,
      devApp: Object,
      catalogItem: Object
    },
    data() {
      return {
        appLogs: [],
        size: 5,
        page: 0,
        total: 0,
      }
    },
    mounted() {
      var me = this;

      this.$root.backend('appLogs/search/findByAppName?appName=' + this.appName+'&sort=regDate,desc'+'&size='+this.size).get()
        .then(function (response) {
          console.log(response);
          me.appLogs = response.data['_embedded'].appLogs;
          me.total = response.data.page.totalElements;
        });
    },
    watch: {
      appLogs: {
        handler: function (newVal, oldVal) {
        },
        deep: true
      }
    },
    methods: {
      onPagination: function (value) {
        var me = this;
        console.log("value",value);
        this.$root.backend('appLogs/search/findByAppName?appName=' + this.appName+'&sort=regDate,desc'+'&page='+(value.page-1)+'&size='+value.size).get()
          .then(function (response) {
            console.log(response);
            me.appLogs = response.data['_embedded'].appLogs;
          });
      }
    }
  }

</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .bar-wrapper {
    .md-button {
      width: 100%;
      margin: 0px;
    }
  }

  .md-theme-default.md-chip {
    margin-top: 8px;
  }

  .checkmark {
    display: inline-block;
    width: 15px;
    height: 15px;
    padding: 4px;
    margin: 3px;
    -ms-transform: rotate(45deg); /* IE 9 */
    -webkit-transform: rotate(45deg); /* Chrome, Safari, Opera */
    transform: rotate(45deg);
  }

  .checkmark_circle {
    position: absolute;
    width: 20px;
    height: 20px;
    border: 1px solid #56D843;
    border-radius: 11px;
    left: 0;
    top: 0;
  }

  .checkmark_stem {
    position: absolute;
    width: 2px;
    height: 8px;
    background-color: #56D843;
    left: 11px;
    top: 6px;
  }

  .checkmark_kick {
    position: absolute;
    width: 3px;
    height: 2px;
    background-color: #56D843;
    left: 8px;
    top: 12px;
  }

  .stopmark_circle
  {
    width:20px;
    height:20px;
    border-radius:10px;
    padding:1px;
    border:1px solid red;
  }
  .stopmark_x
  {
    position: absolute;
    height:10px;
    width:2px;
    background-color:red;
    margin-left:7px;
    margin-top:3px;
    transform: rotate(45deg);
    -ms-transform: rotate(45deg); /* IE 9 */
    -webkit-transform: rotate(45deg); /* Safari and Chrome */
    Z-index:1;

  }
  .stopmark_y
  {
    position: absolute;
    height:10px;
    width:2px;
    background-color:red;
    margin-left:7px;
    margin-top:3px;
    transform: rotate(135deg);
    -ms-transform: rotate(135deg); /* IE 9 */
    -webkit-transform: rotate(135deg); /* Safari and Chrome */
    Z-index:2;

  }
</style>
