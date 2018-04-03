<template xmlns:v-on="http://www.w3.org/1999/xhtml" xmlns:v-bind="http://www.w3.org/1999/xhtml">
  <div v-if="commitInfo">
                    <span
                      class="md-caption">
                      커미터: {{commitInfo.committer_name}} | 날짜: {{commitInfo.committed_date}}</span><br>
    <span class="md-caption">
                      커밋:
                      <a v-on:click="moveGitlab('commit',commitInfo.id)">
                        <md-tooltip md-direction="bottom">커밋 이력으로 이동합니다.</md-tooltip>
                        {{commitInfo.id}}
                      </a>
                    </span><br>
    <span class="md-caption">
                      태그:
                      <a v-if="commitInfo.tag" v-on:click="moveGitlab('tag',commitInfo.tag)">
                        <md-tooltip md-direction="bottom">태그 보기로 이동합니다.</md-tooltip>
                        {{commitInfo.tag}}
                      </a>
                    <span v-else>없음</span>
                    </span><br>
  </div>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      projectId: Number,
      commitRef: String
    },
    data() {
      return {
        commitInfo: null
      }
    },
    mounted() {
      this.createCommitInfo();
    },
    watch: {
      projectId: function () {
        this.createCommitInfo();
      },
      commitRef: function () {
        this.createCommitInfo();
      }
    },
    methods: {
      createCommitInfo: function () {
        console.log('updated');
        var me = this;
        me.commitInfo = null;
        if (!me.projectId || !me.commitRef) {
          return;
        }
        me.getCommitInfo(me.projectId, me.commitRef, function (commitInfo) {
          if (commitInfo) {
            me.commitInfo = commitInfo;
          }
        })
      },
      moveGitlab: function (type, objectId) {
        this.getProject(this.projectId, function (response, err) {
          var url = response.data.web_url;
          if (type == 'project') {
            window.open(url);
          } else if (type == 'commit') {
            url = url + '/commit/' + objectId;
            window.open(url);
          } else if (type == 'tag') {
            url = url + '/tags/' + objectId;
            window.open(url);
          }
        });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-theme-default a:not(.md-button) {
    color: #162A35;
  }

  .md-theme-default a:not(.md-button).active {
    color: #4071AF;
  }

  /*#4071AF*/
</style>
