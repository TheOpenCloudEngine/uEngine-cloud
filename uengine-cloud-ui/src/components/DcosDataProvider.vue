<template>
  <div>
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        dcosData: {
          last: null,
          jobs: null,
          groups: null,
          queue: null,
          deployments: null,
          units: null,
          state: null,
          devopsApps: null,
          config: null
        }
      }
    },
    mounted() {
      this.dcosData = this.$root.dcosData || this.dcosData;
    },
    watch: {
      '$root.dcosData': {
        handler: function (newVal, oldVal) {
          this.dcosData = newVal;
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
      rollbackDcosApp: function (deploymentId, cb) {
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
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      deleteDcosApp: function (appId, force, cb) {
        var me = this;
        var app = this.getDcosAppById(appId);
        if (app) {
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
                  cb(response);
                }
              })
            .finally(function () {
              me.$root.$children[0].unblock();
            });
        }
      },
      restartDcosApp: function (appId, force, cb) {
        var me = this;
        var app = this.getDcosAppById(appId);
        if (app) {
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
                  cb(response);
                }
              })
            .finally(function () {
              me.$root.$children[0].unblock();
            });
        }
      },
      createDcosApp: function (appJson, cb) {
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
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      updateDcosApp: function (appId, appJson, force, cb) {
        var me = this;
        var app = this.getDcosAppById(appId);
        if (app) {
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
                  cb(response);
                }
              })
            .finally(function () {
              me.$root.$children[0].unblock();
            });
        }
      },
      scaleDcosApp: function (appId, instances, force, cb) {
        var me = this;
        var app = this.getDcosAppById(appId);
        if (app) {
          var forceParam = force ? 'true' : 'false';
          var copy = JSON.parse(JSON.stringify(app));
          copy.instances = instances;
          me.$root.$children[0].block();
          this.$root.dcos('service/marathon/v2/apps/' + appId + '?partialUpdate=false&force=' + forceParam)
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
                  cb(response);
                }
              })
            .finally(function () {
              me.$root.$children[0].unblock();
            });
        }
      },
      suspendDcosApp: function (appId, force, cb) {
        var me = this;
        var app = this.getDcosAppById(appId);
        if (app) {
          var forceParam = force ? 'true' : 'false';
          var copy = JSON.parse(JSON.stringify(app));
          copy.instances = 0;
          me.$root.$children[0].block();
          this.$root.dcos('service/marathon/v2/apps/' + appId + '?partialUpdate=false&force=' + forceParam)
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
                  cb(response);
                }
              })
            .finally(function () {
              me.$root.$children[0].unblock();
            });
        }
      },
      getJobById: function (jobId) {
        var selectedJob;
        if (!this.dcosData.jobs) {
          return null;
        }
        $.each(this.dcosData.jobs, function (i, job) {
          if (job.id == jobId) {
            selectedJob = job;
          }
        });
        return selectedJob;
      },
      getDcosAppById: function (appId) {
        var selectedApp;
        if (!this.dcosData.groups) {
          return null;
        }
        $.each(this.dcosData.groups.apps, function (i, app) {
          if (app.id == appId) {
            selectedApp = app;
          }
        });
        return selectedApp;
      },
      getTasksByNodeId: function (nodeId) {
        var tasks = [];
        if (!this.dcosData.state) {
          return null;
        }
        $.each(this.dcosData.state.frameworks, function (f, framework) {
          $.each(framework['completed_tasks'], function (i, task) {
            if (nodeId == task['slave_id']) {
              tasks.push(task);
            }
          });
          $.each(framework['tasks'], function (i, task) {
            if (nodeId == task['slave_id']) {
              tasks.push(task);
            }
          });
        });
        return tasks;
      },
      getTasksByAppId: function (appId) {
        if (appId && appId.indexOf('/') == -1) {
          appId = '/' + appId;
        }
        var tasks = [];
        if (!this.dcosData.state) {
          return null;
        }
        $.each(this.dcosData.state.frameworks, function (f, framework) {
          if (framework.name == 'marathon') {
            $.each(framework['completed_tasks'], function (i, task) {
              if (appId == '/' + task.name) {
                tasks.push(task);
              }
            });
            $.each(framework['tasks'], function (i, task) {
              if (appId == '/' + task.name) {
                tasks.push(task);
              }
            });
          }
        });
        return tasks;
      },
      getTasksByJobId: function (jobId) {
        var tasks = [];
        if (!this.dcosData.state) {
          return null;
        }
        $.each(this.dcosData.state.frameworks, function (f, framework) {
          if (framework.name == 'metronome') {
            $.each(framework['completed_tasks'], function (i, task) {
              if (jobId == task.name.split('.')[1]) {
                tasks.push(task);
              }
            });
            $.each(framework['tasks'], function (i, task) {
              if (jobId == task.name.split('.')[1]) {
                tasks.push(task);
              }
            });
          }
        });
        return tasks;
      },
      getTaskById: function (taskId) {
        var selectedTask;
        if (!this.dcosData.state) {
          return null;
        }
        $.each(this.dcosData.state.frameworks, function (f, framework) {
          $.each(framework['completed_tasks'], function (i, task) {
            if (taskId == task.id) {
              selectedTask = task;
            }
          });
          $.each(framework['tasks'], function (i, task) {
            if (taskId == task.id) {
              selectedTask = task;
            }
          });
        });
        return selectedTask;
      },
      getHostBySlaveId: function (slaveId) {
        var selectedHost;
        if (!this.dcosData.state) {
          return null;
        }
        $.each(this.dcosData.state.slaves, function (s, slave) {
          if (slave.id == slaveId) {
            selectedHost = slave.hostname;
          }
        });
        return selectedHost;
      },
      getDevopsAppById: function (appName) {
        var selectedApp;
        if (!this.dcosData.devopsApps) {
          return null;
        }
        for (var id in this.dcosData.devopsApps) {
          if (id == appName) {
            selectedApp = this.dcosData.devopsApps[id];
          }
        }
        return selectedApp;
      },
      getAppsByDevopsId: function (appName) {
        var devApp = this.getDevopsAppById(appName);
        if (devApp) {
          var newProd;
          if (devApp.prod.deployment == 'green') {
            newProd = this.getDcosAppById('/' + appName + '-blue');
          } else {
            newProd = this.getDcosAppById('/' + appName + '-green');
          }
          var data = {
            prod: this.getDcosAppById(devApp.prod['marathonAppId']),
            stg: this.getDcosAppById(devApp.stg['marathonAppId']),
            dev: this.getDcosAppById(devApp.dev['marathonAppId'])
          };
          if (newProd) {
            data.newProd = newProd;
          }
          return data;
        } else {
          return null;
        }
      },
      updateDevApp: function (appName, data, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName)
          .update({}, data)
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 저장하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 저장할 수 없습니다.');
              if (cb) {
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      rollbackDevApp: function (appName, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/rollback')
          .save({})
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션을 롤백 하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 롤백 할 수 없습니다.');
              if (cb) {
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      getAppPipeLineJson: function (appName, cb) {
        this.$root.backend('app/' + appName + '/pipeline/info').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      createApp: function (appCreate, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app')
          .save({}, appCreate)
          .then(
            function (response) {
              me.$root.$children[0].error('어플리케이션을 생성하였습니다.');
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 생성할 수 없습니다.');
              if (cb) {
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      updateAppExcludeDeployJson: function (appName, json, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '?excludeDeploy=true')
          .update({}, json)
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션 정보를 업데이트 하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('어플리케이션 정보를 업데이트 할 수 없습니다.');
            cb(null, response);
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      updateApp: function (appName, json, cb) {
        var me = this;
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName)
          .update({}, json)
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션 구동 설정을 업데이트 하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('어플리케이션 구동 설정을 변경할 수 없습니다.');
            cb(null, response);
          })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
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
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      runDeployedApp: function (appName, stage, commit, cb) {
        var me = this;
        var url = 'app/' + appName + '/deploy?stage=' + stage;
        if (commit) {
          url = url + '&commit=' + commit;
        }
        me.$root.$children[0].block();
        this.$root.backend(url)
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
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      removeDevAppByName: function (appName, cb) {
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
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      removeDevAppStage: function (appName, stage, cb) {
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
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      getDevAppByName: function (appName, cb) {
        this.$root.backend('app/' + appName).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getDevAppStatusByName: function (appName, cb) {
        this.$root.backend('app/' + appName + '/status').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getDevAppVcapYml: function (appName, cb) {
        this.$root.backend('app/' + appName + '/vcap').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getDevAppConfigYml: function (appName, stage, cb) {
        this.$root.backend('app/' + appName + '/config?stage=' + stage).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      updateDevAppConfigYml: function (appName, stage, yml, cb) {
        var me = this;
        var stageParam = stage ? stage : '';
        me.$root.$children[0].block();
        this.$root.backend('app/' + appName + '/config?stage=' + stageParam)
          .update({}, yml)
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션 환경 정보를 저장하였습니다.');

              var existStages = [];
              if (me.getDcosAppById('/' + appName + '-green') || me.getDcosAppById('/' + appName + '-blue')) {
                existStages.push('prod')
              }
              if (me.getDcosAppById('/' + appName + '-dev')) {
                existStages.push('dev')
              }
              if (me.getDcosAppById('/' + appName + '-stg')) {
                existStages.push('stg')
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
                        me.runDeployedApp(appName, toChangeStage, null, function (response) {

                        });
                      });
                    }
                  });
              }
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              me.$root.$children[0].error('어플리케이션을 환경 정보를 저장할 수 없습니다.');
              if (cb) {
                cb(response);
              }
            })
          .finally(function () {
            me.$root.$children[0].unblock();
          });
      },
      getProject: function (projectId, cb) {
        this.$root.gitlab('api/v4/projects/' + projectId).get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      getGroupsIncludMe: function (gitlabUserId, cb) {
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
      getCategoryItem: function (itemId, cb) {
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
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
