/**
 * BPMN : Exclusive Gateway Shape
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
OG.shape.bpmn.G_Exclusive = function (label) {
	OG.shape.bpmn.G_Exclusive.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.G_Exclusive';
	this.label = label;
};
OG.shape.bpmn.G_Exclusive.prototype = new OG.shape.bpmn.G_Gateway();
OG.shape.bpmn.G_Exclusive.superclass = OG.shape.bpmn.G_Gateway;
OG.shape.bpmn.G_Exclusive.prototype.constructor = OG.shape.bpmn.G_Exclusive;
OG.G_Exclusive = OG.shape.bpmn.G_Exclusive;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.G_Exclusive.prototype.createShape = function () {
	var geom1, geom2, geom3, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Polygon([
		[0, 50],
		[50, 100],
		[100, 50],
		[50, 0]
	]);

	geom2 = new OG.geometry.Line([30, 30], [70, 70]);
	geom2.style = new OG.geometry.Style({
		"stroke-width": 5
	});

	geom3 = new OG.geometry.Line([30, 70], [70, 30]);
	geom3.style = new OG.geometry.Style({
		"stroke-width": 5
	});

	geomCollection.push(geom1);
	geomCollection.push(geom2);
	geomCollection.push(geom3);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);

	return this.geom;
};