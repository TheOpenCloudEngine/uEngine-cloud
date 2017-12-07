/**
 * BPMN : Data Object Shape
 *
 * @class
 * @extends OG.shape.GeomShape
 * @requires OG.common.*
 * @requires OG.geometry.*
 *
 * @param {String} label 라벨 [Optional]
 * @author <a href="mailto:sppark@uengine.org">Seungpil Park</a>
 * @private
 */
OG.shape.bpmn.D_Data = function (label) {
    OG.shape.bpmn.D_Data.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.D_Data';
    this.label = label;
};
OG.shape.bpmn.D_Data.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.D_Data.superclass = OG.shape.GeomShape;
OG.shape.bpmn.D_Data.prototype.constructor = OG.shape.bpmn.D_Data;
OG.D_Data = OG.shape.bpmn.D_Data;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.D_Data.prototype.createShape = function () {
    if (this.geom) {
        return this.geom;
    }

    this.geom = new OG.PolyLine([
        [0, 0],
        [0, 100],
        [100, 100],
        [100, 20],
        [80, 0],
        [0, 0],
        [80, 0],
        [80, 20],
        [100, 20]
    ]);

    return this.geom;
};



OG.shape.bpmn.D_Data.prototype.createSubShape = function () {
  this.sub = [];
  var statusShape, statusAnimation;
  switch (this.status) {
    case "Completed":
      statusShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'complete.png');
      break;
    case "Running":
      statusShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'running.png');
      statusAnimation = new OG.RectangleShape();
      break;
  }
  if (statusShape) {
    this.sub.push({
      shape: statusShape,
      width: '20px',
      height: '20px',
      align: 'center',
      top: '0px',
      style: {}
    })
  }
  if (statusAnimation) {
    this.sub.push({
      shape: statusAnimation,
      'z-index': -1,
      width: '120%',
      height: '120%',
      left: '-10%',
      top: '-10%',
      style: {
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
        "fill": "#C9E2FC",
        "stroke-width": "0.2",
        "r": "10",
        'stroke-dasharray': '--'
      }
    })
  }
  return this.sub;
};

OG.shape.bpmn.D_Data.prototype.createController = function () {
  //선연결 컨트롤러
  var me = this;
  var controllers = [
    {
      image: 'annotation.png',
      create: {
        shape: 'OG.M_Annotation',
        width: 120,
        height: 30,
        style: {}
      }
    }
  ];
  return controllers;
};
