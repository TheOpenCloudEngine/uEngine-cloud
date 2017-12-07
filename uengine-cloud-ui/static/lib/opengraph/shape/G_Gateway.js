/**
 * BPMN : Gateway Shape
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
OG.shape.bpmn.G_Gateway = function (label) {
	OG.shape.bpmn.G_Gateway.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.G_Gateway';
	this.label = label;
};
OG.shape.bpmn.G_Gateway.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.G_Gateway.superclass = OG.shape.GeomShape;
OG.shape.bpmn.G_Gateway.prototype.constructor = OG.shape.bpmn.G_Gateway;
OG.G_Gateway = OG.shape.bpmn.G_Gateway;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.G_Gateway.prototype.createShape = function () {
	if (this.geom) {
		return this.geom;
	}

	this.geom = new OG.geometry.Polygon([
		[0, 50],
		[50, 100],
		[100, 50],
		[50, 0]
	]);

	return this.geom;
};


OG.shape.bpmn.G_Gateway.prototype.createSubShape = function () {
  this.sub = [];

  var statusShape, statusAnimation;
  switch (this.status) {
    case "Completed":
      statusShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'complete.png');
      break;
    case "Running":
      statusShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'running.png');
      statusAnimation = new OG.CircleShape();
      break;
  }
  if (statusShape) {
    this.sub.push({
      shape: statusShape,
      width: '20px',
      height: '20px',
      right: '-10px',
      bottom: '-5px',
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
        //"r": "10",
        'stroke-dasharray': '--'
      }
    })
  }
  return this.sub;
};


OG.shape.bpmn.G_Gateway.prototype.createController = function () {
  //선연결 컨트롤러
  var me = this;
  var controllers = [
    {
      image: 'event_end.png',
      create: {
        shape: 'OG.E_End',
        width: 30,
        height: 30,
        style: {}
      }
    },
    {
      image: 'event_intermediate.png',
      create: {
        shape: 'OG.E_Intermediate',
        width: 30,
        height: 30,
        style: {}
      }
    },
    {
      image: 'gateway_exclusive.png',
      create: {
        shape: 'OG.G_Exclusive',
        width: 30,
        height: 30,
        style: {}
      }
    },
    {
      image: 'annotation.png',
      create: {
        shape: 'OG.M_Annotation',
        width: 120,
        height: 30,
        style: {}
      }
    },
    {
      image: 'task.png',
      create: {
        shape: 'OG.A_Task',
        width: 100,
        height: 100,
        style: {}
      }
    },
    {
      image: 'wrench.png',
      action: function (element) {
        $(me.currentCanvas.getRootElement()).trigger('changeMenu', [element]);
      }
    }
  ];
  return controllers;
};
