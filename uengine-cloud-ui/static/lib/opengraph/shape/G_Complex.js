/**
 * BPMN : Complex Gateway Shape
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
OG.shape.bpmn.G_Complex = function (label) {
	OG.shape.bpmn.G_Complex.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.G_Complex';
	this.label = label;
};
OG.shape.bpmn.G_Complex.prototype = new OG.shape.bpmn.G_Gateway();
OG.shape.bpmn.G_Complex.superclass = OG.shape.bpmn.G_Gateway;
OG.shape.bpmn.G_Complex.prototype.constructor = OG.shape.bpmn.G_Complex;
OG.G_Complex = OG.shape.bpmn.G_Complex;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.G_Complex.prototype.createShape = function () {
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

	geom2 = new OG.geometry.Line([30, 30], [70, 70]);
	geom2.style = new OG.geometry.Style({
		"stroke-width": 3
	});

	geom3 = new OG.geometry.Line([30, 70], [70, 30]);
	geom3.style = new OG.geometry.Style({
		"stroke-width": 3
	});

	geom4 = new OG.geometry.Line([20, 50], [80, 50]);
	geom4.style = new OG.geometry.Style({
		"stroke-width": 3
	});

	geom5 = new OG.geometry.Line([50, 20], [50, 80]);
	geom5.style = new OG.geometry.Style({
		"stroke-width": 3
	});

	geomCollection.push(geom1);
	geomCollection.push(geom2);
	geomCollection.push(geom3);
	geomCollection.push(geom4);
	geomCollection.push(geom5);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);

	return this.geom;
};