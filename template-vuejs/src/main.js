// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import VueMaterial from '../node_modules/vue-material'
import 'vue-material/dist/vue-material.css'
import App from './App'
import router from './router'


Vue.use(VueMaterial);
window.busVue = new Vue();

import VueBreadcrumbs from 'vue-breadcrumbs'
Vue.use(VueBreadcrumbs, {
  template: '<nav class="breadcrumb" v-if="$breadcrumbs.length"> ' +
  '<router-link class="md-title-accent" v-for="(crumb, key) in $breadcrumbs" :to="linkProp(crumb)" :key="key">' +
  '<span class="md-title-accent" v-if="crumb.meta.preTitle">{{ crumb.meta.preTitle }} &nbsp;&nbsp;>&nbsp;&nbsp;</span>' +
  '<span class="md-title-accent" v-if="crumb.meta.breadcrumb.indexOf(' + "':'" + ') != -1">{{ $route.params[crumb.meta.breadcrumb.split(' + "':'" + ')[1]] }} &nbsp;&nbsp;>&nbsp;&nbsp;</span>' +
  '<span class="md-title-accent" v-else>{{ crumb.meta.breadcrumb }} &nbsp;&nbsp;>&nbsp;&nbsp;</span>' +
  '</router-link> ' +
  '</nav>'
});


Vue.config.productionTip = false;
window.Vue = Vue;

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  template: '<App/>',
  components: {App},
  data: {}
});
