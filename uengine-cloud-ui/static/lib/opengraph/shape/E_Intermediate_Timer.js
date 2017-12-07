/**
 * BPMN : Timer Intermediate Event Shape
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
OG.shape.bpmn.E_Intermediate_Timer = function (label) {
	OG.shape.bpmn.E_Intermediate_Timer.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Intermediate_Timer';
	this.label = label;
};
OG.shape.bpmn.E_Intermediate_Timer.prototype = new OG.shape.bpmn.E_Intermediate();
OG.shape.bpmn.E_Intermediate_Timer.superclass = OG.shape.bpmn.E_Intermediate;
OG.shape.bpmn.E_Intermediate_Timer.prototype.constructor = OG.shape.bpmn.E_Intermediate_Timer;
OG.E_Intermediate_Timer = OG.shape.bpmn.E_Intermediate_Timer;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Intermediate_Timer.prototype.createShape = function () {
	var geom1, geom2, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Circle([50, 50], 32);

	geom2 = new OG.geometry.PolyLine([
		[50, 30],
		[50, 50],
		[70, 50]
	]);

	geomCollection.push(new OG.geometry.Circle([50, 50], 50));
	geomCollection.push(new OG.geometry.Circle([50, 50], 44));
	geomCollection.push(geom1);
	geomCollection.push(new OG.geometry.Line([50, 18], [50, 25]));
	geomCollection.push(new OG.geometry.Line([50, 82], [50, 75]));
	geomCollection.push(new OG.geometry.Line([18, 50], [25, 50]));
	geomCollection.push(new OG.geometry.Line([82, 50], [75, 50]));
	geomCollection.push(geom2);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom',
		//"stroke" : "#969149",
		"stroke" : "black",
		"stroke-width" : 1.5,
		fill : "white",
		"fill-opacity" : 1
	});

	return this.geom;
};
