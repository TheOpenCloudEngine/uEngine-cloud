OG.shape.bpmn.Event = function (label) {
    OG.shape.bpmn.Event.superclass.call(this);

    this.SHAPE_ID = 'OG.shape.bpmn.Event';
    this.label = label;
};
OG.shape.bpmn.Event.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.Event.superclass = OG.shape.GeomShape;
OG.shape.bpmn.Event.prototype.constructor = OG.shape.bpmn.Event;
OG.Event = OG.shape.bpmn.Event;

OG.shape.bpmn.Event.prototype.createSubShape = function () {
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
