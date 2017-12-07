/**
 * BPMN : Inclusive Gateway Shape
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
OG.shape.bpmn.G_Inclusive = function (label) {
	OG.shape.bpmn.G_Inclusive.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.G_Inclusive';
	this.label = label;
};
OG.shape.bpmn.G_Inclusive.prototype = new OG.shape.bpmn.G_Gateway();
OG.shape.bpmn.G_Inclusive.superclass = OG.shape.bpmn.G_Gateway;
OG.shape.bpmn.G_Inclusive.prototype.constructor = OG.shape.bpmn.G_Inclusive;
OG.G_Inclusive = OG.shape.bpmn.G_Inclusive;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.G_Inclusive.prototype.createShape = function () {
	var geom1, geom2, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Polygon([
		[0, 50],
		[50, 100],
		[100, 50],
		[50, 0]
	]);

	geom2 = new OG.geometry.Circle([50, 50], 25);
	geom2.style = new OG.geometry.Style({
		"stroke-width": 3
	});

	geomCollection.push(geom1);
	geomCollection.push(geom2);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);

	return this.geom;
};