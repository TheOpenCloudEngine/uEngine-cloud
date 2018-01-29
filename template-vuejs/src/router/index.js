import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/Home'
import Dashboard from '@/components/Dashboard'
Vue.component('dashboard', Dashboard);

import Login from '../../node_modules/metaworks4/src/components/Login.vue'
import Metaworks4 from '../../node_modules/metaworks4'
Vue.use(Metaworks4);

/**
 * Iam && Vue Router
 * @type {IAM}
 */
//let iam = new IAM(location.protocol + '//' + location.hostname + ':8080/iam');
var iam = new IAM('http://' + config.vcap.services.iam.external);
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


Vue.http.interceptors.push(function (request, next) {
  if (request.url.indexOf(configServerUrl) == -1) {
    request.headers['access_token'] = localStorage['access_token'];
  }
  next();
});

export default new Router({
  //mode: 'history',
  routes: [
    {
      path: '/',
      redirect: '/dashboard',
      name: 'home',
      component: Home,
      props: {iam: iam},
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
        }
      ]
    },
    {
      path: '/auth/:command',
      name: 'login',
      component: Login,
      props: {
        iamServer: "http://iam.pas-mini.io/",
        scopes: "cloud-server"
      },
      beforeEnter: RouterGuard.requireGuest
    }
  ]
})
