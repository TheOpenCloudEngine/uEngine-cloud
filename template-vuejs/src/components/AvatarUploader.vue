<template>

  <div>
    <md-dialog md-open-from="#profile" md-close-to="#profile" ref="dialog1">
      <md-dialog-title>User Profile</md-dialog-title>

      <md-dialog-content>
        <vue-img-inputer
          :maxSize='5120'
          placeholder="프로필 사진"
          accept="image/*"
          theme="material"
          size="large"
          :onChange="onFileChange"
        >
        </vue-img-inputer>
      </md-dialog-content>

      <md-dialog-actions>
        <md-button class="md-primary" @click="closeDialog('dialog1')">Cancel</md-button>
        <md-button class="md-primary" @click="upload">Save</md-button>
      </md-dialog-actions>
    </md-dialog>


    <md-button class="md-primary md-raised" id="profile" @click="openDialog('dialog1')">Profile</md-button>
  </div>
</template>


<script>
  export default {
    props: {
      iam: Object,
    },

    watch: {
      taskId: function (val) {
        this.load();
      }
    },

    data: function () {
      return {
        dialog: false,
        file: null
      }
    },

    methods: {
      openDialog(ref) {
        this.$refs[ref].open();
      },
      closeDialog(ref) {
        this.$refs[ref].close();
      },
      onFileChange: function (file, fileName) {
        this.file = file;
      },
      upload: function () {
        var me = this;
        this.iam.createUserAvatarByFormData(me.file, me.file.type, null, localStorage['username'])
          .done(function () {
            me.$root.$children[0].success('사진이 업로드 되었습니다.');
            me.closeDialog('dialog1');
          })
          .fail(function () {
            me.$root.$children[0].error('사진을 업로드할 수 없습니다.');
          })
        ;
      }
    }
  }
</script>

