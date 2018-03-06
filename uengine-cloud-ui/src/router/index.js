import Vue from 'vue'
import Router from 'vue-router'
import Login from '../components/Login'
import ServiceLocator from '../components/ServiceLocator'
import Home from '../components/Home'
import AvatarUploader from '../components/AvatarUploader'

Vue.component('avatar-uploader', AvatarUploader);

import DcosDataProvider from '../components/DcosDataProvider'

Vue.component('dcos-data-provider', DcosDataProvider);

/**
 * 시스템
 */
import System from '../components/system/System'


/**
 * 대쉬보드
 */
import Dashboard from '../components/dashboard/Dashboard'

Vue.component('dashboard', Dashboard);

import Metrics from '../components/dashboard/Metrics'

Vue.component('metrics', Metrics);

import MovingChart from '../components/dashboard/MovingChart'

Vue.component('moving-chart', MovingChart);

import NodeList from '../components/dashboard/NodeList'

Vue.component('node-list', NodeList);

import AppList from '../components/dashboard/AppList'

Vue.component('app-list', AppList);

import JobList from '../components/dashboard/JobList'

Vue.component('job-list', JobList);

/**
 * 노드
 */
import NodeDetail from '../components/node/NodeDetail'

Vue.component('node-detail', NodeDetail);

import NodeTaskList from '../components/node/NodeTaskList'

Vue.component('node-task', NodeTaskList);

/**
 * 서비스
 */
import ServiceProgress from '../components/services/ServiceProgress'

Vue.component('service-progress', ServiceProgress);

import ServiceDetail from '../components/services/ServiceDetail'

Vue.component('service-detail', ServiceDetail);

import ServiceTaskList from '../components/services/ServiceTaskList'

Vue.component('service-task', ServiceTaskList);

import ServiceConfiguration from '../components/services/ServiceConfiguration'

Vue.component('service-configuration', ServiceConfiguration);

import ServiceDebug from '../components/services/ServiceDebug'

Vue.component('service-debug', ServiceDebug);


/**
 * 잡
 */
import JobDetail from '../components/jobs/JobDetail'

Vue.component('job-detail', JobDetail);

import JobRunList from '../components/jobs/JobRunList'

Vue.component('job-runlist', JobRunList);

import JobConfiguration from '../components/jobs/JobConfiguration'

Vue.component('job-configuration', JobConfiguration);

/**
 * 타스크
 */
import TaskList from '../components/task/TaskList'

Vue.component('task-list', TaskList);

import TaskMenu from '../components/task/TaskMenu'

Vue.component('task-menu', TaskMenu);

import TaskDetail from '../components/task/TaskDetail'

Vue.component('task-detail', TaskDetail);

import TaskFiles from '../components/task/TaskFiles'

Vue.component('task-files', TaskFiles);

import TaskLog from '../components/task/TaskLog'

Vue.component('task-log', TaskLog);

/**
 * 에디터
 */
import Confirm from '../components/editor/Confirm'

Vue.component('confirm', Confirm);

import AppEditor from '../components/editor/AppEditor'

Vue.component('app-editor', AppEditor);

import ContainerEditor from '../components/editor/ContainerEditor'

Vue.component('container-editor', ContainerEditor);

import ScaleApp from '../components/editor/ScaleApp'

Vue.component('scale-app', ScaleApp);

import ServiceDeployments from '../components/editor/ServiceDeployments'

Vue.component('service-deployments', ServiceDeployments);


/**
 * 앱
 */

import Apps from '../components/apps/Apps'

Vue.component('apps', Apps);

import AppsCatalog from '../components/apps/AppsCatalog'

Vue.component('apps-catalog', AppsCatalog);

import AppsOverview from '../components/apps/AppsOverview'

Vue.component('apps-overview', AppsOverview);

import AppsCreate from '../components/apps/AppsCreate'

Vue.component('apps-create', AppsCreate);

import AppsDetail from '../components/apps/AppsDetail'

Vue.component('apps-detail', AppsDetail);

import AppsDetailDashboard from '../components/apps/AppsDetailDashboard'

Vue.component('apps-detail-dashboard', AppsDetailDashboard);

import AppsDetailRuntime from '../components/apps/AppsDetailRuntime'

Vue.component('apps-detail-runtime', AppsDetailRuntime);

import AppsDetailDeployment from '../components/apps/AppsDetailDeployment'

Vue.component('apps-detail-deployment', AppsDetailDeployment);

import AppsDetailLog from '../components/apps/AppsDetailLog'

Vue.component('apps-detail-log', AppsDetailLog);

import AppsDetailMonitor from '../components/apps/AppsDetailMonitor'

Vue.component('apps-detail-deployment', AppsDetailMonitor);

import AppsDetailZuulConfig from '../components/apps/AppsDetailZuulConfig'

Vue.component('apps-zuul-config', AppsDetailZuulConfig);

import AppRoute from '../components/apps/AppRoute'

Vue.component('app-route', AppRoute);

