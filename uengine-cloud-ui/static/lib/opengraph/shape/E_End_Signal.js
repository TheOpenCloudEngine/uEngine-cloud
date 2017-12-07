OG.shape.bpmn.E_End_Signal = function (label) {
  OG.shape.bpmn.E_End_Signal.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.E_End_Signal';
  this.label = label;
};
OG.shape.bpmn.E_End_Signal.prototype = new OG.shape.bpmn.E_End();
OG.shape.bpmn.E_End_Signal.superclass = OG.shape.bpmn.E_End;
OG.shape.bpmn.E_End_Signal.prototype.constructor = OG.shape.bpmn.E_End_Signal;
OG.E_End_Signal = OG.shape.bpmn.E_End_Signal;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_End_Signal.prototype.createShape = function () {
  var geom1, geom2, geomCollection = [];
  if (this.geom) {
    return this.geom;
  }

  geom1 = new OG.geometry.Circle([50, 50], 50);
  geom1.style = new OG.geometry.Style({
    "stroke-width": 4
  });

  geom2 = new OG.Polygon([
    [20, 75],
    [50, 10],
    [80, 75]
  ]);
  geom2.style = new OG.geometry.Style({
    "fill": "black",
    "fill-opacity": 1
  });

  geomCollection.push(geom1);
  geomCollection.push(geom2);

  this.geom = new OG.geometry.GeometryCollection(geomCollection);
  this.geom.style = new OG.geometry.Style({
    'label-position': 'bottom'
  });

  return this.geom;
};
