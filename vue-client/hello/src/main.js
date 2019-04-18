import Vue from 'vue'
import './plugins/vuetify'
import App from './App.vue'
import axios from 'axios'



Vue.config.productionTip = false

const HTTP = axios.create({
  baseURL: `http://localhost:8080/`
});

Vue.prototype.$http = HTTP;

new Vue({
  render: h => h(App),
}).$mount('#app')




