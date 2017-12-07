/**
 * BPMN : Compensation Intermediate Event Shape
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
OG.shape.bpmn.E_Intermediate_Compensation = function (label) {
	OG.shape.bpmn.E_Intermediate_Compensation.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Intermediate_Compensation';
	this.label = label;
	this.selectable = true;
	this.movable = true;
};
OG.shape.bpmn.E_Intermediate_Compensation.prototype = new OG.shape.bpmn.E_Intermediate();
OG.shape.bpmn.E_Intermediate_Compensation.superclass = OG.shape.bpmn.E_Intermediate;
OG.shape.bpmn.E_Intermediate_Compensation.prototype.constructor = OG.shape.bpmn.E_Intermediate_Compensation;
OG.E_Intermediate_Compensation = OG.shape.bpmn.E_Intermediate_Compensation;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Intermediate_Compensation.prototype.createShape = function () {
	var geom1, geom2, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Polygon([
		[15, 50],
		[45, 70],
		[45, 30]
	]);

	geom2 = new OG.geometry.Polygon([
		[45, 50],
		[75, 70],
		[75, 30]
	]);

	geomCollection.push(new OG.geometry.Circle([50, 50], 50));
	geomCollection.push(new OG.geometry.Circle([50, 50], 44));
	geomCollection.push(geom1);
	geomCollection.push(geom2);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom',
		"stroke" : "#969149",
		"stroke-width" : 1.5,
		fill : "white",
		"fill-opacity" : 1
	});

	return this.geom;
};
