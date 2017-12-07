<template xmlns:v-on="http://www.w3.org/1999/xhtml">
  <div>
    <md-button class="md-primary md-raised" @click="devToStg">개발 => 스테이징</md-button>
    <md-button class="md-primary md-raised" @click="stgToProd">스테이징 => 프로덕션</md-button>
    <md-button class="md-primary md-raised" @click="rollback">롤백</md-button>
    <md-button class="md-raised" @click="reset">reset</md-button>

    <div style="position: relative;width: 100%;height: 550px">
      <opengraph
        focus-canvas-on-select
        wheelScalable
        dragPageMovable
        :enableContextmenu="false"
        :enableRootContextmenu="false"
        :backgroundColor="'white'"
        ref="opengraph"
        v-on:canvasReady="canvasReady"
      >
        <template>

          <!--분기선-->
          <edge-element
            :vertices="[[650,50],[650,500]]"
            :_style="{
                'arrow-end':'none',
                'stroke': 'grey',
                'opacity':'0.7'
            }"
          >
          </edge-element>

          <!--Haproxy-->
          <geometry-element
            :width="haproxy.width"
            :height="haproxy.height"
            :x="haproxy.x"
            :y="haproxy.y"
            :id="haproxy.id"
          >
            <geometry-rect
              :_style="{
                  'stroke': 'none',
                  'stroke-width': 0,
                  fill: '#FFFFFF',
                  'fill-opacity': 0
                }"
            >
            </geometry-rect>

            <sub-elements>
              <svg-element
                :sub-z-index="-1"
                :sub-width="'160%'"
                :sub-height="'160%'"
                :sub-right="'-30%'"
                :sub-top="'-30%'"
              >
                <svg enable-background="new 0 0 100 100" height="100px" version="1.1" viewBox="0 0 100 100" width="100px" xml:space="preserve" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><g id="Layer_1"><g><path d="M50.853,21.379c15.272,0.458,27.281,13.102,26.826,28.263c-0.455,15.144-13.2,27.054-28.473,26.596    c-15.259-0.458-27.268-13.111-26.813-28.255C22.848,32.821,35.594,20.921,50.853,21.379z" fill="#F58536"/></g><g><defs><path d="M77.679,49.642l-0.071,2.382c-0.455,15.146-13.201,27.056-28.474,26.598     C33.875,78.163,21.867,65.51,22.321,50.364l0.071-2.382c-0.454,15.144,11.555,27.797,26.813,28.255     C64.479,76.695,77.224,64.785,77.679,49.642z" id="SVGID_91_"/></defs><clipPath id="SVGID_2_"/><g clip-path="url(#SVGID_2_)" enable-background="new    "><path d="M76.124,57.937l-0.071,2.384c-0.588,1.669-1.337,3.259-2.226,4.758l0.071-2.384     C74.787,61.195,75.537,59.605,76.124,57.937" fill="#9D5025"/><path d="M73.898,62.694l-0.071,2.384c-4.95,8.363-14.229,13.856-24.693,13.543     C33.875,78.163,21.867,65.51,22.321,50.364l0.071-2.382c-0.454,15.144,11.555,27.797,26.813,28.255     C59.67,76.551,68.948,71.06,73.898,62.694" fill="#9D5025"/><path d="M77.679,49.642l-0.071,2.382c-0.04,1.332-0.175,2.635-0.399,3.91l0.072-2.384     C77.504,52.274,77.639,50.972,77.679,49.642" fill="#9D5025"/><path d="M77.28,53.55l-0.072,2.384c-0.268,1.508-0.653,2.978-1.155,4.387l0.071-2.384     C76.626,56.527,77.012,55.06,77.28,53.55" fill="#9D5025"/></g></g><path d="M67.993,39.5h-9v-1.791l-2.795,3.525l-1.285-1.816L42.229,47.5h10.764v-1.922l6,2.244V44.5h9v9h-9v-3.322   l-6,2.244V50.5H42.231l12.799,8.081l1.168-1.878l2.795,3.588V58.5h9v9h-9v-3.598l-6.218-1.496l0.85-1.805L37.993,50.488V56.5h-10   v-15h10v6.01l15.748-10.112l-0.965-1.804l6.217-1.496V30.5h9V39.5z" fill="#FFFFFF"/></g></svg>
              </svg-element>
            </sub-elements>
          </geometry-element>
          <text-element
            :width="100" :height="100"
            :x="haproxy.x + 40" :y="haproxy.y + 20"
            :text="'Haproxy'"
          >
          </text-element>


          <!--Area-->
          <div v-for="(area, key) in areas">
            <geometry-element :width="area.width"
                              :height="50"
                              :x="area.x"
                              :y="area.y - 165"
                              :label="area.title.label"
                              :_style="area.title.style"
            >
              <geometry-polygon
                :vertices="[[0,20],[3,0],[3,10],[5,10],[5,5],[30,5],[30,10],[32,10],[32,0],[35,20],
              [32,40],[32,30],[30,30],[30,35],[5,35],[5,30],[3,30],[3,40],[0,20]]"
              >
              </geometry-polygon>
            </geometry-element>

            <geometry-element :x="area.x"
                              :y="area.y"
                              :width="area.width"
                              :height="area.height"
                              v-on:mouseover="mouseover"
                              v-on:mouseout="mouseout"
                              :id="area.id"
            >
              <geometry-rect
                :_style="{
                  'fill-r': 1,
                  'fill-cx': .1,
                  'fill-cy': .1,
                  'stroke-width': 1.2,
                  fill: '#FFFFFF',
                  'fill-opacity': 0,
                  r: '10'
                }"
              >
              </geometry-rect>

              <sub-elements>
                <rectangle-element
                  :sub-z-index="-1"
                  :sub-width="'80%'"
                  :sub-height="'80%'"
                  :sub-right="'10%'"
                  :sub-bottom="'10%'"
                  :sub-style="{
                fill: area.color,
                'fill-opacity': 0.5,
            }"
                >
                </rectangle-element>
              </sub-elements>
            </geometry-element>

            <!--Haproxy - Area 연결-->
            <edge-element
              :id="haproxy.id + '-' + area.id"
              :vertices="[[haproxy.x,haproxy.y],[area.x,haproxy.y],[area.x,area.y + (area.height/2)]]"
              :_style="{}"
            >
            </edge-element>
          </div>


          <div v-for="(area, key) in areas">
            <!--컨테이너-->
            <svg-element v-for="container in area.containers"
                         v-if="container.show"
                         :width="container.width" :height="container.height"
                         :x="container.x" :y="container.y"
                         :id="container.id"
                         :redraw.sync="container.redraw"
                         :_style="{

                         }"
            >
              <svg enable-background="new 0 0 100 100" height="100px" version="1.1" viewBox="0 0 100 100" width="100px" xml:space="preserve" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><g id="Layer_1"><g><g><polygon fill="#FFFFFF" points="49.987,86 18.816,67.99      18.827,31.988 50.01,14 81.184,32.011 81.17,68.013    " stroke="#464646" stroke-miterlimit="10" stroke-width="2.2721"/><polygon fill="#FFFFFF" points="18.829,31.988 18.829,31.998      49.989,49.999 81.171,32.012 81.171,32.002 50.012,14    " stroke="#464646" stroke-miterlimit="10" stroke-width="2.2721"/><polygon fill="#FFFFFF" points="18.823,31.999 18.816,32.004      19.422,67.984 50.896,85.459 50.903,85.453 50.298,49.474    " stroke="#464646" stroke-miterlimit="10" stroke-width="2.2721"/></g><g><polygon fill="#CFCFCF" points="49.993,69.684 32.949,59.837      32.955,40.152 50.006,30.315 67.052,40.163 67.044,59.85    " stroke="#FFFFFF" stroke-miterlimit="10" stroke-width="2.2721"/><polygon fill="#CFCFCF" points="32.955,40.152 32.955,40.156      49.993,49.999 67.044,40.164 67.044,40.159 50.006,30.315    " stroke="#FFFFFF" stroke-miterlimit="10" stroke-width="2.2721"/><polygon fill="#CFCFCF" points="32.953,40.157 32.949,40.16      33.281,59.835 50.49,69.388 50.494,69.387 50.163,49.712    " stroke="#FFFFFF" stroke-miterlimit="10" stroke-width="2.2721"/></g></g><polygon fill="#231F20" font-family="'HelveticaNeue-MediumCond'" font-size="14" transform="matrix(1 0 0 1 41.7222 -2.0195)"></polygon></g></svg>

              <sub-elements>
                <!--deploy-->
                <circle-element
                  v-if="container.deploy"
                  :sub-z-index="-1"
                  :sub-width="'80%'"
                  :sub-height="'80%'"
                  :sub-right="'10%'"
                  :sub-bottom="'10%'"
                  :sub-style="animationStyle"
                >
                </circle-element>
                <text-element
                  v-if="container.deploy"
                  :sub-z-index="1"
                  :sub-width="'100%'"
                  :sub-height="'30px'"
                  :sub-align="'center'"
                  :sub-vertical-align="'middle'"
                  :text="'deploying'"
                >
                </text-element>

                <!--healthy-->
                <text-element
                  v-if="container.healthy"
                  :sub-z-index="1"
                  :sub-width="'100%'"
                  :sub-height="'30px'"
                  :sub-align="'center'"
                  :sub-vertical-align="'middle'"
                  :text="'healthy'"
                  :sub-style="{
                    'font-color': 'red'
                  }"
                >
                </text-element>
              </sub-elements>
            </svg-element>

            <!--Zuul LB-->
            <div v-if="area.lb && area.lb.show">
              <geometry-element
                :x="area.lb.x"
                :y="area.lb.y"
                :width="area.lb.width"
                :height="area.lb.height"
                :id="area.lb.id"
                :_style="area.lb.style"
              >
                <geometry-rect
                  :_style="{
                  'stroke': 'none',
                  'stroke-width': 0,
                  fill: '#FFFFFF',
                  'fill-opacity': 0
                }"
                >
                </geometry-rect>

                <sub-elements>
                  <svg-element
                    :sub-z-index="-1"
                    :sub-width="'160%'"
                    :sub-height="'160%'"
                    :sub-right="'-30%'"
                    :sub-top="'-30%'"
                  >
                    <svg enable-background="new 0 0 100 100" height="100px" version="1.1" viewBox="0 0 100 100" width="100px" xml:space="preserve" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><g id="Layer_1"><g><path d="M50.853,21.379c15.272,0.458,27.281,13.102,26.826,28.263c-0.455,15.144-13.2,27.054-28.473,26.596    c-15.259-0.458-27.268-13.111-26.813-28.255C22.848,32.821,35.594,20.921,50.853,21.379z" fill="#F58536"/></g><g><defs><path d="M77.679,49.642l-0.071,2.382c-0.455,15.146-13.201,27.056-28.474,26.598     C33.875,78.163,21.867,65.51,22.321,50.364l0.071-2.382c-0.454,15.144,11.555,27.797,26.813,28.255     C64.479,76.695,77.224,64.785,77.679,49.642z" id="SVGID_91_"/></defs><clipPath id="SVGID_2_"/><g clip-path="url(#SVGID_2_)" enable-background="new    "><path d="M76.124,57.937l-0.071,2.384c-0.588,1.669-1.337,3.259-2.226,4.758l0.071-2.384     C74.787,61.195,75.537,59.605,76.124,57.937" fill="#9D5025"/><path d="M73.898,62.694l-0.071,2.384c-4.95,8.363-14.229,13.856-24.693,13.543     C33.875,78.163,21.867,65.51,22.321,50.364l0.071-2.382c-0.454,15.144,11.555,27.797,26.813,28.255     C59.67,76.551,68.948,71.06,73.898,62.694" fill="#9D5025"/><path d="M77.679,49.642l-0.071,2.382c-0.04,1.332-0.175,2.635-0.399,3.91l0.072-2.384     C77.504,52.274,77.639,50.972,77.679,49.642" fill="#9D5025"/><path d="M77.28,53.55l-0.072,2.384c-0.268,1.508-0.653,2.978-1.155,4.387l0.071-2.384     C76.626,56.527,77.012,55.06,77.28,53.55" fill="#9D5025"/></g></g><path d="M67.993,39.5h-9v-1.791l-2.795,3.525l-1.285-1.816L42.229,47.5h10.764v-1.922l6,2.244V44.5h9v9h-9v-3.322   l-6,2.244V50.5H42.231l12.799,8.081l1.168-1.878l2.795,3.588V58.5h9v9h-9v-3.598l-6.218-1.496l0.85-1.805L37.993,50.488V56.5h-10   v-15h10v6.01l15.748-10.112l-0.965-1.804l6.217-1.496V30.5h9V39.5z" fill="#FFFFFF"/></g></svg>
                  </svg-element>
                  <text-element
                    :sub-width="'100%'"
                    :sub-height="'30px'"
                    :sub-right="'-50px'"
                    :sub-bottom="'0%'"
                    :text="'Zuul ' + key"
                  >
                  </text-element>
                </sub-elements>
              </geometry-element>

              <!--Zuul container 연결선-->
              <edge-element
                v-for="(container, i) in area.containers"
                v-if="container.show"
                :id="area.lb.id + '-' + container.id"
                :from="area.lb.id"
                :to="container.id"
                :_style="{}"
              >
              </edge-element>
            </div>
          </div>
        </template>
      </opengraph>
    </div>
  </div>
