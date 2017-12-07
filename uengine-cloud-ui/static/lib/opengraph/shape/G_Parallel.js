/**
 * BPMN : Parallel Gateway Shape
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
OG.shape.bpmn.G_Parallel = function (label) {
	OG.shape.bpmn.G_Parallel.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.G_Parallel';
	this.label = label;
};
OG.shape.bpmn.G_Parallel.prototype = new OG.shape.bpmn.G_Gateway();
OG.shape.bpmn.G_Parallel.superclass = OG.shape.bpmn.G_Gateway;
OG.shape.bpmn.G_Parallel.prototype.constructor = OG.shape.bpmn.G_Parallel;
OG.G_Parallel = OG.shape.bpmn.G_Parallel;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.G_Parallel.prototype.createShape = function () {
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

	geom2 = new OG.geometry.Line([20, 50], [80, 50]);
	geom2.style = new OG.geometry.Style({
		"stroke-width": 5
	});

	geom3 = new OG.geometry.Line([50, 20], [50, 80]);
	geom3.style = new OG.geometry.Style({
		"stroke-width": 5
	});

	geomCollection.push(geom1);
	geomCollection.push(geom2);
	geomCollection.push(geom3);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);

	return this.geom;
};