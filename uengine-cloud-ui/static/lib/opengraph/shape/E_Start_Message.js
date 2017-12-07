/**
 * BPMN : Message Start Event Shape
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
OG.shape.bpmn.E_Start_Message = function (label) {
	OG.shape.bpmn.E_Start_Message.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Start_Message';
	this.label = label;
};
OG.shape.bpmn.E_Start_Message.prototype = new OG.shape.bpmn.E_Start();
OG.shape.bpmn.E_Start_Message.superclass = OG.shape.bpmn.E_Start;
OG.shape.bpmn.E_Start_Message.prototype.constructor = OG.shape.bpmn.E_Start_Message;
OG.E_Start_Message = OG.shape.bpmn.E_Start_Message;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Start_Message.prototype.createShape = function () {
	var geom1, geom2, geom3, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Circle([50, 50], 50);
	geom1.style = new OG.geometry.Style({
		"stroke-width": 1.5
	});

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

	geomCollection.push(geom1);
	geomCollection.push(geom2);
	geomCollection.push(geom3);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom'
	});

	return this.geom;
};
