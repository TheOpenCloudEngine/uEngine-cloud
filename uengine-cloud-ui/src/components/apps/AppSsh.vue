<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout>
      <md-table-card style="width: 100%">
        <div class="header-top-line"></div>
        <div>
          <md-layout md-gutter="16">
            <md-layout md-flex="60">
              <md-button class="md-primary md-raised"
                         v-for="(taskId, index) in selectedTaskIds"
                         v-on:click="focusTaskFrame(taskId)"
              >인스턴스 {{index}}
                <a style="margin-left: 10px;color: darkgrey" v-on:click="closeSshFrame(taskId, $event)">X</a>
              </md-button>
            </md-layout>
            <md-layout md-flex="10">
              <md-button class="md-primary md-raised"
                         v-on:click="reloadSsh"
              >재시작
              </md-button>
            </md-layout>
            <md-layout md-flex="10">
              <md-input-container>
                <label>접속 쉘</label>
                <md-input type="text" v-model="shell"></md-input>
              </md-input-container>
            </md-layout>
            <md-layout md-flex="20">
              <md-input-container>
                <label>인스턴스</label>
                <md-select multiple v-model="selectedTaskIds">
                  <md-option v-for="(task, index) in tasks"
                             :value="task.id">
                    인스턴스 {{index}}
                  </md-option>
                </md-select>
              </md-input-container>
            </md-layout>
          </md-layout>
        </div>
        <div v-for="taskFrame in taskFrames">
          <div v-if="taskFrame" style="width: 100%;">
            <div v-show="taskFrame.show" style="width: 100%;">
              <iframe v-show="taskFrame.status == 'SUCCESS'" :id="taskFrame.taskId" :src="taskFrame.src"
                      style="width: 100%;height: 600px"></iframe>
              <div v-if="taskFrame.status == 'LOADING'" style="width: 100%;height: 600px">
                <md-spinner :md-size="20" md-indeterminate class="md-accent"></md-spinner>
                Connecting Instance ...
              </div>
              <div v-if="taskFrame.status == 'FAILED'" style="width: 100%;height: 600px">
                Connecting failed
              </div>
            </div>
          </div>
        </div>
      </md-table-card>
    </md-layout>
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
      categoryItem: Object
    },
    data() {
      return {
        shell: '/bin/sh',
        taskFrames: [],
        selectedTaskIds: [],
        tasks: [],
        intervalId: 0,
        publicAgentIp: config['public-agent-ip']
      }
    },
    mounted() {
      this.makeSelectBoxList();
      this.startHeartBeatInterval();
    },
    watch: {
      stage: function (val) {
        this.makeSelectBoxList();
      },
      //selected 박스 조작시... selectedTaskIds 에 없는 아이프레임은 삭제해도록 한다.
      selectedTaskIds: {
        handler: function (newVal, oldVal) {
          var me = this;
          //선택 됨
          if (newVal.length > oldVal.length) {
            $.each(newVal, function (i, taskId) {
              if (oldVal.indexOf(taskId) == -1) {
                me.openSshFrame(taskId);
              }
            })
          }
          //해제 됨
          else if (newVal.length < oldVal.length) {
            $.each(me.taskFrames, function (i, taskFrame) {
              if (taskFrame) {
                if (newVal.indexOf(taskFrame.taskId) == -1) {
                  me.closeSshFrame(taskFrame.taskId);
                }
              }
            });
          }
        },
        deep: true
      }
    },
    beforeDestroy: function () {
      clearInterval(this.intervalId);
      this.closeAllSshFrame();
      console.log("beforeDestroy complete");
    },
    methods: {
      reloadSsh: function () {
        console.log('reloadSsh');
        var me = this;
        var taskFrame;
        $.each(me.taskFrames, function (i, frame) {
          if (frame && frame.show) {
            taskFrame = frame;
          }
        });
        if (!taskFrame) {
          return;
        }
        this.$root.backend('ssh').save(null, {
          taskId: taskFrame.taskId,
          shell: me.shell
        })
          .then(
            function (response) {
              var src = 'http://' + me.publicAgentIp + ':' + response.data.container.portMappings[0].servicePort;
              //taskFrames 갱신.
              setTimeout(function () {
//                taskFrame.src = '';
                taskFrame.src = src;
                document.getElementById(taskFrame.taskId).src = '';
                document.getElementById(taskFrame.taskId).src = src;
                taskFrame.status = 'SUCCESS';
              }, 1000);
            },
            function (response) {
              console.log(response);
              taskFrame.status = 'FAILED';
              me.$root.$children[0].error('컨테이너에 접속할 수 없습니다. 쉘이 올바른지 확인해주세요.');
            }
          );
      },
      startHeartBeatInterval: function () {
        var me = this;
        me.intervalId = setInterval(function () {
          $.each(me.taskFrames, function (i, taskFrame) {
            if (taskFrame) {
              me.$root.backend('ssh/' + taskFrame.taskId + "/heartbeat").save({})
                .then(
                  function () {
                    console.log("heartBeat send: ", taskFrame.taskId);
                  },
                  function () {
                    console.log("heartBeat send failed: ", taskFrame.taskId);
                  })
            }
          });
        }, 5000);
      },
      closeAllSshFrame: function () {
        var me = this;
        $.each(me.taskFrames, function (i, taskFrame) {
          if (taskFrame) {
            me.$root.backend('ssh/' + taskFrame.taskId).delete(null).then(
              function (response) {
                console.log(taskFrame.taskId + " ssh deleted!!");
              }
            );
          }
        });
      },
      closeSshFrame: function (taskId, event) {
        console.log('closeSshFrame', taskId);
        var me = this;
        if (event) {
          event.stopPropagation();
        }
        var index = this.selectedTaskIds.indexOf(taskId);
        if (this.selectedTaskIds.indexOf(taskId) != -1) {
          this.selectedTaskIds.splice(index, 1);
        }

        //taskFrames 중에서 해당하는 taskId 를 null 처리한다. 그냥 splice 처리하게 되면 iframe 이 다시 임베드되면서,
        // ==> ssh 세션이 새로 발급됨 => 기존 ssh 작업 날라가버림.
        $.each(me.taskFrames, function (i, taskFrame) {
          if (taskFrame) {
            if (taskFrame.taskId == taskId) {
              me.taskFrames[i] = null;
              console.log('make null', taskId);
            }
          }
        });
        console.log('me.taskFrames', me.taskFrames);

        this.$root.backend('ssh/' + taskId).delete(null).then(
          function (response) {
            console.log(taskId + " ssh deleted!!");
          },
          function (response) {
            me.$root.$children[0].error('SSh 접속을 해제할 수 없습니다.');
          }
        );
      },
      /**
       * 주어진 타스크 아이프레임을 포커스 한다.
       * @param taskId
       */
      focusTaskFrame(taskId){
        var me = this;
        $.each(me.taskFrames, function (i, taskFrame) {
          if (taskFrame) {
            if (taskFrame.taskId == taskId) {
              taskFrame.show = true;
            } else {
              taskFrame.show = false;
            }
          }
        });
      },
      openSshFrame: function (taskId) {
        console.log('openSshFrame', taskId);
        var me = this;
        var taskFrame = {
          show: false,
          src: null,
          taskId: taskId,
          status: 'LOADING'
        };
        me.taskFrames.push(taskFrame);
        me.focusTaskFrame(taskId);

        this.$root.backend('ssh').save(null, {
          taskId: taskId,
          shell: me.shell
        })
          .then(
            function (response) {
              var src = 'http://' + me.publicAgentIp + ':' + response.data.container.portMappings[0].servicePort;
              //taskFrames 갱신.
              setTimeout(function () {
                taskFrame.src = src;
                taskFrame.status = 'SUCCESS';
              }, 1000);
            },
            function (response) {
              console.log(response);
              taskFrame.status = 'FAILED';
              me.$root.$children[0].error('컨테이너에 접속할 수 없습니다. 쉘이 올바른지 확인해주세요.');
            }
          );
      },
      makeSelectBoxList: function () {
        var me = this;
        me.tasks = [];
        me.selectedTaskIds = [];
        var marathonAppIds = [];
        if (me.stage == 'dev') {
          marathonAppIds = ['/' + me.appName + '-dev'];
        } else if (me.stage == 'stg') {
          marathonAppIds = ['/' + me.appName + '-stg'];
        } else if (me.stage == 'prod') {
          marathonAppIds = ['/' + me.appName + '-blue', '/' + me.appName + '-green'];
        }

        $.each(marathonAppIds, function (i, marathonAppId) {
          var tasksByAppId = me.getTasksByAppId(marathonAppId);
          var runningTasks = [];
          $.each(tasksByAppId, function (t, task) {
            if (task && task.state == 'TASK_RUNNING') {
              runningTasks.push(task);
            }
          });
          me.tasks = me.tasks.concat(runningTasks);
        });
//        stage 값이 dev 일 경우: /-dev
//        stage 값이 stg 일 경우: /-stg
//        stage 값이 prod 일 경우: /-green, /-blue

      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .md-input-container {
    margin-bottom: 10px;
  }
</style>