import AppRuntimeCard from '../components/apps/AppRuntimeCard'

Vue.component('app-runtime-card', AppRuntimeCard);

import AppCloudConfig from '../components/apps/AppCloudConfig'

Vue.component('app-cloud-config', AppCloudConfig);

import AppSsh from '../components/apps/AppSsh'

Vue.component('app-ssh', AppSsh);

import AppPipeLine from '../components/apps/AppPipeLine'

Vue.component('app-pipeline', AppPipeLine);

import GitlabDeploy from '../components/apps/GitlabDeploy'

Vue.component('gitlab-deploy', GitlabDeploy);

import AppLogsList from '../components/apps/AppLogsList'

Vue.component('apps-logs-list', AppLogsList);

/**
 * 조직
 */

import Organization from '../components/organization/Organization'

Vue.component('organization', Organization);

import UserList from '../components/organization/UserList'

Vue.component('user-list', UserList);

import UserDetail from '../components/organization/UserDetail'

Vue.component('user-detail', UserDetail);

import UserCreate from '../components/organization/UserCreate'

Vue.component('user-create', UserCreate);

import GroupDetail from '../components/organization/GroupDetail'

Vue.component('group-detail', GroupDetail);
/**
 * 아바타
 */
import IAMAvatar from '../components/IAMAvatar'

Vue.component('iam-avatar', IAMAvatar);

/**
 * Get cloud config
 */
$.ajax({
  url: backendUrl + "/config/uengine-cloud-server.json",
  type: "get",
  async: false,
  success: function (data) {
    window.config = data;
  },
  error: function () {
    console.log('Failed to get config');
  }
});
console.log('window.config', window.config);


/**
 * Iam && Vue Router
 * @type {IAM}
 */
//var iam = new IAM('http://localhost:18080');
var iam = new IAM(iamUrl);
iam.setDefaultClient('my-client-key', 'my-client-secret');
window.iam = iam;


let RouterGuard = require("./RouterGuard.js")(iam);
Vue.use(Router);

/**
 * VueImgInputer
 */
// https://github.com/waynecz/vue-img-inputer --Document
import VueImgInputer from 'vue-img-inputer'

Vue.component('vue-img-inputer', VueImgInputer)

/**
 * Vue resource configuration
 */
let VueResource = require('vue-resource-2');
Vue.use(VueResource);


/**
 * ServiceLocator
 */
Vue.component('service-locator', ServiceLocator);
Vue.http.interceptors.push(function (request, next) {
  request.headers['access_token'] = localStorage['access_token'];
  next();
});

