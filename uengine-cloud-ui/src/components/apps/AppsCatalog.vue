<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <md-layout :md-gutter="16" v-if="catalog">
    <md-layout md-flex="20">
      <div>
        <span class="md-subheading">모든 카테고리</span>
        <br><br><br>
        <div v-for="(category, key) in catalog.category">
          <a class="md-subheading" v-on:click="setFocusCategory(key)">{{category.type}}</a>
        </div>
      </div>
    </md-layout>
    <md-layout md-flex="80">
      <md-layout md-flex="100">
        <md-input-container>
          <md-icon>search</md-icon>
          <label>항목 검색</label>
          <md-input type="text"></md-input>
          <md-button class="md-raised md-primary">필터</md-button>
        </md-input-container>
      </md-layout>
      <md-layout md-flex="100">
        <md-layout md-flex="100">
          <span class="md-body-1">{{catalog.category[focusCategory].description}}</span>
        </md-layout>
        <br><br>

        <md-layout class="category-item" md-flex="33"
                   v-for="item in catalog.category[focusCategory].items"
        >
          <div v-on:click="moveCreate(item.id)">
            <md-layout>
              <md-layout md-flex="20">
                <div>
                  <md-avatar>
                    <img :src="item.image" alt="item.title">
                  </md-avatar>
                </div>
              </md-layout>
              <md-layout md-flex="80">
                <span class="md-body-1 bold">{{item.title}}</span>
                <br><br>
                <span class="md-caption category-item-text">{{item.description}}</span>
              </md-layout>
            </md-layout>
          </div>
        </md-layout>
      </md-layout>
    </md-layout>
  </md-layout>
</template>
<script>
  import DcosDataProvider from '../DcosDataProvider'
  export default {
    mixins: [DcosDataProvider],
    props: {},
    data() {
      return {
        catalog: null,
        focusCategory: 'app'
      }
    },
    mounted(){
      var me = this;
      $.get('/static/catalog.json', function (catalog) {
        me.catalog = catalog;
      })
    },
    watch: {}
    ,
    methods: {
      setFocusCategory: function (key) {
        this.focusCategory = key;
      },
      moveCreate: function (categoryItemId) {
        console.log('categoryItemId', categoryItemId);
        var me = this;
        this.$router.push(
          {
            name: 'appsCreate',
            params: {categoryItemId: categoryItemId}
          }
        )
      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">
  .category-item {
    padding: 8px;
    cursor: pointer;
  }

  .category-item:hover {
    background: #EBEFF5;
  }

  .category-item-text {
    /*!* 한 줄 자르기 *!*/
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;

    /* 여러 줄 자르기 추가 스타일 */
    white-space: normal;
    line-height: 1.2;
    height: 3.3em;
    text-align: left;
    word-wrap: break-word;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }
</style>
