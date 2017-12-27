// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import VueMaterial from '../node_modules/vue-material'
import 'vue-material/dist/vue-material.css'

// import BpmnVue from './components/bpmn'
import Opengraph from './components/opengraph'
import App from './App'
import router from './router'


Vue.use(VueMaterial);
// Vue.use(BpmnVue);
Vue.use(Opengraph);

var VueCodeMirror = require('vue-codemirror-lite')
Vue.use(VueCodeMirror);

require('codemirror/mode/javascript/javascript');
require('codemirror/mode/yaml/yaml.js');
require('codemirror/mode/vue/vue');

require('codemirror/theme/dracula.css');
require('codemirror/addon/hint/show-hint.js');
require('codemirror/addon/hint/show-hint.css');
require('codemirror/addon/hint/javascript-hint.js');

var YAML = require('yamljs');
Vue.use(YAML);

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

import VueHighlightJS from '../node_modules/vue-highlight.js';
import 'highlight.js/styles/vs2015.css';

Vue.use(VueHighlightJS);

Vue.config.productionTip = false

window.Vue = Vue;

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  template: '<App/>',
  components: {App},
  data: {
    dcosData: {}
  }
});
