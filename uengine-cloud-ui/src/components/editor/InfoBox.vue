<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout
      v-bind:class="{ 'info-border': border }"
      style="width: 100%;padding: 8px;">
      <md-icon v-if="type == 'info'" style="margin-left: 16px;margin-right: 16px"
               class="md-primary">
        info_outline
      </md-icon>
      <md-icon v-if="type == 'warn'" style="margin-left: 16px;margin-right: 16px"
               class="md-warn">
        info_outline
      </md-icon>
      <md-layout>
        <slot>

        </slot>
      </md-layout>
    </md-layout>
  </div>
</template>
<script>
  export default {
    props: {
      border: {
        type: Boolean,
        default: false
      },
      type: {
        type: String,
        default: 'info'
      },
    },
    data() {
      return {}
    },
    mounted() {

    }
    ,
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .info-border {
    border: 2px solid darkblue
  }
</style>
