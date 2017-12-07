/**
 * BPMN : Data Store Shape
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
OG.shape.bpmn.D_Store = function (label) {
    OG.shape.bpmn.D_Store.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.D_Store';
    this.label = label;
};
OG.shape.bpmn.D_Store.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.D_Store.superclass = OG.shape.GeomShape;
OG.shape.bpmn.D_Store.prototype.constructor = OG.shape.bpmn.D_Store;
OG.D_Store = OG.shape.bpmn.D_Store;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.D_Store.prototype.createShape = function () {
    var geom1, geom2, geom3, geom4, geom5, geomCollection = [];
    if (this.geom) {
        return this.geom;
    }

    geom1 = new OG.geometry.Ellipse([50, 10], 50, 10);
    geom2 = new OG.geometry.Line([0, 10], [0, 90]);
    geom3 = new OG.geometry.Line([100, 10], [100, 90]);
    geom4 = new OG.geometry.Curve([
        [100, 90],
        [96, 94],
        [85, 97],
        [50, 100],
        [15, 97],
        [4, 94],
        [0, 90]
    ]);
    geom5 = new OG.geometry.Rectangle([0, 10], 100, 80);
    geom5.style = new OG.geometry.Style({
        "stroke": 'none'
    });

    geomCollection.push(geom1);
    geomCollection.push(geom2);
    geomCollection.push(geom3);
    geomCollection.push(geom4);
    geomCollection.push(geom5);

    this.geom = new OG.geometry.GeometryCollection(geomCollection);

    return this.geom;
};


OG.shape.bpmn.D_Store.prototype.createSubShape = function () {
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

OG.shape.bpmn.D_Store.prototype.createController = function () {
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
