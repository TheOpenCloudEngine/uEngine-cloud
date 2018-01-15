// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import VueMaterial from '../node_modules/vue-material'
import 'vue-material/dist/vue-material.css'

import App from './App'
import router from './router'


Vue.use(VueMaterial);

var VueCodeMirror = require('vue-codemirror-lite')
Vue.use(VueCodeMirror);

require('codemirror/mode/javascript/javascript');
require('codemirror/mode/yaml/yaml.js');
require('codemirror/mode/vue/vue');

require('codemirror/theme/dracula.css');
require('codemirror/addon/hint/show-hint.js');
require('codemirror/addon/hint/show-hint.css');
require('codemirror/addon/hint/javascript-hint.js');

window.busVue = new Vue();

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
