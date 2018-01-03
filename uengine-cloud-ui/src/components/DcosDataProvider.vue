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
      rollback: function (deploymentId, cb) {
        var me = this;
        this.$root.dcos('service/marathon/v2/deployments/' + deploymentId)
          .remove({})
          .then(
            function (response) {
              me.$root.$children[0].success('배포를 중단하였습니다.');
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
      },
      deleteApp: function (appId, force, cb) {
        var me = this;
        var app = this.getAppById(appId);
        if (app) {
          var forceParam = force ? 'true' : 'false';
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
        }
      },
      restartApp: function (appId, force, cb) {
        var me = this;
        var app = this.getAppById(appId);
        if (app) {
          var forceParam = force ? 'true' : 'false';
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
        }
      },
      scaleApp: function (appId, instances, force, cb) {
        var me = this;
        var app = this.getAppById(appId);
        if (app) {
          var forceParam = force ? 'true' : 'false';
          var copy = JSON.parse(JSON.stringify(app));
          copy.instances = instances;
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
        }
      },
      suspendApp: function (appId, force, cb) {
        var me = this;
        var app = this.getAppById(appId);
        if (app) {
          var forceParam = force ? 'true' : 'false';
          var copy = JSON.parse(JSON.stringify(app));
          copy.instances = 0;
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
      getAppById: function (appId) {
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
        for (var id in this.dcosData.devopsApps.dcos.apps) {
          if (id == appName) {
            selectedApp = this.dcosData.devopsApps.dcos.apps[id];
          }
        }
        return selectedApp;
      },
      getAppsByDevopsId: function (appName) {
        var devApp = this.getDevopsAppById(appName);
        if (devApp) {
          var newProd;
          if (devApp.prod.deployment == 'green') {
            newProd = this.getAppById('/' + appName + '-blue');
          } else {
            newProd = this.getAppById('/' + appName + '-green');
          }
          var data = {
            prod: this.getAppById(devApp.prod['marathonAppId']),
            stg: this.getAppById(devApp.stg['marathonAppId']),
            dev: this.getAppById(devApp.dev['marathonAppId'])
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
      },
      rollbackDevApp: function (appName, cb) {
        var me = this;
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
      },
      getAppPipeLineJson: function (appName, cb) {
        this.$root.backend('app/' + appName + '/pipeline/info').get()
          .then(function (response) {
            cb(response);
          }, function (response) {
            cb(null, response);
          })
      },
      updateAppExcludeDeployJson: function (appName, json, cb) {
        var me = this;
        this.$root.backend('app/' + appName + '?excludeDeploy=true')
          .update({}, json)
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션 정보를 업데이트 하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('어플리케이션 정보를 업데이트 할 수 없습니다.');
            cb(null, response);
          })
      },
      updateApp: function (appName, json, cb) {
        var me = this;
        this.$root.backend('app/' + appName)
          .update({}, json)
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션 빌드 설정을 하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('어플리케이션 빌드 설정을 할 수 없습니다.');
            cb(null, response);
          })
      },
      updateAppPipeLineJson: function (appName, json, cb) {
        var me = this;
        this.$root.backend('app/' + appName + '/pipeline/info')
          .update({}, json)
          .then(function (response) {
            me.$root.$children[0].success('어플리케이션 빌드 설정을 하였습니다.');
            cb(response);
          }, function (response) {
            me.$root.$children[0].error('어플리케이션 빌드 설정을 할 수 없습니다.');
            cb(null, response);
          })
      },
      excutePipelineTrigger: function (appName, ref, stage, cb) {
        var me = this;
        var url = 'app/' + appName + '/pipeline?ref=' + ref;
        if (stage) {
          url = url + '&stage=' + stage;
        }
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
      },
      runDeployedApp: function (appName, stage, commit, cb) {
        var me = this;
        var url = 'app/' + appName + '/deploy?stage=' + stage;
        if (commit) {
          url = url + '&commit=' + commit;
        }
        this.$root.backend(url)
          .save({})
          .then(
            function (response) {
              if (cb) {
                cb(response);
              }
            },
            function (response) {
              if (cb) {
                cb(response);
              }
            })
      },
      removeDevAppByName: function (appName, cb) {
        var me = this;
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
      },
      removeDevAppStage: function (appName, stage, cb) {
        var me = this;
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
      getCategoryItemById: function (categoryItemId, cb) {
        var me = this;
        var selectedItem;
        $.get('/static/catalog.json', function (catalog) {
          for (var key in catalog['category']) {
            var category = catalog['category'][key];
            $.each(category.items, function (i, item) {
              if (item.id == categoryItemId) {
                selectedItem = item;
              }
            })
          }
          cb(selectedItem);
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
        this.$root.backend('app/' + appName + '/config?stage=' + stage)
          .update({}, yml)
          .then(
            function (response) {
              me.$root.$children[0].success('어플리케이션 환경 정보를 저장하였습니다.');
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
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
