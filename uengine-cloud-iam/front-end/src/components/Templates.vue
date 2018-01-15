<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-dialog md-open-from="#open" md-close-to="#open" ref="open" class="fullscreen">
    <md-dialog-title style="margin-bottom: 0px;">
      <md-layout md-align="center">
        <md-button class="md-primary" @click="close">닫기</md-button>
        <md-radio v-model="notificationType" :mdValue="'SIGN_UP'">
          <span class="md-caption">가입 요청</span>
        </md-radio>
        <md-radio v-model="notificationType" :mdValue="'SIGNED_UP'">
          <span class="md-caption">가입 완료 알림</span>
        </md-radio>
        <md-radio v-model="notificationType" :mdValue="'FORGOT_PASSWORD'">
          <span class="md-caption">비밀번호 찾기 요청</span>
        </md-radio>
        <md-radio v-model="notificationType" :mdValue="'PASSWORD_CHANGED'">
          <span class="md-caption">비밀번호 변경 알림</span>
        </md-radio>
      </md-layout>
    </md-dialog-title>

    <md-dialog-content ref="container" style="overflow-x: hidden;padding: inherit;overflow-y: hidden;">
      <div style="width: 100%" name="summernote">

      </div>
    </md-dialog-content>
  </md-dialog>
</template>
<script>
  export default {
    props: {
      clientKey: String
    },
    data() {
      return {
        notificationType: 'SIGN_UP',
        templates: {}
      }
    },
    mounted() {

    },
    watch: {
      notificationType: function () {
        this.toNote();
      }
    }
    ,
    methods: {
      open(clientKey) {
        this.$refs['open'].open();
        this.getTemplates(clientKey);
      },
      close(ref) {
        this.$refs['open'].close();
      },
      getTemplates: function (clientKey) {
        var me = this;
        window.iam.getAllTemplate(clientKey)
          .done(function (templates) {
            me.templates = templates;
            me.toNote();
          });
      },
      toNote: function () {
        var me = this;
        //me.notificationType
        var summernote = $(me.$el).find('[name=summernote]');
        summernote.summernote('destroy');
        summernote.summernote({
          height: 650,
          focus: true
        });
        if (me.templates[me.notificationType]) {
          summernote.summernote('code', me.templates[me.notificationType].body);
        }
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-dialog-title {
    background-color: #f5f5f6;
    /*height: 50px;*/
    border-bottom: solid 1px #e6e6e6
  }

</style>
