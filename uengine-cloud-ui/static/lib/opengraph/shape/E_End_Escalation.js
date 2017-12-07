OG.shape.bpmn.E_End_Escalation = function (label) {
  OG.shape.bpmn.E_End_Escalation.superclass.call(this);

  this.SHAPE_ID = 'OG.shape.bpmn.E_End_Escalation';
  this.label = label;
};
OG.shape.bpmn.E_End_Escalation.prototype = new OG.shape.bpmn.E_End();
OG.shape.bpmn.E_End_Escalation.superclass = OG.shape.bpmn.E_End;
OG.shape.bpmn.E_End_Escalation.prototype.constructor = OG.shape.bpmn.E_End_Escalation;
OG.E_End_Escalation = OG.shape.bpmn.E_End_Escalation;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_End_Escalation.prototype.createShape = function () {
  var geom1, geom2, geomCollection = [];
  if (this.geom) {
    return this.geom;
  }

  geom1 = new OG.geometry.Circle([50, 50], 50);
  geom1.style = new OG.geometry.Style({
    "stroke-width": 4
  });

  geom2 = new OG.geometry.Polygon([
    [20, 80],
    [50, 20],
    [80, 80],
    [50, 50]
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
