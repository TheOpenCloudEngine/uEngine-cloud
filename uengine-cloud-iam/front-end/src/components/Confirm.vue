<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog
    md-open-from="#confirm" md-close-to="#confirm" ref="open">
    <md-dialog-title>{{title}}</md-dialog-title>
    <md-dialog-content>
      <span>{{contentHtml}}</span>
      <div v-if="choice">
        <div v-for="(item,index) in choice">
          <md-radio v-model="choiceValue" :mdValue="index">
            <span>{{item}}</span>
          </md-radio>
        </div>
      </div>
      <div v-if="prompt" style="width: 400px">
        <md-input-container>
          <label>{{promptLabel}}</label>
          <md-input v-model="promptValue" type="text"></md-input>
        </md-input-container>
      </div>
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
        choiceValue: 0,
        title: '',
        contentHtml: '',
        choice: null,
        prompt: false,
        promptValue: '',
        promptLabel: '',
        okText: '',
        cancelText: '',
        callback: function () {

        }
      }
    },
    mounted() {

    }
    ,
    methods: {
      action: function () {
        this.close();
        if(this.choice){
          this.callback(this.choiceValue);
        }else if(this.prompt){
          this.callback(this.promptValue);
        }else{
          this.callback();
        }
      },
      open(options) {
        var me = this;
        me.promptLabel = me.promptLabel || '';
        me.promptValue = options.promptValue || '';
        me.prompt = options.prompt || false;
        me.choice = options.choice || null;
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
