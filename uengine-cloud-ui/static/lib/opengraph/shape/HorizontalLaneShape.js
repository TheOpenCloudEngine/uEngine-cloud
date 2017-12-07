/**
 * Horizontal Swimlane Shape
 *
 * @class
 * @extends OG.shape.GroupShape
 * @requires OG.common.*
 * @requires OG.geometry.*
 *
 * @param {String} label 라벨 [Optional]
 * @author <a href="mailto:sppark@uengine.org">Seungpil Park</a>
 */
OG.shape.HorizontalLaneShape = function (label) {
	OG.shape.HorizontalLaneShape.superclass.call(this, label);

	this.SHAPE_ID = 'OG.shape.HorizontalLaneShape';

};
OG.shape.HorizontalLaneShape.prototype = new OG.shape.GroupShape();
OG.shape.HorizontalLaneShape.superclass = OG.shape.GroupShape;
OG.shape.HorizontalLaneShape.prototype.constructor = OG.shape.HorizontalLaneShape;
OG.HorizontalLaneShape = OG.shape.HorizontalLaneShape;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.HorizontalLaneShape.prototype.createShape = function () {
	if (this.geom) {
		return this.geom;
	}

	this.geom = new OG.geometry.Rectangle([0, 0], 100, 100);
	this.geom.style = new OG.geometry.Style({
		'label-direction': 'vertical',
		'vertical-align' : 'top',
		'fill' : '#ffffff',
		'fill-opacity': 0
	});

	return this.geom;
};


OG.shape.HorizontalLaneShape.prototype.createSubShape = function () {
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

OG.shape.HorizontalLaneShape.prototype.createController = function () {
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
