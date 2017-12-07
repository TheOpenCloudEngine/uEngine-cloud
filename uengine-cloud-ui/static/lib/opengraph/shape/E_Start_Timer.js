/**
 * BPMN : Timer Start Event Shape
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
OG.shape.bpmn.E_Start_Timer = function (label) {
	OG.shape.bpmn.E_Start_Timer.superclass.call(this);

	this.SHAPE_ID = 'OG.shape.bpmn.E_Start_Timer';
	this.label = label;
};
OG.shape.bpmn.E_Start_Timer.prototype = new OG.shape.bpmn.E_Start();
OG.shape.bpmn.E_Start_Timer.superclass = OG.shape.bpmn.E_Start;
OG.shape.bpmn.E_Start_Timer.prototype.constructor = OG.shape.bpmn.E_Start_Timer;
OG.E_Start_Timer = OG.shape.bpmn.E_Start_Timer;

/**
 * 드로잉할 Shape 을 생성하여 반환한다.
 *
 * @return {OG.geometry.Geometry} Shape 정보
 * @override
 */
OG.shape.bpmn.E_Start_Timer.prototype.createShape = function () {
	var geom1, geom2, geom3, geomCollection = [];
	if (this.geom) {
		return this.geom;
	}

	geom1 = new OG.geometry.Circle([50, 50], 50);
	geom1.style = new OG.geometry.Style({
		"stroke-width": 1.5
	});

	geom2 = new OG.geometry.Circle([50, 50], 32);

	geom3 = new OG.geometry.PolyLine([
		[50, 30],
		[50, 50],
		[70, 50]
	]);

	geomCollection.push(geom1);
	geomCollection.push(geom2);
	geomCollection.push(new OG.geometry.Line([50, 18], [50, 25]));
	geomCollection.push(new OG.geometry.Line([50, 82], [50, 75]));
	geomCollection.push(new OG.geometry.Line([18, 50], [25, 50]));
	geomCollection.push(new OG.geometry.Line([82, 50], [75, 50]));
	geomCollection.push(geom3);

	this.geom = new OG.geometry.GeometryCollection(geomCollection);
	this.geom.style = new OG.geometry.Style({
		'label-position': 'bottom'
	});

	return this.geom;
};
