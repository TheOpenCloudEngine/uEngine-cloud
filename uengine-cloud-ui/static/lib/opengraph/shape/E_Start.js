/**
 * BPMN : Start Event Shape
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
OG.shape.bpmn.E_Start = function (label) {
	OG.shape.bpmn.E_Start.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Start';
	this.label = label;
	this.inclusion = false;
};
OG.shape.bpmn.E_Start.prototype = new OG.shape.bpmn.Event();
OG.shape.bpmn.E_Start.superclass = OG.shape.bpmn.Event;
OG.shape.bpmn.E_Start.prototype.constructor = OG.shape.bpmn.E_Start;
OG.E_Start = OG.shape.bpmn.E_Start;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Start.prototype.createShape = function () {
	if (this.geom) {
		return this.geom;
	}

	this.geom = new OG.geometry.Circle([50, 50], 50);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom',
		"stroke-width" : 1.5
	});

	return this.geom;
};


OG.shape.bpmn.E_Start.prototype.createController = function () {
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
