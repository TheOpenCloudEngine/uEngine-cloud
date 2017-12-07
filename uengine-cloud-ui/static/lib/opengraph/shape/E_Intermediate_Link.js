/**
 * BPMN : Link Intermediate Event Shape
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
OG.shape.bpmn.E_Intermediate_Link = function (label) {
	OG.shape.bpmn.E_Intermediate_Link.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Intermediate_Link';
	this.label = label;
};
OG.shape.bpmn.E_Intermediate_Link.prototype = new OG.shape.bpmn.E_Intermediate();
OG.shape.bpmn.E_Intermediate_Link.superclass = OG.shape.bpmn.E_Intermediate;
OG.shape.bpmn.E_Intermediate_Link.prototype.constructor = OG.shape.bpmn.E_Intermediate_Link;
OG.E_Intermediate_Link = OG.shape.bpmn.E_Intermediate_Link;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Intermediate_Link.prototype.createShape = function () {
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

  geomCollection.push(new OG.geometry.Circle([50, 50], 50));
  geomCollection.push(new OG.geometry.Circle([50, 50], 44));
  geomCollection.push(geom1);

  this.geom = new OG.geometry.GeometryCollection(geomCollection);
  this.geom.style = new OG.geometry.Style({
    'label-position': 'bottom'
  });

  return this.geom;
};
