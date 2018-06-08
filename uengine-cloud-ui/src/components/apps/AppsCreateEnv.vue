<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout>
    <div class="bold">앱 이름:</div>
    <md-input-container v-bind:class="{ 'md-input-invalid': invalidAppName }">
      <label>고유한 이름 입력</label>
      <md-input v-model="appName"></md-input>
      <span class="md-error">'-' 를 제외한 공백,또는 특수문자를 허용하지 않습니다.</span>
    </md-input-container>
    <br><br>
    <md-layout md-flex="100" :md-gutter="16">
      <md-layout>
        <div class="bold">외부 접속 주소:</div>
        <md-input-container>
          <label>외부 프로덕션 도메인 주소</label>
          <md-input v-model="externalProdDomain"></md-input>
        </md-input-container>
        <md-input-container>
          <label>외부 스테이징 도메인 주소</label>
          <md-input readonly v-model="externalStgDomain"></md-input>
        </md-input-container>
        <md-input-container>
          <label>외부 개발 도메인 주소</label>
          <md-input readonly v-model="externalDevDomain"></md-input>
        </md-input-container>
      </md-layout>
      <md-layout>
        <div class="bold">내부 접속 주소:</div>
        <md-input-container>
          <label>내부 프로덕션 주소</label>
          <md-input readonly v-model="internalProdDomain"></md-input>
        </md-input-container>
        <md-input-container>
          <label>내부 스테이징 주소</label>
          <md-input readonly v-model="internalStgDomain"></md-input>
        </md-input-container>
        <md-input-container>
          <label>내부 개발 주소</label>
          <md-input readonly v-model="internalDevDomain"></md-input>
        </md-input-container>
      </md-layout>
    </md-layout>
    <md-layout md-flex="50">
      <div class="bold">리소스:</div>
      <md-layout md-flex="100" :md-gutter="16">
        <md-layout>
          <md-input-container>
            <label>cpu</label>
            <md-input type="number" v-model.number="cpu"></md-input>
          </md-input-container>
        </md-layout>
        <md-layout>
          <md-input-container>
            <label>메모리 (MB)</label>
            <md-input type="number" v-model.number="mem"></md-input>
          </md-input-container>
        </md-layout>
        <md-layout>
          <md-input-container>
            <label>인스턴스 수</label>
            <md-input type="number" v-model.number="instances"></md-input>
          </md-input-container>
        </md-layout>
      </md-layout>
    </md-layout>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  import PathProvider from '../PathProvider'

  export default {
    mixins: [DcosDataProvider, PathProvider],
    props: {
      env: Object
    },
    data() {
      return {
        defaultHost: window.config['default-host'],
        cpu: 0.4,
        mem: 512,
        instances: 1,
        appName: null,
        externalProdDomain: null,
        externalStgDomain: null,
        externalDevDomain: null,
        internalProdDomain: null,
        internalStgDomain: null,
        internalDevDomain: null,
        prodPort: null,
        stgPort: null,
        devPort: null,
        invalidAppName: true
      }
    },
    mounted() {

    },
    watch: {
      appName: function (val) {
        var me = this;
        if (!val) {
          val = '';
        }
        this.internalProdDomain = 'marathon-lb-internal.marathon.mesos:port';
        this.internalStgDomain = 'marathon-lb-internal.marathon.mesos:port';
        this.internalDevDomain = 'marathon-lb-internal.marathon.mesos:port';
        this.externalProdDomain = val + '.' + this.defaultHost;

        var special_pattern = /[_`~!@#$%^&*|\\\'\";:\/?]/gi;
        if (special_pattern.test(val) == true) {
          this.invalidAppName = true;
        }
        else if (val.indexOf(' ') != -1 || val.length < 1) {
          this.invalidAppName = true;
        }
        else {
          this.invalidAppName = false;
        }

        //emit update
        if (this.invalidAppName) {
          this.$emit('update:env', null)
        } else {
          var appEnv = {
            cpu: me.cpu,
            mem: me.mem,
            instances: me.instances,
            appName: me.appName,
            externalProdDomain: me.externalProdDomain,
            externalStgDomain: me.externalStgDomain,
            externalDevDomain: me.externalDevDomain
          };
          this.$emit('update:env', appEnv)
        }
      },
      externalProdDomain: function (val) {
        if (val) {
          let split = val.split('.');
          let subDomain = split[0];
          if (subDomain && subDomain.length > 0) {
            var left = val.substring(subDomain.length, val.length);
            this.externalStgDomain = subDomain + '-stg' + left;
            this.externalDevDomain = subDomain + '-dev' + left;
          } else {
            this.externalStgDomain = '';
            this.externalDevDomain = '';
          }
        } else {
          this.externalStgDomain = '';
          this.externalDevDomain = '';
        }
      }
    }
    ,
    methods: {}
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