export default new Router({
  // mode: 'history',
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
      name: 'home',
      component: Home,
      meta: {
        breadcrumb: '홈'
      },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: Dashboard,
          beforeEnter: RouterGuard.requireUser,
          meta: {
            breadcrumb: '대시보드'
          },
        },
        {
          path: 'system',
          name: 'system',
          component: System,
          beforeEnter: RouterGuard.requireUser,
          meta: {
            breadcrumb: '시스템 메트릭스'
          },
        },
        {
          path: 'nodeDetail/:nodeId/',
          name: 'nodeDetail',
          component: NodeDetail,
          redirect: 'nodeDetail/:nodeId/task',
          meta: {
            preTitle: '노드',
            breadcrumb: ':nodeId'
          },
          children: [
            {
              path: 'task',
              name: 'nodeTaskList',
              component: NodeTaskList,
              beforeEnter: RouterGuard.requireUser,
            },
            {
              path: 'task/:taskId/',
              name: 'nodeTaskMenu',
              redirect: 'task/:taskId/detail',
              component: TaskMenu,
              props: {from: 'node'},
              meta: {
                breadcrumb: ':taskId'
              },
              children: [
                {
                  path: 'detail',
                  name: 'nodeTaskDetail',
                  component: TaskDetail,
                  beforeEnter: RouterGuard.requireUser
                },
                {
                  path: 'files',
                  name: 'nodeTaskFiles',
                  component: TaskFiles,
                  beforeEnter: RouterGuard.requireUser
                },
                {
                  path: 'log',
                  name: 'nodeTaskLog',
                  component: TaskLog,
                  beforeEnter: RouterGuard.requireUser
                },
              ]
            }
          ]
        },
        {
          path: 'services/detail/:appId/',
          name: 'serviceDetail',
          redirect: 'services/detail/:appId/task',
          component: ServiceDetail,
          meta: {
            preTitle: '서비스',
            breadcrumb: ':appId'
          },
          children: [
            {
              path: 'task',
              name: 'serviceTaskList',
              component: ServiceTaskList,
              beforeEnter: RouterGuard.requireUser,
            },
            {
              path: 'configuration',
              name: 'serviceConfiguration',
              component: ServiceConfiguration,
              beforeEnter: RouterGuard.requireUser
            },
            {
              path: 'debug',
              name: 'serviceDebug',
              component: ServiceDebug,
              beforeEnter: RouterGuard.requireUser
            },
            {
              path: 'task/:taskId/',
              name: 'serviceTaskMenu',
              redirect: 'task/:taskId/detail',
              component: TaskMenu,
              props: {from: 'service'},
              meta: {
                breadcrumb: ':taskId'
              },
              children: [
                {
                  path: 'detail',
                  name: 'serviceTaskDetail',
                  component: TaskDetail,
                  beforeEnter: RouterGuard.requireUser
                },
                {
                  path: 'files',
                  name: 'serviceTaskFiles',
                  component: TaskFiles,
                  beforeEnter: RouterGuard.requireUser
                },
                {
                  path: 'log',
                  name: 'serviceTaskLog',
                  component: TaskLog,
                  beforeEnter: RouterGuard.requireUser
                },
              ]
            }
          ]
        },
        {
          path: 'jobs/detail/:jobId/',
          name: 'jobDetail',
          redirect: 'jobs/detail/:jobId/runs',
          component: JobDetail,
          meta: {
            preTitle: '잡',
            breadcrumb: ':jobId'
          },
          children: [
            {
              path: 'runs',
              name: 'jobRunList',
              component: JobRunList,
              beforeEnter: RouterGuard.requireUser,
            },
            {
              path: 'configuration',
              name: 'jobConfiguration',
              component: JobConfiguration,
              beforeEnter: RouterGuard.requireUser
            },
            {
              path: 'task/:taskId/',
              name: 'jobTaskMenu',
              redirect: 'task/:taskId/detail',
              component: TaskMenu,
              props: {from: 'job'},
              meta: {
                breadcrumb: ':taskId'
              },
              children: [
                {
                  path: 'detail',
                  name: 'jobTaskDetail',
                  component: TaskDetail,
                  beforeEnter: RouterGuard.requireUser
                },
                {
                  path: 'files',
                  name: 'jobTaskFiles',
                  component: TaskFiles,
                  beforeEnter: RouterGuard.requireUser
                },
                {
                  path: 'log',
                  name: 'jobTaskLog',
                  component: TaskLog,
                  beforeEnter: RouterGuard.requireUser
                },
              ]
            }
          ]
        },
        {
          path: 'apps/',
          name: 'apps',
          redirect: '/apps/overview',
          component: Apps,
          meta: {
            breadcrumb: '앱'
          },
          children: [
            {
              path: 'catalog',
              name: 'appsCatalog',
              component: AppsCatalog,
              beforeEnter: RouterGuard.requireUser,
            },
            {
              path: 'overview',
              name: 'appsOverview',
              component: AppsOverview,
              beforeEnter: RouterGuard.requireUser,
            },
            {
              path: 'create/:categoryItemId/',
              name: 'appsCreate',
              component: AppsCreate,
              beforeEnter: RouterGuard.requireUser,
            },
            {
              path: 'detail/:appName/',
              name: 'appsDetail',
              redirect: 'detail/:appName/dashboard',
              component: AppsDetail,
              meta: {
                breadcrumb: ':appName'
              },
              children: [
                {
                  path: 'dashboard',
                  name: 'appsDetailDashboard',
                  component: AppsDetailDashboard,
                  beforeEnter: RouterGuard.requireUser,
                },
                {
                  path: 'runtime',
                  name: 'appsDetailRuntime',
                  component: AppsDetailRuntime,
                  beforeEnter: RouterGuard.requireUser,
                },
                {
                  path: 'deployment',
                  name: 'appsDetailDeployment',
                  component: AppsDetailDeployment,
                  beforeEnter: RouterGuard.requireUser,
                },
                {
                  path: 'log',
                  name: 'appsDetailLog',
                  component: AppsDetailLog,
                  beforeEnter: RouterGuard.requireUser,
                },
                {
                  path: 'monitor',
                  name: 'appsDetailMonitor',
                  component: AppsDetailMonitor,
                  beforeEnter: RouterGuard.requireUser,
                },
                {
                  path: 'appLogs',
                  name: 'appLogsList',
                  component: AppLogsList,
                  beforeEnter: RouterGuard.requireUser,
                }
              ]
            },
          ]
        },
        {
          path: 'organization/',
          name: 'organization',
          redirect: '/organization/list',
          component: Organization,
          beforeEnter: RouterGuard.requireUser,
          meta: {
            breadcrumb: 'Organization'
          },
          children: [
            {
              path: 'list',
              name: 'user-list',
              component: UserList,
            },
            {
              path: 'create',
              name: 'userCreate',
              component: UserCreate,
            },
            {
              path: 'detail/:userName',
              name: 'userDetail',
              props: true,
              component: UserDetail,
              meta: {
                preTitle: "사용자정보",
                breadcrumb: ':userName'
              }
            },
            {
              path: 'groupdetail/:groupId',
              name: 'groupDetail',
              props: true,
              component: GroupDetail,
            }
          ]
        },
      ]
    },
    {
      path: '/auth/:command',
      name: 'login',
      component: Login,
      beforeEnter: RouterGuard.requireGuest
    }
  ]
})
