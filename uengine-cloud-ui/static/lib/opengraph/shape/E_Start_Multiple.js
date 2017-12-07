/**
 * BPMN : Multiple Start Event Shape
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
OG.shape.bpmn.E_Start_Multiple = function (label) {
	OG.shape.bpmn.E_Start_Multiple.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Start_Multiple';
	this.label = label;
};
OG.shape.bpmn.E_Start_Multiple.prototype = new OG.shape.bpmn.E_Start();
OG.shape.bpmn.E_Start_Multiple.superclass = OG.shape.bpmn.E_Start;
OG.shape.bpmn.E_Start_Multiple.prototype.constructor = OG.shape.bpmn.E_Start_Multiple;
OG.E_Start_Multiple = OG.shape.bpmn.E_Start_Multiple;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Start_Multiple.prototype.createShape = function () {
	var geom1, geom2, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Circle([50, 50], 50);

	geom2 = new OG.geometry.Polygon([
		[50, 15],
		[39, 33],
		[20, 33],
		[29, 50],
		[19, 67],
		[40, 67],
		[50, 85],
		[60, 68],
		[80, 68],
		[70, 50],
		[79, 33],
		[60, 33]
	]);

	geomCollection.push(geom1);
	geomCollection.push(geom2);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom'
	});

	return this.geom;
};