</template>
<script>
  export default {
    props: {},
    data() {
      return {
        backup: null,
        haproxy: {
          x: 500,
          y: 450,
          width: 60,
          height: 60,
          id: 'haproxy'
        },
        areas: {
          prod: {
            id: 'area-prod',
            title: {
              style: {
                fill: 'blue',
                'fill-opacity': 0.7,
                'font-color': 'white',
              },
              label: 'Production \n api.uengine.io',
            },
            deployment: 'blue',
            x: 200,
            y: 250,
            width: 250,
            height: 250,
            color: 'blue',
            containers: [
              {
                x: 150,
                y: 200,
                width: 100,
                height: 100,
                id: 'prod-c-1',
                healthy: false,
                deploy: false,
                show: true,
                redraw: false
              },
              {
                x: 250,
                y: 200,
                width: 100,
                height: 100,
                id: 'prod-c-2',
                healthy: false,
                deploy: false,
                show: true,
                redraw: false
              },
              {
                x: 150,
                y: 300,
                width: 100,
                height: 100,
                id: 'prod-c-3',
                healthy: false,
                deploy: false,
                show: false
              },
              {
                x: 250,
                y: 300,
                width: 100,
                height: 100,
                id: 'prod-c-4',
                healthy: false,
                deploy: false,
                show: false
              }
            ],
            lb: {
              show: true,
              x: 200,
              y: 300,
              width: 60,
              height: 60,
              id: 'prod-lb',
              style: {}
            }
          },
          stg: {
            id: 'area-stg',
            title: {
              style: {
                fill: 'green',
                'fill-opacity': 0.7,
                'font-color': 'white'
              },
              label: 'Staging \n api-stg.uengine.io',
            },
            deployment: 'green',
            x: 500,
            y: 250,
            width: 250,
            height: 250,
            color: 'green',
            containers: [
              {
                x: 450,
                y: 200,
                width: 100,
                height: 100,
                id: 'stg-c-1',
                healthy: false,
                deploy: false,
                show: true,
                redraw: false
              },
              {
                x: 550,
                y: 200,
                width: 100,
                height: 100,
                id: 'stg-c-2',
                healthy: false,
                deploy: false,
                show: true,
                redraw: false
              },
              {
                x: 450,
                y: 300,
                width: 100,
                height: 100,
                id: 'stg-c-3',
                healthy: false,
                deploy: false,
                show: false,
                redraw: false
              },
              {
                x: 550,
                y: 300,
                width: 100,
                height: 100,
                id: 'stg-c-4',
                healthy: false,
                deploy: false,
                show: false,
                redraw: false
              }
            ],
            lb: {
              show: true,
              x: 500,
              y: 300,
              width: 60,
              height: 60,
              id: 'stg-lb',
              style: {}
            }
          },
          dev: {
            id: 'area-dev',
            title: {
              style: {
                fill: 'gray',
                'fill-opacity': 0.7,
                'font-color': 'white'
              },
              label: 'Dev \n api-dev.uengine.io',
            },
            deployment: 'dev',
            x: 800,
            y: 250,
            width: 250,
            height: 250,
            color: 'gray',
            containers: [
              {
                x: 750,
                y: 200,
                width: 100,
                height: 100,
                id: 'dev-c-1',
                healthy: false,
                deploy: false,
                show: true
              },
              {
                x: 850,
                y: 200,
                width: 100,
                height: 100,
                id: 'dev-c-2',
                healthy: false,
                deploy: false,
                show: true
              },
              {
                x: 750,
                y: 300,
                width: 100,
                height: 100,
                id: 'dev-c-3',
                healthy: false,
                deploy: false,
                show: false
              },
              {
                x: 850,
                y: 300,
                width: 100,
                height: 100,
                id: 'dev-c-4',
                healthy: false,
                deploy: false,
                show: false
              }
            ],
            lb: {
              show: true,
              x: 800,
              y: 300,
              width: 60,
              height: 60,
              id: 'dev-lb',
              style: {}
            }
          }
        },
        animationStyle: {
          'fill-opacity': 1,
          animation: [
            {
              start: {
                fill: 'white'
              },
              to: {
                fill: '#C9E2FC'
              },
              ms: 1000
            },
            {
              start: {
                fill: '#C9E2FC'
              },
              to: {
                fill: 'white'
              },
              ms: 1000,
              delay: 1000
            }
          ],
          'animation-repeat': true,
          fill: '#C9E2FC',
          'stroke-width': '0.2',
          'stroke-dasharray': '--'
        }
      }
    },
    mounted(){
      this.backup = JSON.parse(JSON.stringify(this.areas));
    }
    ,
    methods: {
      mouseover: function () {
        console.log('mouseover')
      },
      mouseout: function () {
        console.log('mouseout')
      },
      reset: function () {
        this.clearAnimation();
        this.areas = JSON.parse(JSON.stringify(this.backup));
      },
      devToStg: function () {
        var me = this;
        this.clearAnimation();
        this.areas.stg.lb.show = false;

        //헬스체크 && 교체
        var updateHealthy = function (container) {
          setTimeout(function () {
            container.deploy = false;
            container.healthy = true;
            me.makeAnimation(container.id, '0,0', '0,-100', '0.5s');
            me.makeAnimation(container.id, '0,0', '0,-100', '0.5s');
            me.$nextTick(function () {
              container.redraw = true;
            })
          }, 3000);
        };

        $.each(me.areas.stg.containers, function (c, container) {
          if (c > 1) {
            container.show = true;
            container.deploy = true;
            updateHealthy(container);
          }
        });

        //원복
        setTimeout(function () {
          $.each(me.areas.stg.containers, function (c, container) {
            if (c > 1) {
              container.show = false;
            } else {
              container.show = true;
            }
          });
          me.areas.stg.lb.show = true;
        }, 4000);
      },
      stgToProd: function () {
        this.clearAnimation();
        this.makeAnimation('prod-lb', '0,0', '300,0', '0.5s');
        this.makeAnimation('stg-lb', '0,0', '-300,0', '0.5s');
        this.areas.stg.title.label = 'Production \n api.uengine.io';
        this.areas.prod.title.label = 'Staging \n api-stg.uengine.io';
      },
      rollback: function () {
        this.clearAnimation();
      },
      clearAnimation: function () {
        $(this.$refs['opengraph'].$el).find('animateTransform').remove();
      },
      makeAnimation: function (id, from, to, duration) {
        var me = this;

        function makeSVG(tag, attrs) {
          var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
          for (var k in attrs)
            el.setAttribute(k, attrs[k]);
          return el;
        }

        var ele = makeSVG('animateTransform',
          {
            attributeType: 'xml',
            attributeName: 'transform',
            type: 'translate',
            from: from,
            to: to,
            begin: '0',
            dur: duration,
            fill: 'freeze'
          }
        );

        $(me.$refs['opengraph'].$el).find('#' + id).get(0).appendChild(ele);
        ele.beginElement();
      },
      canvasReady: function () {

      }
    }
  }
</script>

<style scoped lang="scss" rel="stylesheet/scss">

</style>
