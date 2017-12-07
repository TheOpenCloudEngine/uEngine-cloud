OG.shape.bpmn.M_Annotation = function (label) {
	OG.shape.bpmn.M_Annotation.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.M_Annotation';
	this.label = label;
};
OG.shape.bpmn.M_Annotation.prototype = new OG.shape.GeomShape();
OG.shape.bpmn.M_Annotation.superclass = OG.shape.GeomShape;
OG.shape.bpmn.M_Annotation.prototype.constructor = OG.shape.bpmn.M_Annotation;
OG.M_Annotation = OG.shape.bpmn.M_Annotation;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.M_Annotation.prototype.createShape = function () {
	if (this.geom) {
		return this.geom;
	}

	var geom1, geom2, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Rectangle([0, 0], 100, 100);
	geom1.style = new OG.geometry.Style({
		"stroke": 'none'
	});

	geom2 = new OG.geometry.PolyLine([
		[10, 0],
		[0, 0],
		[0, 100],
		[10, 100]
	]);
	geom2.style = new OG.geometry.Style({
		"stroke": 'black'
	});

	geomCollection.push(geom1);
	geomCollection.push(geom2);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
	});

	return this.geom;
};