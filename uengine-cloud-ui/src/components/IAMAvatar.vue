<template>
  <div>
    <md-list class="md-transparent">
      <md-list-item class="md-avatar-list">
        <!--avatar iam 으로 변경-->
        <md-avatar class="md-large">
          <!--<img src="https://placeimg.com/64/64/people/8" alt="People">-->
          <img
            :src="iamHost + '/rest/v1/avatar?userName='+ user.userName"
            v-if="user.userName"
            alt="People">
        </md-avatar>

        <span style="flex: 1"></span>
      </md-list-item>

      <md-list-item>
        <div class="md-list-text-container">
          <span v-if="user.metaData.name">{{user.metaData.name}}</span>
          <span v-if="user.metaData.email">{{user.metaData.email}}</span>
        </div>

        <md-button class="md-icon-button md-list-action" @click="openDialog('userProfile')">
          <md-icon>arrow_drop_down</md-icon>
        </md-button>
        <md-dialog md-open-from="#custom" md-close-to="#custom" ref="userProfile">
          <md-dialog-title>User Profile</md-dialog-title>

          <md-dialog-content>
            <md-card class="card-example">
              <md-card-area md-inset>
                <md-card-media md-ratio="16:9">
                  <img
                    :src="iamHost + '/rest/v1/avatar?userName='+ user.userName"
                    v-if="user.userName"
                    alt="User Image">
                </md-card-media>

                <md-card-header>
                  <h2 class="md-title">User Infomation</h2>
                </md-card-header>

                <md-card-content>
                  <div>Email : {{user.metaData.email}}</div>
                  <div>Name : {{user.metaData.name}}</div>
                </md-card-content>
              </md-card-area>
            </md-card>
          </md-dialog-content>

          <md-dialog-actions>
            <md-button class="md-primary" @click="closeDialog('userProfile')">Ok</md-button>
          </md-dialog-actions>
        </md-dialog>
      </md-list-item>
    </md-list>
  </div>
</template>


<script>
  export default {
    props: {},
    data: function () {
      return {
        iam: window.iam,
        iamHost: window.iam.host,
        user: {
          metaData:{}
        }
      }
    },
    watch: {},
    mounted() {
      var me = this;
      me.iam.getUser(localStorage['userName']).then(function (response) {
        me.user = response;
      })

    },
    methods: {
      openDialog(ref) {
        this.$refs[ref].open();
      },
      closeDialog(ref) {
        this.$refs[ref].close();
      },
    }
  }
</script>

