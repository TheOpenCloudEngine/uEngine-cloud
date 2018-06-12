<template>
  <div>
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        last: null
      }
    },
    mounted() {
      this.last = this.$root.last || this.last;
      console.log('this.last', this.last);
    },
    watch: {
      '$root.last': {
        handler: function (newVal, oldVal) {
          this.last = newVal;
        },
        deep: true
      }
    },
    methods: {
      ddhhmmssDifFromDate: function (startDate, endDate) {
        var end = endDate ? endDate.getTime() : new Date().getTime();
        var start = startDate.getTime();
        var secs = (end - start) / 1000;
        var days = Math.floor(secs / 86400);
        secs -= days * 86400;
        var hours = Math.floor(secs / 3600) % 24;
        secs -= hours * 3600;
        var minutes = Math.floor(secs / 60) % 60;
        secs -= minutes * 60;
        var seconds = Math.round(secs % 60);  // in theory the modulus is not required
        var ddhhmmss = {
          days: days,
          hours: hours,
          minutes: minutes,
          seconds: seconds
        };

        var text = '';
        var ago = startDate && endDate ? '' : ' ago';
        if (ddhhmmss.days) {
          text = ddhhmmss.days + ' days' + ago;
        } else if (ddhhmmss.hours) {
          text = ddhhmmss.hours + ' hours' + ago;
        } else if (ddhhmmss.minutes) {
          text = ddhhmmss.minutes + ' minutes' + ago;
        } else {
          text = ddhhmmss.seconds + ' sec' + ago;
        }
        return text;
      },
      //=======================
      //=======Native marathon
      //=======================
      rollbackMarathonApp: function (deploymentId, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.dcos('service/marathon/v2/deployments/' + deploymentId)
          .remove({})
          .then(
            function (response) {
              me.$root.$children[0].success('배포를 중단하였습니다. 이전 설정으로 되돌아갑니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('배포를 중단할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      deleteMarathonApp: function (appId, force, cb) {
        var me = this;
        var app = this.getMarathonAppById(appId);
        var forceParam = force ? 'true' : 'false';
        me.$root.$children[0].block();
        this.$root.dcos('service/marathon/v2/apps/' + appId + '?force=' + forceParam)
          .remove({})
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 삭제하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 삭제할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      restartMarathonApp: function (appId, force, cb) {
        var me = this;
        var forceParam = force ? 'true' : 'false';
        me.$root.$children[0].block();
        this.$root.dcos('service/marathon/v2/apps/' + appId + '/restart?force=' + forceParam)
          .save({}, null)
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 재시작하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response, a, b) {
              me.$root.$children[0].error('어플리케이션을 재시작 할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      createMarathonApp: function (appJson, cb) {
        var me = this;
        var copy = JSON.parse(JSON.stringify(appJson));
        me.$root.$children[0].block();
        this.$root.dcos('service/marathon/v2/apps')
          .save({}, copy)
          .then(
            function (response) {
              me.$root.$children[0].success("서비스를 생성하였습니다.");
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error("서비스를 생성할 수 없습니다.");
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      updateMarathonApp: function (appId, appJson, force, cb) {
        var me = this;
        var forceParam = force ? 'true' : 'false';
        var copy = JSON.parse(JSON.stringify(appJson));
        me.$root.$children[0].block();
        this.$root.dcos('service/marathon/v2/apps/' + appId + '?partialUpdate=false&force=' + forceParam)
          .update({}, copy)
          .then(
            function (response) {
              me.$root.$children[0].success("서비스를 수정하였습니다.");
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error("서비스 수정에 실패하였습니다.");
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      scaleMarathonApp: function (appId, instances, force, cb) {
        var me = this;
        this.getMarathonAppById(appId, function (app) {
          if (app) {
            var forceParam = force ? 'true' : 'false';
            var copy = JSON.parse(JSON.stringify(app));
            copy.instances = instances;
            me.$root.$children[0].block();
            me.$root.dcos('service/marathon/v2/apps/' + appId + '?partialUpdate=false&force=' + forceParam)
              .update({}, copy)
              .then(
                function (response) {
                  me.$root.$children[0].success('인스턴스 개수가 변경되었습니다.');
                  if (cb) {
                    cb(response);
                  }
                },
                function (response, a, b) {
                  me.$root.$children[0].error('인스턴스 개수를 변경할 수 없습니다.');
                  if (cb) {
                    cb(null, response);
                  }
                })
              .finally(function () {
                me.$root.$children[0].unblock();
              });
          } else {
            me.$root.$children[0].error('인스턴스 개수를 변경할 수 없습니다.');
          }
        });
      },
      suspendMarathonApp: function (appId, force, cb) {
        var me = this;
        this.getMarathonAppById(appId, function (app) {
          if (app) {
            var forceParam = force ? 'true' : 'false';
            var copy = JSON.parse(JSON.stringify(app));
            copy.instances = 0;
            me.$root.$children[0].block();
            me.$root.dcos('service/marathon/v2/apps/' + appId + '?partialUpdate=false&force=' + forceParam)
              .update({}, copy)
              .then(
                function (response) {
                  me.$root.$children[0].success('어플리케이션을 정지하였습니다.');
                  if (cb) {
                    cb(response);
                  }
                },
                function (response) {
                  me.$root.$children[0].error('어플리케이션을 정지할 수 없습니다.');
                  if (cb) {
                    cb(null, response);
                  }
                })
              .finally(function () {
                me.$root.$children[0].unblock();
              });
          }
        });
      },

      //=======================
      //====marathon service
      //=======================
      getCommitRefFromMarathonApp: function (marathonApp) {
        if (!marathonApp) {
          return null;
        }
        var image = marathonApp.container.docker.image;
        return image.substring(image.lastIndexOf(":") + 1);
      },
      getMarathonAppsByAppName: function (appName, cb) {
        this.$root.backend('marathon/app?appName=' + appName).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getMarathonAppById: function (marathonAppId, cb) {
        this.$root.backend('marathon/app/' + marathonAppId).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getMarathonServiceApps: function (cb) {
        this.$root.backend('marathon/service').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getMesosTaskById: function (taskId, cb) {
        this.$root.backend('marathon/task?taskId=' + taskId).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getTasksBySlaveId: function (salveId, cb) {
        this.$root.backend('marathon/task/slave/' + salveId).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getHostBySlaveId: function (slaveId) {
        var selectedHost;
        if (!this.last) {
          return null;
        }
        $.each(this.last.slaves, function (s, slave) {
          if (slave.id == slaveId) {
            selectedHost = slave.hostname;
          }
        });
        return selectedHost;
      },

      //=======================
      //====app web service
      //=======================
      getApps: function (name, page, size, cb) {
        name = name ? name : '';
        this.$root.backend('app?name=' + name + '&page=' + page + '&size=' + size + '&sort=name').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getApp: function (appName, cb) {
        this.$root.backend('app/' + appName).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getAppMember: function (appName, cb) {
        this.$root.backend('app/' + appName + '/member').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      updateApp: function (appName, data, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName)
          .update({}, data)
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 정보를 업데이트 하였습니다.');
              cb(response);
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 정보를 업데이트 할 수 없습니다.');
              cb(null, response);
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      deleteApp: function (appName, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName)
          .remove({})
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 삭제하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 삭제할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      createApp: function (appCreate, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app')
          .save({}, appCreate)
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 생성하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 생성할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },


      //=======================
      //====deployment service
      //=======================
      removeDeployedApp: function (appName, stage, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/deploy?stage=' + stage)
          .remove({})
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션 ' + stage + ' 영역을 삭제하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션 ' + stage + ' 영역을 삭제할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      rollbackApp: function (appName, stage, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/rollback?stage=' + stage)
          .remove({})
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션을 롤백 하였습니다.');
            if (cb) {
              cb(response);
            }
          }, function (response) {
            me.$root.$children[0].error('어플리케이션을 롤백 할 수 없습니다.');
            if (cb) {
              cb(null, response);
            }
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      finishManualCanaryDeployment: function (appName, stage, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/finishManualCanaryDeployment?stage=' + stage)
          .remove({})
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 배포를 수동 종료 하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 배포를 수동 종료 할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      convertManualCanaryDeployment: function (appName, stage, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/convertManualCanaryDeployment?stage=' + stage)
          .update({})
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 배포를 수동 전환 하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 배포를 수동 전환 할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      deployApp: function (appName, stage, commit, cb) {
        //프로덕션을 배포할 경우 override confirm
        var me = this;
        var url = 'app/' + appName + '/deploy?stage=' + stage;
        if (commit) {
          url = url + '&commit=' + commit;
        }
        me.$root.$children[0].block();
        me.$root.backend(url)
          .save({})
          .then(
            function (response) {
              me.$root.$children[0].success("앱 배포를 시작하였습니다.");
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error("앱 배포 요청에 실패하였습니다.");
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      deployAppByExistStages: function (appName, stage) {
        var me = this;
        var existStages = [];
        me.getMarathonAppsByAppName(appName, function (response) {
          if (response && response.data) {
            if (response.data.dev) {
              existStages.push('dev');
            }
            if (response.data.stg) {
              existStages.push('stg');
            }
            if (response.data.prod) {
              existStages.push('prod');
            }
            var toChangeStages = [];
            //스테이지가 지정된 경우
            if (stage) {
              if (existStages.indexOf(stage) != -1) {
                toChangeStages.push(stage);
              }
            }
            //지정되지 않은 경우 (공통)
            else {
              toChangeStages = existStages;
            }
            if (toChangeStages && toChangeStages.length) {
              var stageNames = '';
              $.each(toChangeStages, function (i, toChangeStage) {
                if (toChangeStage == 'prod') {
                  stageNames += '[프로덕션] '
                }
                else if (toChangeStage == 'stg') {
                  stageNames += '[스테이징] '
                }
                else if (toChangeStage == 'dev') {
                  stageNames += '[개발] '
                }
              });
              me.$root.$children[0].confirm(
                {
                  contentHtml: '변경된 설정으로 인해 ' + stageNames + ' 서버들이 영향을 받습니다. 앱들을 재시작하겠습니까?',
                  okText: '진행하기',
                  cancelText: '취소',
                  callback: function () {
                    //스테이지 디플로이
                    $.each(toChangeStages, function (s, toChangeStage) {
                      me.deployApp(appName, toChangeStage, null, function (response) {

                      });
                    });
                  }
                });
            }

          }
        })
      },
      convertManualConfirm: function (appName, stage, cb) {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: '배포중인 작업을 수동 전환합니다. 진행하시겠습니까?',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.convertManualCanaryDeployment(appName, stage, cb);
            }
          });
      },
      finishManualConfirm: function (appName, stage, cb) {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: '배포를 완료합니다. 진행하시겠습니까?',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.finishManualCanaryDeployment(appName, stage, cb);
            }
          });
      },
      rollbackAppConfirm: function (appName, stage, cb) {
        var me = this;
        me.$root.$children[0].confirm(
          {
            contentHtml: '배포중인 작업을 취소합니다. 진행하시겠습니까?',
            okText: '진행하기',
            cancelText: '취소',
            callback: function () {
              me.rollbackApp(appName, stage, cb);
            }
          });
      },

      //=====================
      //====pipeline service
      //=====================
      getAppPipeLineLast: function (appName, cb) {
        this.$root.backend('app/' + appName + '/pipeline/last').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getAppPipeLineJson: function (appName, cb) {
        this.$root.backend('app/' + appName + '/pipeline/info').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      updateAppPipeLineJson: function (appName, json, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/pipeline/info')
          .update({}, json)
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션 빌드 설정을 하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('어플리케이션 빌드 설정을 할 수 없습니다.');
            cb(null, response);
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      excutePipelineTrigger: function (appName, ref, stage, cb) {
        var me = this;
        var url = 'app/' + appName + '/pipeline?ref=' + ref;
        if (stage) {
          url = url + '&stage=' + stage;
        }
        me.$root.$children[0].block();
        this.$root.backend(url)
          .save({})
          .then(
            function (response) {
              if (cb) {
                me.$root.$children[0].success('어플리케이션 빌드를 시작하였습니다.');
                cb(response);
              }
            },
            function (response) {
              if (cb) {
                me.$root.$children[0].error('어플리케이션을 빌드를 시작할 수 없습니다.');
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },

      //=======================
      //====deployjson service
      //=======================
      getAllDeployJson: function (appName, cb) {
        this.$root.backend('app/' + appName + '/deployJson').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getDeployJson: function (appName, stage, cb) {
        this.$root.backend('app/' + appName + '/deployJson?stage=' + stage).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      updateDeployJson: function (appName, stage, json, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/deployJson/?stage=' + stage)
          .update({}, json)
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션 설정을 업데이트 하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('어플리케이션 설정을 업데이트 할 수 없습니다.');
            cb(null, response);
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },

      //=====================
      //====config service
      //=====================
      getVcapServices: function (appName, cb) {
        this.$root.backend('app/' + appName + '/vcap').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getAppConfigYml: function (appName, stage, cb) {
        this.$root.backend('app/' + appName + '/config?stage=' + stage).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      updateAppConfigYml: function (appName, stage, yml, cb) {
        var me = this;
        var stageParam = stage ? stage : '';
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/config?stage=' + stageParam)
          .update({}, yml)
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션 환경 정보를 저장하였습니다.');

              me.deployAppByExistStages(appName, stage);
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 환경 정보를 저장할 수 없습니다.');
              if (cb) {
                cb(null, response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },

      //=====================
      //====git service
      //=====================
      getProject: function (projectId, cb) {
        this.$root.gitlab('api/v4/projects/' + projectId).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getGroupsIncludeMe: function (gitlabUserId, cb) {
        var me = this;
        var myGroups = [];
        var isDuplicated = function (groupId) {
          var flag = false;
          $.each(myGroups, function (g, group) {
            if (group.id == groupId) {
              flag = true;
            }
          });
          return flag;
        };

        this.$root.gitlab('api/v4/groups').get()
          .then(function (response) {
            var groups = response.data;
            var promises = [];
            if (groups.length) {
              $.each(groups, function (i, group) {
                promises.push(
                  me.$root.gitlab('api/v4/groups/' + group.id + '/members').get()
                )
              });
              Promise.all(promises)
                .then(function (results) {
                  $.each(results, function (r, members) {
                    $.each(members.data, function (m, member) {
                      //30 은 마스터, 40은 오너
                      if (member.id == gitlabUserId && member['access_level'] >= 30) {
                        if (!isDuplicated(groups[r].id)) {
                          myGroups.push(groups[r]);
                        }
                      }
                    })
                  });
                  cb(myGroups);
                })
            }
          }, function (response) {
            cb(null, response);
          })
      },
      getCommitInfo: function (projectId, commitRef, cb) {
        var me = this;
        var commitInfo = null;
        var tagList = [];
        this.$root.gitlab('api/v4//projects/' + projectId + '/repository/commits/' + commitRef).get()
          .then(function (response) {
            commitInfo = response.data;
            me.$root.gitlab('api/v4//projects/' + projectId + '/repository/tags').get()
              .then(function (response) {
                tagList = response.data;
                $.each(tagList, function (i, tag) {
                  if (tag.commit.id == commitInfo.id) {
                    commitInfo.tag = tag.name;
                  }
                });
                cb(commitInfo);
              }, function () {
                cb(commitInfo);
              });
          }, function (response) {
            cb(null, response);
          })
      },
      openGitlab: function (projectId, type, objectId) {
        this.getProject(projectId, function (response, err) {
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
      },

      //=====================
      //====metric service
      //=====================
      getCadvisorUrlBySlaveId: function (slaveId) {
        var url = null;
        var hostName = this.getHostBySlaveId(slaveId);
        var cadvisorList = config['cadvisor'];
        if (cadvisorList && cadvisorList.length) {
          $.each(cadvisorList, function (i, cadvisorSet) {
            var agentHost = cadvisorSet.split(',')[0];
            var cadvisorUrl = cadvisorSet.split(',')[1];
            if (agentHost == hostName) {
              url = cadvisorUrl;
            }
          })
        }
        return url;
      },

      //=====================
      //====category service
      //=====================
      getCategoryItem: function (itemId, cb) {
        if (itemId == 'import') {
          cb({
            id: "import",
            category: "app",
            header: "Fork from git",
            title: "Fork from git",
            description: "Git url 로부터 소스코드를 임포트하여 새로운 앱을 시작합니다.",
            version: "1.0",
            logoSrc: '/static/image/catalog/import.png',
            type: '애플리케이션'
          })
          return;
        }

        if (itemId == 'github') {
          cb({
            id: "github",
            category: "app",
            header: "Sync with github",
            title: "Sync with github",
            description: "기존의 보유한 Github 레파지토리를 클라우드 플랫폼과 연동합니다.",
            version: "1.0",
            logoSrc: '/static/image/catalog/github.svg',
            type: '애플리케이션'
          })
          return;
        }
        var selected = null;
        this.$root.backend('catalog').get()
          .then(function (response) {
            var catalog = response.data;
            if (catalog && catalog.length) {
              $.each(catalog, function (i, category) {
                if (category.items.length) {
                  $.each(category.items, function (t, item) {
                    if (item.id == itemId) {
                      selected = item;
                    }
                  })
                }
              })
            }
            cb(selected, null);
          }, function (response) {
            cb(null, response);
          });
      },

      //=====================
      //====snapshot service
      //=====================
      getSnapshotById: function (snapshotId, cb) {
        this.$root.backend('snapshots/' + snapshotId).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      createSnapshot: function (appName, snapshotName, cb) {
        var me = this;
        me.$root.$children[0].block();
        var url = 'app/' + appName + '/snapshot';
        if (snapshotName) {
          url = url + '?name=' + encodeURI(snapshotName);
        }
        this.$root.backend(url)
          .save({})
          .then(function (response) {
            me.$root.$children[0].success('스냅샷을 생성하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('스냅샷을 생성할 수 없습니다.');
            cb(null, response);
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
