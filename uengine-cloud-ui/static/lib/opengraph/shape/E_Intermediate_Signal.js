OG.shape.bpmn.E_Intermediate_Signal = function (label) {
  OG.shape.bpmn.E_Intermediate_Signal.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.E_Intermediate_Signal';
  this.label = label;
};
OG.shape.bpmn.E_Intermediate_Signal.prototype = new OG.shape.bpmn.E_Intermediate();
OG.shape.bpmn.E_Intermediate_Signal.superclass = OG.shape.bpmn.E_Intermediate;
OG.shape.bpmn.E_Intermediate_Signal.prototype.constructor = OG.shape.bpmn.E_Intermediate_Signal;
OG.E_Intermediate_Signal = OG.shape.bpmn.E_Intermediate_Signal;


/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Intermediate_Signal.prototype.createShape = function () {
  var geom1, geom2, geom3, geomCollection = [];
  if (this.geom) {
    return this.geom;
  }

  geom1 = new OG.geometry.Circle([50, 50], 50);
  geom2 = new OG.geometry.Circle([50, 50], 44);
  geom3 = new OG.Polygon([
    [20, 75],
    [50, 10],
    [80, 75]
  ]);

  geomCollection.push(geom1);
  geomCollection.push(geom2);
  geomCollection.push(geom3);

  this.geom = new OG.geometry.GeometryCollection(geomCollection);
  this.geom.style = new OG.geometry.Style({
    'label-position': 'bottom',
    "stroke" : "black",
    "stroke-width" : 1,
    fill : "white",
    "fill-opacity" : 1
  });

  return this.geom;
};
