/**
 * BPMN : Subprocess Activity Shape
 *
 * @class
 * @extends OG.shape.GroupShape
 * @requires OG.common.*
 * @requires OG.geometry.*
 *
 * @param {String} label 라벨 [Optional]
 * @author <a href="mailto:sppark@uengine.org">Seungpil Park</a>
 * @private
 */
OG.shape.bpmn.A_Subprocess = function (label) {
    OG.shape.bpmn.A_Subprocess.superclass.call(this);

    this.label = label;
    this.SHAPE_ID = 'OG.shape.bpmn.A_Subprocess';
    this.GROUP_COLLAPSIBLE = false;
    this.HaveButton = true;
    this.status = "None";
    this.inclusion = false;
};
OG.shape.bpmn.A_Subprocess.prototype = new OG.shape.GroupShape();
OG.shape.bpmn.A_Subprocess.superclass = OG.shape.GroupShape;
OG.shape.bpmn.A_Subprocess.prototype.constructor = OG.shape.bpmn.A_Subprocess;
OG.A_Subprocess = OG.shape.bpmn.A_Subprocess;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.A_Subprocess.prototype.createShape = function () {
    if (this.geom) {
        return this.geom;
    }
    this.CONNECTABLE = true;


    this.geom = new OG.geometry.Rectangle([0, 0], 100, 100);
    this.geom.style = new OG.geometry.Style({
        "stroke-width": 1.2,
        'r': 6,
        fill: '#FFFFFF',
        'fill-opacity': 0.7
    });

    return this.geom;
};

OG.shape.bpmn.A_Subprocess.prototype.createSubShape = function () {
    this.sub = [];

    var statusShape, statusAnimation;
    switch (this.status) {
        case "Completed":
            statusShape = new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + "complete.png");
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
            right: '25px',
            top: '5px',
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

    if (this.inclusion) {
        this.sub.push({
            shape: new OG.ImageShape(this.currentCanvas._CONFIG.IMAGE_BASE + 'complete.png'),
            width: '20px',
            height: '20px',
            right: '0px',
            bottom: '20px',
            style: {}
        })
    }

    if (this.HaveButton) {
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

    return this.sub;
};

OG.shape.bpmn.A_Subprocess.prototype.createController = function () {
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
