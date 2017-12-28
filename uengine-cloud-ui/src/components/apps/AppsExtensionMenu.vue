<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <component v-if="tagExists" :is="'apps-' + appType + '-config'"
               :stage="stage"
               :devApp="devApp"
               :catalogItem="catalogItem"
    ></component>
  </div>
</template>
<script>
  export default {
    name: 'extension-menu',
    props: {
      appType: String,
      stage: String,
      devApp: Object,
      catalogItem: Object
    },
    data() {
      return {}
    },
    computed: {
      tagName: function () {
        console.log("this.appType",this.appType);
        return 'apps-' + this.appType + '-config';
      },
    }
    ,
    methods: {
      tagExists: function () {
        return Vue.options.components['apps-' + this.appType + '-config'];
      },
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
