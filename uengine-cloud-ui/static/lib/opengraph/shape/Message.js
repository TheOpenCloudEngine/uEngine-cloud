OG.shape.bpmn.Message = function (label) {
	OG.shape.bpmn.Message.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.Message';
	this.label = label;
};
OG.shape.bpmn.Message.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.Message.superclass = OG.shape.GeomShape;
OG.shape.bpmn.Message.prototype.constructor = OG.shape.bpmn.Message;
OG.Message = OG.shape.bpmn.Message;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.Message.prototype.createShape = function () {
	var geom1, geom2, geom3, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom2 = new OG.geometry.PolyLine([
		[20, 25],
		[50, 45],
		[80, 25],
		[20, 25]
	]);

	geom3 = new OG.geometry.PolyLine([
		[20, 35],
		[20, 70],
		[80, 70],
		[80, 35],
		[50, 55],
		[20, 35]
	]);

	geomCollection.push(geom2);
	geomCollection.push(geom3);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'fill': '#000',
		'fill-opacity': 1
	});

	return this.geom;
};
