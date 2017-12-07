/**
 * BPMN : Task Activity Shape
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
OG.shape.bpmn.A_Task = function (label) {
  OG.shape.bpmn.A_Task.superclass.call(this);

  this.GROUP_DROPABLE = false;
  this.SHAPE_ID = 'OG.shape.bpmn.A_Task';
  this.label = label;
  this.CONNECTABLE = true;
  this.CONNECT_CLONEABLE = false;

  this.GROUP_COLLAPSIBLE = false;
  this.LABEL_EDITABLE = false;
  this.Events = [];

};
OG.shape.bpmn.A_Task.prototype = new OG.shape.GroupShape();
OG.shape.bpmn.A_Task.superclass = OG.shape.GroupShape;
OG.shape.bpmn.A_Task.prototype.constructor = OG.shape.bpmn.A_Task;
OG.A_Task = OG.shape.bpmn.A_Task;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.A_Task.prototype.createShape = function () {
  if (this.geom) {
    return this.geom;
  }

  this.geom = new OG.geometry.Rectangle([0, 0], 100, 100);
  this.geom.style = new OG.geometry.Style({
    'fill-r': 1,
    'fill-cx': .1,
    'fill-cy': .1,
    "stroke-width": 1.2,
    fill: '#FFFFFF',
    'fill-opacity': 0,
    r: '10'
  });

  if (this instanceof OG.A_CallActivity) {
    this.geom.style = new OG.geometry.Style({
      'fill-r': 1,
      'fill-cx': .1,
      'fill-cy': .1,
      "stroke-width": 3,
      fill: '#FFFFFF',
      'fill-opacity': 0,
      r: '10'
    });
  }
  return this.geom;
};

OG.shape.bpmn.A_Task.prototype.createSubShape = function () {
  this.sub = [];

  var loopShape;
  switch (this.LoopType) {
    case 'Standard' :
      loopShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'loop_standard.png');
      break;
    case 'MIParallel' :
      loopShape = new OG.MIParallel();
      break;
    case 'MISequential' :
      loopShape = new OG.MISequential();
      break;
  }
  if (loopShape) {
    this.sub.push({
      shape: loopShape,
      width: '15px',
      height: '15px',
      bottom: '5px',
      align: 'center',
      style: {}
    })
  }

  var taskTypeShape;
  if (this instanceof OG.A_SendTask) {
    taskTypeShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + "Send.png");
  }
  if (this instanceof OG.A_ReceiveTask) {
    taskTypeShape = new OG.Message();
  }
  if (this instanceof OG.A_HumanTask) {
    taskTypeShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + "User.png");
  }
  if (this instanceof OG.A_ManualTask) {
    taskTypeShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + "Manual.png");
  }
  if (this instanceof OG.A_BusinessTask) {
    taskTypeShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + "BusinessRule.png");
  }
  if (this instanceof OG.A_ServiceTask) {
    taskTypeShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + "Service.png");
  }
  if (this instanceof OG.A_BusinessTask) {
    taskTypeShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + "BusinessRule.png");
  }
  if (taskTypeShape) {
    this.sub.push({
      shape: taskTypeShape,
      width: '20px',
      height: '20px',
      top: '5px',
      left: '5px',
      style: {}
    })
  }

  if (this instanceof OG.A_CallActivity) {
    this.sub.push({
      shape: new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'subprocess.png'),
      width: '20px',
      height: '20px',
      align: 'center',
      bottom: '5px',
      style: {
        "stroke-width": 1,
        fill: "white",
        "fill-opacity": 0,
        "shape-rendering": "crispEdges"
      }
    })
  }
  ;

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

OG.shape.bpmn.A_Task.prototype.createController = function () {
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
