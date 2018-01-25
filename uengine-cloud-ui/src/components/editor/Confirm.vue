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
    props: {},
    data() {
      return {
        title: '',
        contentHtml: '',
        okText: '',
        cancelText: '',
        callback: function () {

        }
      }
    },
    mounted(){

    }
    ,
    methods: {
      action: function () {
        this.close();
        this.callback();
      },
      open(options) {
        var me = this;
        me.title = options.title || 'Are you sure?';
        me.contentHtml = options.contentHtml || '진행하시겠습니까?';
        me.okText = options.okText || '수락';
        me.cancelText = options.cancelText || '취소';
        if (options.callback) {
          me.callback = options.callback;
        } else {
          me.callback = function () {

          }
        }
        this.$refs['open'].open();
      },
      close(ref) {
        this.$refs['open'].close();
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
