<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#confirm" md-close-to="#confirm" ref="open">
    <md-dialog-title>{{title}}</md-dialog-title>
    <md-dialog-content>
      <span>{{contentHtml}}</span>
    </md-dialog-content>
    <md-dialog-actions>
      <md-button class="md-primary" @click="action">{{okText}}</md-button>
      <md-button class="md-primary" @click="close">{{cancelText}}</md-button>
    </md-dialog-actions>
  </md-dialog>
</template>
<script>
  export default {
    props: {
      title: String,
      contentHtml: String,
      okText: String,
      cancelText: String
    },
    data() {
      return {
        callback: null
      }
    },
    mounted(){

    }
    ,
    methods: {
      action: function () {
        this.callback();
      },
      open(cb) {
        this.callback = cb;
        this.$refs['open'].open();
      },
      close(ref) {
        this.callback = null;
        this.$refs['open'].close();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
