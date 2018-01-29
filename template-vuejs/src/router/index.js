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

var clientKey = "my-client-key";

//This required for managing user rest api (avatar upload, curl user data, etc..)
//If the client type is not public,(described in iam yaml setting) the rest api will rejected.
var clientSecret = "my-client-secret";

//window.profile will be autowired by uengine-cloud-server. It can be local,dev,stg,prod. default is 'local'.
var profile = window.profile;


//Change the url your IAM application's vcap service's profile url.
//For example, 'http://' + config.vcap.services['your-iam-server'][profile].external;
var iamUrl = 'http://' + config.vcap.services.iam.external;

//Define iam client
var iam = new IAM(iamUrl);

//Set clientKey, clientSecret(optional).
iam.setDefaultClient(clientKey, clientSecret);

//Mark in window
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
  request.headers['access_token'] = localStorage['access_token'];
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
        iamServer: iamUrl,
        scopes: "cloud-server"
      },
      beforeEnter: RouterGuard.requireGuest
    }
  ]
})
