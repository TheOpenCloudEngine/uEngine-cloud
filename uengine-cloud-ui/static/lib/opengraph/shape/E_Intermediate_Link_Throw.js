OG.shape.bpmn.E_Intermediate_Link_Throw = function (label) {
  OG.shape.bpmn.E_Intermediate_Link_Throw.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.E_Intermediate_Link_Throw';
  this.label = label;
};
OG.shape.bpmn.E_Intermediate_Link_Throw.prototype = new OG.shape.bpmn.E_Intermediate();
OG.shape.bpmn.E_Intermediate_Link_Throw.superclass = OG.shape.bpmn.E_Intermediate;
OG.shape.bpmn.E_Intermediate_Link_Throw.prototype.constructor = OG.shape.bpmn.E_Intermediate_Link_Throw;
OG.E_Intermediate_Link_Throw = OG.shape.bpmn.E_Intermediate_Link_Throw;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Intermediate_Link_Throw.prototype.createShape = function () {

  var geom1, geomCollection = [];
  if (this.geom) {
    return this.geom;
  }

  geom1 = new OG.geometry.Polygon([
    [20, 35],
    [20, 65],
    [60, 65],
    [60, 80],
    [85, 50],
    [60, 20],
    [60, 35]
  ]);
  geom1.style = new OG.geometry.Style({
    fill : "#000",
    "fill-opacity" : 1
  });

  geomCollection.push(new OG.geometry.Circle([50, 50], 50));
  geomCollection.push(new OG.geometry.Circle([50, 50], 44));
  geomCollection.push(geom1);

  this.geom = new OG.geometry.GeometryCollection(geomCollection);
  this.geom.style = new OG.geometry.Style({
    'label-position': 'bottom'
  });

  return this.geom;
};
