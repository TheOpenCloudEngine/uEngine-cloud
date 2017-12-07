OG.shape.bpmn.G_Event = function (label) {
  OG.shape.bpmn.G_Event.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.G_Event';
  this.label = label;
};
OG.shape.bpmn.G_Event.prototype = new OG.shape.bpmn.G_Gateway();
OG.shape.bpmn.G_Event.superclass = OG.shape.bpmn.G_Gateway;
OG.shape.bpmn.G_Event.prototype.constructor = OG.shape.bpmn.G_Event;
OG.G_Event = OG.shape.bpmn.G_Event;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.G_Event.prototype.createShape = function () {
  var geom1, geom2, geom3, geom4, geom5, geomCollection = [];
  if (this.geom) {
    return this.geom;
  }

  geom1 = new OG.geometry.Polygon([
    [0, 50],
    [50, 100],
    [100, 50],
    [50, 0]
  ]);

  geom2 = new OG.geometry.Polygon([
    [50, 15],
    [39, 33],
    [20, 33],
    [29, 50],
    [19, 67],
    [40, 67],
    [50, 85],
    [60, 68],
    [80, 68],
    [70, 50],
    [79, 33],
    [60, 33]
  ]);

  geomCollection.push(geom1);
  geomCollection.push(geom2);

  this.geom = new OG.geometry.GeometryCollection(geomCollection);

  return this.geom;
};
