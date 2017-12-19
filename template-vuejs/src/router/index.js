import Vue from 'vue'
import Router from 'vue-router'
import Login from '@/components/Login'
import ServiceLocator from '@/components/ServiceLocator'
import Home from '@/components/Home'
import AvatarUploader from '@/components/AvatarUploader'
Vue.component('avatar-uploader', AvatarUploader);

import IAMAvatar from '@/components/IAMAvatar'
Vue.component('iam-avatar', IAMAvatar);

import Dashboard from '@/components/Dashboard'
Vue.component('dashboard', Dashboard);

/**
 * Iam && Vue Router
 * @type {IAM}
 */
//let iam = new IAM(location.protocol + '//' + location.hostname + ':8080/iam');
var iam = new IAM('http://' + config.vcap.services.iam.external);
iam.setDefaultClient('e74a9505-a811-407f-b4f6-129b7af1c703', '109cf590-ac67-4b8c-912a-913373ada046');


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
  if (request.url.indexOf(configServerUrl) == -1) {
    request.headers['access_token'] = localStorage['access_token'];
  }
  next();
});

export default new Router({
  mode: 'history',
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
      props: {iam: iam},
      beforeEnter: RouterGuard.requireGuest
    }
  ]
})
