OG.shape.bpmn.E_Intermediate_Message_Throw = function (label) {
  OG.shape.bpmn.E_Intermediate_Message_Throw.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.E_Intermediate_Message_Throw';
  this.label = label;
};
OG.shape.bpmn.E_Intermediate_Message_Throw.prototype = new OG.shape.bpmn.E_Intermediate();
OG.shape.bpmn.E_Intermediate_Message_Throw.superclass = OG.shape.bpmn.E_Intermediate;
OG.shape.bpmn.E_Intermediate_Message_Throw.prototype.constructor = OG.shape.bpmn.E_Intermediate_Message_Throw;
OG.E_Intermediate_Message_Throw = OG.shape.bpmn.E_Intermediate_Message_Throw;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Intermediate_Message_Throw.prototype.createShape = function () {
  var geom1, geom2, geomCollection = [];
  if (this.geom) {
    return this.geom;
  }

  geom1 = new OG.geometry.PolyLine([
    [20, 25],
    [50, 45],
    [80, 25],
    [20, 25]
  ]);
  geom1.style = new OG.geometry.Style({
    fill : "#000",
    "fill-opacity" : 1
  });

  geom2 = new OG.geometry.PolyLine([
    [20, 35],
    [20, 70],
    [80, 70],
    [80, 35],
    [50, 55],
    [20, 35]
  ]);
  geom2.style = new OG.geometry.Style({
    fill : "#000",
    "fill-opacity" : 1
  });


  geomCollection.push(new OG.geometry.Circle([50, 50], 50));
  geomCollection.push(new OG.geometry.Circle([50, 50], 44));
  geomCollection.push(geom1);
  geomCollection.push(geom2);

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
