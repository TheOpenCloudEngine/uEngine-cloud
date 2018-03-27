import Vue from 'vue'
import Router from 'vue-router'
import AdminLogin from '../components/AdminLogin'
import Login from '../components/Login'
import ServiceLocator from '../components/ServiceLocator'
import Home from '../components/Home'

import Confirm from '../components/Confirm'
Vue.component('confirm', Confirm);

import KeyValueTable from '../components/KeyValueTable'
Vue.component('key-value-table', KeyValueTable);

import TokenTable from '../components/TokenTable'
Vue.component('token-table', TokenTable);

import Dashboard from '../components/Dashboard'
Vue.component('dashboard', Dashboard);

import System from '../components/System'
Vue.component('system', System);

import Clients from '../components/Clients'
Vue.component('clients', Clients);

import UserList from '../components/UserList'
Vue.component('user-list', UserList);

import UserEdit from '../components/UserEdit'
Vue.component('user-edit', UserEdit);

import Templates from '../components/Templates'
Vue.component('templates', Templates);




//브라우저 url
window.browserUrl = location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '');

/**
 * 로컬 서버 응답이 오면 패기지, 로컬서버 응답이 없으면 스탠드어론
 */
$.ajax({
  url: "/health",
  type: "get",
  dataType: "json",
  async: false,
  success: function (data) {
    window.backendUrl = '';
  },
  error: function () {
    window.backendUrl = 'http://localhost:8080';
  }
});

/**
 * Iam && Vue Router
 */
window.iam = new IAM(window.backendUrl);
var RouterGuard = require("./RouterGuard.js")(window.iam);
Vue.use(Router);

/**
 * Vue resource configuration
 */
var VueResource = require('vue-resource-2');
Vue.use(VueResource);

/**
 * ServiceLocator
 */
Vue.component('service-locator', ServiceLocator);

export default new Router({
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
          beforeEnter: RouterGuard.requireUser
        },
        {
          path: 'system',
          name: 'system',
          component: System,
          beforeEnter: RouterGuard.requireUser
        },
        {
          path: 'clients',
          name: 'clients',
          component: Clients,
          beforeEnter: RouterGuard.requireUser
        }
      ]
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLogin,
      beforeEnter: RouterGuard.requireGuest
    },
    {
      path: '/auth/:command',
      name: 'login',
      component: Login
    }
  ]
})
