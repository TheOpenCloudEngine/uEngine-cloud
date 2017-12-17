<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-layout style="width:100%;height:83vh;" v-if="model">
      <md-layout class="list-link" md-flex="20">
        <div class="phone-viewport" style="width: 100%;">
          <md-list class="md-dense">
            <md-list-item>
              <span class="listspan" @click="changeView('serviceview')">Service</span>
            </md-list-item>

            <md-list-item>
              <span class="listspan" @click="changeView('networkingview')">Networking</span>
            </md-list-item>

            <md-list-item>
              <span class="listspan" @click="changeView('volumesview')">Volumes</span>
            </md-list-item>

            <md-list-item>
              <span class="listspan" @click="changeView('healthchecksview')">Health Checks</span>
            </md-list-item>

            <md-list-item>
              <span class="listspan" @click="changeView('environmentview')">Environment</span>
            </md-list-item>
          </md-list>
        </div>
      </md-layout>
      <md-layout class="main-body" style="margin-left: 3%;overflow-y: scroll;">
        <div id="errorForm" style="background-color: rgba(243,55,69,.1);width: 90%;margin-top:5%;" v-if="errorView">
          <h3 style="color:#f33745;
          background: url(' ../../../static/image/symbol/small_cancel_end.gif') left no-repeat;
          padding-left:20px; margin-left:10px;">
            There is an error with your configuration</h3>
          <ul style="color:#f33745;list-style: none;">
            <li v-if="id == '/'">Service ID must be defined</li>
            <li v-if="!cpus && cpus==0">CPUs must be bigger than or equal to 0.001</li>
            <li v-if="!container.docker.image && !cmd">cmd: You must specify a command, an argument or a container</li>
            <li v-if="!container.docker.image">args: You must specify a command, an argument or a container</li>
            <li v-if="!container.docker.image">
              container.docker.image: You must specify a command, an argument or a container
            </li>
            <li v-if="!container.docker.image">container.docker.image: Must be defined</li>
          </ul>
        </div>


        <!------------------------------------------------------------------------Service------------------------------------------------>
        <div v-if="menu.serviceview" style="width: 100%">
          <h3 class="md-title">Service</h3>
          <span class="md-body-2">Configure your service below. Start by giving your service an ID.</span><br/>
          <md-layout class="mt30">
            <md-layout md-flex="70" class="mr5">
              SERVICE ID &nbsp;<span style="color: #ff5c3c">*</span>
              <div>
                <md-button class="md-primary small">?
                  <md-tooltip class="fontb" md-direction="top">Include the path to your service, if applicable.<br>E.g.
                    /dev/tools/my-service.ool
                  </md-tooltip>
                </md-button>
              </div>
              <md-input-container>
                <md-input v-model="id" :disabled="id.indexOf('{{')!=-1 && id.indexOf('}}')!=-1"></md-input>
              </md-input-container>
            </md-layout>
            <md-layout md-flex="20">
              INSTANCES
              <md-input-container>
                <md-input v-model.number="instances" type="number"></md-input>
              </md-input-container>
            </md-layout>
          </md-layout>
          <span class="md-caption">Give your service a unique name within the cluster, e.g. my-service.</span>

          <md-layout class="mt30">
            <md-layout md-flex="45" class="mr5">
              CONTAINER IMAGE
              <div>
                <md-button class="md-primary small">?
                  <md-tooltip class="fontb" md-direction="top">
                    Enter a Docker image or browse Docker Hub to find more. You can also enter an image from your
                    private registry
                  </md-tooltip>
                </md-button>
              </div>
              <md-input-container>
                <md-input v-model="container.docker.image"
                          :disabled="container.docker.image.indexOf('{{')!=-1 && container.docker.image.indexOf('}}')!=-1"></md-input>
              </md-input-container>
            </md-layout>
            <md-layout md-flex="20" class="mr5">
              CPUs &nbsp;<span style="color: #ff5c3c">*</span>
              <md-input-container>
                <md-input v-model.number="cpus" type="number"></md-input>
              </md-input-container>
            </md-layout>
            <md-layout md-flex="20">
              Memory (MiB) &nbsp;<span style="color: #ff5c3c">*</span>
              <md-input-container>
                <md-input v-model.number="mem" type="number"></md-input>
              </md-input-container>
            </md-layout>
          </md-layout>
          <span class="md-caption">Enter a Docker image you want to run, e.g. nginx.</span>

          <md-layout class="mt30">
            <md-layout class="mr5">
              COMMAND
              <div>
                <md-button class="md-primary small">?
                  <md-tooltip class="fontb" md-direction="top">
                    The command value will be wrapped by the underlying Mesos executor via /bin/sh -c ${cmd}.
                  </md-tooltip>
                </md-button>
              </div>
              <md-input-container>
                <md-input v-model="cmd"></md-input>
              </md-input-container>
              <span class="md-caption">A shell command for your container to execute.</span>
            </md-layout>
          </md-layout>
          <a @click="expandSettings('moresetting')">
            <span v-if="moresetting">▼</span> <span v-else>▶</span>
            MORE SETTINGS
          </a>
          <div v-if="moresetting" class="mt30">
            <span class="md-title">Container Runtime</span><br>
            <span class="md-subheading">The container runtime is responsible for running your service. We support the Docker Engine and Universal Container Runtime (UCR).</span><br>
            <div style="width: 100%">
              <md-radio v-model="container.type" mdValue="DOCKER" class="md-primary">DOCKER ENGINE</md-radio>
              <br>
              <span class="md-caption">Docker’s container runtime. No support for multiple containers (Pods) or GPU resources.</span>
            </div>
            <div style="width: 100%">
              <md-radio v-model="container.type" mdValue="MESOS" class="md-primary">
                UNIVERSAL CONTAINER RUNTIME (UCR)
              </md-radio>
              <br>
              <span class="md-caption">Universal Container Runtime using native Mesos engine. Supports Docker file format, multiple containers (Pods) and GPU resources.</span>
            </div>

            <span class="md-title">Placement Constraints</span><br>
            <span class="md-subheading">Constraints control where apps run to allow optimization for either fault tolerance or locality.</span><br>
            <div>
              <md-layout v-for="(constraint,index) in constraints">
                <md-layout md-flex="25" class="mr5">
                  <div v-if="index==0">FIELD &nbsp;<span style="color: #ff5c3c">*</span>
                    <div style="display: inline;">
                      <md-button class="md-primary small">?
                        <md-tooltip class="fontb" md-direction="top">
                          If you enter `hostname`, the constraint will map to the agent node hostname. If you do not
                          enter an agent node hostname, the field will be treated as a Mesos agent node attribute, which
                          allows you to tag an agent node.
                        </md-tooltip>
                      </md-button>
                    </div>
                  </div>
                  <md-input-container>
                    <md-input v-model="constraint[0]"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex class="mr5">
                  <div v-if="index==0">OPERATOR &nbsp;<span style="color: #ff5c3c">*</span>
                    <div style="display: inline;">
                      <md-button class="md-primary small">?
                        <md-tooltip class="fontb" md-direction="top">
                          Operators specify where your app will run.
                        </md-tooltip>
                      </md-button>
                    </div>
                  </div>
                  <md-input-container>
                    <md-select v-model="constraint[1]">
                      <md-option value="">Select</md-option>
                      <md-option v-for="value in operator" :value="value">{{value}}</md-option>
                    </md-select>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="25" v-if="constraint[1] != 'UNIQUE'">
                  <div v-if="index==0">VALUE &nbsp;<span style="color: #ff5c3c">*</span>
                    <div style="display: inline;">
                      <md-button class="md-primary small">?
                        <md-tooltip class="fontb" md-direction="top">
                          Values allow you to further specify your constraint. Learn more.
                        </md-tooltip>
                      </md-button>
                    </div>
                  </div>
                  <md-input-container>
                    <md-input v-model="constraint[2]"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="10">
                  <button style="border: hidden; background-color: inherit;margin-bottom: 20px;"
                          v-on:click="constraints.splice(index,1)"
                  ><b>X</b></button>
                </md-layout>
              </md-layout>
              <a @click="constraints.push([])">+ ADD PLACEMENT CONSTRAINT</a>


              <div class="mt30">
                <span class="md-title">Advanced Settings</span><br>
                <span class="md-subheading">Advanced settings related to the runtime you have selected above.</span><br>
                <div style="width: 100%">
                  <md-checkbox class="md-primary" v-model="container.docker.privileged"
                               :disabled="model.container.type=='MESOS'">
                    GRANT RUNTIME PRIVILEGES
                  </md-checkbox>
                  <div class="md-caption">
                    By default, containers are “unprivileged” and cannot, for example, run a Docker daemon inside a
                    Docker container.
                  </div>
                </div>
                <div style="width: 100%">
                  <md-checkbox class="md-primary" v-model="container.docker.forcePullImage">FORCE PULL IMAGE ON LAUNCH
                  </md-checkbox>
                  <div class="md-caption">Force Docker to pull the image before launching each instance.</div>
                </div>
              </div>
              <md-layout class="mt10">
                <md-layout md-flex="25" class="mr5">
                  GPUs
                  <md-input-container>
                    <md-input type="number" v-model.number="gpus"
                              :disabled="model.container.type!='MESOS'"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="25">
                  Disk (MiB)
                  <md-input-container>
                    <md-input type="number" v-model.number="disk"></md-input>
                  </md-input-container>
                </md-layout>
              </md-layout>
              <div style="width: 100%;">
                ARTIFACT URI
                <div style="display: inline;">
                  <md-button class="md-primary small">?
                    <md-tooltip class="fontb" md-direction="top">
                      If your service requires additional files and/or archives of files, enter their URIs to download
                      and, if necessary, extract these resources.
                    </md-tooltip>
                  </md-button>
                </div>
                <md-layout v-for="(item,index) in fetch">
                  <md-layout md-flex="85" class="mr5">
                    <md-input-container md-clearable>
                      <md-input v-model="item.uri" placeholder="http://example.com"></md-input>
                    </md-input-container>
                  </md-layout>
                  <md-layout md-flex="10">
                    <button style="border: hidden; background-color: inherit;margin-bottom: 20px;"
                            v-on:click="fetch.splice(index,1)"
                    ><b>X</b></button>
                  </md-layout>
                </md-layout>
                <div>
                  <a v-on:click="fetch.push({})">+ ADD ARTIFACT</a>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!------------------------------------------------------------------------Networking------------------------------------------------>
        <div v-if="menu.networkingview" style="width: 100%">
          <h1 class="md-title">Networking</h1>
          <span class="md-body-2">Configure the networking for your service.</span><br/>
          <md-layout class="mt30">
            <md-layout md-flex="70" class="mr5">
              NETWORK TYPE
              <md-input-container>
                <md-select v-model="networks[0].mode">
                  <md-option value="host">Host</md-option>
                  <md-option value="container/bridge">Bridge</md-option>
                </md-select>
              </md-input-container>
            </md-layout>
          </md-layout>
          <span class="md-title">Service Endpoints</span><br/>
          <div class="md-caption">
            DC/OS can automatically generate a Service Address to connect to each of your load balanced endpoints.
          </div>
          <md-checkbox v-if="networks[0].mode=='host'" v-model="portsAutoAssign" class="md-primary">
            ASSIGN HOST PORTS AUTOMATICALLY
          </md-checkbox>


          <div class="add-input mt10" v-if="networks[0].mode=='host'" v-for="(portDefinition,index) in portDefinitions">
            <span class="md-subheading">SERVICE ENDPOINT NAME</span>
            <div style="display: inline;">
              <md-button class="md-primary small">?
                <md-tooltip class="fontb" md-direction="top">
                  Name your endpoint to search for it by a meaningful name, rather than the port number.
                </md-tooltip>
              </md-button>
            </div>
            <div style="float: right;">
              <button style="border: hidden; background-color: inherit;margin-bottom: 20px;"
                      v-on:click="portDefinitions.splice(index,1)"><b>X</b></button>
            </div>
            <md-input-container style="width: 80%;height: 20px;">
              <md-input v-model="portDefinition.name"></md-input>
            </md-input-container>
            <md-layout>
              <md-layout md-flex="40" class="mr5">
                <span class="md-subheading">HOST PORT</span>
                <div style="display: inline;">
                  <md-button class="md-primary small">?
                    <md-tooltip class="fontb" md-direction="top">
                      This host port will be accessible as an environment variable called '$PORT0'.
                    </md-tooltip>
                  </md-button>
                </div>
                <md-input-container style="width: 100%;height: 20px;">
                  <md-input v-model.number="portDefinition.port" :disabled="portsAutoAssign"
                            :placeholder="portsAutoAssign?'$PORT'+index:''" type="number"></md-input>
                </md-input-container>
              </md-layout>
              <md-layout md-flex="30">
                <span class="md-subheading">PROTOCOL</span>
                <div style="">
                  <md-button class="md-primary small">?
                    <md-tooltip class="fontb" md-direction="top">
                      Most services will use TCP.
                    </md-tooltip>
                  </md-button>
                </div>
                <div>
                  <md-checkbox v-if="networks[0].mode=='host'" v-model="portDefinition.protocolUdp"
                               class="md-primary">UDP
                  </md-checkbox>
                  <md-checkbox v-if="networks[0].mode=='host'" v-model="portDefinition.protocolTcp"
                               class="md-primary">TCP
                  </md-checkbox>
                </div>
              </md-layout>
            </md-layout>
            <md-checkbox v-if="networks[0].mode=='host'" v-model="portDefinition.enableLoadBalanced" class="md-primary">
              ENABLE LOAD BALANCED SERVICE ADDRESS
            </md-checkbox>
            <div v-if="portDefinition.enableLoadBalanced">
              <md-layout>
                <md-layout md-flex="40" class="mr5">
                  <span class="md-subheading">LOAD BALANCED PORT</span>
                  <div style="display: inline;">
                    <md-button class="md-primary small">?
                      <md-tooltip class="fontb" md-direction="top">
                        This port will be used to load balance this service address internally
                      </md-tooltip>
                    </md-button>
                  </div>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-input v-model.number="portDefinition.vipPort" type="number"></md-input>
                  </md-input-container>
                </md-layout>
              </md-layout>
            </div>
            <div class="md-caption">Load balance this service internally at .marathon.l4lb.thisdcos.directory:1</div>
          </div>

          <div class="add-input mt10" v-if="networks[0].mode!='host'"
               v-for="(portDefinition,index) in portDefinitions">
            <md-layout>
              <md-layout md-flex="30" class="mr5">
                <span class="md-subheading">CONTAINER PORT</span>
                <md-input-container style="width: 100%;height: 20px;">
                  <md-input v-model.number="portDefinition.containerPort" type="number"></md-input>
                </md-input-container>
              </md-layout>
              <md-layout md-flex="50" class="mr5">
                <span class="md-subheading">SERVICE ENDPOINT NAME</span>
                <div style="display: inline;">
                  <md-button class="md-primary small">?
                    <md-tooltip class="fontb" md-direction="top">
                      Name your endpoint to search for it by a meaningful name, rather than the port number.
                    </md-tooltip>
                  </md-button>
                </div>
                <md-input-container style="width: 100%;height: 20px;">
                  <md-input v-model="portDefinition.name"></md-input>
                </md-input-container>
              </md-layout>
              <md-layout md-flex="10">
                <button style="border: hidden; background-color: inherit;margin-bottom: 20px;"
                        v-on:click="portDefinitions.splice(index,1)"><b>X</b></button>
              </md-layout>
            </md-layout>
            <md-layout>
              <md-layout md-flex="30" class="mr4">
                <span class="md-subheading">HOST PORT</span>
                <div style="display: inline;">
                  <md-button class="md-primary small">?
                    <md-tooltip class="fontb" md-direction="top">
                      This host port will be accessible as an environment variable called '$PORT0'.
                    </md-tooltip>
                  </md-button>
                </div>
                <md-input-container style="width: 100%;height: 20px;">
                  <md-input v-model.number="portDefinition.hostPort" :disabled="portDefinition.portsAutoAssign"
                            type="number"
                            :placeholder="portDefinition.portsAutoAssign?'$PORT'+index:''"></md-input>
                </md-input-container>
              </md-layout>
              <md-layout md-flex="30">
                <md-checkbox v-if="networks[0].mode!='host'" v-model="portDefinition.portsAutoAssign" class="md-primary"
                             style="margin-top: 40px;">ASSIGN AUTOMATICALLY
                </md-checkbox>
              </md-layout>
              <md-layout md-flex="30">
                <span class="md-subheading">PROTOCOL</span>
                <div style="display: inline;">
                  <md-button class="md-primary small">?
                    <md-tooltip class="fontb" md-direction="top">
                      Most services will use TCP.
                    </md-tooltip>
                  </md-button>
                </div>
                <div style="width: 100%;">
                  <md-checkbox v-if="networks[0].mode!='host'" v-model="portDefinition.protocolUdp" class="md-primary">
                    UDP
                  </md-checkbox>
                  <md-checkbox v-if="networks[0].mode!='host'" v-model="portDefinition.protocolTcp" class="md-primary">
                    TCP
                  </md-checkbox>
                </div>
              </md-layout>
            </md-layout>
            <md-checkbox v-if="networks[0].mode!='host'" v-model="portDefinition.enableLoadBalanced"
                         class="md-primary">ENABLE LOAD BALANCED SERVICE ADDRESS
            </md-checkbox>
            <div v-if="portDefinition.enableLoadBalanced">
              <md-layout>
                <md-layout md-flex="35" class="mr5">
                  <span class="md-subheading">LOAD BALANCED PORT</span>
                  <div style="display: inline;">
                    <md-button class="md-primary small">?
                      <md-tooltip class="fontb" md-direction="top">
                        This port will be used to load balance this service address internally
                      </md-tooltip>
                    </md-button>
                  </div>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-input v-model.number="portDefinition.vipPort" type="number"></md-input>
                  </md-input-container>
                </md-layout>
              </md-layout>
            </div>
            <div class="md-caption">Load balance this service internally at .marathon.l4lb.thisdcos.directory:1</div>
          </div>

          <br>
          <a v-on:click="portDefinitions.push({})">+ ADD SERVICE ENDPOINT</a>
        </div>

        <!-------------------------------------------------Volumes -------------------------------------------------------------->
        <div v-if="menu.volumesview" style="width: 100%">
          <h1 class="md-title">Volumes</h1>
          <span class="md-body-2">Create a stateful service by configuring a persistent volume. Persistent volumes enable instances to be restarted without data loss.</span><br/>
          <div class="mt30">
            <span class="md-title">Local Volumes</span><br/>
            <div
              class="md-caption">Choose a local persistent volume if you need quick access to stored data.
            </div>
            <br>

            <div class="add-input mt10" v-for="(localVolume,index) in localVolumes">
              <md-layout>
                <md-layout md-flex="95">
                  <span class="md-subheading">VOLUME TYPE</span>
                </md-layout>
                <md-layout md-flex="5">
                  <button style="border: hidden; background-color: inherit;margin-bottom: 20px;"
                          v-on:click="localVolumes.splice(index,1)"><b>X</b></button>
                </md-layout>
              </md-layout>
              <md-input-container>
                <md-select v-model="localVolume.type">
                  <md-option value="default">Select..</md-option>
                  <md-option value="hostVolume">Host Volume</md-option>
                  <md-option value="persistentVolume">Persistance Volume</md-option>
                </md-select>
              </md-input-container>
              <md-layout v-if="localVolume.type=='hostVolume'">
                <md-layout md-flex="30" class="mr5">
                  <span class="md-subheading">HOST PATH</span>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-input v-model="localVolume.hostPath"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="30" class="mr5">
                  <span class="md-subheading">CONTAINER PATH</span>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-input v-model="localVolume.containerPath"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="30">
                  <span class="md-subheading">MODE</span>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-select v-model="localVolume.mode">
                      <md-option value="RW">Read and Write</md-option>
                      <md-option value="RO">Read Only</md-option>
                    </md-select>
                  </md-input-container>
                </md-layout>
              </md-layout>

              <md-layout v-if="localVolume.type=='persistentVolume'">
                <md-layout md-flex="30" class="mr5">
                  <span class="md-subheading">SIZE (MiB)</span>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-input v-model.number="localVolume.size" type="number"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="45">
                  <span class="md-subheading">CONTAINER PATH</span>
                  <div style="display: inline;">
                    <md-button class="md-primary small">?
                      <md-tooltip class="fontb" md-direction="top">
                        The path where your application will read and write data. This must be a single-level path
                        relative to the container.
                      </md-tooltip>
                    </md-button>
                  </div>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-input v-model="localVolume.containerPath"></md-input>
                  </md-input-container>
                </md-layout>
              </md-layout>

            </div>

            <a v-on:click="localVolumes.push({'mode':'RW',type:'default'})">+ ADD LOCAL VOLUME</a>
            <!--<a v-on:click="localVolumes.push({})">+ ADD LOCAL VOLUME</a>-->
          </div>
          <div class="mt30">
            <span class="md-title">External Volumes</span><br/>
            <div
              class="md-caption">
              Choose an external persistent volume if fault-tolerance is crucial for your service.
            </div>

            <div class="add-input mt10" v-for="(externalVolume,index) in externalVolumes">
              <md-layout>
                <md-layout md-flex="95">
                  <span class="md-subheading">NAME</span>
                </md-layout>
                <md-layout md-flex="5">
                  <button style="border: hidden; background-color: inherit;margin-bottom: 20px;"
                          v-on:click="externalVolumes.splice(index,1)"><b>X</b></button>
                </md-layout>
              </md-layout>
              <md-input-container style="width: 50%;height: 20px;">
                <md-input v-model="externalVolume.name"></md-input>
              </md-input-container>
              <md-layout>
                <md-layout md-flex="30" class="mr5">
                  <span class="md-subheading">SIZE (GiB)</span>
                  <md-input-container style="width: 100%;height: 20px;"
                                      :class="{'md-input-invalid':container.type!='MESOS'}">
                    <md-input v-model="externalVolume.size" :disabled="container.type!='MESOS'"
                              :class="{'mouse-disabled':container.type!='MESOS'}"></md-input>
                    <md-tooltip class="fontb" md-direction="top" v-if="container.type!='MESOS'">
                      Docker Runtime only supports the default size for implicit volumes, please select Universal
                      Container Runtime(UCR) if you want to modify the size.
                    </md-tooltip>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="60">
                  <span class="md-subheading">CONTAINER PATH</span>
                  <md-input-container style="width: 100%;height: 20px;">
                    <md-input v-model="externalVolume.containerPath"></md-input>
                  </md-input-container>
                </md-layout>
              </md-layout>
            </div>

            <a v-on:click="externalVolumes.push({})">+ ADD EXTERNAL VOLUME</a>
          </div>
        </div>

        <!-------------------------------------------------------Health Checks-------------------------------------------------------->
        <div v-if="menu.healthchecksview" style="width: 100%">
          <h1 class="md-title" style="display: flex;">Health Checks
            <div>
              <md-button class="md-primary small">?
                <md-tooltip class="fontb" md-direction="top">
                  A health check passes if (1) its HTTP response code is between 200 and 399 inclusive, and (2) its
                  response is received within the timeoutSeconds period.
                </md-tooltip>
              </md-button>
            </div>
          </h1>

          <span class="md-body-2">Health checks may be specified per application to be run against the application's instances.</span><br/>

          <div class="add-input mt30" v-for="(healthCheck,index) in healthChecks">
            <div v-if="healthCheck.protocol == 'HTTP' && healthCheck.protocol">
              <span class="md-subheading">UNABLE TO EDIT THIS HEALTHCHECK{{index}}</span>
              <button style="border: hidden; background-color: inherit;float:right;"
                      v-on:click="healthChecks.splice(index,1)"><b>X</b></button>
              <pre lang="json" style="height:inherit;">{{healthCheck}}</pre>
            </div>
            <div v-else>
              <span class="md-subheading">PROTOCOL</span>
              <button style="border: hidden; background-color: inherit;float:right;"
                      v-on:click="healthChecks.splice(index,1)"><b>X</b></button>
              <div style="display: inline;">
                <md-button class="md-primary small">?
                  <md-tooltip class="fontb" md-direction="top">You have several protocol options.
                  </md-tooltip>
                </md-button>
              </div>
              <md-input-container style="width: 40%;height: 20px;">
                <md-select v-model="healthCheck.protocol">
                  <md-option value="">Select Protocol</md-option>
                  <md-option value="COMMAND">Command</md-option>
                  <md-option value="MESOS_HTTP">HTTP</md-option>
                </md-select>
              </md-input-container>
              <div v-if="healthCheck.protocol=='COMMAND'">
                <span class="md-subheading">COMMAND</span>
                <md-input-container style="width: 100%;height: 20px;">
                  <md-input v-model="healthCheck.command"></md-input>
                </md-input-container>
                <a v-on:click="healthCheck.advancehealthcheck = !healthCheck.advancehealthcheck">
                  <span v-if="healthCheck.advancehealthcheck">▼</span> <span v-else>▶</span>
                  ADVANCED HEALTH CHECK SETTINGS
                </a>
                <div v-if="healthCheck.advancehealthcheck">
                  <md-layout>
                    <md-layout md-flex="21" class="mr4">
                      <span style="font-size: 13px;margin-top: 5px;">GRACE PERIOD (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 300): Health check failures are ignored within this number of seconds or
                            until the instance becomes healthy for the first time.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model.number="healthCheck.gracePeriodSeconds" placeholder="300"
                                  type="number"></md-input>
                      </md-input-container>
                    </md-layout>
                    <md-layout md-flex="23" class="mr4">
                      <span style="font-size: 13px;margin-top: 5px;">INTERVAL (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 60): Number of seconds to wait between health checks.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model.number="healthCheck.intervalSeconds" type="number"
                                  placeholder="60"></md-input>
                      </md-input-container>
                    </md-layout>
                    <md-layout md-flex="20" class="mr4">
                      <span style="font-size: 13px;margin-top: 5px;">TIMEOUT (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 20): Number of seconds after which a health check is considered a
                            failure
                            regardless of the response.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model.number="healthCheck.timeoutSeconds" type="number"
                                  placeholder="20"></md-input>
                      </md-input-container>
                    </md-layout>
                    <md-layout md-flex="21">
                      <span style="font-size: 13px;margin-top: 5px;">MAX FAILURES (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 3): Number of consecutive health check failures after which the
                            unhealthy instance should be killed. HTTP & TCP health checks: If this value is 0, instances
                            will not be killed if they fail the health check.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model.number="healthCheck.maxConsecutiveFailures" type="number"
                                  placeholder="3"></md-input>
                      </md-input-container>
                    </md-layout>
                  </md-layout>
                </div>
              </div>
              <div v-if="healthCheck.protocol=='MESOS_HTTP'">
                <md-layout>
                  <md-layout md-flex="45" class="mr5">
                    <span class="md-subheading">SERVICE ENDPOINT</span>
                    <div style="display: inline;">
                      <md-button class="md-primary small">?
                        <md-tooltip class="fontb" md-direction="top">
                          Select a service endpoint that you configured in Networking.
                        </md-tooltip>
                      </md-button>
                    </div>
                    <md-input-container style="width: 100%;height: 20px;">
                      <md-select v-model="healthCheck.portIndex">
                        <md-option value="host">Select Endpoint</md-option>
                        <md-option v-for="(portDefinition,index) in portDefinitions" :value="index">
                          {{portDefinition.name != null ? portDefinition.name : index }}
                        </md-option>
                      </md-select>
                    </md-input-container>
                  </md-layout>
                  <md-layout md-flex="45">
                    <span class="md-subheading">PATH</span>
                    <div style="display: inline;">
                      <md-button class="md-primary small">?
                        <md-tooltip class="fontb" md-direction="top">
                          Enter a path that is reachable in your service and where you expect a response code between
                          200
                          and 399.
                        </md-tooltip>
                      </md-button>
                    </div>
                    <md-input-container style="width: 100%;height: 20px;">
                      <md-input v-model="healthCheck.path"></md-input>
                    </md-input-container>
                  </md-layout>
                </md-layout>
                <md-checkbox v-if="healthCheck.protocol.indexOf('HTTP') > 0 " v-model="healthCheck.https"
                             class="md-primary">MAKE HTTPS
                </md-checkbox>
                <br>
                <a
                  v-on:click="healthCheck.advancehealthcheck = !healthCheck.advancehealthcheck">
                  <span v-if="healthCheck.advancehealthcheck">▼</span> <span v-else>▶</span>
                  ADVANCED HEALTH CHECK SETTINGS
                </a>
                <div v-if="healthCheck.advancehealthcheck">
                  <md-layout>
                    <md-layout md-flex="21" class="mr4">
                      <span style="font-size: 13px;margin-top: 5px;">GRACE PERIOD (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 300): Health check failures are ignored within this number of seconds or
                            until the instance becomes healthy for the first time.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model="healthCheck.gracePeriodSeconds" placeholder="300"></md-input>
                      </md-input-container>
                    </md-layout>
                    <md-layout md-flex="23" class="mr4">
                      <span style="font-size: 13px;margin-top: 5px;">INTERVAL (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 60): Number of seconds to wait between health checks.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model="healthCheck.intervalSeconds" placeholder="60"></md-input>
                      </md-input-container>
                    </md-layout>
                    <md-layout md-flex="20" class="mr4">
                      <span style="font-size: 13px;margin-top: 5px;">TIMEOUT (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 20): Number of seconds after which a health check is considered a
                            failure
                            regardless of the response.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model="healthCheck.timeoutSeconds" placeholder="20"></md-input>
                      </md-input-container>
                    </md-layout>
                    <md-layout md-flex="21">
                      <span style="font-size: 13px;margin-top: 5px;">MAX FAILURES (S)</span>
                      <div style="display: inline;">
                        <md-button class="md-primary small">?
                          <md-tooltip class="fontb" md-direction="top">
                            (Optional. Default: 3): Number of consecutive health check failures after which the
                            unhealthy
                            instance should be killed. HTTP & TCP health checks: If this value is 0, instances will not
                            be
                            killed if they fail the health check.
                          </md-tooltip>
                        </md-button>
                      </div>
                      <md-input-container>
                        <md-input v-model="healthCheck.maxConsecutiveFailures" placeholder="3"></md-input>
                      </md-input-container>
                    </md-layout>
                  </md-layout>
                </div>
              </div>
            </div>
          </div>
          <a v-on:click="healthChecks.push({protocol:'',advancehealthcheck:false})">+ ADD HEALTH CHECK</a>
        </div>


        <!-------------------------------------------------------Environment-------------------------------------------------------->
        <div v-if="menu.environmentview" style="width: 100%">
          <h1 class="md-title" style="display: flex;">Environment</h1>
          <span
            class="md-body-2">Configure any environment values to be attached to each instance that is launched.</span>
          <br/>

          <div class="mt30">
            <span class="md-title">Environment Variables</span>
            <div style="display: inline;">
              <md-button class="md-primary small">?
                <md-tooltip class="fontb" md-direction="top">
                  DC/OS also exposes environment variables for host ports and metdata.
                </md-tooltip>
              </md-button>
            </div>
            <br>
            <span class="md-body-2">Set up environment variables for each instance your service launches.</span> <br/>
            <div v-for="(environment,index) in env">
              <md-layout class="mt10">
                <md-layout md-flex="40">
                  <div v-if="index == 0">KEY</div>
                  <md-input-container>
                    <md-input v-model="environment.key"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="5">
                  <div style="font-size: 20px;margin-top: 60%;margin-left: 50%;">:</div>
                </md-layout>
                <md-layout md-flex="40">
                  <div v-if="index == 0">VALUE</div>
                  <md-input-container>
                    <md-input v-model="environment.value"
                              :disabled="environment.value.indexOf('{{') != -1 && environment.value.indexOf('}}') != -1 "></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="10">
                  <button style="border: hidden; background-color: inherit;margin-bottom: 20%;"
                          v-on:click="env.splice(index,1)"><b>X</b></button>
                </md-layout>
              </md-layout>
            </div>

            <a @click="env.push({key:'',value:''})">+ ADD ENVIRONMENT VARIABLE</a>
          </div>
          <div class="mt30">
            <span class="md-title">Labels</span>
            <div style="display: inline;">
              <md-button class="md-primary small">?
                <md-tooltip class="fontb" md-direction="top">
                  For example, you could label services “staging” and “production” to mark them by their position in the
                  pipeline.
                </md-tooltip>
              </md-button>
            </div>
            <br>
            <span class="md-body-2">Set up environment variables for each instance your service launches.</span> <br/>
            <div v-for="(label,index) in labels">
              <md-layout class="mt10">
                <md-layout md-flex="40">
                  <div v-if="index == 0">KEY</div>
                  <md-input-container>
                    <md-input v-model="label.key"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="5">
                  <div style="font-size: 20px;margin-top: 20px;margin-left: 13px;">:</div>
                </md-layout>
                <md-layout md-flex="40">
                  <div v-if="index == 0">VALUE</div>
                  <md-input-container>
                    <md-input v-model="label.value"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-flex="10">
                  <button style="border: hidden; background-color: inherit;margin-bottom: 20%;"
                          v-on:click="labels.splice(index,1)"><b>X</b></button>
                </md-layout>
              </md-layout>
            </div>
            <a v-on:click="labels.push({})">+ ADD LABEL</a>
          </div>
        </div>
        <!-------------------------------------------------------ReviewView-------------------------------------------------------->
        <div v-if="menu.reviewview" style="width: 100%">
          <div>
            <md-table v-once>
              <md-table-body>
                <md-table-header>
                  <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">General</span>
                  </md-table-head>
                </md-table-header>
                <md-table-row>
                  <md-table-cell><span class="md-title">SERVICE ID</span></md-table-cell>
                  <md-table-cell>{{id}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">INSTANCES</span></md-table-cell>
                  <md-table-cell>{{instances}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">CONTAINER RUNTIME</span></md-table-cell>
                  <md-table-cell>{{container.type}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">CPU</span></md-table-cell>
                  <md-table-cell>{{cpus}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">MEMORY</span></md-table-cell>
                  <md-table-cell>{{mem}} MiB</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">DISK</span></md-table-cell>
                  <md-table-cell>{{disk}} B</md-table-cell>
                </md-table-row>
                <md-table-row v-if="model.backoffSeconds">
                  <md-table-cell><span class="md-title">BACKOFF SECONDS</span></md-table-cell>
                  <md-table-cell>{{model.backoffSeconds}}</md-table-cell>
                </md-table-row>
                <md-table-row v-if="model.backoffFactor">
                  <md-table-cell><span class="md-title">BACKOFF FACTOR</span></md-table-cell>
                  <md-table-cell>{{model.backoffFactor}}</md-table-cell>
                </md-table-row>
                <md-table-row v-if="model.maxLaunchDelaySeconds">
                  <md-table-cell><span class="md-title">BACKOFF MAX LAUNCH DELAY</span></md-table-cell>
                  <md-table-cell>{{model.maxLaunchDelaySeconds}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">CONTAINER IMAGE</span></md-table-cell>
                  <md-table-cell>{{container.docker.image}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">EXTENDED RUNTIME PRIV.</span></md-table-cell>
                  <md-table-cell>{{container.docker.privileged}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">FORCE PULL ON LAUNCH</span></md-table-cell>
                  <md-table-cell>{{container.docker.forcePullImage}}</md-table-cell>
                </md-table-row>
                <md-table-row>
                  <md-table-cell><span class="md-title">COMMAND</span></md-table-cell>
                  <md-table-cell>
                    <span v-if="cmd">{{cmd}}</span>
                    <span v-else>Not Configured</span>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>


            <md-table v-once style="margin-top: 10%;">

              <md-table-header>
                <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Network</span>
                </md-table-head>
              </md-table-header>
              <md-table-row>
                <md-table-cell><span style="color: #111111;font-size: 15px;">NETWORK MODE</span></md-table-cell>
                <md-table-cell>{{networks[0].mode}}</md-table-cell>
              </md-table-row>
              <md-table-header>
                <md-table-head><span class="md-title" style="color: #111111;font-size: 25px;">Service Endpoints</span>
                </md-table-head>
              </md-table-header>
              <md-table-header>
                <md-table-row>
                  <md-table-head><span style="font-size: 15px;">NAME</span></md-table-head>
                  <md-table-head><span style="font-size: 15px;">PROTOCOL</span></md-table-head>
                  <md-table-head><span style="font-size: 15px;">CONTAINER PORT</span></md-table-head>
                  <md-table-head><span style="font-size: 15px;" v-if="networks[0].mode=='host'">HOST PORT</span>
                  </md-table-head>
                  <!--<md-table-head><span style="font-size: 15px;">SERVICE PORT</span></md-table-head>-->
                  <md-table-head><span style="font-size: 15px;">LOAD BALANCED ADDRESS</span></md-table-head>
                </md-table-row>
              </md-table-header>
              <md-table-body v-for="(portDefinition,index) in portDefinitions">
                <md-table-row>
                  <md-table-cell style="color: #111111;">
                    <span v-if="portDefinition.name">{{portDefinition.name}}</span>
                    <span v-else>Not Configured</span>
                  </md-table-cell>
                  <md-table-cell style="color: #111111;">{{portDefinition.protocol}}</md-table-cell>
                  <md-table-cell style="color: #111111;">
                    <span v-if="portDefinition.containerPort">{{portDefinition.containerPort}}</span>
                    <span v-else>Not Configured</span>
                  </md-table-cell>
                  <md-table-cell style="color: #111111;">
                    <span v-if="portDefinition.hostPort">{{portDefinition.hostPort}}</span>
                    <span v-else-if="portDefinition.port">{{portDefinition.port}}</span>
                    <span v-else>Not Configured</span>
                  </md-table-cell>
                  <!--<md-table-cell style="color: #111111;" v-if="portDefinition.servicePort">-->
                  <!--<span v-if="portDefinition.servicePort">{{portDefinition.servicePort}}</span>-->
                  <!--<span v-else>Not Configured</span>-->
                  <!--</md-table-cell>-->
                  <md-table-cell style="color: #111111;">
                    <span v-if="portDefinition.loadBalancedAddress">{{portDefinition.loadBalancedAddress}}</span>
                    <span v-else>Not Enabled</span>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>

            <md-table v-once style="margin-top: 10%;" v-if="env">
              <md-table-header>
                <md-table-head><span class="md-title"
                                     style="color: #111111;font-size: 30px;">Environment Variables</span>
                </md-table-head>
              </md-table-header>
              <md-table-header>
                <md-table-row>
                  <md-table-head style="color: #111111;font-size: 12px;">KEY</md-table-head>
                  <md-table-head style="color: #111111;font-size: 12px;">VALUE</md-table-head>
                </md-table-row>
              </md-table-header>
              <md-table-body>
                <md-table-row v-for="(envValue,index) in env">
                  <md-table-cell>{{envValue.key}}</md-table-cell>
                  <md-table-cell>{{envValue.value}}</md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>

            <md-table v-once style="margin-top: 10%;" v-if="labels">
              <md-table-header>
                <md-table-head><span class="md-title" style="color: #111111;font-size: 30px;">Labels</span>
                </md-table-head>
              </md-table-header>
              <md-table-header>
                <md-table-row>
                  <md-table-head style="color: #111111;font-size: 13px;">KEY</md-table-head>
                  <md-table-head style="color: #111111;font-size: 13px;">VALUE</md-table-head>
                </md-table-row>
              </md-table-header>
              <md-table-body>
                <md-table-row v-for="(label,index) in labels">
                  <md-table-cell>{{label.key}}</md-table-cell>
                  <md-table-cell>{{label.value}}</md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>

            <md-table v-once style="margin-top: 10%;margin-bottom:10%;">
              <md-table-body>
                <md-table-header>
                  <md-table-head>
                    <span class="md-title" style="color: #111111;font-size: 30px;">Health Checks</span>
                  </md-table-head>
                </md-table-header>
                <md-table-row>
                </md-table-row>
              </md-table-body>
            </md-table>

          </div>
        </div>

      </md-layout>
      <md-layout id="slideEditor" v-if="jsonEditor"
                 :class="{'md-layout':jsonEditor,'sideEditor':!jsonEditor, 'sideEditor-open':jsonEditor, 'md-flex-30':jsonEditor}">
        <div class="md-right bgblack" ref="rightSidenav" style="width: 100%;">
          <codemirror v-if="opened"
                      :options="{
                                  theme: 'dracula',
                                  mode: 'javascript',
                                  extraKeys: {'Ctrl-Space': 'autocomplete'},
                                  lineNumbers: true,
                                  lineWrapping: true
                                }"
                      :value="editorData"
                      v-on:change="editorToModel"
          >
          </codemirror>
        </div>
      </md-layout>
    </md-layout>
  </div>
</template>
<script>
  export default {
    props: {
      _service: {
        type: Object,
        default: function () {
          return {
            id: "/",
            instances: 1,
            portDefinitions: [],
            container: {
              type: "DOCKER",
              volumes: []
            },
            cpus: 0.1,
            mem: 128,
            requirePorts: false,
            networks: [],
            healthChecks: [],
            fetch: [],
            constraints: []
          };
        }
      },
      jsonEditor: Boolean,
      editable: Boolean,
      newSingleContainer: Boolean
    },
    data() {
      var me = this;
      return {
        editorData: null,
        model: null,

        opened: false,
        menu: {
          serviceview: true,
          networkingview: false,
          volumesview: false,
          healthchecksview: false,
          environmentview: false,
          reviewview: false
        },

        //돔 컨트롤
        moresetting: false,
        errorView: false,


        //====== Service =========//
        id: '/',
        instances: 1,
        container: {
          type: 'DOCKER',
          docker: {
            image: '',
            forcePullImage: false,
            privileged: false
          }
        },
        cpus: 0.1,
        mem: 128,
        cmd: '',

        gpus: null,
        disk: null,
        constraints: [],
        operator: ["UNIQUE", "CLUSTER", "GROUP_BY", "LIKE", "UNLIKE", "MAX_PER"],
        fetch: [],


        //====== 네트워킹 =========//
        networks: [
          {
            mode: 'host'
          }
        ],
        portsAutoAssign: true,
        portDefinitions: [],
        requirePorts: false,

        //====== 볼륨 =========//
        localVolumes: [],
        externalVolumes: [],

        //====== 헬스체크 =========//
        healthChecks: [],

        //====== 환경 =========//
        env: [],
        labels: [],

        //분리
        separate: {
          container: function (val) {
            for (var key in val) {
              if (key == 'docker') {
                for (var dockerKey in me.container[key]) {
                  if (dockerKey == 'image') {
                    if (val[key][dockerKey].indexOf("{{") != -1 && val[key][dockerKey].indexOf("{{") != -1) {
                      val[key][dockerKey] = "{{IMAGE}}";
                    }
                  }
                }
              }
              me.container[key] = val[key];
            }
          },
          constraints: function (val) {
            me.constraints = val || [];
          }
          ,
          fetch: function (val) {
            me.fetch = val || [];
          },
          networks: function (val) {
            if (!val || !val[0] || !val[0].mode) {
              me.networks = [{mode: 'host'}];
            } else {
              me.networks = val;
            }
          },
          portDefinitions: function (val) {
            console.log("separate", val);
            me.portDefinitions = [];
            //모델값이 없으면 패스
            if (!val || !val.length) {
              return;
            }

            for (var i in val) {
              //copy 는 모델에 넣을 definition;
              var copy = {};
              var definition = val[i];
              if (definition.labels && definition.labels['VIP_' + i]) {
                copy.enableLoadBalanced = true;
                copy.vipPort = definition.labels['VIP_' + i].substring(definition.labels['VIP_' + i].indexOf(":") + 1, definition.labels['VIP_' + i].length);
              }
              if (definition.protocol) {
                if (definition.protocol.indexOf('tcp') != -1) {
                  copy.protocolTcp = true;
                } else {
                  copy.protocolTcp = false;
                }
                if (definition.protocol.indexOf('udp') != -1) {
                  copy.protocolUdp = true;
                } else {
                  copy.protocolUdp = false;
                }
              }
              definition.name ? copy.name = definition.name : "";
              definition.containerPort ? copy.containerPort = definition.containerPort : "";
              console.log("definition.servicePort", definition.servicePort);
              if (definition.hostPort != undefined) {
                copy.hostPort = definition.hostPort;
                copy.portsAutoAssign = true;
              } else if (definition.port != undefined) {
                copy.port = definition.port;
              } else {
                copy.portsAutoAssign = true;
              }
              definition.servicePort ? copy.servicePort = definition.servicePort : "";
              me.portDefinitions.push(copy);
            }
          },
          portsAutoAssign: function (val) {
            me.requirePorts = !val;
          },
          localVolumes: function (val) {
            me.localVolumes = [];
            var copy = JSON.parse(JSON.stringify(val));
            for (var i in copy) {
              var localVolume = copy[i];
              if (!localVolume.external) {
                if (localVolume.persistent) {
                  localVolume.type = "persistentVolume";
                } else {
                  localVolume.type = "hostVolume";
                }
                me.localVolumes.push(localVolume);
              }
            }
          },
          externalVolumes: function (val) {
            me.externalVolumes = [];
            var copy = JSON.parse(JSON.stringify(val));
            for (var i in copy) {
              var externalVolume = copy[i];
              if (externalVolume.external) {
                me.externalVolumes.push(externalVolume);
              }
            }
          },
          healthChecks: function (val) {
            var copy = JSON.parse(JSON.stringify(val));
            me.healthChecks = copy;
          },
          env: function (val) {
            var copy = JSON.parse(JSON.stringify(val));
            var environment = [];
            for (var key in copy) {
              environment.push({key: key, value: copy[key]})
            }
            me.env = environment;
          },
          labels: function (val) {
            var copy = JSON.parse(JSON.stringify(val));
            var label = [];
            for (var key in copy) {
              label.push({key: key, value: copy[key]})
            }
            me.labels = label;
          },
        },
        //조합
        combine: {

          //서비스
          container: function (val) {
            me.model.container = val;
          },
          constraints: function (val) {
            if (val && val.length) {
              for (var i in val) {
                if (val[i][1] && val[i][1] == 'UNIQUE') {
                  val[i].splice(2, 1);
                }
              }
            }
            me.model.constraints = val;
          },
          fetch: function (val) {
            me.model.fetch = val;
          },
          networks: function (val) {
            me.model.networks[0] = val[0]
          },
          portDefinitions: function (val) {
            console.log("portDefinitions", val);
            if (!val.length) {
              me.model.portDefinitions = [];
              me.model.container.portMappings = [];
            }
            if (me.model.networks[0].mode == 'host') {
              me.model.portDefinitions = [];
              for (var i in val) {
                var portDefinition = val;
                var label = {};
                if (portDefinition[i].protocolUdp && portDefinition[i].protocolTcp) {
                  portDefinition[i].protocol = "udp,tcp";
                } else if (portDefinition[i].protocolTcp) {
                  portDefinition[i].protocol = "tcp";
                } else if (portDefinition[i].protocolUdp) {
                  portDefinition[i].protocol = "udp";
                }
                if (portDefinition[i].vipPort) {
                  label['VIP_' + i] = "/" + me.model.id.replace("/", "") + ":" + portDefinition[i].vipPort;
                  portDefinition[i].labels = label;
                }
                if (portDefinition[i].hostPort) {
                  portDefinition[i].port = portDefinition[i].hostPort;
                }
                var copy = JSON.parse(JSON.stringify(portDefinition[i]));
                delete copy.protocolTcp;
                delete copy.protocolUdp;
                delete copy.containerPort;
                delete copy.vipPort;
                delete copy.enableLoadBalanced;
                me.model.portDefinitions.push(copy);
              }
            } else {
              me.model.container.portMappings = [];
              for (var i in val) {
                var portDefinition = val[i];
                var label = {};
                if (portDefinition.protocolUdp && portDefinition.protocolTcp) {
                  portDefinition.protocol = "udp,tcp";
                } else if (portDefinition.protocolTcp) {
                  portDefinition.protocol = "tcp";
                } else if (portDefinition.protocolUdp) {
                  portDefinition.protocol = "udp";
                } else {
                  delete portDefinition.protocol;
                }
                if (portDefinition.vipPort) {
                  label['VIP_' + i] = "/" + me.model.id.replace("/", "") + ":" + portDefinition.vipPort;
                  portDefinition.labels = label;
                }
                if (portDefinition.port) {
                  portDefinition.hostPort = portDefinition.port;
                }
//                console.log("bf portDefinition.portsAutoAssign",portDefinition.portsAutoAssign);
//                if (portDefinition.portsAutoAssign == undefined){
//                  portDefinition.portsAutoAssign = "true";
//                  console.log("af portDefinition.portsAutoAssign",portDefinition.portsAutoAssign);
//                } else {
//
//                }

                var copy = JSON.parse(JSON.stringify(portDefinition));
                delete copy.protocolTcp;
                delete copy.protocolUdp;
                delete copy.portsAutoAssign;
                delete copy.vipPort;
                delete copy.port;
                delete copy.enableLoadBalanced;
                me.model.container.portMappings[i] = copy;
              }
            }
          },
          portsAutoAssign: function (val) {
            me.model.requirePorts = !val;
          },
          localVolumes: function (val) {
            var copy = JSON.parse(JSON.stringify(val));

            for (var i in copy) {
              if (copy[i].type == 'persistentVolume') {
                copy[i].size ? copy[i].persistent = {size: copy[i].size} : null;
                me.model.residency = {relaunchEscalationTimeoutSeconds: 10, taskLostBehavior: "WAIT_FOREVER"}
                copy[i].hostPath ? delete copy[i].hostPath : null;
              } else {
                me.model.residency ? delete me.model.residency : null;
              }
//              delete copy[i].type;
              delete copy[i].size;
            }
            me.model.container.volumes = copy;

          },
          externalVolumes: function (val) {
            var copy = JSON.parse(JSON.stringify(me.model.container.volumes));
            for (i in val) {
              copy.push(JSON.parse(JSON.stringify(val[i])));
            }
            for (var i in copy) {
              if (!copy[i].type) {
                copy[i].external = {provider: "dvdi", options: {"dvdi/driver": "rexray"}};
                copy[i].size ? copy[i].external.size = copy[i].size : null;
                copy[i].name ? copy[i].external.name = copy[i].name : null;
                copy[i].mode = "RW";
                delete copy[i].name;
                delete copy[i].size;
              }
              delete copy[i].type;
            }
            me.model.container.volumes = copy;
          },
          healthChecks: function (val) {
            me.model.healthChecks = [];
            var copy = JSON.parse(JSON.stringify(val));
            for (var i in copy) {
              copy.advancehealthcheck = copy.advancehealthcheck ? !copy.advancehealthcheck : false;
              if (copy[i].protocol != "") {
                if (copy[i].https) {
                  copy[i].protocol += "S";
                } else {
                  copy[i].protocol = "MESOS_HTTP";
                }
                delete copy[i].https;
                delete copy[i].advancehealthcheck;
                copy[i].command ? copy[i].command = {value: copy[i].command} : null;
                me.model.healthChecks.push(copy[i]);
              }
            }
//            me.model.healthChecks = copy;
          },
          env: function (val) {
            var copy = JSON.parse(JSON.stringify(val));
            var environment = {};
            for (var i in copy) {
              if (copy[i].key && copy[i].value) {
                environment[copy[i].key] = copy[i].value;
              } else {
                delete environment[copy[i].key];
              }
            }
            me.model.env = environment;
          },
          labels: function (val) {
            var copy = JSON.parse(JSON.stringify(val));
            var label = {};
            for (var i in copy) {
              if (copy[i].key && copy[i].value) {
                label[copy[i].key] = copy[i].value;
              } else {
                delete label[copy[i].key];
              }
            }
            me.model.labels = label;
          },
        }
      }
    },
    mounted() {
      this.serviceToModel();
      this.openSlideEditor();
    },
    compute: {}
    ,
    watch: {
      _service: {
        handler: function (newVal, oldVal) {
          console.log(newVal);
          this.serviceToModel();
        },
        deep: true
      },
      id: function () {
        this.combination();
      },
      instances: function () {
        this.combination();
      },
      cpus: function () {
        this.combination();
      },
      mem: function () {
        this.combination();
      },
      cmd: function () {
        this.combination();
      },
      gpus: function () {
        this.combination();
      },
      disk: function () {
        this.combination();
      },
      constraints: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      fetch: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      container: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      networks: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      portDefinitions: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      portsAutoAssign: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      localVolumes: {
        handler: function (newVal, oldVal) {
          this.combination();
        }
        ,
        deep: true
      },
      externalVolumes: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      healthChecks: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      env: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
      labels: {
        handler: function (newVal, oldVal) {
          this.combination();
        },
        deep: true
      },
    },
    methods: {
      /**
       * 모델을 조합한다.
       **/
      combination: function () {
        if (this.working) {
          return;
        }

        this.working = true;
        console.log('combination Start!!');

        //서비스
        if (this.model.id.indexOf("{{") == -1 && this.model.id.indexOf("}}") == -1) {
          this.model.id = this.id;
        } else {
          this.model.id = "{{APP_ID}}";
        }

        this.model.instances = this.instances;
        this.combine.container(this.container);
        this.model.cpus = this.cpus;
        this.model.mem = this.mem;
        this.model.cmd = this.cmd ? this.cmd : null;
        this.model.gpus = this.gpus;
        this.model.disk = this.disk;
        this.combine.constraints(this.constraints);
        this.combine.fetch(this.fetch);

        //네트워크
        this.combine.networks(this.networks);
        this.model.requirePorts = this.portsAutoAssign ? false : true;
        this.combine.portDefinitions(this.portDefinitions);

        //볼륨
        this.combine.localVolumes(this.localVolumes);
        this.combine.externalVolumes(this.externalVolumes);

        //헬스체크
        this.combine.healthChecks(this.healthChecks);

        //환경
        this.combine.env(this.env);
        this.combine.labels(this.labels);

        this.editorData = JSON.stringify(this.model, null, 2);
        this.$emit('update:_service', this.model);
        //원래 메소드가 종료되는 시점에 바뀐 값들로 인해 watch,dom binding 이 활성화된다.
        //$nextTick 을 사용하게 되면, 위의 사항을 먼저 발생시킨 후, $nextTick 안의 메소드를 나중에 실행.
        this.$nextTick(function () {
          this.working = false;
        });
      }
      ,
      /**
       * 모델을 분리한다.
       **/
      separation: function () {
        if (this.working) {
          return;
        }
        this.working = true;
        console.log('separation Start!!', this.model);

        //서비스
        if (this.model.id.indexOf("{{") == -1 && this.model.id.indexOf("}}") == -1) {
          this.id = this.model.id;
        } else {
          this.model.id = "{{APP_ID}}";
          this.id = this.model.id;
        }
        this.instances = this.model.instances;
        this.separate.container(this.model.container);
        this.cpus = this.model.cpus;
        this.mem = this.model.mem;
        this.cmd = this.model.cmd ? this.model.cmd : null;
        this.gpus = this.model.gpus;
        this.disk = this.model.disk;
        this.separate.constraints(this.model.constraints);
        this.separate.fetch(this.model.fetch);

        //네트워크
        this.separate.networks(this.model.networks);
        this.portsAutoAssign = this.model.requirePorts ? false : true;
        this.separate.portDefinitions(this.model.portDefinitions.length ? this.model.portDefinitions : this.model.container.portMappings);

        //볼륨
        this.separate.localVolumes(this.model.container.volumes);
        this.separate.externalVolumes(this.model.container.volumes);

        //헬스체크
        this.separate.healthChecks(this.model.healthChecks);

        //환경
        if (this.model.env) {
          this.separate.env(this.model.env);
        }
        if (this.model.labels) {
          this.separate.labels(this.model.labels);
        }

        this.$emit('update:_service', this.model);

        this.$nextTick(function () {
          this.working = false;
        });
      }
      ,
      /**
       * _service prop 를 모델로 전달.
       */
      serviceToModel: function () {
        this.model = JSON.parse(JSON.stringify(this._service));

        //에디터시 필요한 필수 모델값 지정
        if (this.model) {
          if (!this.model.portDefinitions) {
            this.model.portDefinitions = [];
          }
        }

        //console.log("this._service", this._service);
        delete this.model.tasksStaged;
        delete this.model.tasksRunning;
        delete this.model.tasksHealthy;
        delete this.model.tasksUnhealthy;
        delete this.model.deployments;
        delete this.model.tasks;
        delete this.model.taskStats;
        delete this.model.version;
        delete this.model.killSelection;
        delete this.model.unreachableStrategy;
        delete this.model.backoffSeconds;
        delete this.model.acceptedResourceRoles;
        delete this.model.backoffFactor;
        delete this.model.versionInfo;
        this.separation();
      }
      ,
      /**
       * editorData 를 Json 전환하여 모델에 전달.
       * @param text
       */
      editorToModel: function (text) {
//        this.model = JSON.parse(this.editorData);
        this.model = JSON.parse(text);
        this.separation();
      }
      ,

      changeView: function (viewname) {
        var me = this;
        $.each(me.menu, function (key, value) {
          me.menu[key] = false;
        })
        this.menu[viewname] = true;
        return this.menu[viewname];
      }
      ,

      openSlideEditor: function () {
        if (!this.jsonEditor) {
          this.opened = true;
          this.combination();
        } else {
          this.opened = false;
//          this.combination();
        }
      }
      ,
      expandSettings: function (key) {
        this[key] = this[key] ? false : true;
      }
      ,
      validation: function () {
        if (!this.container.docker.image || !this.id || this.cpus == 0) {
          this.errorView = true;
        } else {
          this.errorView = false;
        }
        return this.errorView;
      },
    }
  }
</script>
<style scoped lang="scss" rel="stylesheet/scss">
  .list-link {
    border-right: solid 1px #aaaaaa;
  }

  ,
  a {
    cursor: pointer;
  }

  .md-tooltip {
    display: inline-block;
    height: auto;
  }

  ,
  .fontb {
    width: 300px;
    min-height: 50px;
    font-size: 12px;
    padding: 10px;
    word-break: break-all;
    white-space: normal;
  }

  ,

  .add-input {
    width: 95%;
    min-height: 100px;
    border: solid 1px #cccccc;
    border-radius: 5px;
    padding: 10px;
  }

  ,
  .add-input:hover {
    width: 95%;
    min-height: 100px;
    border: solid 2px #4A78B3;
    border-radius: 5px;
    padding: 10px;
  }

  ,
  .mt30 {
    margin-top: 30px;
  }

  .mt10 {
    margin-top: 10px;
  }

  ,
  .after-list {
    width: 100%;
    border-right: solid 2px #4A78B3;
  }

  ,

  .listspan {
    color: #666666;
    width: 100%;
    text-align: right;
  }

  ,

  md-list-item {
    width: 100%;
    border-right: solid 2px #4A78B3;
  }

  ,
  .small {
    min-width: 10px !important;
    min-height: 10px !important;
    height: 15px;
    width: 15px;
    border-radius: 10px;
    padding: 2px;
    background-color: #4A78B3 !important;
    line-height: 0px !important;
    color: #e6e6e6 !important;
  }

  ,
  .mr5 {
    margin-right: 5%;
  }

  ,
  .mr4 {
    margin-right: 4%;
  }

  ,
  .listspan:hover {
    color: #4E88CC;
    cursor: pointer;
  }

  .bggray {
    background-color: #2F3129;
    height: inherit;
  }

  .main-body::-webkit-scrollbar-track {
    -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
    border-radius: 10px;
    background-color: #F5F5F5;
  }

  .main-body::-webkit-scrollbar {
    width: 6px;
    background-color: #F5F5F5;
  }

  .main-body::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background-color: #666666;
  }

  .sideEditor {
    height: inherit;
    display: none;
  }

  .sideEditor-open {
    height: inherit;
    will-change: transform;
  }

  .mouse-disabled {
    cursor: not-allowed;
  }

  #errorForm > li:before {
    content: "• ";
    color: #666666;
  }
</style>
