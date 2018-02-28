# App 에 배포
어플리케이션 생성시 목록에서 Vuejs를 선택하여 생성하면 된다. 
어플리케이션 생성에 관련한 내용은 [이곳](https://github.com/TheOpenCloudEngine/uEngine-cloud/wiki/OCE-MSA-%ED%94%8C%EB%9E%AB%ED%8F%BC%EC%9D%98-%EC%82%AC%EC%9A%A9#%EC%95%A0%ED%94%8C%EB%A6%AC%EC%BC%80%EC%9D%B4%EC%85%98-%EC%83%9D%EC%84%B1)를 참조하면 된다.
![image](https://user-images.githubusercontent.com/16382067/35032478-68651c04-fbaa-11e7-9854-c8ae93ebacde.png)

# 주문서비스 Web UI 만들기
1. 주문서비스와, 주문서비스와 연동된 Web UI를 만들기 위해서는 Metaworks4 Application과, Vuejs Application을 모두 사용 하면 된다.
주문서비스는 [링크](edu/주문서비스의-구현)를 참조하여 만들면 된다.
주문서비스에서 필요한 것은 @id를 제외한 총 5가지이며, itemName, stock, point, price, img 이다.

1. 상품 등록페이지 Web UI 만들기
1. OrderService.vue파일을 만든다.
1. Vue파일을 생성 후에는 Router에 등록을 해주어야한다. src/router/index.js에서 OrderService.vue를 등록하여준다.
```
import OrderService from '@/components/OrderService'
Vue.component('orderservice', OrderService);
```
1. 하단의 export default new Router 부분을 보면,
<details>
<summary>수정 전</summary>

```javascript
export default new Router({
//  mode: 'history',
  routes: [
    {
      path: '/',
      redirect: '/orderservice',
      name: 'home',
      component: Home,
      props: {iam: iam},
      meta: {
        breadcrumb: '홈'
      },
      children: [
        {
          path: 'orderservice',
          name: 'orderservice',
          component: orderservice,
          beforeEnter: RouterGuard.requireUser,
          meta: {
            breadcrumb: 'orderservice'
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
```
</details>
위와 같이 작성이 되어있다.

우리가 사용할 내용은 우선, dashboard가 아닌, orderservice이므로
<details>
<summary>수정 전</summary>

```javascript
export default new Router({
//  mode: 'history',
  routes: [
    {
      path: '/',
      redirect: '/orderservice',
      name: 'home',
      component: Home,
      props: {iam: iam},
      meta: {
        breadcrumb: '홈'
      },
      children: [
        {
          path: 'orderservice',
          name: 'orderservice',
          component: orderservice,
          beforeEnter: RouterGuard.requireUser,
          meta: {
            breadcrumb: 'orderservice'
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
```
</details>
위와 같이 dashboard를 모두 orderservice로 바꾸어 준다.
 
1. orderservice.vue를 작성한다.
<details>
<summary> OrderService.vue </summary>

``` javascript
<template>
  <div>
    <!-- AddDialog Start -->
    <md-dialog md-open-from="#custom" md-close-to="#custom" ref="addDialog">
      <md-dialog-title>제품 등록</md-dialog-title>

      <md-dialog-content>
        <form novalidate @submit.stop.prevent="submit">
          <md-input-container>
            <label>제품번호</label>
            <md-input type="number" placeholder="제품번호을 입력해 주세요" v-model="pushItems.item"></md-input>
          </md-input-container>

          <md-input-container>
            <label>제품명</label>
            <md-input placeholder="제품명을 입력해 주세요" v-model="pushItems.itemName"></md-input>
          </md-input-container>

          <md-input-container>
            <label>수량</label>
            <md-input type="number" placeholder="수량을 입력해 주세요" v-model="pushItems.stock"></md-input>
          </md-input-container>

          <md-input-container>
            <label>가격</label>
            <md-input type="number" placeholder="가격을 입력해 주세요" v-model="pushItems.price"></md-input>
          </md-input-container>

          <md-input-container>
            <label>포인트</label>
            <md-input type="number" placeholder="포인트를 입력해 주세요" v-model="pushItems.point"></md-input>
          </md-input-container>

          <md-input-container>
            <label>설명</label>
            <md-input placeholder="이미지 주소를 입력해 주세요" v-model="pushItems.img"></md-input>
          </md-input-container>
        </form>
      </md-dialog-content>

      <md-dialog-actions>
        <md-button class="md-primary" @click.native="saveData">Confirm</md-button>
        <md-button class="md-primary" @click.native="closeDialog('dialog1')">Close</md-button>
      </md-dialog-actions>
    </md-dialog>
    <!-- AddDialog End -->
    <!-- Bottom Right Button Start -->
    <md-button class="md-fab md-fab-bottom-right" @click.native="openDialog('addDialog')">
      <md-icon>add</md-icon>
    </md-button>
    <!-- Bottom Right Button End -->
    <!-- Table Start -->
    <md-table>
      <md-table-header>
        <md-table-row>
          <md-table-head>제품명</md-table-head>
          <md-table-head>재고</md-table-head>
          <md-table-head>가격</md-table-head>
          <md-table-head>포인트</md-table-head>
          <md-table-head>이미지 경로</md-table-head>
        </md-table-row>
      </md-table-header>

      <md-table-body>
        <md-table-row v-for="item in items">
          <md-table-cell> {{item.itemName }}</md-table-cell>
          <md-table-cell> {{item.stock }} </md-table-cell>
          <md-table-cell> {{item.price }}</md-table-cell>
          <md-table-cell> {{item.point }}</md-table-cell>
          <md-table-cell> {{item.img }} </md-table-cell>
        </md-table-row>
      </md-table-body>
    </md-table>
    <!-- Table End -->
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
    return {
      pushItems: {
        item: '',
        stock: '',
        price: '',
        point: '',
        img: '',
        itemName: '',
      },
      items: null
    }
  },
  created: function () {

  },
  mounted() {
    var me = this;

    me.loadData();
  },
  watch: {

    'pushItems.item': function () {
      this.$emit('input', this.pushItems.item)
    },
    'pushItems.stock': function () {
      this.$emit('input', this.pushItems.stock)
    },
    'items.price': function () {
      this.$emit('input', this.pushItems.price)
    },
    'pushItems.point': function () {
      this.$emit('input', this.pushItems.point)
    },
    'items.img': function () {
      this.$emit('input', this.pushItems.img)
    },
    'pushItems.itemName': function () {
      this.$emit('input', this.pushItems.itemName)
    },
  },
  methods: {
    saveData: function () {
      var access_token = localStorage["access_token"];
      var item = [];
      var backend = hybind('http://e-shop-api-dev.pas-mini.io/order-service/', {headers:{'access_token': access_token}});
      var pushItems = this.pushItems;
      backend.$bind('items', item);
      item.$create(pushItems);
      this.loadData();
      this.$refs["dialog1"].close();
    },
    loadData: function () {
      var access_token = localStorage["access_token"];
      var items = [];
      var backend = hybind('http://e-shop-api-dev.pas-mini.io/order-service/', {headers:{'access_token': access_token}});
      var pushItems = this.pushItems;
      var me = this;
      backend.$bind('items', items);
      items.$load().then(function(items){
        me.items = items;
      });

    },
    openDialog(ref) {
      this.$refs[ref].open();
    },
    closeDialog(ref) {
      this.$refs[ref].close();
    },
    onOpen() {
      console.log('Opened');
    },
    onClose(type) {
      console.log('Closed', type);
    }
  }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
```
</details>
 
2. Shops.vue를 작성한다.
<details>
<summary> Shops.vue </summary>

```javascript
<template>
  <div>

    <!-- Table Start -->
    <div v-for="item in items" style="min-width: 300px; max-width: 300px; min-height: 400px; max-height: 450px; margin: 10px; float: left;">
      <itemcards :item="item"></itemcards>
    </div>

    <!-- Table End -->
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
    return {
      pushItems: {
        item: '',
        stock: '',
        price: '',
        point: '',
        img: '',
        itemName: '',
      },
      items: null
    }
  },
  created: function () {

  },
  mounted() {
    var me = this;

    me.loadData();
  },
  watch: {

  },
  methods: {
    loadData: function () {
      var access_token = localStorage["access_token"];
      var items = [];
      var backend = hybind('http://e-shop-api-dev.pas-mini.io/order-service/', {headers:{'access_token': access_token}});
      var pushItems = this.pushItems;
      var me = this;
      backend.$bind('items', items);
      items.$load().then(function(items){
        me.items = items;
      });
    },
  }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>

```
</details>



3. ItemCards.Vue를 작성한다
<details>
<summary> Shops.vue </summary>

```javascript
<template>
  <div>
    <md-card>
      <md-card-media>
        <img :src="item.img" style="min-height: 200px; min-width: 300px;">
      </md-card-media>

      <md-card-header>
        <div class="md-title">{{item.itemName}}</div>
        <div class="md-subhead">가격: {{item.price}}</div>
      </md-card-header>
      <md-card-content>
        재고: {{item.stock}}
        포인트 : {{item.point}}
      </md-card-content>
    </md-card>
  </div>
</template>
<script>
  export default {
    props: {
      item: Array
    },
    data() {
    return {

    }
  },
  created: function () {

  },
  mounted(){
  },
  watch: {

  },
  methods: {

  }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>

```
</details>
