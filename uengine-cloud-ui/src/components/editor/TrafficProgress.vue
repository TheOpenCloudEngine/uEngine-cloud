<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div v-if="mode == 'traffic'">
    <md-layout>
      <md-layout md-flex="15">
        이전 버전
      </md-layout>
      <md-layout md-flex="70">
        <vue-slider
          v-on:drag-end="$emit('drag-end')"
          width="100%"
          :clickable="editable"
          :tooltip="false"
          :min="0"
          :max="100"
          :height="10"
          :dot-size="12"
          v-model="oldWeight"></vue-slider>
      </md-layout>
      <md-layout md-flex="15">
        {{oldWeight}} %
      </md-layout>
    </md-layout>

    <md-layout>
      <md-layout md-flex="15">
        현재 버전
      </md-layout>
      <md-layout md-flex="70">
        <vue-slider
          v-on:drag-end="$emit('drag-end')"
          width="100%"
          :clickable="editable"
          :tooltip="false"
          :min="0"
          :max="100"
          :height="10"
          :dot-size="12"
          v-model="newWeight"></vue-slider>
      </md-layout>
      <md-layout md-flex="15">
        {{weight}} %
      </md-layout>
    </md-layout>
  </div>
  <div v-else style="padding: 32px">
    <md-layout>
      <md-layout class="right-border left-border">
        <span class="left-header">
          0 분
        </span>
        <span class="right-header">
          {{increase}} 분
        </span>
        <span class="step-label">
          증가
        </span>
        <vue-slider style="margin-top: 20px"
                    width="100%"
                    :clickable="false"
                    :tooltip="false"
                    :min="0"
                    :max="increase * 60"
                    :dot-size="0"
                    v-model="currentIncrease"></vue-slider>
      </md-layout>
      <md-layout class="right-border">
        <span class="right-header">
          {{increase + test}} 분
        </span>
        <span class="step-label">
          테스트
        </span>
        <vue-slider style="margin-top: 20px"
                    width="100%"
                    :clickable="false"
                    :tooltip="false"
                    :min="0"
                    :max="test * 60"
                    :dot-size="0"
                    v-model="currentTest"></vue-slider>
      </md-layout>
      <md-layout class="right-border">
        <span class="right-header">
          {{increase + test + decrease}} 분
        </span>
        <span class="step-label">
          감소
        </span>
        <vue-slider style="margin-top: 20px"
                    width="100%"
                    :clickable="false"
                    :tooltip="false"
                    :min="0"
                    :max="decrease * 60"
                    :dot-size="0"
                    v-model="currentDecrease"></vue-slider>
      </md-layout>
    </md-layout>
  </div>
</template>
<script>
  export default {
    props: {
      editable: {
        type: Boolean,
        default: false
      },
      mode: {
        type: String,
        default: 'traffic' //step
      },
      weight: Number,
      increase: Number,
      test: Number,
      decrease: Number,
      deploymentEndTime: Number,
      currentStep: String
    },
    computed: {
      oldWeight: {
        get: function () {
          return 100 - this.newWeight;
        },
        set: function (val) {
          this.newWeight = 100 - val;
        }
      }
    },
    data() {
      return {
        interval: true,
        newWeight: this.weight,
        currentIncrease: 0,
        currentTest: 0,
        currentDecrease: 0
      }
    },
    watch: {
      increase: function () {
        this.createData();
      },
      test: function () {
        this.createData();
      },
      decrease: function () {
        this.createData();
      },
      deploymentEndTime: function () {
        this.createData();
      },
      currentStep: function () {
        this.createData();
      },
      weight: function (val) {
        this.newWeight = val
      },
      newWeight: function (val) {
        this.$emit('update:weight', val);
      }
    },
    mounted() {
      var me = this;
      if (me.mode == 'step') {
        var intervalCreateData = function () {
          me.createData();
          if (me.interval) {
            setTimeout(function () {
              intervalCreateData();
            }, 1000);
          }
        };
        intervalCreateData();
      } else {
        me.createData();
      }
    },
    destroyed: function () {
      this.interval = false;
    }
    ,
    methods: {
      createData: function () {
        //only for step
        if (!this.mode == 'step') {
          return;
        }
        var me = this;
        var currentTime = new Date().getTime();
        var text;
        var currentIncrease, currentTest, currentDecrease;
        if (me.currentStep == 'increase') {
          currentIncrease = currentTime - me.deploymentEndTime;
          currentTest = 0;
          currentDecrease = 0;

          currentIncrease = Math.ceil((currentIncrease / (1000)))
        }
        else if (me.currentStep == 'test') {
          currentIncrease = me.increase * 60;
          currentTest = currentTime
            - (me.increase * 1000)
            - me.deploymentEndTime;
          currentDecrease = 0;

          currentTest = Math.ceil((currentTest / (1000)))
        }
        else if (me.currentStep == 'decrease') {
          currentIncrease = me.increase * 60;
          currentTest = me.test * 60;
          currentDecrease = currentTime
            - ((me.increase + me.test) * 1000)
            - me.deploymentEndTime;

          currentDecrease = Math.ceil((currentDecrease / (1000)))
        } else {
          currentIncrease = 0;
          currentTest = 0;
          currentDecrease = 0;
        }

        me.currentIncrease = currentIncrease;
        me.currentTest = currentTest;
        me.currentDecrease = currentDecrease;
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .right-border {
    height: 50px;
    border-right: 2px solid #959799;
    position: relative;
  }

  .left-border {
    height: 50px;
    border-left: 2px solid #959799;
    position: relative;
  }

  .right-header {
    position: absolute;
    top: -20px;
    right: -25px;
    width: 50px;
    height: 16px;
    text-align: center
  }

  .left-header {
    position: absolute;
    top: -20px;
    left: -25px;
    width: 50px;
    height: 16px;
    text-align: center
  }

  .step-label {
    position: absolute;
    bottom: 0px;
    left: 0px;
    width: 100%;
    height: 16px;
    text-align: center
  }
</style>
